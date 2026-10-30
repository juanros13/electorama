package com.electorama.msvc.structure.model.repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * El listado de convencidos de un puesto y de todo lo que cuelga de él, para la pantalla del
 * movilizador (y de quien tiene gente a cargo): buscar, filtrar y las tarjetas de arriba.
 */
@Repository
@RequiredArgsConstructor
public class SupporterListRepository {

  private final NamedParameterJdbcTemplate jdbc;

  /** El subárbol del puesto; mismo recorrido que el avance contra la meta. */
  private static final String TREE =
      "WITH RECURSIVE tree AS ("
          + "   SELECT id FROM position WHERE id = :position"
          + "   UNION ALL"
          + "   SELECT c.id FROM position c JOIN tree t ON c.parent_position_id = t.id"
          + "    WHERE c.valid_to IS NULL) ";

  /** El día de captura en hora de México. */
  private static final String DAY =
      "CAST(lower(m.validity) AT TIME ZONE 'America/Mexico_City' AS date)";

  public record Row(
      long id,
      String name,
      String curp,
      String phone,
      String section,
      String positionLabel,
      String supportsMovement,
      String committee,
      String pollingRep,
      String invitingOthers,
      Instant captured,
      Double latitude,
      Double longitude) {}

  public record Page(List<Row> content, long total) {}

  public record Filter(
      String q,
      String supports,
      Boolean committee,
      Boolean pollingRep,
      Boolean withLocation,
      LocalDate from,
      LocalDate to) {}

  public record Summary(
      long total,
      long today,
      long last7,
      long supportsYes,
      long withPhone,
      long withLocation,
      long committee,
      long pollingRep) {}

  public Page search(long positionId, Filter f, int page, int size) {
    MapSqlParameterSource p =
        new MapSqlParameterSource()
            .addValue("position", positionId)
            .addValue("size", size)
            .addValue("offset", (long) page * size);
    StringBuilder where = new StringBuilder(" WHERE upper(m.validity) IS NULL");
    if (f.q() != null && !f.q().isBlank()) {
      where.append(
          " AND (regexp_replace(concat_ws(' ', pe.names, pe.last_name, pe.sur_name), '\\s+',"
              + " ' ', 'g') ILIKE :q OR pe.curp ILIKE :q OR pe.cell_phone_number LIKE :q)");
      p.addValue("q", "%" + f.q().trim().replaceAll("\\s+", " ") + "%");
    }
    if (f.supports() != null) {
      where.append(" AND coalesce(d.supports_movement, 'SI') = :supports");
      p.addValue("supports", f.supports());
    }
    if (f.committee() != null) {
      where.append(" AND (d.committee_participation = 'SI') = :committee");
      p.addValue("committee", f.committee());
    }
    if (f.pollingRep() != null) {
      where.append(" AND (d.polling_rep_participation = 'SI') = :pollingRep");
      p.addValue("pollingRep", f.pollingRep());
    }
    if (f.withLocation() != null) {
      where.append(
          f.withLocation()
              ? " AND d.latitude ~ '^-?[0-9]+([.][0-9]+)?$'"
              : " AND (d.latitude IS NULL OR d.latitude !~ '^-?[0-9]+([.][0-9]+)?$')");
    }
    if (f.from() != null) {
      where.append(" AND ").append(DAY).append(" >= :from");
      p.addValue("from", f.from());
    }
    if (f.to() != null) {
      where.append(" AND ").append(DAY).append(" <= :to");
      p.addValue("to", f.to());
    }
    String sql =
        TREE
            + "SELECT m.id,"
            + "       nullif(regexp_replace(trim(concat_ws(' ', pe.names, pe.last_name,"
            + "              pe.sur_name)), '\\s+', ' ', 'g'), '') AS name,"
            + "       pe.curp, pe.cell_phone_number, ts.code AS section,"
            + "       coalesce(pp.section_key, tp.code) AS position_label,"
            + "       d.supports_movement, d.committee_participation, d.polling_rep_participation,"
            + "       d.inviting_others, lower(m.validity) AS captured,"
            + "       CASE WHEN d.latitude ~ '^-?[0-9]+([.][0-9]+)?$'"
            + "            THEN CAST(d.latitude AS double precision) END AS lat,"
            + "       CASE WHEN d.longitude ~ '^-?[0-9]+([.][0-9]+)?$'"
            + "            THEN CAST(d.longitude AS double precision) END AS lng,"
            + "       count(*) OVER () AS total"
            + "  FROM membership m"
            + "  JOIN tree ON tree.id = m.position_id"
            + "  JOIN person pe ON pe.id = m.person_id"
            + "  JOIN position pp ON pp.id = m.position_id"
            + "  JOIN territory tp ON tp.id = pp.territory_id"
            + "  LEFT JOIN territory ts ON ts.id = m.territory_id"
            + "  LEFT JOIN membership_detail d ON d.membership_id = m.id"
            + where
            + " ORDER BY lower(m.validity) DESC, m.id DESC"
            + " LIMIT :size OFFSET :offset";
    long[] total = {0};
    List<Row> rows =
        jdbc.query(
            sql,
            p,
            (rs, i) -> {
              total[0] = rs.getLong("total");
              return new Row(
                  rs.getLong("id"),
                  rs.getString("name"),
                  rs.getString("curp"),
                  rs.getString("cell_phone_number"),
                  rs.getString("section"),
                  rs.getString("position_label"),
                  rs.getString("supports_movement"),
                  rs.getString("committee_participation"),
                  rs.getString("polling_rep_participation"),
                  rs.getString("inviting_others"),
                  rs.getTimestamp("captured").toInstant(),
                  (Double) rs.getObject("lat"),
                  (Double) rs.getObject("lng"));
            });
    return new Page(rows, total[0]);
  }

  public Summary summary(long positionId) {
    return jdbc.queryForObject(
        TREE
            + "SELECT count(*) AS total,"
            + "       count(*) FILTER (WHERE "
            + DAY
            + " = CAST(now() AT TIME ZONE 'America/Mexico_City' AS date)) AS today,"
            + "       count(*) FILTER (WHERE "
            + DAY
            + " > CAST(now() AT TIME ZONE 'America/Mexico_City' AS date) - 7) AS last7,"
            + "       count(*) FILTER (WHERE coalesce(d.supports_movement, 'SI') = 'SI')"
            + "         AS supports_yes,"
            + "       count(pe.cell_phone_number) AS with_phone,"
            + "       count(*) FILTER (WHERE d.latitude ~ '^-?[0-9]+([.][0-9]+)?$')"
            + "         AS with_location,"
            + "       count(*) FILTER (WHERE d.committee_participation = 'SI') AS committee,"
            + "       count(*) FILTER (WHERE d.polling_rep_participation = 'SI') AS polling_rep"
            + "  FROM membership m"
            + "  JOIN tree ON tree.id = m.position_id"
            + "  JOIN person pe ON pe.id = m.person_id"
            + "  LEFT JOIN membership_detail d ON d.membership_id = m.id"
            + " WHERE upper(m.validity) IS NULL",
        Map.of("position", positionId),
        (rs, i) ->
            new Summary(
                rs.getLong("total"),
                rs.getLong("today"),
                rs.getLong("last7"),
                rs.getLong("supports_yes"),
                rs.getLong("with_phone"),
                rs.getLong("with_location"),
                rs.getLong("committee"),
                rs.getLong("polling_rep")));
  }
}
