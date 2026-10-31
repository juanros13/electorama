package com.electorama.msvc.electoralhistory.service;

import com.electorama.msvc.electoralhistory.dto.ElectionSummary;
import com.electorama.msvc.electoralhistory.dto.GroupVotes;
import com.electorama.msvc.electoralhistory.dto.SectionHistory;
import com.electorama.msvc.electoralhistory.dto.SectionReport;
import com.electorama.msvc.electoralhistory.dto.SectionTotals;
import com.electorama.msvc.electoralhistory.dto.TerritoryLevel;
import com.electorama.msvc.electoralhistory.dto.TerritoryReport;
import com.electorama.msvc.electoralhistory.dto.TerritoryResult;
import com.electorama.msvc.electoralhistory.dto.TrendPoint;
import com.electorama.msvc.electoralhistory.dto.TrendReport;
import com.electorama.msvc.electoralhistory.dto.TrendRow;
import com.electorama.msvc.electoralhistory.exception.ElectoralHistoryException;
import com.electorama.msvc.electoralhistory.repository.HistoryQueryRepository;
import com.electorama.msvc.electoralhistory.repository.HistoryQueryRepository.CandidateRow;
import com.electorama.msvc.electoralhistory.repository.HistoryQueryRepository.GroupMeta;
import com.electorama.msvc.electoralhistory.repository.HistoryQueryRepository.GroupRow;
import com.electorama.msvc.electoralhistory.repository.HistoryQueryRepository.TurnoutRow;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Arma los resultados por territorio a partir de los agregados de E5.
 *
 * <p>Definiciones (las mismas que documenta E5):
 *
 * <ul>
 *   <li>% sobre la votación total emitida, que incluye nulos y no registrados; también se da sobre
 *       la válida.
 *   <li>Participación: votos de casillas ordinarias / su lista nominal.
 *   <li>Ganador y margen: por la agrupación pedida; con {@code candidacy} es el resultado real.
 *   <li>Nombre de la persona candidata: sólo cuando el reporte está en el territorio de la
 *       contienda (diputación federal por distrito federal, ayuntamiento por municipio…) o la
 *       contienda es estatal. En otro nivel, un distrito junta candidaturas de varios municipios y
 *       no hay una sola persona que nombrar.
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ElectionService {

  public static final Set<String> GROUP_BY = Set.of("candidacy", "party", "bloc");

  /** Lo que no es de ningún territorio del nivel: sólo cuenta al estado. */
  private static final int STATEWIDE_ONLY = 0;

  private final HistoryQueryRepository history;

  public List<ElectionSummary> elections(int state) {
    return history.elections(state);
  }

  public TerritoryReport byTerritory(
      int state, int year, String office, TerritoryLevel level, String groupBy) {
    ElectionSummary election = election(state, year, office, groupBy);

    List<TurnoutRow> turnout = history.turnoutByTerritory(election.id(), state, level);
    Map<Integer, String> names = history.territoryNames(election.id(), state, level);
    Map<Integer, List<GroupRow>> groups =
        history.groupsByTerritory(election.id(), state, level, groupBy).stream()
            .collect(Collectors.groupingBy(GroupRow::territory));
    Map<String, GroupMeta> meta = history.groupMeta(election.id(), state, year, groupBy);
    Candidates candidates = candidates(election, state, level, groupBy);

    List<TerritoryResult> territories = new ArrayList<>();
    for (TurnoutRow row : turnout) {
      if (row.code() != STATEWIDE_ONLY) {
        TerritoryResult t =
            result(
                row.named(names.getOrDefault(row.code(), String.valueOf(row.code()))),
                groups.getOrDefault(row.code(), List.of()),
                meta);
        territories.add(candidates.name(t, String.valueOf(row.code())));
      }
    }
    TerritoryResult total =
        result(
            sum(turnout, history.stateName(state)),
            groups.values().stream().flatMap(List::stream).toList(),
            meta);

    return new TerritoryReport(
        election,
        groupBy,
        level,
        candidates.available(),
        candidates.name(total, null),
        territories);
  }

  public SectionReport bySection(
      int state, int year, String office, TerritoryLevel level, int territory, String groupBy) {
    ElectionSummary election = election(state, year, office, groupBy);

    List<TurnoutRow> turnout = history.turnoutBySection(election.id(), state, level, territory);
    if (turnout.isEmpty()) {
      throw ElectoralHistoryException.notFound("el territorio", territory);
    }
    List<GroupRow> rows = history.groupsBySection(election.id(), state, level, territory, groupBy);
    Map<Integer, List<GroupRow>> groups =
        rows.stream().collect(Collectors.groupingBy(GroupRow::territory));
    Map<String, GroupMeta> meta = history.groupMeta(election.id(), state, year, groupBy);
    String name =
        history
            .territoryNames(election.id(), state, level)
            .getOrDefault(territory, String.valueOf(territory));

    List<TerritoryResult> sections =
        turnout.stream()
            .map(row -> result(row, groups.getOrDefault(row.code(), List.of()), meta))
            .toList();
    TerritoryResult total =
        candidates(election, state, level, groupBy)
            .name(result(sum(turnout, name), rows, meta), String.valueOf(territory));
    return new SectionReport(election, groupBy, level, total, sections);
  }

  /**
   * Todas las secciones del estado en una elección, con sus votos por bloque: la base para cruzarla
   * con la estructura de campaña, que vive en otro servicio y se junta por número de sección. Sin
   * el municipio 0 (voto anticipado, en prisión y en el extranjero).
   */
  public List<SectionTotals> allSections(int state, int year, String office) {
    ElectionSummary election = election(state, year, office, "bloc");
    Map<Integer, Map<String, Long>> blocs = new HashMap<>();
    for (GroupRow r : history.sectionGroups(election.id(), state, "bloc")) {
      blocs
          .computeIfAbsent(r.territory(), k -> new LinkedHashMap<>())
          .merge(r.code(), r.votes(), Long::sum);
    }
    return history.sectionTurnout(election.id(), state).stream()
        .map(
            s ->
                new SectionTotals(
                    s.section(),
                    s.municipality(),
                    s.localDistrict(),
                    s.federalDistrict(),
                    s.nominalList(),
                    s.totalVotes(),
                    s.nominalList() == 0
                        ? null
                        : round(100.0 * s.ordinaryVotes() / s.nominalList()),
                    blocs.getOrDefault(s.section(), Map.of())))
        .toList();
  }

  /**
   * Un cargo en todos sus años. Cada punto trae los grupos pedidos y, aparte, el ganador por
   * candidatura: comparar el bloque entre años sólo tiene sentido junto al resultado real.
   */
  public TrendReport trend(int state, String office, String groupBy, TerritoryLevel level) {
    if (!GROUP_BY.contains(groupBy)) {
      throw ElectoralHistoryException.badRequest("groupBy debe ser uno de " + GROUP_BY);
    }
    List<ElectionSummary> elections =
        history.elections(state).stream()
            .filter(e -> e.office().equalsIgnoreCase(office))
            .sorted(Comparator.comparingInt(ElectionSummary::year))
            .toList();
    if (elections.isEmpty()) {
      throw ElectoralHistoryException.notFound("el cargo", office);
    }

    Map<Integer, List<TrendPoint>> byTerritory = new LinkedHashMap<>();
    Map<Integer, String> names = new LinkedHashMap<>();
    List<TrendPoint> statewide = new ArrayList<>();
    for (ElectionSummary e : elections) {
      TerritoryReport groups = byTerritory(state, e.year(), e.office(), level, groupBy);
      TerritoryReport real =
          groupBy.equals("candidacy")
              ? groups
              : byTerritory(state, e.year(), e.office(), level, "candidacy");
      Map<Integer, TerritoryResult> winners =
          real.territories().stream().collect(Collectors.toMap(TerritoryResult::code, t -> t));

      statewide.add(point(e.year(), groups.state(), real.state()));
      for (TerritoryResult t : groups.territories()) {
        // el nombre del distrito lleva su cabecera: la del año más reciente
        names.put(t.code(), t.name());
        byTerritory
            .computeIfAbsent(t.code(), k -> new ArrayList<>())
            .add(point(e.year(), t, winners.get(t.code())));
      }
    }

    List<TrendRow> territories =
        byTerritory.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> new TrendRow(entry.getKey(), names.get(entry.getKey()), entry.getValue()))
            .toList();
    return new TrendReport(
        office.toUpperCase(),
        groupBy,
        level,
        elections,
        new TrendRow(0, history.stateName(state), statewide),
        territories);
  }

  private static TrendPoint point(int year, TerritoryResult groups, TerritoryResult real) {
    GroupVotes winner = real == null || real.groups().isEmpty() ? null : real.groups().get(0);
    return new TrendPoint(
        year,
        groups.totalVotes(),
        groups.turnout(),
        groups.groups(),
        winner,
        real == null ? null : real.marginPp());
  }

  /**
   * Una sección en todas las elecciones: participación, votos por grupo (de mayor a menor), ganador
   * y margen. Las elecciones donde la sección no aparece (p.ej. distritos que no la incluyen) se
   * omiten.
   */
  public SectionHistory sectionHistory(int state, int section, String groupBy) {
    if (!GROUP_BY.contains(groupBy)) {
      throw ElectoralHistoryException.badRequest("groupBy debe ser uno de " + GROUP_BY);
    }
    Map<Integer, HistoryQueryRepository.SectionElectionTurnout> turnout =
        history.sectionHistoryTurnout(state, section).stream()
            .collect(Collectors.toMap(t -> t.electionId(), t -> t));
    if (turnout.isEmpty()) {
      throw ElectoralHistoryException.notFound("la sección", section);
    }
    Map<Integer, List<HistoryQueryRepository.SectionElectionGroup>> groups =
        history.sectionHistoryGroups(state, section, groupBy).stream()
            .collect(Collectors.groupingBy(g -> g.electionId()));

    List<SectionHistory.Election> out = new ArrayList<>();
    Integer municipality = null;
    for (ElectionSummary e : history.elections(state)) {
      HistoryQueryRepository.SectionElectionTurnout t = turnout.get(e.id());
      if (t == null) {
        continue;
      }
      municipality = t.municipality();
      Map<String, GroupMeta> meta = history.groupMeta(e.id(), state, e.year(), groupBy);
      long valid = t.totalVotes() - t.nullVotes() - t.nonRegisteredVotes();
      List<GroupVotes> votes =
          groups.getOrDefault(e.id(), List.of()).stream()
              .map(
                  g -> {
                    GroupMeta m = meta.get(g.code());
                    return new GroupVotes(
                        g.code(),
                        m == null ? g.code() : m.label(),
                        m == null ? null : m.color(),
                        g.votes(),
                        t.totalVotes() == 0 ? 0 : round(100.0 * g.votes() / t.totalVotes()),
                        valid <= 0 ? 0 : round(100.0 * g.votes() / valid),
                        null);
                  })
              .sorted(Comparator.comparingLong(GroupVotes::votes).reversed())
              .toList();
      GroupVotes first = votes.isEmpty() ? null : votes.get(0);
      GroupVotes second = votes.size() < 2 ? null : votes.get(1);
      out.add(
          new SectionHistory.Election(
              e.year(),
              e.office(),
              e.electionDate(),
              t.nominalList(),
              t.totalVotes(),
              t.nominalList() == 0 ? null : round(100.0 * t.ordinaryVotes() / t.nominalList()),
              votes,
              first == null ? null : first.code(),
              first == null || second == null ? null : round(first.pct() - second.pct())));
    }
    return new SectionHistory(section, municipality, out);
  }

  private ElectionSummary election(int state, int year, String office, String groupBy) {
    if (!GROUP_BY.contains(groupBy)) {
      throw ElectoralHistoryException.badRequest("groupBy debe ser uno de " + GROUP_BY);
    }
    return history
        .election(year, office.toUpperCase(), state)
        .orElseThrow(() -> ElectoralHistoryException.notFound("la elección", year + " " + office));
  }

  /**
   * Los nombres de las personas candidatas que se pueden mostrar en este reporte: sólo por
   * candidatura, y sólo si el nivel es el de la contienda o la contienda es estatal.
   */
  private Candidates candidates(
      ElectionSummary election, int state, TerritoryLevel level, String groupBy) {
    List<CandidateRow> rows = history.candidates(election.id(), state);
    boolean statewide = "STATE".equals(election.scope());
    boolean applies =
        "candidacy".equals(groupBy) && (statewide || level.matchesScope(election.scope()));
    Map<String, String> byKey = new HashMap<>();
    if (applies) {
      for (CandidateRow r : rows) {
        byKey.put(key(r.territory(), r.code()), r.person());
      }
    }
    // hay nombres si la fuente los trae para algo más que las independientes
    boolean available = rows.stream().anyMatch(r -> !r.code().startsWith("IND"));
    return new Candidates(byKey, statewide, available);
  }

  private static String key(String territory, String code) {
    return (territory == null ? "" : territory) + "|" + code;
  }

  private record Candidates(Map<String, String> byKey, boolean statewide, boolean available) {

    /** El territorio con el nombre de la persona en cada candidatura que lo tenga. */
    TerritoryResult name(TerritoryResult t, String territory) {
      if (byKey.isEmpty()) {
        return t;
      }
      String where = statewide ? null : territory;
      if (where == null && !statewide) {
        return t; // el total estatal de una contienda por territorio: varias personas por código
      }
      List<GroupVotes> groups =
          t.groups().stream().map(g -> g.withCandidate(byKey.get(key(where, g.code())))).toList();
      return new TerritoryResult(
          t.code(),
          t.name(),
          t.stations(),
          t.specialStations(),
          t.nominalList(),
          t.totalVotes(),
          t.turnout(),
          t.nullVotes(),
          t.nonRegisteredVotes(),
          groups,
          t.winner(),
          t.runnerUp(),
          t.marginPp());
    }
  }

  private TerritoryResult result(TurnoutRow row, List<GroupRow> rows, Map<String, GroupMeta> meta) {
    long valid = row.totalVotes() - row.nullVotes() - row.nonRegisteredVotes();
    Map<String, Long> byCode =
        rows.stream()
            .collect(
                Collectors.groupingBy(GroupRow::code, Collectors.summingLong(GroupRow::votes)));

    List<GroupVotes> votes =
        byCode.entrySet().stream()
            .map(
                e -> {
                  GroupMeta m = meta.get(e.getKey());
                  return new GroupVotes(
                      e.getKey(),
                      m == null ? e.getKey() : m.label(),
                      m == null ? null : m.color(),
                      e.getValue(),
                      pct(e.getValue(), row.totalVotes()),
                      pct(e.getValue(), valid),
                      null);
                })
            .sorted(Comparator.comparingLong(GroupVotes::votes).reversed())
            .toList();

    GroupVotes first = votes.isEmpty() ? null : votes.get(0);
    GroupVotes second = votes.size() < 2 ? null : votes.get(1);
    Double turnout =
        row.nominalList() == 0 ? null : round(100.0 * row.ordinaryVotes() / row.nominalList());

    return new TerritoryResult(
        row.code(),
        row.name(),
        row.stations(),
        row.specialStations(),
        row.nominalList(),
        row.totalVotes(),
        turnout,
        row.nullVotes(),
        row.nonRegisteredVotes(),
        votes,
        first == null ? null : first.code(),
        second == null ? null : second.code(),
        first == null || second == null ? null : round(first.pct() - second.pct()));
  }

  private static TurnoutRow sum(List<TurnoutRow> rows, String name) {
    return new TurnoutRow(
        0,
        name,
        rows.stream().mapToLong(TurnoutRow::stations).sum(),
        rows.stream().mapToLong(TurnoutRow::specialStations).sum(),
        rows.stream().mapToLong(TurnoutRow::nominalList).sum(),
        rows.stream().mapToLong(TurnoutRow::totalVotes).sum(),
        rows.stream().mapToLong(TurnoutRow::ordinaryVotes).sum(),
        rows.stream().mapToLong(TurnoutRow::nullVotes).sum(),
        rows.stream().mapToLong(TurnoutRow::nonRegisteredVotes).sum());
  }

  private static double pct(long part, long whole) {
    return whole == 0 ? 0 : round(100.0 * part / whole);
  }

  private static double round(double value) {
    return Math.round(value * 100) / 100.0;
  }
}
