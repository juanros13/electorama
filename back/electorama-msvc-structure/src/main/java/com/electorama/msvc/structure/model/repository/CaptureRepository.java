package com.electorama.msvc.structure.model.repository;

import com.electorama.msvc.structure.model.dto.CaptureRequests.CaptureTarget;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Escrituras de la captura de convencidos. Va por JDBC porque {@code membership.validity} es un
 * TSTZRANGE que JPA no mapea y {@code membership_detail} no tiene entidad.
 */
@Repository
@RequiredArgsConstructor
public class CaptureRepository {

  private final NamedParameterJdbcTemplate jdbc;

  /** El puesto vigente donde queda el convencido, con su estructura y el estado de su campaña. */
  /**
   * El puesto donde queda el convencido. {@code sectionGeoId} sólo si el puesto es de una sección;
   * {@code municipalityGeoId} siempre que se sepa (el de la sección o el del propio puesto).
   */
  public record TargetRow(
      Long id,
      Long structureId,
      String level,
      String campaignStatus,
      Integer sectionGeoId,
      Integer municipalityGeoId) {}

  public Optional<TargetRow> target(Long positionId) {
    return jdbc
        .query(
            "SELECT p.id, p.structure_id, p.level, c.status, t.section_geo_id,"
                + "       coalesce(t.municipality_geo_id, par.municipality_geo_id)"
                + "  FROM position p"
                + "  JOIN structure s ON s.id = p.structure_id"
                + "  JOIN campaign c ON c.id = s.campaign_id"
                + "  JOIN territory t ON t.id = p.territory_id"
                + "  LEFT JOIN territory par ON par.id = t.parent_territory_id"
                + " WHERE p.id = :id AND p.valid_to IS NULL",
            Map.of("id", positionId),
            (rs, i) ->
                new TargetRow(
                    rs.getLong(1),
                    rs.getLong(2),
                    rs.getString(3),
                    rs.getString(4),
                    (Integer) rs.getObject(5),
                    (Integer) rs.getObject(6)))
        .stream()
        .findFirst();
  }

  /** El municipio de una sección del catálogo. */
  public Optional<Integer> sectionMunicipality(Integer sectionGeoId) {
    return jdbc
        .queryForList(
            "SELECT municipality_geo_id FROM territory WHERE section_geo_id = :s"
                + " AND type = 'SECTION' AND valid_to IS NULL LIMIT 1",
            Map.of("s", sectionGeoId),
            Integer.class)
        .stream()
        .findFirst();
  }

  /** El territorio vigente de una sección del catálogo. */
  public Optional<Long> sectionTerritory(Integer sectionGeoId) {
    return jdbc
        .queryForList(
            "SELECT id FROM territory WHERE section_geo_id = :s AND type = 'SECTION'"
                + " AND valid_to IS NULL LIMIT 1",
            Map.of("s", sectionGeoId),
            Long.class)
        .stream()
        .findFirst();
  }

  public Optional<Long> personByCurp(String curp) {
    return jdbc
        .queryForList(
            "SELECT id FROM person WHERE upper(curp) = upper(:c) LIMIT 1",
            Map.of("c", curp),
            Long.class)
        .stream()
        .findFirst();
  }

  public long insertPerson(MapSqlParameterSource p) {
    Long id =
        jdbc.queryForObject(
            "INSERT INTO person (curp, ine, names, last_name, sur_name, birthdate,"
                + "                    cell_phone_number, cell_phone_number_other, email, street,"
                + "                    external_number, internal_number, neighborhood, zip_code,"
                + "                    residence_entity_id, ine_validity)"
                + " VALUES (:curp, :ine, :names, :lastName, :surName, :birthdate,"
                + "         :phone, :phoneOther, :email, :street,"
                + "         :externalNumber, :internalNumber, :neighborhood, :zipCode,"
                + "         (SELECT state_geo_id FROM territory WHERE id = :territory), :ineValidity)"
                + " RETURNING id",
            p,
            Long.class);
    return id == null ? 0 : id;
  }

  /**
   * La persona ya estaba en el padrón (por CURP): se completan los datos que no tenía, sin pisar
   * los que ya tenía.
   */
  public void fillPerson(long personId, MapSqlParameterSource p) {
    jdbc.update(
        "UPDATE person SET"
            + "   ine = coalesce(ine, :ine),"
            + "   names = coalesce(names, :names),"
            + "   last_name = coalesce(last_name, :lastName),"
            + "   sur_name = coalesce(sur_name, :surName),"
            + "   birthdate = coalesce(birthdate, :birthdate),"
            + "   cell_phone_number = coalesce(cell_phone_number, :phone),"
            + "   cell_phone_number_other = coalesce(cell_phone_number_other, :phoneOther),"
            + "   email = coalesce(email, :email),"
            + "   street = coalesce(street, :street),"
            + "   external_number = coalesce(external_number, :externalNumber),"
            + "   internal_number = coalesce(internal_number, :internalNumber),"
            + "   neighborhood = coalesce(neighborhood, :neighborhood),"
            + "   zip_code = coalesce(zip_code, :zipCode),"
            + "   ine_validity = coalesce(ine_validity, :ineValidity),"
            + "   updated = now()"
            + " WHERE id = :personId",
        p.addValue("personId", personId));
  }

