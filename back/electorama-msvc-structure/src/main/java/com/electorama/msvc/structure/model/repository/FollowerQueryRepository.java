package com.electorama.msvc.structure.model.repository;

import com.electorama.msvc.structure.model.entity.Membership;
import com.electorama.msvc.structure.model.projection.FollowerRow;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * La consulta que `msvc-followers` hacía a mano: los convencidos de un coordinador de zona con su
 * cadena de mando.
 *
 * <p>Sustituye al {@code followers ⋈ promoters} de {@code FollowersPromotersFacade}, que armaba el
 * SQL concatenando el authId —inyección de SQL incluida—, se comía las excepciones con un {@code
 * System.err.println} y contaba el total ignorando el filtro por promotor, así que la paginación
 * mentía en cuanto se filtraba. Aquí el filtro es un parámetro, el conteo usa el mismo criterio que
 * la página y la jerarquía se lee del organigrama en vez de adivinarse por authId.
 *
 * <p><b>Las dos CTE MATERIALIZED no son adorno.</b> Hibernate manda siempre sentencias preparadas y
 * PostgreSQL, a partir de la sexta ejecución, cambia al plan genérico: sin saber qué authId viene,
 * estima que el {@code IN (subconsulta)} devuelve muchos puestos, arranca por {@code membership}
 * —un millón de filas— y ejecuta los tres LATERAL por cada una. Medido en la base local, la misma
 * consulta tardaba <b>22.9 s</b>; con las CTE materializadas el conjunto de arranque son los diez
 * puestos del coordinador y tarda <b>3 ms</b>. El plan deja de depender de la suerte del parámetro.
 */
@Repository
public interface FollowerQueryRepository extends JpaRepository<Membership, Long> {

  /**
   * El conjunto de arranque: los puestos de promotor que cuelgan de los puestos que ese coordinador
   * ocupa ahora mismo. Va por el organigrama, no por el authId copiado en cada fila.
   */
  String DRIVING_SET =
      "WITH cz AS MATERIALIZED ("
          + "  SELECT a.position_id FROM assignment a"
          + "   JOIN position p2 ON p2.id = a.position_id"
          + "  WHERE a.auth_id = :coordinatorAuthId AND a.role = 'HOLDER'"
          + "    AND upper(a.validity) IS NULL"
          + "    AND p2.level = 'ZONE_COORDINATOR'"
          + "    AND p2.structure_id IN (SELECT id FROM structure WHERE campaign_id = :campaignId)),"
          + " prom_pos AS MATERIALIZED ("
          + "  SELECT p.id, p.parent_position_id FROM position p"
          + "   JOIN cz ON p.parent_position_id = cz.position_id"
          + "  WHERE p.valid_to IS NULL) ";

