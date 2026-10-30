package com.electorama.msvc.structure.model.repository;

import com.electorama.msvc.structure.model.entity.Membership;
import com.electorama.msvc.structure.model.projection.FollowerCountRow;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Los reportes de captación: convencidos y promotores agrupados por municipio, por coordinador de
 * zona o por referente.
 *
 * <p>Sustituye a tres facades de `msvc-followers` que armaban el SQL a mano, y de los que dos
 * estaban <b>roídos por el mismo defecto</b>: agrupaban uniendo {@code promoters.follower_id =
 * followers.id}, y ese puntero resuelve en 86 de sus 27,680 filas. Medido contra las tablas
 * congeladas, el reporte por municipio y el reporte por referente contaban <b>86 convencidos de
 * 1,067,484</b>. Además, en la rama de rol nombraban dos columnas que no existen —{@code
 * reference_auth_id} y {@code coordinate_zone_auth_id}—, así que para un coordinador municipal o de
 * zona la consulta fallaba y el {@code catch} devolvía lista vacía: el reporte llevaba meses en
 * blanco sin que nada avisara.
 *
 * <p>Aquí la jerarquía sale del organigrama: el convencido cuelga de un puesto de promotor, ése de
 * un coordinador y ése de un referente. No hay punteros que resolver.
 *
 * <p>Cada reporte tiene dos variantes, con y sin ámbito, en vez de un {@code (:param IS NULL OR
 * …)}: ese truco fuerza el plan genérico de la sentencia preparada y ya costó un factor de mil en
 * {@link FollowerQueryRepository}.
 */
@Repository
public interface ReportQueryRepository extends JpaRepository<Membership, Long> {

  /** Todos los puestos de promotor vigentes: la vista de administrador. */
  String PROM_ALL =
      "WITH prom AS MATERIALIZED ("
          + "  SELECT p.id, p.parent_position_id, p.territory_id, p.goal"
          + "    FROM position p"
          + "   WHERE p.level = 'PROMOTER' AND p.valid_to IS NULL"
          + "     AND p.structure_id IN (SELECT id FROM structure WHERE campaign_id = :campaignId)) ";

  /**
   * Los puestos de promotor que cuelgan —a cualquier profundidad— de un puesto que ocupa esa
   * cuenta. Sirve igual para un referente que para un coordinador de zona: el recorrido es el
   * mismo, lo que cambia es dónde arranca.
   */
  String PROM_SCOPED =
      "WITH RECURSIVE seed AS ("
          + "  SELECT a.position_id AS id FROM assignment a"
          + "    JOIN position sp ON sp.id = a.position_id"
          + "   WHERE a.auth_id = :scopeAuthId AND a.role = 'HOLDER'"
          + "     AND upper(a.validity) IS NULL"
          + "     AND sp.structure_id IN (SELECT id FROM structure WHERE campaign_id = :campaignId)),"
          + " tree AS ("
          + "  SELECT id FROM seed"
          + "  UNION ALL"
          + "  SELECT p.id FROM position p JOIN tree t ON p.parent_position_id = t.id"
          + "   WHERE p.valid_to IS NULL),"
          + " prom AS MATERIALIZED ("
          + "  SELECT p.id, p.parent_position_id, p.territory_id, p.goal"
          + "    FROM position p JOIN tree t ON t.id = p.id"
          + "   WHERE p.level = 'PROMOTER' AND p.valid_to IS NULL) ";

  /**
   * El cuerpo común: los convencidos de cada puesto dentro del rango de fechas, y las claves de
   * agrupación resueltas subiendo por el organigrama.
   */
  String BODY =
      ", counted AS ("
          + "  SELECT prom.id, prom.goal,"
          + "         mun.municipality_geo_id AS municipality_id,"
          + "         sec.section_geo_id AS section_id,"
          + "         czh.auth_id AS cz_auth, refh.auth_id AS ref_auth,"
          + "         (SELECT count(*) FROM membership m"
          + "           WHERE m.position_id = prom.id"
          + "             AND upper(m.validity) IS NULL"
          + "             AND lower(m.validity) >= :from AND lower(m.validity) < :to) AS followers"
          + "    FROM prom"
          + "    LEFT JOIN territory sec ON sec.id = prom.territory_id"
          + "    LEFT JOIN territory mun ON mun.id = sec.parent_territory_id"
          + "    LEFT JOIN position pcz  ON pcz.id = prom.parent_position_id"
          + "    LEFT JOIN LATERAL (SELECT COALESCE(a.auth_id, pe.auth_id) AS auth_id"
          + "           FROM assignment a JOIN person pe ON pe.id = a.person_id"
          + "          WHERE a.position_id = pcz.id AND a.role = 'HOLDER'"
          + "            AND upper(a.validity) IS NULL LIMIT 1) czh ON true"
          + "    LEFT JOIN LATERAL (SELECT COALESCE(a.auth_id, pe.auth_id) AS auth_id"
          + "           FROM assignment a JOIN person pe ON pe.id = a.person_id"
          + "          WHERE a.position_id = pcz.parent_position_id AND a.role = 'HOLDER'"
          + "            AND upper(a.validity) IS NULL LIMIT 1) refh ON true) ";

