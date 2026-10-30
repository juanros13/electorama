package com.electorama.msvc.electoralhistory.repository;

import com.electorama.msvc.electoralhistory.dto.ElectionSummary;
import com.electorama.msvc.electoralhistory.dto.TerritoryLevel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Las consultas del histórico. Todo sale de los agregados por sección de E5 ({@code
 * mv_section_turnout}, {@code mv_section_group}); el territorio (municipio, distrito local o
 * federal) y el estado se suman aquí.
 *
 * <p>El territorio 0 junta lo que no es de ninguno del nivel pedido: el municipio 0 (voto
 * anticipado, en prisión y en el extranjero) o las casillas sin distrito (el voto anticipado del
 * IEPCT viene sin sección y no tiene distrito federal). Sólo cuenta al estado.
 *
 * <p>La columna de cada nivel sale de {@link TerritoryLevel#column()}, fija en el código: nunca se
 * arma SQL con texto del cliente.
 */
@Repository
@RequiredArgsConstructor
public class HistoryQueryRepository {

  private final NamedParameterJdbcTemplate jdbc;

  private static final String ELECTION_SELECT =
      "SELECT e.id, e.year, e.office, e.scope, e.election_date,"
          + "       s.publisher, s.url, s.file_name, s.cut_label"
          + "  FROM election e"
          + "  JOIN source s ON s.id = e.source_id";

  private static final String TURNOUT_SUMS =
      "       sum(t.stations) AS stations, sum(t.special_stations) AS special_stations,"
          + "       coalesce(sum(t.nominal_list), 0) AS nominal_list,"
          + "       coalesce(sum(t.total_votes), 0) AS total_votes,"
          + "       coalesce(sum(t.ordinary_votes), 0) AS ordinary_votes,"
          + "       coalesce(sum(t.null_votes), 0) AS null_votes,"
          + "       coalesce(sum(t.non_registered_votes), 0) AS non_registered_votes";

  public List<ElectionSummary> elections() {
    return jdbc.query(ELECTION_SELECT + " ORDER BY e.year DESC, e.office", (rs, i) -> election(rs));
  }

  public Optional<ElectionSummary> election(int year, String office) {
    return jdbc
        .query(
            ELECTION_SELECT + " WHERE e.year = :year AND e.office = :office",
            new MapSqlParameterSource("year", year).addValue("office", office),
            (rs, i) -> election(rs))
        .stream()
        .findFirst();
  }

  /** Participación y votos por territorio del nivel, incluido el 0 (sólo cuenta al estado). */
  public List<TurnoutRow> turnoutByTerritory(int electionId, TerritoryLevel level) {
    String code = "coalesce(t." + level.column() + ", 0)";
    return jdbc.query(
        "SELECT "
            + code
            + " AS code,"
            + TURNOUT_SUMS
            + "  FROM mv_section_turnout t"
            + " WHERE t.election_id = :election"
            + " GROUP BY 1 ORDER BY 1",
        new MapSqlParameterSource("election", electionId),
        (rs, i) -> turnout(rs, null));
  }

  /**
   * El nombre de cada territorio: el del municipio, o "DISTRITO LOCAL 04 · CENTRO" con la cabecera
   * como el municipio con más lista nominal dentro del distrito.
   */
  public Map<Integer, String> territoryNames(int electionId, TerritoryLevel level) {
    if (level == TerritoryLevel.MUNICIPALITY) {
      return jdbc
          .query(
              "SELECT cve, name FROM municipality",
              (rs, i) -> Map.entry(rs.getInt("cve"), rs.getString("name")))
          .stream()
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    return jdbc
        .query(
            "SELECT DISTINCT ON (t."
                + level.column()
                + ") t."
                + level.column()
                + " AS code, m.name"
                + "  FROM mv_section_turnout t"
                + "  JOIN municipality m ON m.cve = t.municipality_cve"
                + " WHERE t.election_id = :election AND t."
                + level.column()
                + " IS NOT NULL"
                + " GROUP BY t."
                + level.column()
                + ", m.name"
                + " ORDER BY t."
                + level.column()
                + ", sum(coalesce(t.nominal_list, 0)) DESC",
            new MapSqlParameterSource("election", electionId),
            (rs, i) ->
                Map.entry(
                    rs.getInt("code"),
                    level.prefix()
                        + " "
                        + String.format("%02d", rs.getInt("code"))
                        + " · "
                        + rs.getString("name")))
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /** Votos por territorio del nivel en la agrupación pedida. */
  public List<GroupRow> groupsByTerritory(int electionId, TerritoryLevel level, String groupBy) {
    return jdbc.query(
        "SELECT coalesce(g."
            + level.column()
            + ", 0) AS territory, g.code, sum(g.votes) AS votes"
            + "  FROM mv_section_group g"
            + " WHERE g.election_id = :election AND g.group_by = :groupBy"
            + " GROUP BY 1, 2",
        new MapSqlParameterSource("election", electionId).addValue("groupBy", groupBy),
        (rs, i) -> new GroupRow(rs.getInt("territory"), rs.getString("code"), rs.getLong("votes")));
  }

  /** Participación y votos por sección de un territorio del nivel. */
  public List<TurnoutRow> turnoutBySection(int electionId, TerritoryLevel level, int territory) {
    return jdbc.query(
        "SELECT t.section AS code,"
            + TURNOUT_SUMS
            + "  FROM mv_section_turnout t"
            + " WHERE t.election_id = :election AND coalesce(t."
            + level.column()
            + ", 0) = :territory"
            + " GROUP BY t.section ORDER BY t.section",
        new MapSqlParameterSource("election", electionId).addValue("territory", territory),
        (rs, i) -> turnout(rs, "SECCIÓN " + String.format("%04d", rs.getInt("code"))));
  }

  /** Votos por sección de un territorio del nivel en la agrupación pedida. */
  public List<GroupRow> groupsBySection(
      int electionId, TerritoryLevel level, int territory, String groupBy) {
    return jdbc.query(
        "SELECT g.section AS territory, g.code, sum(g.votes) AS votes"
            + "  FROM mv_section_group g"
            + " WHERE g.election_id = :election AND coalesce(g."
            + level.column()
            + ", 0) = :territory"
            + "   AND g.group_by = :groupBy"
            + " GROUP BY 1, 2",
        new MapSqlParameterSource("election", electionId)
            .addValue("territory", territory)
            .addValue("groupBy", groupBy),
        (rs, i) -> new GroupRow(rs.getInt("territory"), rs.getString("code"), rs.getLong("votes")));
  }

  /**
   * Nombre y color de cada código de la agrupación. Una candidatura toma el color del bloque de su
   * primer partido y se nombra SIN la persona ("MORENA", "Sigamos Haciendo Historia"): al sumar
   * varios territorios, la persona cambia; va aparte, en {@link #candidates}.
   */
  public Map<String, GroupMeta> groupMeta(int electionId, int year, String groupBy) {
    String sql =
        switch (groupBy) {
          case "candidacy" -> "SELECT DISTINCT ON (c.code) c.code,"
              + "       regexp_replace(c.name, '\\s*\\([^()]*\\)\\s*$', '') AS label, b.color"
              + "  FROM candidacy c"
              + "  LEFT JOIN bloc_party bp ON bp.year = :year AND bp.party_code = c.party_codes[1]"
              + "  LEFT JOIN bloc b ON b.code = bp.bloc_code"
              + " WHERE c.election_id = :election"
              + " ORDER BY c.code, c.territory NULLS FIRST";
          case "party" -> "SELECT p.code, p.code AS label, p.color FROM party p";
          default -> "SELECT b.code, b.name AS label, b.color FROM bloc b";
        };
    return jdbc
        .query(
            sql,
            new MapSqlParameterSource("election", electionId).addValue("year", year),
            (rs, i) ->
                new GroupMeta(rs.getString("code"), rs.getString("label"), rs.getString("color")))
        .stream()
        .collect(Collectors.toMap(GroupMeta::code, Function.identity()));
  }

  /**
   * Quién encabezó cada candidatura en cada territorio de la contienda (territory null = estatal).
   * El nombre va entre paréntesis al final de {@code candidacy.name}; las candidaturas del IEPCT no
   * lo traen, salvo las independientes.
   */
  public List<CandidateRow> candidates(int electionId) {
    return jdbc.query(
        "SELECT c.territory, c.code, substring(c.name from '\\(([^()]*)\\)\\s*$') AS person"
            + "  FROM candidacy c"
            + " WHERE c.election_id = :election AND c.name ~ '\\([^()]*\\)\\s*$'",
        new MapSqlParameterSource("election", electionId),
        (rs, i) ->
            new CandidateRow(
                rs.getString("territory"), rs.getString("code"), rs.getString("person")));
  }

  /**
   * De cada candidatura: si lleva a MORENA y el bloque de su primer partido ese año (el mismo que
   * da su color en {@link #groupMeta}).
   */
  public Map<String, CandidacyTraits> candidacyTraits(int electionId, int year) {
    return jdbc
        .query(
            "SELECT DISTINCT ON (c.code) c.code, 'MORENA' = ANY (c.party_codes) AS morena,"
                + "       bp.bloc_code"
                + "  FROM candidacy c"
                + "  LEFT JOIN bloc_party bp ON bp.year = :year AND bp.party_code = c.party_codes[1]"
                + " WHERE c.election_id = :election"
                + " ORDER BY c.code",
            new MapSqlParameterSource("election", electionId).addValue("year", year),
            (rs, i) ->
                new CandidacyTraits(
                    rs.getString("code"), rs.getBoolean("morena"), rs.getString("bloc_code")))
        .stream()
        .collect(Collectors.toMap(CandidacyTraits::code, Function.identity()));
  }

  /**
   * Participación de cada sección del estado, con su municipio y distritos (sin el municipio 0).
   */
  public List<SectionTurnoutRow> sectionTurnout(int electionId) {
    return jdbc.query(
        "SELECT t.section, t.municipality_cve,"
            + "       max(t.local_district) AS local_district,"
            + "       max(t.federal_district) AS federal_district,"
            + "       coalesce(sum(t.nominal_list), 0) AS nominal_list,"
            + "       coalesce(sum(t.total_votes), 0) AS total_votes,"
            + "       coalesce(sum(t.ordinary_votes), 0) AS ordinary_votes"
            + "  FROM mv_section_turnout t"
            + " WHERE t.election_id = :election AND t.municipality_cve > 0 AND t.section > 0"
            + " GROUP BY t.section, t.municipality_cve"
            + " ORDER BY t.section",
        new MapSqlParameterSource("election", electionId),
        (rs, i) ->
            new SectionTurnoutRow(
                rs.getInt("section"),
                rs.getInt("municipality_cve"),
                (Integer) rs.getObject("local_district"),
                (Integer) rs.getObject("federal_district"),
                rs.getLong("nominal_list"),
                rs.getLong("total_votes"),
                rs.getLong("ordinary_votes")));
  }

  /** Votos por sección de todo el estado en la agrupación pedida (sin el municipio 0). */
  public List<GroupRow> sectionGroups(int electionId, String groupBy) {
    return jdbc.query(
        "SELECT g.section AS territory, g.code, sum(g.votes) AS votes"
            + "  FROM mv_section_group g"
            + " WHERE g.election_id = :election AND g.group_by = :groupBy"
            + "   AND g.municipality_cve > 0 AND g.section > 0"
            + " GROUP BY 1, 2",
        new MapSqlParameterSource("election", electionId).addValue("groupBy", groupBy),
        (rs, i) -> new GroupRow(rs.getInt("territory"), rs.getString("code"), rs.getLong("votes")));
  }

  // ─────────────────────────── una sección en todas las elecciones

  /** La participación de una sección en cada elección donde aparece. */
  public List<SectionElectionTurnout> sectionHistoryTurnout(int section) {
    return jdbc.query(
        "SELECT t.election_id, max(t.municipality_cve) AS municipality_cve,"
            + "       coalesce(sum(t.nominal_list), 0) AS nominal_list,"
            + "       coalesce(sum(t.total_votes), 0) AS total_votes,"
            + "       coalesce(sum(t.ordinary_votes), 0) AS ordinary_votes,"
            + "       coalesce(sum(t.null_votes), 0) AS null_votes,"
            + "       coalesce(sum(t.non_registered_votes), 0) AS non_registered_votes"
            + "  FROM mv_section_turnout t"
            + " WHERE t.section = :section AND t.municipality_cve > 0"
            + " GROUP BY t.election_id",
        new MapSqlParameterSource("section", section),
        (rs, i) ->
            new SectionElectionTurnout(
                rs.getInt("election_id"),
                rs.getInt("municipality_cve"),
                rs.getLong("nominal_list"),
                rs.getLong("total_votes"),
                rs.getLong("ordinary_votes"),
                rs.getLong("null_votes"),
                rs.getLong("non_registered_votes")));
  }

  /** Los votos de una sección en cada elección, en la agrupación pedida. */
  public List<SectionElectionGroup> sectionHistoryGroups(int section, String groupBy) {
    return jdbc.query(
        "SELECT g.election_id, g.code, sum(g.votes) AS votes"
            + "  FROM mv_section_group g"
            + " WHERE g.section = :section AND g.group_by = :groupBy AND g.municipality_cve > 0"
            + " GROUP BY 1, 2",
        new MapSqlParameterSource("section", section).addValue("groupBy", groupBy),
        (rs, i) ->
            new SectionElectionGroup(
                rs.getInt("election_id"), rs.getString("code"), rs.getLong("votes")));
  }

  public record SectionElectionTurnout(
      int electionId,
      int municipality,
      long nominalList,
      long totalVotes,
      long ordinaryVotes,
      long nullVotes,
      long nonRegisteredVotes) {}

  public record SectionElectionGroup(int electionId, String code, long votes) {}

  private static ElectionSummary election(ResultSet rs) throws SQLException {
    return new ElectionSummary(
        rs.getInt("id"),
        rs.getInt("year"),
        rs.getString("office"),
        rs.getString("scope"),
        rs.getObject("election_date", java.time.LocalDate.class),
        rs.getString("publisher"),
        rs.getString("url"),
        rs.getString("file_name"),
        rs.getString("cut_label"));
  }

  private static TurnoutRow turnout(ResultSet rs, String name) throws SQLException {
    return new TurnoutRow(
        rs.getInt("code"),
        name,
        rs.getLong("stations"),
        rs.getLong("special_stations"),
        rs.getLong("nominal_list"),
        rs.getLong("total_votes"),
        rs.getLong("ordinary_votes"),
        rs.getLong("null_votes"),
        rs.getLong("non_registered_votes"));
  }

  public record TurnoutRow(
      int code,
      String name,
      long stations,
      long specialStations,
      long nominalList,
      long totalVotes,
      long ordinaryVotes,
      long nullVotes,
      long nonRegisteredVotes) {

    public TurnoutRow named(String newName) {
      return new TurnoutRow(
          code,
          newName,
          stations,
          specialStations,
          nominalList,
          totalVotes,
          ordinaryVotes,
          nullVotes,
          nonRegisteredVotes);
    }
  }

  public record GroupRow(int territory, String code, long votes) {}

  public record GroupMeta(String code, String label, String color) {}

  public record CandidateRow(String territory, String code, String person) {}

  public record CandidacyTraits(String code, boolean morena, String bloc) {}

  public record SectionTurnoutRow(
      int section,
      int municipality,
      Integer localDistrict,
      Integer federalDistrict,
      long nominalList,
      long totalVotes,
      long ordinaryVotes) {}
}
