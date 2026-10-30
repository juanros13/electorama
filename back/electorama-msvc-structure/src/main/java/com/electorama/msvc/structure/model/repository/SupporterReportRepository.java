package com.electorama.msvc.structure.model.repository;

import com.electorama.msvc.structure.model.entity.Membership;
import com.electorama.msvc.structure.model.projection.SupporterCoordinatorRow;
import com.electorama.msvc.structure.model.projection.SupporterProfileRow;
import com.electorama.msvc.structure.model.projection.SupporterSectionRow;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * El análisis de los convencidos de una campaña (hoy, la cerrada de 2024): dónde estaban, cuánto
 * rindió cada coordinador y qué contestaron en la encuesta de captura. Sólo lectura y todo
 * agregado: no sale ningún dato personal salvo el nombre del coordinador.
 *
 * <p>Cuenta los convencidos vigentes y activos de la campaña, como el tablero ({@code
 * municipalitySummary}); los de 2024 siguen vigentes porque la campaña se cerró sin tocarlos.
 */
@Repository
public interface SupporterReportRepository extends JpaRepository<Membership, Long> {

  /** Por sección de residencia: principal, alterna, «votó» y el rango de captura. */
  @Query(
      value =
          "SELECT t.section_geo_id AS sectionGeoId,"
              + "       count(*) FILTER (WHERE s.type = 'MAIN') AS main,"
              + "       count(*) FILTER (WHERE s.type = 'ALTERNATE') AS alternate,"
              + "       count(*) FILTER (WHERE m.vote_flag) AS voted,"
              + "       min(lower(m.validity)) AS firstCaptured,"
              + "       max(lower(m.validity)) AS lastCaptured"
              + "  FROM membership m"
              + "  JOIN structure s ON s.id = m.structure_id AND s.campaign_id = :campaignId"
              + "  JOIN territory t ON t.id = m.territory_id"
              + " WHERE upper_inf(m.validity) AND m.status = 'ACTIVO'"
              + "   AND t.section_geo_id IS NOT NULL"
              + " GROUP BY t.section_geo_id",
      nativeQuery = true)
  List<SupporterSectionRow> bySection(@Param("campaignId") Long campaignId);

  /**
   * Cada coordinador de zona de la estructura principal con sus promotores y los convencidos de
   * toda su rama (los de sus promotores más los anclados a él).
   */
  @Query(
      value =
          "WITH mem AS MATERIALIZED ("
              + "  SELECT m.position_id, count(*) AS n, count(*) FILTER (WHERE m.vote_flag) AS v"
              + "    FROM membership m"
              + "    JOIN structure s ON s.id = m.structure_id AND s.campaign_id = :campaignId"
              + "                     AND s.type = 'MAIN'"
              + "   WHERE upper_inf(m.validity) AND m.status = 'ACTIVO'"
              + "   GROUP BY 1),"
              + " cz AS MATERIALIZED ("
              + "  SELECT p.id, p.code, p.goal, p.territory_id"
              + "    FROM position p"
              + "    JOIN structure s ON s.id = p.structure_id AND s.campaign_id = :campaignId"
              + "                     AND s.type = 'MAIN'"
              + "   WHERE p.level = 'ZONE_COORDINATOR' AND p.valid_to IS NULL),"
              // los promotores de cada coordinador, sumados de una pasada: con un LATERAL
              // por coordinador se recorrían los 42 mil agregados 5,700 veces (15 s)
              + " ch AS ("
              + "  SELECT c.parent_position_id AS cz_id, count(*) AS promoters,"
              + "         sum(mem.n) AS n, sum(mem.v) AS v"
              + "    FROM position c LEFT JOIN mem ON mem.position_id = c.id"
              + "   WHERE c.level = 'PROMOTER' AND c.valid_to IS NULL"
              + "     AND c.parent_position_id IN (SELECT id FROM cz)"
              + "   GROUP BY 1)"
              + " SELECT cz.id AS positionId, cz.code AS code, t.type AS territoryType,"
              + "        t.section_geo_id AS sectionGeoId,"
              + "        coalesce(t.municipality_geo_id, par.municipality_geo_id) AS municipalityGeoId,"
              + "        h.name AS holderName, cz.goal AS goal,"
              + "        coalesce(ch.promoters, 0) AS promoters,"
              + "        coalesce(own.n, 0) + coalesce(ch.n, 0) AS members,"
              + "        coalesce(own.v, 0) + coalesce(ch.v, 0) AS voted"
              + "   FROM cz"
              + "   JOIN territory t ON t.id = cz.territory_id"
              + "   LEFT JOIN territory par ON par.id = t.parent_territory_id"
              + "   LEFT JOIN mem own ON own.position_id = cz.id"
              + "   LEFT JOIN ch ON ch.cz_id = cz.id"
              + "   LEFT JOIN LATERAL ("
              + "       SELECT regexp_replace(trim(concat_ws(' ', pe.names, pe.last_name,"
              + "              pe.sur_name)), '\\s+', ' ', 'g') AS name"
              + "         FROM assignment a JOIN person pe ON pe.id = a.person_id"
              + "        WHERE a.position_id = cz.id AND a.role = 'HOLDER'"
              + "        ORDER BY upper_inf(a.validity) DESC, lower(a.validity) DESC"
              + "        LIMIT 1) h ON true",
      nativeQuery = true)
  List<SupporterCoordinatorRow> coordinators(@Param("campaignId") Long campaignId);