  // `CAST(NULL AS bigint)` y no `NULL::bigint`: en una consulta nativa Hibernate
  // lee `::` como el prefijo de un parámetro con nombre y la sentencia nunca
  // llega a la base. El modelo viejo lo sorteaba escribiendo `::::` en el Java,
  // que es más corto de teclear y más difícil de entender al leerlo.
  String BY_MUNICIPALITY =
      "SELECT municipality_id AS municipalityId, CAST(NULL AS bigint) AS sectionId,"
          + "       CAST(NULL AS varchar) AS coordinatorZoneAuthId, CAST(NULL AS varchar) AS referentAuthId,"
          + "       sum(followers) AS followers, count(*) AS promoters,"
          + "       COALESCE(sum(goal), 0) AS goal"
          + "  FROM counted GROUP BY 1 ORDER BY 1";

  String BY_COORDINATOR =
      "SELECT municipality_id AS municipalityId, section_id AS sectionId,"
          + "       cz_auth AS coordinatorZoneAuthId, ref_auth AS referentAuthId,"
          + "       sum(followers) AS followers, count(*) AS promoters,"
          + "       COALESCE(sum(goal), 0) AS goal"
          + "  FROM counted GROUP BY 1, 2, 3, 4 ORDER BY 1, 3";

  String BY_REFERENT =
      "SELECT municipality_id AS municipalityId, CAST(NULL AS bigint) AS sectionId,"
          + "       CAST(NULL AS varchar) AS coordinatorZoneAuthId, ref_auth AS referentAuthId,"
          + "       sum(followers) AS followers, count(*) AS promoters,"
          + "       COALESCE(sum(goal), 0) AS goal"
          + "  FROM counted GROUP BY 1, 4 ORDER BY 1, 4";

  @Query(value = PROM_ALL + BODY + BY_MUNICIPALITY, nativeQuery = true)
  List<FollowerCountRow> byMunicipality(
      @Param("from") OffsetDateTime from,
      @Param("to") OffsetDateTime to,
      @Param("campaignId") Long campaignId);

  @Query(value = PROM_SCOPED + BODY + BY_MUNICIPALITY, nativeQuery = true)
  List<FollowerCountRow> byMunicipalityScoped(
      @Param("from") OffsetDateTime from,
      @Param("to") OffsetDateTime to,
      @Param("scopeAuthId") String scopeAuthId,
      @Param("campaignId") Long campaignId);

  @Query(value = PROM_ALL + BODY + BY_COORDINATOR, nativeQuery = true)
  List<FollowerCountRow> byCoordinator(
      @Param("from") OffsetDateTime from,
      @Param("to") OffsetDateTime to,
      @Param("campaignId") Long campaignId);

  @Query(value = PROM_SCOPED + BODY + BY_COORDINATOR, nativeQuery = true)
  List<FollowerCountRow> byCoordinatorScoped(
      @Param("from") OffsetDateTime from,
      @Param("to") OffsetDateTime to,
      @Param("scopeAuthId") String scopeAuthId,
      @Param("campaignId") Long campaignId);

  @Query(value = PROM_ALL + BODY + BY_REFERENT, nativeQuery = true)
  List<FollowerCountRow> byReferent(
      @Param("from") OffsetDateTime from,
      @Param("to") OffsetDateTime to,
      @Param("campaignId") Long campaignId);

  @Query(value = PROM_SCOPED + BODY + BY_REFERENT, nativeQuery = true)
  List<FollowerCountRow> byReferentScoped(
      @Param("from") OffsetDateTime from,
      @Param("to") OffsetDateTime to,
      @Param("scopeAuthId") String scopeAuthId,
      @Param("campaignId") Long campaignId);

  /**
   * Movilización de una sección: convencidos con marca de voto contra el total.
   *
   * <p>La sección se identifica por su id del catálogo y no por el número impreso, que en el modelo
   * viejo estaba mal en 840 de las parejas medidas.
   */
  @Query(
      value =
          "SELECT count(*) FILTER (WHERE m.vote_flag) AS progress, count(*) AS goal"
              + "  FROM membership m"
              + "  JOIN territory t ON t.id = m.territory_id"
              + " WHERE upper(m.validity) IS NULL AND t.section_geo_id = :sectionGeoId"
              + "   AND m.structure_id IN (SELECT id FROM structure WHERE campaign_id = :campaignId)",
      nativeQuery = true)
  List<Object[]> mobilizationBySection(
      @Param("sectionGeoId") Integer sectionGeoId, @Param("campaignId") Long campaignId);

  /**
   * Lo mismo, pero por el número impreso de la sección.
   *
   * <p>El reporte viejo comparaba contra `followers.section_number`, así que lo que le llega del
   * front es el número, no el id del catálogo. Se admiten los dos para no adivinar: el número se
   * resuelve contra el código del territorio, que después de V2e siempre viene con cuatro dígitos.
   */
  @Query(
      value =
          "SELECT count(*) FILTER (WHERE m.vote_flag) AS progress, count(*) AS goal"
              + "  FROM membership m"
              + "  JOIN territory t ON t.id = m.territory_id"
              + " WHERE upper(m.validity) IS NULL"
              + "   AND t.type = 'SECTION'"
              + "   AND t.code = 'S-' || lpad(CAST(:sectionNumber AS varchar), 4, '0')"
              + "   AND m.structure_id IN (SELECT id FROM structure WHERE campaign_id = :campaignId)",
      nativeQuery = true)
  List<Object[]> mobilizationBySectionNumber(
      @Param("sectionNumber") Integer sectionNumber, @Param("campaignId") Long campaignId);
}
