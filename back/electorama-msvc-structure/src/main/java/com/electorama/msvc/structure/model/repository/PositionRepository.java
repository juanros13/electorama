package com.electorama.msvc.structure.model.repository;

import com.electorama.msvc.structure.model.entity.Position;
import com.electorama.msvc.structure.model.projection.HeldPosition;
import com.electorama.msvc.structure.model.projection.MunicipalityPosition;
import com.electorama.msvc.structure.model.projection.MunicipalitySummary;
import com.electorama.msvc.structure.model.projection.PositionSummary;
import com.electorama.msvc.structure.model.projection.SectionGoal;
import com.electorama.msvc.structure.model.projection.SectionOverview;
import com.electorama.msvc.structure.model.projection.TerritoryScope;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

  Optional<Position> findByIdAndValidToIsNull(Long id);

  String SUMMARY_SELECT =
      "SELECT p.id AS id, p.level AS level, p.code AS code, p.section_key AS sectionKey,"
          + "       p.level_id AS levelId, lv.rank AS levelRank, lv.name_singular AS levelName,"
          + "       p.goal AS goal,"
          + "       p.child_limit AS childLimit, p.parent_position_id AS parentId,"
          + "       p.structure_id AS structureId,"
          + "       t.id AS territoryId, t.type AS territoryType, t.code AS territoryCode,"
          + "       t.section_geo_id AS sectionGeoId, t.municipality_geo_id AS municipalityGeoId,"
          + "       h.auth_id AS holderAuthId, h.person_id AS holderPersonId,"
          + "       trim(concat_ws(' ', pe.names, pe.last_name, pe.sur_name)) AS holderName,"
          + "       (SELECT count(*) FROM position c"
          + "         WHERE c.parent_position_id = p.id AND c.valid_to IS NULL) AS childCount,"
          + "       (SELECT count(*) FROM membership m"
          + "         WHERE m.position_id = p.id AND upper(m.validity) IS NULL) AS membershipCount"
          + " FROM position p"
          + " JOIN territory t ON t.id = p.territory_id"
          + " JOIN campaign_level lv ON lv.id = p.level_id"
          + " LEFT JOIN LATERAL ("
          + "     SELECT a.auth_id, a.person_id FROM assignment a"
          + "      WHERE a.position_id = p.id AND a.role = 'HOLDER'"
          + "        AND upper(a.validity) IS NULL LIMIT 1) h ON true"
          + " LEFT JOIN person pe ON pe.id = h.person_id";

  /**
   * El puesto con su titular vigente y sus dos conteos, en una sola consulta.
   *
   * <p>Va en SQL nativo porque {@code assignment.validity} es un {@code TSTZRANGE} que JPA no
   * mapea: preguntar por la vigencia abierta es {@code upper(validity) IS NULL}. El titular se
   * resuelve con LATERAL para que sea una búsqueda por índice por fila y no una reordenación de
   * joins.
   */
  @Query(value = SUMMARY_SELECT + " WHERE p.id = :id", nativeQuery = true)
  Optional<PositionSummary> summary(@Param("id") Long id);

  @Query(
      value =
          SUMMARY_SELECT
              + " WHERE p.parent_position_id = :parentId AND p.valid_to IS NULL"
              + " ORDER BY t.code, p.id",
      nativeQuery = true)
  List<PositionSummary> children(@Param("parentId") Long parentId);

  /** La raíz de una estructura: los puestos vigentes del nivel de la cúspide de su campaña. */
  @Query(
      value =
          SUMMARY_SELECT
              + " WHERE p.structure_id = :structureId AND p.valid_to IS NULL"
              + "   AND lv.rank = (SELECT min(x.rank) FROM campaign_level x"
              + "                   WHERE x.campaign_id = lv.campaign_id)"
              + " ORDER BY t.code, p.id",
      nativeQuery = true)
  List<PositionSummary> roots(@Param("structureId") Long structureId);

  /** Los puestos vigentes sobre una sección del catálogo de geography. */
  @Query(
      value =
          SUMMARY_SELECT
              + " WHERE t.section_geo_id = :sectionGeoId AND t.type = 'SECTION'"
              + "   AND t.valid_to IS NULL AND p.valid_to IS NULL"
              + "   AND p.structure_id IN (SELECT id FROM structure WHERE campaign_id = :campaignId)"
              + " ORDER BY p.level, p.id",
      nativeQuery = true)
  List<PositionSummary> bySection(
      @Param("sectionGeoId") Integer sectionGeoId, @Param("campaignId") Long campaignId);

  /**
   * La meta repartida en cada sección y la gente que la cubre, en la estructura principal: la suma
   * de las metas de los coordinadores de zona con puesto sobre ella, cuántos coordinadores y
   * cuántos promotores hay. La meta se compara contra el techo de la sección ({@code
   * geo_secciones.meta}); los conteos, contra lo que pide la campaña (1 coordinador por cada 200 de
   * meta y 1 promotor por cada 20).
   *
   * <p>Los puestos municipales cubren la sección pero no le asignan meta, así que no cuentan. La
   * estructura alterna tampoco: no aporta a las metas y sus promotores inflarían la cobertura.
   */
  @Query(
      value =
          "SELECT t.section_geo_id AS sectionGeoId,"
              + "       coalesce(sum(p.goal) FILTER (WHERE p.level = 'ZONE_COORDINATOR'), 0) AS goal,"
              + "       count(p.id) FILTER (WHERE p.level = 'ZONE_COORDINATOR') AS positions,"
              + "       count(p.id) FILTER (WHERE p.level = 'PROMOTER') AS promoters"
              + " FROM territory t"
              + " JOIN position p ON p.territory_id = t.id AND p.valid_to IS NULL"
              + "                AND p.level IN ('ZONE_COORDINATOR', 'PROMOTER')"
              + " JOIN structure s ON s.id = p.structure_id AND s.type = 'MAIN'"
              + "                  AND s.campaign_id = :campaignId"
              + " WHERE t.type = 'SECTION' AND t.valid_to IS NULL"
              + "   AND t.section_geo_id IN (:sectionGeoIds)"
              + " GROUP BY t.section_geo_id",
      nativeQuery = true)
  List<SectionGoal> sectionGoals(
      @Param("sectionGeoIds") List<Integer> sectionGeoIds, @Param("campaignId") Long campaignId);

  /**
   * Todas las secciones con estructura principal en una sola consulta: lo mismo que {@link
   * #sectionGoals} más los convencidos (como en {@link #municipalitySummary}, pero por sección) y
   * los nombres de los coordinadores de zona. Una sección sin puestos pero con convencidos también
   * viene.
   */
  @Query(
      value =
          "WITH pos AS ("
              + "  SELECT t.section_geo_id AS sec,"
              + "         coalesce(sum(p.goal) FILTER (WHERE p.level = 'ZONE_COORDINATOR'), 0) AS goal,"
              + "         count(p.id) FILTER (WHERE p.level = 'ZONE_COORDINATOR') AS positions,"
              + "         count(p.id) FILTER (WHERE p.level = 'PROMOTER') AS promoters,"
              + "         string_agg(nullif(regexp_replace(trim(concat_ws(' ', pe.names, pe.last_name,"
              + "           pe.sur_name)), '\\s+', ' ', 'g'), ''),"
              + "                    ', ' ORDER BY p.code) FILTER (WHERE p.level = 'ZONE_COORDINATOR')"
              + "           AS coordinators"
              + "    FROM territory t"
              + "    JOIN position p ON p.territory_id = t.id AND p.valid_to IS NULL"
              + "                   AND p.level IN ('ZONE_COORDINATOR', 'PROMOTER')"
              + "    JOIN structure s ON s.id = p.structure_id AND s.type = 'MAIN'"
              + "                     AND s.campaign_id = :campaignId"
              + "    LEFT JOIN LATERAL ("
              + "        SELECT a.person_id FROM assignment a"
              + "         WHERE a.position_id = p.id AND a.role = 'HOLDER'"
              + "           AND upper(a.validity) IS NULL LIMIT 1) h ON true"
              + "    LEFT JOIN person pe ON pe.id = h.person_id"
              + "   WHERE t.type = 'SECTION' AND t.valid_to IS NULL"
              + "   GROUP BY 1),"
              + " mem AS ("
              + "  SELECT t.section_geo_id AS sec, count(*) AS members"
              + "    FROM membership m"
              + "    JOIN structure s ON s.id = m.structure_id AND s.type = 'MAIN'"
              + "                     AND s.campaign_id = :campaignId"
              + "    JOIN position p ON p.id = m.position_id"
              + "    JOIN territory t ON t.id = p.territory_id"
              + "   WHERE upper_inf(m.validity) AND m.status = 'ACTIVO'"
              + "     AND t.section_geo_id IS NOT NULL"
              + "   GROUP BY 1)"
              + " SELECT coalesce(pos.sec, mem.sec) AS sectionGeoId,"
              + "        coalesce(pos.goal, 0) AS goal,"
              + "        coalesce(pos.positions, 0) AS positions,"
              + "        coalesce(pos.promoters, 0) AS promoters,"
              + "        coalesce(mem.members, 0) AS members,"
              + "        pos.coordinators AS coordinators"
              + "   FROM pos FULL JOIN mem ON mem.sec = pos.sec",
      nativeQuery = true)
  List<SectionOverview> sectionOverview(@Param("campaignId") Long campaignId);

  /**
   * La estructura principal agregada por municipio, para el tablero de campaña. Un puesto cuenta en
   * el municipio de su territorio (sección o municipio); los estatales no tienen municipio y no
   * cuentan. Los convencidos se cuentan en el municipio del puesto al que están anclados.
   */
  @Query(
      value =
          "WITH pos AS ("
              + "  SELECT t.municipality_geo_id AS mun,"
              + "         count(*) FILTER (WHERE p.level = 'ZONE_COORDINATOR') AS promoters,"
              + "         count(*) FILTER (WHERE p.level = 'PROMOTER') AS mobilizers,"
              + "         coalesce(sum(p.goal) FILTER (WHERE p.level = 'ZONE_COORDINATOR'), 0) AS goal"
              + "    FROM position p"
              + "    JOIN structure s ON s.id = p.structure_id AND s.type = 'MAIN'"
              + "                     AND s.campaign_id = :campaignId"
              + "    JOIN territory t ON t.id = p.territory_id"
              + "   WHERE p.valid_to IS NULL AND t.municipality_geo_id IS NOT NULL"
              + "   GROUP BY 1),"
              + " mem AS ("
              + "  SELECT t.municipality_geo_id AS mun, count(*) AS members"
              + "    FROM membership m"
              + "    JOIN structure s ON s.id = m.structure_id AND s.type = 'MAIN'"
              + "                     AND s.campaign_id = :campaignId"
              + "    JOIN position p ON p.id = m.position_id"
              + "    JOIN territory t ON t.id = p.territory_id"
              + "   WHERE upper_inf(m.validity) AND m.status = 'ACTIVO'"
              + "     AND t.municipality_geo_id IS NOT NULL"
              + "   GROUP BY 1)"
              + " SELECT coalesce(pos.mun, mem.mun) AS municipalityGeoId,"
              + "        coalesce(pos.promoters, 0) AS promoters,"
              + "        coalesce(pos.mobilizers, 0) AS mobilizers,"
              + "        coalesce(pos.goal, 0) AS goal,"
              + "        coalesce(mem.members, 0) AS members"
              + "   FROM pos FULL JOIN mem ON mem.mun = pos.mun",
      nativeQuery = true)
  List<MunicipalitySummary> municipalitySummary(@Param("campaignId") Long campaignId);

  /**
   * Los puestos vigentes de la estructura principal de un municipio, con su titular y sus
   * convencidos directos: el desglose del tablero de campaña. El referente cuelga del territorio
   * municipio; promotores y movilizadores, de las secciones del municipio.
   */
  @Query(
      value =
          "SELECT p.id AS id, p.parent_position_id AS parentId, p.level AS level,"
              + "       t.section_geo_id AS sectionGeoId, p.section_key AS sectionKey,"
              + "       p.goal AS goal,"
              + "       nullif(regexp_replace(trim(concat_ws(' ', pe.names, pe.last_name,"
              + "         pe.sur_name)), '\\s+', ' ', 'g'), '') AS holderName,"
              + "       h.person_id AS holderPersonId,"
              + "       coalesce(m.members, 0) AS members,"
              + "       coalesce(m.last7, 0) AS membersLast7,"
              + "       m.last_capture AS lastCapture"
              + "  FROM position p"
              + "  JOIN structure s ON s.id = p.structure_id AND s.type = 'MAIN'"
              + "                  AND s.campaign_id = :campaignId"
              + "  JOIN territory t ON t.id = p.territory_id"
              + "  LEFT JOIN LATERAL ("
              + "      SELECT a.person_id FROM assignment a"
              + "       WHERE a.position_id = p.id AND a.role = 'HOLDER'"
              + "         AND upper(a.validity) IS NULL LIMIT 1) h ON true"
              + "  LEFT JOIN person pe ON pe.id = h.person_id"
              + "  LEFT JOIN LATERAL ("
              + "      SELECT count(*) AS members,"
              + "             count(*) FILTER (WHERE lower(x.validity) >= now() - interval '7 days')"
              + "               AS last7,"
              + "             max(lower(x.validity)) AS last_capture"
              + "        FROM membership x"
              + "       WHERE x.position_id = p.id AND upper_inf(x.validity)"
              + "         AND x.status = 'ACTIVO') m ON true"
              + " WHERE p.valid_to IS NULL AND t.municipality_geo_id = :municipalityGeoId"
              + " ORDER BY p.level, p.section_key NULLS FIRST, p.id",
      nativeQuery = true)
  List<MunicipalityPosition> municipalityPositions(
      @Param("municipalityGeoId") Integer municipalityGeoId, @Param("campaignId") Long campaignId);

  // ─────────────────────────── la rama de quien llama (control de acceso)
  //
  // La rama de una cuenta son los puestos vigentes que ocupa (de cualquier nivel) y todo lo que
  // cuelga de ellos: el referente ve su municipio, el promotor a sus movilizadores.

  /** Si la cuenta es titular vigente de algún puesto vigente, de cualquier campaña o nivel. */
  @Query(
      value =
          "SELECT EXISTS (SELECT 1 FROM assignment a"
              + "  JOIN position p ON p.id = a.position_id AND p.valid_to IS NULL"
              + " WHERE a.auth_id = :authId AND a.role = 'HOLDER' AND upper(a.validity) IS NULL)",
      nativeQuery = true)
  boolean holdsAnyPosition(@Param("authId") String authId);

  /** Los puestos vigentes que ocupa una cuenta en una estructura, de cualquier nivel. */
  @Query(
      value =
          "SELECT p.id FROM position p"
              + " JOIN assignment a ON a.position_id = p.id"
              + " WHERE a.auth_id = :authId AND a.role = 'HOLDER' AND upper(a.validity) IS NULL"
              + "   AND p.valid_to IS NULL AND p.structure_id = :structureId"
              + " ORDER BY p.id",
      nativeQuery = true)
  List<Long> positionsHeldIn(
      @Param("authId") String authId, @Param("structureId") Long structureId);

  /**
   * Si el puesto cuelga (o es, con {@code includeSelf}) de un puesto vigente que ocupa la cuenta.
   * Sube por el organigrama desde el puesto, tantos niveles como tenga la campaña.
   */
  @Query(
      value =
          "WITH RECURSIVE up AS ("
              + "   SELECT id, parent_position_id FROM position WHERE id = :positionId"
              + "   UNION ALL"
              + "   SELECT p.id, p.parent_position_id FROM position p"
              + "     JOIN up ON p.id = up.parent_position_id)"
              + " SELECT EXISTS ("
              + "   SELECT 1 FROM up"
              + "     JOIN position r ON r.id = up.id AND r.valid_to IS NULL"
              + "     JOIN assignment a ON a.position_id = r.id AND a.role = 'HOLDER'"
              + "                      AND upper(a.validity) IS NULL AND a.auth_id = :authId"
              + "    WHERE :includeSelf OR up.id <> :positionId)",
      nativeQuery = true)
  boolean inHeldBranch(
      @Param("authId") String authId,
      @Param("positionId") Long positionId,
      @Param("includeSelf") boolean includeSelf);

  /**
   * Si la cuenta {@code targetAuthId} es titular vigente de algún puesto DEBAJO de un puesto que
   * ocupa {@code callerAuthId} (no cuentan los puestos de quien llama).
   */
  @Query(
      value =
          "WITH RECURSIVE held AS ("
              + "   SELECT a.position_id AS id FROM assignment a"
              + "     JOIN position p ON p.id = a.position_id AND p.valid_to IS NULL"
              + "    WHERE a.auth_id = :targetAuthId AND a.role = 'HOLDER'"
              + "      AND upper(a.validity) IS NULL),"
              + " up AS ("
              + "   SELECT h.id AS start_id, p.id, p.parent_position_id"
              + "     FROM held h JOIN position p ON p.id = h.id"
              + "   UNION ALL"
              + "   SELECT up.start_id, p.id, p.parent_position_id FROM position p"
              + "     JOIN up ON p.id = up.parent_position_id)"
              + " SELECT EXISTS ("
              + "   SELECT 1 FROM up"
              + "     JOIN position r ON r.id = up.id AND r.valid_to IS NULL"
              + "     JOIN assignment a ON a.position_id = r.id AND a.role = 'HOLDER'"
              + "                      AND upper(a.validity) IS NULL AND a.auth_id = :callerAuthId"
              + "    WHERE up.id <> up.start_id)",
      nativeQuery = true)
  boolean holdsInHeldBranch(
      @Param("callerAuthId") String callerAuthId, @Param("targetAuthId") String targetAuthId);

  /** Los puestos vigentes que ocupa una cuenta, cada uno con el referente raíz de su rama. */
  @Query(
      value =
          "WITH RECURSIVE held AS ("
              + "   SELECT p.id FROM position p"
              + "     JOIN assignment a ON a.position_id = p.id AND a.role = 'HOLDER'"
              + "                      AND upper(a.validity) IS NULL"
              + "    WHERE a.auth_id = :authId AND p.valid_to IS NULL),"
              + " up AS ("
              + "   SELECT h.id AS start_id, p.id, p.parent_position_id, p.level"
              + "     FROM held h JOIN position p ON p.id = h.id"
              + "   UNION ALL"
              + "   SELECT up.start_id, q.id, q.parent_position_id, q.level FROM position q"
              + "     JOIN up ON q.id = up.parent_position_id)"
              + " SELECT p.id AS positionId, p.level AS level, p.section_key AS sectionKey,"
              + "        t.code AS territoryCode, s.type AS structureType,"
              + "        c.status AS campaignStatus,"
              + "        lv.role_name AS roleName, lv.alternate_role_name AS alternateRoleName,"
              + "        coalesce("
              + "          (SELECT u.id FROM up u WHERE u.start_id = p.id AND u.level = 'REFERENT'"
              + "            LIMIT 1),"
              + "          (SELECT u.id FROM up u WHERE u.start_id = p.id"
              + "              AND u.parent_position_id IS NULL LIMIT 1)) AS rootId"
              + "   FROM held h"
              + "   JOIN position p ON p.id = h.id"
              + "   JOIN campaign_level lv ON lv.id = p.level_id"
              + "   JOIN structure s ON s.id = p.structure_id"
              + "   JOIN campaign c ON c.id = s.campaign_id"
              + "   JOIN territory t ON t.id = p.territory_id"
              + "  ORDER BY p.id",
      nativeQuery = true)
  List<HeldPosition> heldWithRoot(@Param("authId") String authId);

  /**
   * La raíz de la rama de un puesto: el puesto con función REFERENT del que cuelga (el propio
   * puesto si lo es) o, si no hay, el de la cúspide.
   */
  @Query(
      value =
          "WITH RECURSIVE up AS ("
              + "   SELECT id, parent_position_id, level FROM position WHERE id = :positionId"
              + "   UNION ALL"
              + "   SELECT p.id, p.parent_position_id, p.level FROM position p"
              + "     JOIN up ON p.id = up.parent_position_id)"
              + " SELECT coalesce("
              + "   (SELECT id FROM up WHERE level = 'REFERENT' LIMIT 1),"
              + "   (SELECT id FROM up WHERE parent_position_id IS NULL LIMIT 1))",
      nativeQuery = true)
  Optional<Long> rootOf(@Param("positionId") Long positionId);

  /** Los puestos que ocupa una cuenta de Keycloak ahora mismo. */
  @Query(
      value =
          "SELECT p.id FROM position p"
              + " JOIN assignment a ON a.position_id = p.id"
              + " WHERE a.auth_id = :authId AND a.role = 'HOLDER'"
              + "   AND upper(a.validity) IS NULL AND p.valid_to IS NULL"
              + "   AND p.structure_id IN (SELECT id FROM structure WHERE campaign_id = :campaignId)"
              + " ORDER BY p.id",
      nativeQuery = true)
  List<Long> positionsHeldBy(@Param("authId") String authId, @Param("campaignId") Long campaignId);

  /**
   * El ámbito territorial de un coordinador: las secciones que cubre.
   *
   * <p>Un puesto sobre una sección cubre esa sección. Un puesto sobre un MUNICIPIO cubre todas las
   * del municipio según el catálogo, excepto donde el mismo coordinador tenga un puesto de sección:
   * el específico gana al municipal. Es la misma regla que la vista de compatibilidad de {@code
   * users_seccions}, y la razón por la que el ámbito ya no se queda atrás cuando el catálogo crece.
   */
  @Query(
      value =
          "SELECT t.id AS territoryId, t.code AS territoryCode, t.section_geo_id AS sectionGeoId,"
              + "       mun.municipality_geo_id AS municipalityGeoId, p.id AS positionId,"
              + "       p.goal AS goal, p.child_limit AS childLimit, false AS fromMunicipality"
              + " FROM assignment a"
              + " JOIN position p  ON p.id = a.position_id AND p.valid_to IS NULL"
              + "                 AND p.level = 'ZONE_COORDINATOR'"
              + "                 AND p.structure_id IN (SELECT id FROM structure WHERE campaign_id = :campaignId)"
              + " JOIN territory t ON t.id = p.territory_id AND t.type = 'SECTION'"
              + " LEFT JOIN territory mun ON mun.id = t.parent_territory_id"
              + " WHERE a.auth_id = :authId AND a.role = 'HOLDER' AND upper(a.validity) IS NULL"
              + " UNION ALL"
              + " SELECT s.id, s.code, s.section_geo_id, mun.municipality_geo_id, p.id,"
              + "        NULL, NULL, true"
              + " FROM assignment a"
              + " JOIN position p    ON p.id = a.position_id AND p.valid_to IS NULL"
              + "                   AND p.level = 'ZONE_COORDINATOR'"
              + "                   AND p.structure_id IN (SELECT id FROM structure WHERE campaign_id = :campaignId)"
              + " JOIN territory mun ON mun.id = p.territory_id AND mun.type = 'MUNICIPALITY'"
              + " JOIN territory s   ON s.parent_territory_id = mun.id AND s.type = 'SECTION'"
              + "                   AND s.valid_to IS NULL"
              + " WHERE a.auth_id = :authId AND a.role = 'HOLDER' AND upper(a.validity) IS NULL"
              + "   AND NOT EXISTS (SELECT 1 FROM assignment sa"
              + "         JOIN position sp ON sp.id = sa.position_id"
              + "        WHERE sa.auth_id = a.auth_id AND sa.role = 'HOLDER'"
              + "          AND upper(sa.validity) IS NULL"
              + "          AND sp.level = 'ZONE_COORDINATOR' AND sp.valid_to IS NULL"
              + "          AND sp.structure_id IN (SELECT id FROM structure WHERE campaign_id = :campaignId)"
              + "          AND sp.territory_id = s.id)"
              + " ORDER BY 2",
      nativeQuery = true)
  List<TerritoryScope> scopeOf(
      @Param("authId") String authId, @Param("campaignId") Long campaignId);

  /**
   * Cierra el puesto. No se borra nunca.
   *
   * <p>{@code clock_timestamp()} y no {@code now()}: dentro de una transacción {@code now()} es
   * constante, así que cerrar algo creado en la misma daría un rango vacío y la fila se seguiría
   * leyendo como vigente. El {@code GREATEST} impide además el rango invertido.
   */
  @Modifying
  @Query(
      value =
          "UPDATE position"
              + "   SET valid_to = GREATEST(clock_timestamp(),"
              + "                           valid_from + interval '1 microsecond')"
              + " WHERE id = :id AND valid_to IS NULL",
      nativeQuery = true)
  int close(@Param("id") Long id);

  /** ¿Le queda algún hijo vigente? Un puesto con promotores debajo no se cierra sin reubicarlos. */
  @Query(
      value =
          "SELECT count(*) FROM position" + " WHERE parent_position_id = :id AND valid_to IS NULL",
      nativeQuery = true)
  long countCurrentChildren(@Param("id") Long id);
}
