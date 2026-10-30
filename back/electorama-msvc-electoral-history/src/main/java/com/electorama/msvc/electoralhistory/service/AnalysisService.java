package com.electorama.msvc.electoralhistory.service;

import com.electorama.msvc.electoralhistory.dto.AnalysisKpis;
import com.electorama.msvc.electoralhistory.dto.AnalysisReport;
import com.electorama.msvc.electoralhistory.dto.ElectionSummary;
import com.electorama.msvc.electoralhistory.dto.GroupVotes;
import com.electorama.msvc.electoralhistory.dto.RiskStatus;
import com.electorama.msvc.electoralhistory.dto.RivalTrend;
import com.electorama.msvc.electoralhistory.dto.SectionRisk;
import com.electorama.msvc.electoralhistory.dto.TerritoryLevel;
import com.electorama.msvc.electoralhistory.dto.TerritoryReport;
import com.electorama.msvc.electoralhistory.dto.TerritoryResult;
import com.electorama.msvc.electoralhistory.dto.TerritoryRisk;
import com.electorama.msvc.electoralhistory.exception.ElectoralHistoryException;
import com.electorama.msvc.electoralhistory.repository.HistoryQueryRepository;
import com.electorama.msvc.electoralhistory.repository.HistoryQueryRepository.CandidacyTraits;
import com.electorama.msvc.electoralhistory.repository.HistoryQueryRepository.GroupMeta;
import com.electorama.msvc.electoralhistory.repository.HistoryQueryRepository.GroupRow;
import com.electorama.msvc.electoralhistory.repository.HistoryQueryRepository.SectionTurnoutRow;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * El análisis de un año para dirección, desde MORENA y sus aliados, en lo que se vuelve a elegir:
 * ayuntamientos y diputaciones locales.
 *
 * <p>Definiciones:
 *
 * <ul>
 *   <li>Lado propio: la mejor candidatura del bloque 4T en ese territorio, MORENA (sola o en
 *       coalición) o un aliado que compitió aparte; si ganó el aliado, se marca. Rival: la mejor
 *       fuera del bloque.
 *   <li>Margen: lado propio menos la rival, en pp sobre la votación total; el estado (perdido,
 *       cerrado, competido, seguro) sale de {@link RiskStatus}.
 *   <li>Bloque 4T: el del año (E2), sume o no en coalición.
 *   <li>Marca: el bloque 4T en gubernatura si hubo ese año; si no, en presidencia. La brecha es
 *       cuánto se quedó el candidato local debajo de ella.
 *   <li>Secciones a vigilar: las perdidas, las ganadas por menos de 10 pp y aquellas donde la
 *       brecha con la marca pasa de {@link #SECTION_GAP_PP} pp.
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class AnalysisService {

  private static final String BLOC_4T = "MORENA_ALLIES";
  private static final String MAYOR = "MAYOR";
  private static final String LOCAL_DEPUTY = "LOCAL_DEPUTY";
  private static final List<String> RIVAL_PARTIES = List.of("MC", "PRD", "PRI", "PAN");
  private static final double SECTION_GAP_PP = 20;
  private static final Locale MX = Locale.forLanguageTag("es-MX");
  private static final CandidacyTraits NONE = new CandidacyTraits(null, false, null);

  private final ElectionService elections;
  private final HistoryQueryRepository history;

  public AnalysisReport analysis(int year) {
    ElectionSummary mayor = find(year, MAYOR).orElseThrow(() -> notFound(year, MAYOR));
    ElectionSummary deputy =
        find(year, LOCAL_DEPUTY).orElseThrow(() -> notFound(year, LOCAL_DEPUTY));
    ElectionSummary brand =
        find(year, "GOVERNOR")
            .or(() -> find(year, "PRESIDENT"))
            .orElseThrow(() -> notFound(year, "GOVERNOR/PRESIDENT"));
    Optional<ElectionSummary> previous =
        history.elections().stream()
            .filter(e -> e.office().equals(MAYOR) && e.year() < year)
            .max(Comparator.comparingInt(ElectionSummary::year));

    TerritoryLevel muni = TerritoryLevel.MUNICIPALITY;
    TerritoryLevel dist = TerritoryLevel.LOCAL_DISTRICT;
    TerritoryReport brandBloc = elections.byTerritory(year, brand.office(), muni, "bloc");
    TerritoryReport brandBlocByDistrict = elections.byTerritory(year, brand.office(), dist, "bloc");

    List<TerritoryRisk> municipalities =
        risks(
            mayor,
            elections.byTerritory(year, MAYOR, muni, "candidacy"),
            elections.byTerritory(year, MAYOR, muni, "bloc"),
            brandBloc,
            previous.map(p -> elections.byTerritory(p.year(), MAYOR, muni, "bloc")).orElse(null));
    List<TerritoryRisk> districts =
        risks(
            deputy,
            elections.byTerritory(year, LOCAL_DEPUTY, dist, "candidacy"),
            elections.byTerritory(year, LOCAL_DEPUTY, dist, "bloc"),
            brandBlocByDistrict,
            null);

    TerritoryReport mayorBloc = elections.byTerritory(year, MAYOR, muni, "bloc");
    List<SectionRisk> sections = sections(mayor, brand, municipalities);
    List<RivalTrend> rivals = rivals(year);

    double brand4t = pct4t(brandBloc.state());
    double mayor4t = pct4t(mayorBloc.state());
    long sectionCount = history.sectionTurnout(mayor.id()).size();
    AnalysisKpis kpis =
        new AnalysisKpis(
            brand4t,
            mayor4t,
            round(brand4t - mayor4t),
            municipalities.size(),
            count(municipalities, RiskStatus.LOST),
            count(municipalities, RiskStatus.CLOSE),
            districts.size(),
            count(districts, RiskStatus.LOST),
            (int) sectionCount,
            (int) sections.stream().filter(s -> s.status() == RiskStatus.LOST).count(),
            (int)
                sections.stream()
                    .filter(
                        s -> s.status() == RiskStatus.CLOSE || s.status() == RiskStatus.COMPETITIVE)
                    .count(),
            mayorBloc.state().nominalList() - mayorBloc.state().totalVotes(),
            mayorBloc.state().turnout());

    return new AnalysisReport(
        year,
        mayor,
        deputy,
        brand,
        previous.map(ElectionSummary::year).orElse(null),
        findings(year, brand, previous.orElse(null), kpis, municipalities, districts, rivals),
        kpis,
        municipalities,
        districts,
        rivals,
        sections);
  }

  // ---------------------------------------------------------------- territorios

  private List<TerritoryRisk> risks(
      ElectionSummary election,
      TerritoryReport candidacies,
      TerritoryReport blocs,
      TerritoryReport brand,
      TerritoryReport previous) {
    Map<String, CandidacyTraits> traits = history.candidacyTraits(election.id(), election.year());
    Map<Integer, TerritoryResult> blocBy = byCode(blocs);
    Map<Integer, TerritoryResult> brandBy = byCode(brand);
    Map<Integer, TerritoryResult> previousBy = previous == null ? Map.of() : byCode(previous);

    List<TerritoryRisk> out = new ArrayList<>();
    for (TerritoryResult t : candidacies.territories()) {
      GroupVotes winner = t.groups().isEmpty() ? null : t.groups().get(0);
      GroupVotes best4t = first(t.groups(), traits, true);
      GroupVotes rival = first(t.groups(), traits, false);
      Double margin = best4t == null || rival == null ? null : round(best4t.pct() - rival.pct());
      RiskStatus status = RiskStatus.of(margin);

      Double bloc4t = pct4t(blocBy.get(t.code()));
      Double brand4t = pct4t(brandBy.get(t.code()));
      Double previous4t = pct4t(previousBy.get(t.code()));
      GroupVotes second = t.groups().size() < 2 ? null : t.groups().get(1);
      boolean allyWinner =
          winner != null && winner == best4t && !traits.getOrDefault(winner.code(), NONE).morena();
      boolean allyRunnerUp = second != null && winner == best4t && is4t(second, traits);

      out.add(
          new TerritoryRisk(
              t.code(),
              t.name(),
              status,
              winner,
              best4t,
              rival,
              margin,
              bloc4t,
              brand4t,
              diff(brand4t, bloc4t),
              previous4t,
              diff(bloc4t, previous4t),
              t.turnout(),
              t.nominalList(),
              status == RiskStatus.LOST
                  && bloc4t != null
                  && winner != null
                  && bloc4t > winner.pct(),
              allyWinner,
              allyRunnerUp));
    }
    out.sort(
        Comparator.comparing(TerritoryRisk::status)
            .thenComparing(r -> r.marginPp() == null ? 0 : r.marginPp()));
    return out;
  }

  // ---------------------------------------------------------------- secciones

  private List<SectionRisk> sections(
      ElectionSummary mayor, ElectionSummary brand, List<TerritoryRisk> municipalities) {
    Map<String, CandidacyTraits> traits = history.candidacyTraits(mayor.id(), mayor.year());
    Map<String, GroupMeta> meta = history.groupMeta(mayor.id(), mayor.year(), "candidacy");
    Map<Integer, List<GroupRow>> votes = bySection(history.sectionGroups(mayor.id(), "candidacy"));
    Map<Integer, List<GroupRow>> blocs = bySection(history.sectionGroups(mayor.id(), "bloc"));
    Map<Integer, List<GroupRow>> brandBlocs = bySection(history.sectionGroups(brand.id(), "bloc"));
    Map<Integer, Long> brandTotals =
        history.sectionTurnout(brand.id()).stream()
            .collect(
                Collectors.toMap(
                    SectionTurnoutRow::section, SectionTurnoutRow::totalVotes, Long::sum));
    Map<Integer, String> names =
        municipalities.stream().collect(Collectors.toMap(TerritoryRisk::code, TerritoryRisk::name));

    List<SectionRisk> out = new ArrayList<>();
    for (SectionTurnoutRow s : history.sectionTurnout(mayor.id())) {
      List<GroupRow> rows = votes.getOrDefault(s.section(), List.of());
      GroupRow best4t = best(rows, r -> is4t(r.code(), traits));
      GroupRow rival = best(rows, r -> !is4t(r.code(), traits));
      if (best4t == null || rival == null || s.totalVotes() == 0) {
        continue;
      }
      double best4tPct = pct(best4t.votes(), s.totalVotes());
      double rivalPct = pct(rival.votes(), s.totalVotes());
      double margin = round(best4tPct - rivalPct);
      Double bloc4t = blocPct(blocs.get(s.section()), s.totalVotes());
      Double brand4t =
          blocPct(brandBlocs.get(s.section()), brandTotals.getOrDefault(s.section(), 0L));
      Double gap = diff(brand4t, bloc4t);
      RiskStatus status = RiskStatus.of(margin);
      if (status == RiskStatus.SAFE && (gap == null || gap <= SECTION_GAP_PP)) {
        continue;
      }
      out.add(
          new SectionRisk(
              s.section(),
              s.municipality(),
              names.get(s.municipality()),
              s.localDistrict(),
              s.federalDistrict(),
              s.nominalList(),
              s.nominalList() == 0 ? null : round(100.0 * s.ordinaryVotes() / s.nominalList()),
              status,
              label(meta, best4t.code()),
              best4tPct,
              label(meta, rival.code()),
              rivalPct,
              margin,
              best4t.votes() - rival.votes(),
              bloc4t,
              brand4t,
              gap));
    }
    out.sort(
        Comparator.comparing(SectionRisk::status)
            .thenComparingLong(SectionRisk::marginVotes)
            .thenComparingInt(SectionRisk::section));
    return out;
  }

  // ---------------------------------------------------------------- rivales

  /** MC, PRD, PRI y PAN (votos de su emblema) e independientes, en todos los ayuntamientos. */
  private List<RivalTrend> rivals(int upTo) {
    List<ElectionSummary> mayors =
        history.elections().stream()
            .filter(e -> e.office().equals(MAYOR) && e.year() <= upTo)
            .sorted(Comparator.comparingInt(ElectionSummary::year))
            .toList();
    Map<String, List<RivalTrend.Point>> points = new java.util.LinkedHashMap<>();
    Map<String, String> colors = new java.util.HashMap<>();
    for (ElectionSummary e : mayors) {
      TerritoryResult parties =
          elections.byTerritory(e.year(), MAYOR, TerritoryLevel.MUNICIPALITY, "party").state();
      TerritoryResult candidacies =
          elections.byTerritory(e.year(), MAYOR, TerritoryLevel.MUNICIPALITY, "candidacy").state();
      for (String code : RIVAL_PARTIES) {
        GroupVotes g =
            parties.groups().stream().filter(v -> v.code().equals(code)).findFirst().orElse(null);
        long v = g == null ? 0 : g.votes();
        if (g != null) {
          colors.put(code, g.color());
        }
        points
            .computeIfAbsent(code, k -> new ArrayList<>())
            .add(new RivalTrend.Point(e.year(), v, pct(v, candidacies.totalVotes())));
      }
      long independents =
          candidacies.groups().stream()
              .filter(g -> g.code().startsWith("IND"))
              .mapToLong(GroupVotes::votes)
              .sum();
      points
          .computeIfAbsent("IND", k -> new ArrayList<>())
          .add(
              new RivalTrend.Point(
                  e.year(), independents, pct(independents, candidacies.totalVotes())));
    }
    return points.entrySet().stream()
        .map(
            e ->
                new RivalTrend(
                    e.getKey(),
                    e.getKey().equals("IND") ? "Independientes" : e.getKey(),
                    e.getKey().equals("IND") ? "#6B7280" : colors.get(e.getKey()),
                    e.getValue()))
        .toList();
  }

  // ---------------------------------------------------------------- hallazgos

  private static List<String> findings(
      int year,
      ElectionSummary brand,
      ElectionSummary previous,
      AnalysisKpis k,
      List<TerritoryRisk> municipalities,
      List<TerritoryRisk> districts,
      List<RivalTrend> rivals) {
    List<String> out = new ArrayList<>();
    String brandName = brand.office().equals("GOVERNOR") ? "gubernatura" : "presidencia";

    List<TerritoryRisk> lost = with(municipalities, RiskStatus.LOST);
    if (!lost.isEmpty()) {
      out.add(
          String.format(
              MX,
              "Se perdieron %d de %d alcaldías: %s.",
              lost.size(),
              municipalities.size(),
              lost.stream()
                  .map(r -> title(r.name()) + " (ganó " + r.winner().label() + ")")
                  .collect(Collectors.joining(", "))));
    }

    List<TerritoryRisk> split = municipalities.stream().filter(TerritoryRisk::alliesSplit).toList();
    if (!split.isEmpty()) {
      out.add(
          String.format(
              MX,
              "En %s, MORENA y sus aliados juntos superaban al ganador; compitieron separados y se"
                  + " perdió.",
              split.stream().map(r -> title(r.name())).collect(Collectors.joining(" y "))));
    }

    List<TerritoryRisk> allies = municipalities.stream().filter(TerritoryRisk::allyWinner).toList();
    if (!allies.isEmpty()) {
      out.add(
          String.format(
              MX,
              "%d alcaldías las ganó un aliado compitiendo contra MORENA: %s.",
              allies.size(),
              allies.stream()
                  .map(r -> title(r.name()) + " (" + r.winner().label() + ")")
                  .collect(Collectors.joining(", "))));
    }

    List<TerritoryRisk> close = with(municipalities, RiskStatus.CLOSE);
    if (!close.isEmpty()) {
      out.add(
          "Ganadas por menos de 5 pp: "
              + close.stream()
                  .map(
                      r ->
                          String.format(
                              MX,
                              "%s (%.1f pp ante %s)",
                              title(r.name()),
                              r.marginPp(),
                              r.rival().label()))
                  .collect(Collectors.joining(", "))
              + ".");
    }

    List<TerritoryRisk> gaps =
        municipalities.stream()
            .filter(r -> r.brandGapPp() != null && r.brandGapPp() >= SECTION_GAP_PP)
            .sorted(Comparator.comparing(TerritoryRisk::brandGapPp).reversed())
            .limit(3)
            .toList();
    out.add(
        String.format(
                MX,
                "La marca sacó %.1f %% en %s y los candidatos a ayuntamiento %.1f %%: %.1f pp menos.",
                k.brand4tPct(),
                brandName,
                k.mayor4tPct(),
                k.brandGapPp())
            + (gaps.isEmpty()
                ? ""
                : " La brecha más grande: "
                    + gaps.stream()
                        .map(
                            r -> String.format(MX, "%s (%.0f pp)", title(r.name()), r.brandGapPp()))
                        .collect(Collectors.joining(", "))
                    + "."));

    if (previous != null) {
      List<TerritoryRisk> falls =
          municipalities.stream()
              .filter(r -> r.change4tPp() != null && r.change4tPp() <= -5)
              .sorted(Comparator.comparing(TerritoryRisk::change4tPp))
              .toList();
      if (!falls.isEmpty()) {
        out.add(
            String.format(MX, "Caída del bloque 4T en ayuntamientos contra %d: ", previous.year())
                + falls.stream()
                    .map(r -> String.format(MX, "%s (%.0f pp)", title(r.name()), r.change4tPp()))
                    .collect(Collectors.joining(", "))
                + ".");
      }
    }

    rivals.stream()
        .filter(r -> r.points().size() >= 2)
        .max(Comparator.comparingLong(AnalysisService::growth))
        .filter(r -> growth(r) > 0)
        .ifPresent(
            r -> {
              RivalTrend.Point a = r.points().get(r.points().size() - 2);
              RivalTrend.Point b = r.points().get(r.points().size() - 1);
              out.add(
                  String.format(
                      MX,
                      "El rival que más creció es %s: de %,d a %,d votos en ayuntamientos (%d → %d).",
                      r.label(),
                      a.votes(),
                      b.votes(),
                      a.year(),
                      b.year()));
            });

    out.add(
        String.format(
            MX,
            "%d secciones perdidas y %d competidas (menos de 10 pp) en ayuntamientos, de %d.",
            k.sectionsLost(),
            k.sectionsCompetitive(),
            k.sections()));

    TerritoryRisk tightest =
        districts.stream()
            .filter(r -> r.marginPp() != null)
            .min(Comparator.comparing(TerritoryRisk::marginPp))
            .orElse(null);
    out.add(
        String.format(
                MX,
                "Diputación local: se ganaron %d de %d distritos.",
                k.localDistricts() - k.localDistrictsLost(),
                k.localDistricts())
            + (tightest == null
                ? ""
                : String.format(
                    MX,
                    " El margen más corto: %s, %.1f pp ante %s.",
                    title(tightest.name()),
                    tightest.marginPp(),
                    tightest.rival().label())));

    out.add(
        String.format(
            MX,
            "En %d, %,d personas de la lista nominal no votaron por ayuntamiento (participación %.1f %%).",
            year,
            k.abstention(),
            k.turnout() == null ? 0 : k.turnout()));
    return out;
  }

  // ---------------------------------------------------------------- utilidades

  private Optional<ElectionSummary> find(int year, String office) {
    return history.election(year, office);
  }

  private static ElectoralHistoryException notFound(int year, String office) {
    return ElectoralHistoryException.notFound("la elección", year + " " + office);
  }

  private static Map<Integer, TerritoryResult> byCode(TerritoryReport report) {
    return report.territories().stream()
        .collect(Collectors.toMap(TerritoryResult::code, Function.identity()));
  }

  private static Map<Integer, List<GroupRow>> bySection(List<GroupRow> rows) {
    return rows.stream().collect(Collectors.groupingBy(GroupRow::territory));
  }

  private static GroupVotes first(
      List<GroupVotes> groups, Map<String, CandidacyTraits> traits, boolean in4t) {
    return groups.stream().filter(g -> is4t(g, traits) == in4t).findFirst().orElse(null);
  }

  private static boolean is4t(GroupVotes g, Map<String, CandidacyTraits> traits) {
    return is4t(g.code(), traits);
  }

  /** Lleva a MORENA, o su primer partido es del bloque 4T ese año (un aliado solo). */
  private static boolean is4t(String code, Map<String, CandidacyTraits> traits) {
    CandidacyTraits t = traits.get(code);
    return t != null && (t.morena() || BLOC_4T.equals(t.bloc()));
  }

  private static GroupRow best(List<GroupRow> rows, java.util.function.Predicate<GroupRow> which) {
    return rows.stream().filter(which).max(Comparator.comparingLong(GroupRow::votes)).orElse(null);
  }

  private static String label(Map<String, GroupMeta> meta, String code) {
    GroupMeta m = meta.get(code);
    return m == null ? code : m.label();
  }

  private static Double pct4t(TerritoryResult t) {
    if (t == null) {
      return null;
    }
    return t.groups().stream()
        .filter(g -> g.code().equals(BLOC_4T))
        .map(GroupVotes::pct)
        .findFirst()
        .orElse(0.0);
  }

  private static Double blocPct(List<GroupRow> rows, long total) {
    if (rows == null || total == 0) {
      return null;
    }
    long v = rows.stream().filter(r -> r.code().equals(BLOC_4T)).mapToLong(GroupRow::votes).sum();
    return pct(v, total);
  }

  private static long growth(RivalTrend r) {
    List<RivalTrend.Point> p = r.points();
    return p.get(p.size() - 1).votes() - p.get(p.size() - 2).votes();
  }

  private static int count(List<TerritoryRisk> risks, RiskStatus status) {
    return with(risks, status).size();
  }

  private static List<TerritoryRisk> with(List<TerritoryRisk> risks, RiskStatus status) {
    return risks.stream().filter(r -> r.status() == status).toList();
  }

  /** "EMILIANO ZAPATA" → "Emiliano Zapata"; "DISTRITO LOCAL 04 · CENTRO" se deja como viene. */
  private static String title(String name) {
    if (name == null || name.startsWith("DISTRITO")) {
      return name;
    }
    return java.util.Arrays.stream(name.toLowerCase(MX).split(" "))
        .map(w -> w.length() <= 2 ? w : Character.toUpperCase(w.charAt(0)) + w.substring(1))
        .collect(Collectors.joining(" "));
  }

  private static Double diff(Double a, Double b) {
    return a == null || b == null ? null : round(a - b);
  }

  private static double pct(long part, long whole) {
    return whole == 0 ? 0 : round(100.0 * part / whole);
  }

  private static double round(double value) {
    return Math.round(value * 100) / 100.0;
  }
}