  String FOLLOWER_SELECT =
      "SELECT m.legacy_id AS id, per.names AS names, per.last_name AS lastName,"
          + "       per.sur_name AS surName, per.curp AS curp, per.ine AS ine,"
          + "       per.birthdate AS birthdate, per.cell_phone_number AS cellPhoneNumber,"
          + "       per.cell_phone_number_other AS cellPhoneNumberOther, per.email AS email,"
          + "       m.status AS status, lower(m.validity) AS created, d.updated AS updated,"
          + "       upper(m.validity) AS deleted,"
          + "       per.street AS street, per.external_number AS externalNumber,"
          + "       per.internal_number AS internalNumber, per.neighborhood AS neighborhood,"
          + "       per.zip_code AS zipCode, per.birth_entity_id AS birthEntityId,"
          + "       per.residence_entity_id AS residenceEntityId,"
          + "       per.residence_municipality_id AS residenceMunicipalityId,"
          + "       d.sex_id AS catSex, d.marital_status_id AS catMaritalStatus,"
          + "       d.family_role_id AS catFamilyRole, d.is_adult AS isAdult,"
          + "       d.is_in_sync AS isInSync, d.ine_validity AS validity,"
          + "       d.section_number_ine AS sectionNumberIne,"
          + "       d.support_whatsapp AS supportWhatsapp, d.supports_movement AS supportsMovement,"
          + "       d.committee_participation AS committeeParticipation,"
          + "       d.polling_rep_participation AS pollinStationRepresentativeParticipation,"
          + "       d.inviting_others AS invitingOthersToParticipate,"
          + "       d.number_of_people_invited AS numberOfPeopleInvited,"
          + "       d.latitude AS latitude, d.longitude AS longitude, d.app_uuid AS uuid,"
          + "       d.device_id AS deviceId, d.follower_auth_id AS authId,"
          + "       COALESCE(ca.auth_id, cap.auth_id, m.captured_by_auth_id) AS userCreateId,"
          // la rama decide en qué columna vieja iba el promotor
          + "       (CASE WHEN st.type = 'MAIN' THEN prom.auth_id END) AS promoterAuthId,"
          + "       (CASE WHEN st.type = 'ALTERNATE' THEN prom.auth_id END) AS promoterAltAuthId,"
          + "       prom.name AS promoterName,"
          + "       czh.auth_id AS coordinatorZoneAuthId, czh.name AS coordinatorName,"
          + "       ref.auth_id AS referentAuthId, ref.name AS referentName,"
          + "       m.vote_flag AS voteFlag, m.vote_flag_timestamp AS voteFlagTimestamp,"
          + "       lpad(regexp_replace(sec.code, '^S-', ''), 4, '0') AS sectionNumber,"
          + "       sec.section_geo_id AS sectionId, mun.municipality_geo_id AS municipalityId,"
          + "       sec.federal_district_geo_id AS federalDistrictId,"
          + "       sec.local_district_geo_id AS localDistrictId,"
          + "       m.position_id AS positionId"
          + " FROM prom_pos"
          + " JOIN membership m ON m.position_id = prom_pos.id AND upper(m.validity) IS NULL"
          + " JOIN structure st ON st.id = m.structure_id"
          + " LEFT JOIN membership_detail d ON d.membership_id = m.id"
          + " LEFT JOIN person per ON per.id = m.person_id"
          + " LEFT JOIN territory sec ON sec.id = m.territory_id"
          + " LEFT JOIN territory mun ON mun.id = sec.parent_territory_id"
          // el referente es el abuelo: el padre del puesto del coordinador
          + " LEFT JOIN position pref ON pref.id ="
          + "        (SELECT pp.parent_position_id FROM position pp"
          + "          WHERE pp.id = prom_pos.parent_position_id)"
          // el titular de cada nivel, cada uno con su LATERAL para que sea una
          // búsqueda por índice por fila y no una reordenación de joins
          + " LEFT JOIN LATERAL (SELECT COALESCE(a.auth_id, pe.auth_id) AS auth_id,"
          + "        trim(concat_ws(' ', pe.names, pe.last_name, pe.sur_name)) AS name"
          + "        FROM assignment a JOIN person pe ON pe.id = a.person_id"
          + "       WHERE a.position_id = prom_pos.id AND a.role = 'HOLDER'"
          + "         AND upper(a.validity) IS NULL LIMIT 1) prom ON true"
          + " LEFT JOIN LATERAL (SELECT COALESCE(a.auth_id, pe.auth_id) AS auth_id,"
          + "        trim(concat_ws(' ', pe.names, pe.last_name, pe.sur_name)) AS name"
          + "        FROM assignment a JOIN person pe ON pe.id = a.person_id"
          + "       WHERE a.position_id = prom_pos.parent_position_id AND a.role = 'HOLDER'"
          + "         AND upper(a.validity) IS NULL LIMIT 1) czh ON true"
          + " LEFT JOIN LATERAL (SELECT COALESCE(a.auth_id, pe.auth_id) AS auth_id,"
          + "        trim(concat_ws(' ', pe.names, pe.last_name, pe.sur_name)) AS name"
          + "        FROM assignment a JOIN person pe ON pe.id = a.person_id"
          + "       WHERE a.position_id = pref.id AND a.role = 'HOLDER'"
          + "         AND upper(a.validity) IS NULL LIMIT 1) ref ON true"
          + " LEFT JOIN assignment ca ON ca.id = m.captured_by_assignment_id"
          + " LEFT JOIN person cap ON cap.id = ca.person_id";

  String AND_PROMOTER = " WHERE prom.auth_id = :promoterAuthId";

  String ORDER = " ORDER BY per.names, per.curp";

  // Dos consultas y no una con `(:promoterAuthId IS NULL OR …)`: ese truco
  // parece inofensivo y le cuesta al planificador el mismo plan genérico que
  // las CTE están evitando.
  @Query(value = DRIVING_SET + FOLLOWER_SELECT + ORDER, nativeQuery = true)
  List<FollowerRow> byCoordinator(
      @Param("coordinatorAuthId") String coordinatorAuthId,
      @Param("campaignId") Long campaignId,
      Pageable pageable);

  @Query(value = DRIVING_SET + FOLLOWER_SELECT + AND_PROMOTER + ORDER, nativeQuery = true)
  List<FollowerRow> byCoordinatorAndPromoter(
      @Param("coordinatorAuthId") String coordinatorAuthId,
      @Param("promoterAuthId") String promoterAuthId,
      @Param("campaignId") Long campaignId,
      Pageable pageable);

  /** El conteo con EL MISMO filtro que la página: el facade viejo ignoraba el del promotor. */
  @Query(
      value =
          DRIVING_SET
              + "SELECT count(*) FROM prom_pos"
              + " JOIN membership m ON m.position_id = prom_pos.id AND upper(m.validity) IS NULL",
      nativeQuery = true)
  long countByCoordinator(
      @Param("coordinatorAuthId") String coordinatorAuthId, @Param("campaignId") Long campaignId);

  @Query(
      value =
          DRIVING_SET
              + "SELECT count(*) FROM prom_pos"
              + " JOIN membership m ON m.position_id = prom_pos.id AND upper(m.validity) IS NULL"
              + " LEFT JOIN LATERAL (SELECT COALESCE(a.auth_id, pe.auth_id) AS auth_id"
              + "        FROM assignment a JOIN person pe ON pe.id = a.person_id"
              + "       WHERE a.position_id = prom_pos.id AND a.role = 'HOLDER'"
              + "         AND upper(a.validity) IS NULL LIMIT 1) prom ON true"
              + " WHERE prom.auth_id = :promoterAuthId",
      nativeQuery = true)
  long countByCoordinatorAndPromoter(
      @Param("coordinatorAuthId") String coordinatorAuthId,
      @Param("promoterAuthId") String promoterAuthId,
      @Param("campaignId") Long campaignId);
}