  /**
   * El perfil en barras: cada pregunta de la encuesta de captura, sexo, grupo de edad a la fecha de
   * la jornada de la campaña, origen y mes de captura.
   *
   * <p>Una sola pasada con GROUPING SETS: antes un CTE materializado se leía diez veces con UNION
   * ALL y agregaba 10.6 millones de filas (7 s con los 1.07 M de 2024; así, 3 s).
   */
  @Query(
      value =
          "WITH g AS ("
              + "  SELECT coalesce(d.supports_movement, '') AS sm,"
              + "         coalesce(d.committee_participation, '') AS cp,"
              + "         coalesce(d.polling_rep_participation, '') AS pr,"
              + "         coalesce(d.inviting_others, '') AS io,"
              + "         coalesce(d.number_of_people_invited, '') AS pi,"
              + "         coalesce(d.support_whatsapp, '') AS sw,"
              + "         CASE d.sex_id WHEN 1 THEN 'HOMBRE' WHEN 2 THEN 'MUJER' ELSE '' END AS sx,"
              + "         CASE WHEN pe.birthdate IS NULL OR pe.birthdate < DATE '1900-01-01' THEN ''"
              + "              WHEN age(coalesce(c.election_day, CURRENT_DATE), pe.birthdate)"
              + "                   < interval '30 years' THEN '18-29'"
              + "              WHEN age(coalesce(c.election_day, CURRENT_DATE), pe.birthdate)"
              + "                   < interval '45 years' THEN '30-44'"
              + "              WHEN age(coalesce(c.election_day, CURRENT_DATE), pe.birthdate)"
              + "                   < interval '60 years' THEN '45-59'"
              + "              ELSE '60+' END AS ag,"
              + "         coalesce(m.source, '') AS src,"
              + "         to_char(lower(m.validity), 'YYYY-MM') AS mon"
              + "    FROM membership m"
              + "    JOIN structure s ON s.id = m.structure_id AND s.campaign_id = :campaignId"
              + "    JOIN campaign c ON c.id = s.campaign_id"
              + "    JOIN person pe ON pe.id = m.person_id"
              + "    LEFT JOIN membership_detail d ON d.membership_id = m.id"
              + "   WHERE upper_inf(m.validity) AND m.status = 'ACTIVO')"
              // grouping() = 0 marca la columna por la que se agrupó en cada fila; las demás
              // vienen en NULL y el coalesce toma el único valor presente
              + " SELECT CASE WHEN grouping(sm) = 0 THEN 'supportsMovement'"
              + "             WHEN grouping(cp) = 0 THEN 'committeeParticipation'"
              + "             WHEN grouping(pr) = 0 THEN 'pollingRepParticipation'"
              + "             WHEN grouping(io) = 0 THEN 'invitingOthers'"
              + "             WHEN grouping(pi) = 0 THEN 'peopleInvited'"
              + "             WHEN grouping(sw) = 0 THEN 'supportWhatsapp'"
              + "             WHEN grouping(sx) = 0 THEN 'sex'"
              + "             WHEN grouping(ag) = 0 THEN 'ageGroup'"
              + "             WHEN grouping(src) = 0 THEN 'source'"
              + "             ELSE 'capturedMonth' END AS dimension,"
              + "        coalesce(sm, cp, pr, io, pi, sw, sx, ag, src, mon) AS value,"
              + "        count(*) AS count"
              + "   FROM g"
              + "  GROUP BY GROUPING SETS ((sm), (cp), (pr), (io), (pi), (sw), (sx), (ag), (src),"
              + "                         (mon))"
              + "  ORDER BY 1, 2",
      nativeQuery = true)
  List<SupporterProfileRow> profile(@Param("campaignId") Long campaignId);
}