  /** Dónde está ya la persona como convencido vigente en esa estructura, si lo está. */
  public Optional<String> activeMembershipLabel(long personId, long structureId) {
    return jdbc
        .queryForList(
            "SELECT coalesce(p.section_key, t.code)"
                + "       || coalesce(' · ' || nullif(regexp_replace(trim(concat_ws(' ',"
                + "                   pe.names, pe.last_name)), '\\s+', ' ', 'g'), ''), '')"
                + "  FROM membership m"
                + "  JOIN position p ON p.id = m.position_id"
                + "  JOIN territory t ON t.id = p.territory_id"
                + "  LEFT JOIN assignment a ON a.position_id = p.id AND a.role = 'HOLDER'"
                + "                        AND upper(a.validity) IS NULL"
                + "  LEFT JOIN person pe ON pe.id = a.person_id"
                + " WHERE m.person_id = :person AND m.structure_id = :structure"
                + "   AND upper(m.validity) IS NULL"
                + " LIMIT 1",
            Map.of("person", personId, "structure", structureId),
            String.class)
        .stream()
        .findFirst();
  }

  /** El nombramiento vigente de quien captura en esa estructura: es el linaje del convencido. */
  public Optional<Long> callerAssignment(String authId, long structureId) {
    return jdbc
        .queryForList(
            "SELECT a.id FROM assignment a JOIN position p ON p.id = a.position_id"
                + " WHERE a.auth_id = :auth AND a.role = 'HOLDER' AND upper(a.validity) IS NULL"
                + "   AND p.valid_to IS NULL AND p.structure_id = :structure"
                + " ORDER BY p.id LIMIT 1",
            Map.of("auth", authId, "structure", structureId),
            Long.class)
        .stream()
        .findFirst();
  }

  public long insertMembership(MapSqlParameterSource p) {
    Long id =
        jdbc.queryForObject(
            "INSERT INTO membership (person_id, structure_id, position_id, territory_id,"
                + "                        captured_by_assignment_id, captured_by_auth_id, source,"
                + "                        status)"
                + " VALUES (:person, :structure, :position, :territory,"
                + "         :assignment, :authId, 'WEB', 'ACTIVO')"
                + " RETURNING id",
            p,
            Long.class);
    return id == null ? 0 : id;
  }

  public void insertDetail(MapSqlParameterSource p) {
    jdbc.update(
        "INSERT INTO membership_detail (membership_id, sex_id, is_adult, supports_movement,"
            + "       committee_participation, polling_rep_participation, inviting_others,"
            + "       number_of_people_invited, support_whatsapp, ine_validity, latitude, longitude,"
            + "       updated)"
            + " VALUES (:membership, :sexId, true, :supports, :committee, :pollingRep, :inviting,"
            + "         :invited, :whatsapp, :ineValidity, :latitude, :longitude, now())",
        p);
  }

  /** La etiqueta de un puesto: su clave de sección (o la del territorio) y su titular. */
  public String positionLabel(long positionId) {
    return jdbc.queryForObject(
        "SELECT coalesce(p.section_key, t.code)"
            + "  FROM position p JOIN territory t ON t.id = p.territory_id WHERE p.id = :id",
        Map.of("id", positionId),
        String.class);
  }

  /**
   * Dónde puede dejar convencidos quien captura en la campaña activa: sus puestos y los puestos con
   * titular de su rama (no las vacantes: el convencido cuelga de alguien).
   */
  public List<CaptureTarget> targets(String authId) {
    return jdbc.query(
        "WITH RECURSIVE mine AS ("
            + "   SELECT p.id FROM position p"
            + "     JOIN assignment a ON a.position_id = p.id AND a.role = 'HOLDER'"
            + "                      AND upper(a.validity) IS NULL AND a.auth_id = :auth"
            + "     JOIN structure s ON s.id = p.structure_id"
            + "     JOIN campaign c ON c.id = s.campaign_id AND c.status = 'ACTIVE'"
            + "    WHERE p.valid_to IS NULL),"
            + " tree AS ("
            + "   SELECT id, true AS own FROM mine"
            + "   UNION"
            + "   SELECT c.id, false FROM position c JOIN tree t ON c.parent_position_id = t.id"
            + "    WHERE c.valid_to IS NULL)"
            + " SELECT p.id, p.level, coalesce(p.section_key, t.code),"
            + "        nullif(regexp_replace(trim(concat_ws(' ', pe.names, pe.last_name,"
            + "               pe.sur_name)), '\\s+', ' ', 'g'), ''),"
            + "        t.section_geo_id, bool_or(tr.own),"
            + "        coalesce(t.municipality_geo_id, par.municipality_geo_id)"
            + "   FROM tree tr"
            + "   JOIN position p ON p.id = tr.id"
            + "   JOIN territory t ON t.id = p.territory_id"
            + "   JOIN assignment a ON a.position_id = p.id AND a.role = 'HOLDER'"
            + "                    AND upper(a.validity) IS NULL"
            + "   LEFT JOIN person pe ON pe.id = a.person_id"
            + "   LEFT JOIN territory par ON par.id = t.parent_territory_id"
            + "  GROUP BY p.id, p.level, p.section_key, t.code, pe.names, pe.last_name,"
            + "           pe.sur_name, t.section_geo_id, t.municipality_geo_id,"
            + "           par.municipality_geo_id"
            + "  ORDER BY bool_or(tr.own) DESC, coalesce(p.section_key, t.code)"
            + "  LIMIT 1000",
        Map.of("auth", authId),
        (rs, i) ->
            new CaptureTarget(
                rs.getLong(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                (Integer) rs.getObject(5),
                rs.getBoolean(6),
                (Integer) rs.getObject(7)));
  }
}
