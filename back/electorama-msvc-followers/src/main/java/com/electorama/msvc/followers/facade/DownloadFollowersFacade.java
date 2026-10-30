package com.electorama.msvc.followers.facade;

import com.electorama.msvc.followers.model.dto.DownloadFollowersDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DownloadFollowersFacade {
  @PersistenceContext EntityManager em;

  public List<DownloadFollowersDto> getAllFollowers(String cordinatorZoneAuthId) {
    List<DownloadFollowersDto> result = new ArrayList<DownloadFollowersDto>();
    List<Object[]> objectRes = new ArrayList<>();
    String select =
        "SELECT "
            + "promoters.promoter_auth_id as promotor_id, "
            + "concat(promoters.names,' ',promoters.last_name,' ',promoters.sur_name) as promotor, "
            + "promoters.curp as promotor_curp, "
            + "promoters.cell_phone_number as promotor_telefono, "
            + "promoters.ine as promotor_ine, "
            + "followers.id as simpatizante_id, "
            + "concat(followers.names,' ',followers.last_name,' ',followers.sur_name) as simpatizante, "
            + "followers.curp as curp, "
            + "followers.cell_phone_number as telefono, "
            + "followers.ine as ine, "
            + "followers.residence_entity_id as entidad_residencia_id, "
            + "followers.residence_municipality_id as municipio_residencia_id, "
            + "followers.section_number as seccion, "
            + "followers.neighborhood as colonia, "
            + "followers.street as calle, "
            + "followers.external_number as numero_exterior, "
            + "followers.internal_number as numero_interno, "
            + "followers.zip_code as codigo_postal, "
            + "followers.latitude as latitud, "
            + "followers.longitude as longitud, "
            + "cat_family_role.nombre as rol_familiar, "
            + "cat_marital_status.nombre as estado_civil, "
            + "cat_sex.nombre AS sexo, "
            + "followers.number_of_people_invited as personas_invitadas, "
            + "followers.pollin_station_representative_participation as representante_mesas_electorales, "
            + "followers.committee_participation as participacion_comite, "
            + "CASE WHEN followers.is_adult = 't' THEN 'Sí' ELSE 'No' END adulto_mayor, "
            + "followers.support_whatsapp as telefono_compatible_whatsapp, "
            + "followers.supports_movement as apoya_movimiento, "
            + "followers.validity as ine_vencimiento, "
            + "followers.created as fecha_creacion, "
            + "followers.updated as fecha_actualizacion "
            + "FROM "
            + "promoters "
            + "LEFT JOIN followers ON followers.promoter_auth_id = promoters.promoter_auth_id "
            + "LEFT JOIN cat_family_role on cat_family_role.id = followers.cat_family_role "
            + "LEFT JOIN cat_marital_status on cat_marital_status.id = followers.cat_marital_status "
            + "LEFT JOIN cat_sex on cat_sex.id = followers.cat_sex "
            + "WHERE followers.deleted IS NULL AND promoters.cordinator_zone_auth_id ='"
            + cordinatorZoneAuthId
            + "'";

    log.info("Query: " + select);

    Query query = em.createNativeQuery(select);
    try {
      objectRes = query.getResultList();
      for (Object[] obj : objectRes) {
        DownloadFollowersDto edd = new DownloadFollowersDto();
        edd.setPromotorId(obj[0] != null ? obj[0].toString() : null);
        edd.setPromotor(obj[1] != null ? obj[1].toString() : null);
        edd.setPromotorCurp(obj[2] != null ? obj[2].toString() : null);
        edd.setPromotorTelefono(obj[3] != null ? obj[3].toString() : null);
        edd.setPromotorIne(obj[4] != null ? obj[4].toString() : null);
        edd.setSimpatizanteId(obj[5] != null ? (Long) obj[5] : null);
        edd.setSimpatizante(obj[6] != null ? obj[6].toString() : null);
        edd.setCurp(obj[7] != null ? obj[7].toString() : null);
        edd.setTelefono(obj[8] != null ? obj[8].toString() : null);
        edd.setIne(obj[9] != null ? obj[9].toString() : null);
        edd.setEntidadResidenciaId(obj[10] != null ? (Integer) obj[10] : null);
        edd.setMunicipioResidenciaId(obj[11] != null ? (Integer) obj[11] : null);
        edd.setSeccion(obj[12] != null ? obj[12].toString() : null);
        edd.setColonia(obj[13] != null ? obj[13].toString() : null);
        edd.setCalle(obj[14] != null ? obj[14].toString() : null);
        edd.setNumeroExterior(obj[15] != null ? obj[15].toString() : null);
        edd.setNumeroInterno(obj[16] != null ? obj[16].toString() : null);
        edd.setCodigoPostal(obj[17] != null ? obj[17].toString() : null);
        edd.setLatitud(obj[18] != null ? obj[18].toString() : null);
        edd.setLongitud(obj[19] != null ? obj[19].toString() : null);
        edd.setRolFamiliar(obj[20] != null ? obj[20].toString() : null);
        edd.setEstadoCivil(obj[21] != null ? obj[21].toString() : null);
        edd.setSexo(obj[22] != null ? obj[22].toString() : null);
        edd.setPersonasInvitadas(obj[23] != null ? obj[23].toString() : null);
        edd.setRepresentanteMesasElectorales(obj[24] != null ? obj[24].toString() : null);
        edd.setParticipacionComite(obj[25] != null ? obj[25].toString() : null);
        edd.setAdultoMayor(obj[26] != null ? obj[26].toString() : null);
        edd.setTelefonoCompatibleWhatsapp(obj[27] != null ? obj[27].toString() : null);
        edd.setApoyaMovimiento(obj[28] != null ? obj[28].toString() : null);
        edd.setIneVencimiento(obj[29] != null ? (Integer) obj[29] : null);
        edd.setFechaCreacion(obj[30] != null ? (Date) obj[30] : null);
        edd.setFechaActualizacion(obj[31] != null ? (Date) obj[31] : null);
        result.add(edd);
      }
    } catch (Exception e) {
      System.err.println("Error al obtener el reporte:" + e.getMessage());
    }
    return result;
  }

  public List<DownloadFollowersDto> getAllFollowersByReference(String referenceAuthId) {
    List<DownloadFollowersDto> result = new ArrayList<DownloadFollowersDto>();
    List<Object[]> objectRes = new ArrayList<>();
    String select =
        "SELECT "
            + "promoters.promoter_auth_id as promotor_id, "
            + "concat(promoters.names,' ',promoters.last_name,' ',promoters.sur_name) as promotor, "
            + "promoters.curp as promotor_curp, "
            + "promoters.cell_phone_number as promotor_telefono, "
            + "promoters.ine as promotor_ine, "
            + "followers.id as simpatizante_id, "
            + "concat(followers.names,' ',followers.last_name,' ',followers.sur_name) as simpatizante, "
            + "followers.curp as curp, "
            + "followers.cell_phone_number as telefono, "
            + "followers.ine as ine, "
            + "followers.residence_entity_id as entidad_residencia_id, "
            + "followers.residence_municipality_id as municipio_residencia_id, "
            + "followers.section_number as seccion, "
            + "followers.neighborhood as colonia, "
            + "followers.street as calle, "
            + "followers.external_number as numero_exterior, "
            + "followers.internal_number as numero_interno, "
            + "followers.zip_code as codigo_postal, "
            + "followers.latitude as latitud, "
            + "followers.longitude as longitud, "
            + "cat_family_role.nombre as rol_familiar, "
            + "cat_marital_status.nombre as estado_civil, "
            + "cat_sex.nombre AS sexo, "
            + "followers.number_of_people_invited as personas_invitadas, "
            + "followers.pollin_station_representative_participation as representante_mesas_electorales, "
            + "followers.committee_participation as participacion_comite, "
            + "CASE WHEN followers.is_adult = 't' THEN 'Sí' ELSE 'No' END adulto_mayor, "
            + "followers.support_whatsapp as telefono_compatible_whatsapp, "
            + "followers.supports_movement as apoya_movimiento, "
            + "followers.validity as ine_vencimiento, "
            + "followers.created as fecha_creacion, "
            + "followers.updated as fecha_actualizacion,"
            + "promoters.cordinator_zone_auth_id as cordinator_zone_auth_id "
            + "FROM "
            + "promoters "
            + "LEFT JOIN followers ON followers.promoter_auth_id = promoters.promoter_auth_id "
            + "LEFT JOIN cat_family_role on cat_family_role.id = followers.cat_family_role "
            + "LEFT JOIN cat_marital_status on cat_marital_status.id = followers.cat_marital_status "
            + "LEFT JOIN cat_sex on cat_sex.id = followers.cat_sex "
            + "WHERE followers.deleted IS NULL AND promoters.referent_auth_id ='"
            + referenceAuthId
            + "'";

    log.info("Query: " + select);

    Query query = em.createNativeQuery(select);
    try {
      objectRes = query.getResultList();
      for (Object[] obj : objectRes) {
        DownloadFollowersDto edd = new DownloadFollowersDto();
        edd.setPromotorId(obj[0] != null ? obj[0].toString() : null);
        edd.setPromotor(obj[1] != null ? obj[1].toString() : null);
        edd.setPromotorCurp(obj[2] != null ? obj[2].toString() : null);
        edd.setPromotorTelefono(obj[3] != null ? obj[3].toString() : null);
        edd.setPromotorIne(obj[4] != null ? obj[4].toString() : null);
        edd.setSimpatizanteId(obj[5] != null ? (Long) obj[5] : null);
        edd.setSimpatizante(obj[6] != null ? obj[6].toString() : null);
        edd.setCurp(obj[7] != null ? obj[7].toString() : null);
        edd.setTelefono(obj[8] != null ? obj[8].toString() : null);
        edd.setIne(obj[9] != null ? obj[9].toString() : null);
        edd.setEntidadResidenciaId(obj[10] != null ? (Integer) obj[10] : null);
        edd.setMunicipioResidenciaId(obj[11] != null ? (Integer) obj[11] : null);
        edd.setSeccion(obj[12] != null ? obj[12].toString() : null);
        edd.setColonia(obj[13] != null ? obj[13].toString() : null);
        edd.setCalle(obj[14] != null ? obj[14].toString() : null);
        edd.setNumeroExterior(obj[15] != null ? obj[15].toString() : null);
        edd.setNumeroInterno(obj[16] != null ? obj[16].toString() : null);
        edd.setCodigoPostal(obj[17] != null ? obj[17].toString() : null);
        edd.setLatitud(obj[18] != null ? obj[18].toString() : null);
        edd.setLongitud(obj[19] != null ? obj[19].toString() : null);
        edd.setRolFamiliar(obj[20] != null ? obj[20].toString() : null);
        edd.setEstadoCivil(obj[21] != null ? obj[21].toString() : null);
        edd.setSexo(obj[22] != null ? obj[22].toString() : null);
        edd.setPersonasInvitadas(obj[23] != null ? obj[23].toString() : null);
        edd.setRepresentanteMesasElectorales(obj[24] != null ? obj[24].toString() : null);
        edd.setParticipacionComite(obj[25] != null ? obj[25].toString() : null);
        edd.setAdultoMayor(obj[26] != null ? obj[26].toString() : null);
        edd.setTelefonoCompatibleWhatsapp(obj[27] != null ? obj[27].toString() : null);
        edd.setApoyaMovimiento(obj[28] != null ? obj[28].toString() : null);
        edd.setIneVencimiento(obj[29] != null ? (Integer) obj[29] : null);
        edd.setFechaCreacion(obj[30] != null ? (Date) obj[30] : null);
        edd.setFechaActualizacion(obj[31] != null ? (Date) obj[31] : null);
        edd.setCordinatorZoneAuthId(obj[32] != null ? obj[32].toString() : null);
        result.add(edd);
      }
    } catch (Exception e) {
      System.err.println("Error al obtener el reporte:" + e.getMessage());
    }
    return result;
  }

  public List<DownloadFollowersDto> getAllByAdmin() {
    List<DownloadFollowersDto> result = new ArrayList<DownloadFollowersDto>();
    List<Object[]> objectRes = new ArrayList<>();
    String select =
        "SELECT "
            + "promoters.promoter_auth_id as promotor_id, "
            + "concat(promoters.names,' ',promoters.last_name,' ',promoters.sur_name) as promotor, "
            + "promoters.curp as promotor_curp, "
            + "promoters.cell_phone_number as promotor_telefono, "
            + "promoters.ine as promotor_ine, "
            + "followers.id as simpatizante_id, "
            + "concat(followers.names,' ',followers.last_name,' ',followers.sur_name) as simpatizante, "
            + "followers.curp as curp, "
            + "followers.cell_phone_number as telefono, "
            + "followers.ine as ine, "
            + "followers.residence_entity_id as entidad_residencia_id, "
            + "followers.residence_municipality_id as municipio_residencia_id, "
            + "followers.section_number as seccion, "
            + "followers.neighborhood as colonia, "
            + "followers.street as calle, "
            + "followers.external_number as numero_exterior, "
            + "followers.internal_number as numero_interno, "
            + "followers.zip_code as codigo_postal, "
            + "followers.latitude as latitud, "
            + "followers.longitude as longitud, "
            + "cat_family_role.nombre as rol_familiar, "
            + "cat_marital_status.nombre as estado_civil, "
            + "cat_sex.nombre AS sexo, "
            + "followers.number_of_people_invited as personas_invitadas, "
            + "followers.pollin_station_representative_participation as representante_mesas_electorales, "
            + "followers.committee_participation as participacion_comite, "
            + "CASE WHEN followers.is_adult = 't' THEN 'Sí' ELSE 'No' END adulto_mayor, "
            + "followers.support_whatsapp as telefono_compatible_whatsapp, "
            + "followers.supports_movement as apoya_movimiento, "
            + "followers.validity as ine_vencimiento, "
            + "followers.created as fecha_creacion, "
            + "followers.updated as fecha_actualizacion,"
            + "promoters.cordinator_zone_auth_id as cordinator_zone_auth_id "
            + "FROM "
            + "promoters "
            + "LEFT JOIN followers ON followers.promoter_auth_id = promoters.promoter_auth_id "
            + "LEFT JOIN cat_family_role on cat_family_role.id = followers.cat_family_role "
            + "LEFT JOIN cat_marital_status on cat_marital_status.id = followers.cat_marital_status "
            + "LEFT JOIN cat_sex on cat_sex.id = followers.cat_sex "
            + "WHERE followers.deleted IS NULL ";

    log.info("Query: " + select);

    Query query = em.createNativeQuery(select);
    try {
      objectRes = query.getResultList();
      for (Object[] obj : objectRes) {
        DownloadFollowersDto edd = new DownloadFollowersDto();
        edd.setPromotorId(obj[0] != null ? obj[0].toString() : null);
        edd.setPromotor(obj[1] != null ? obj[1].toString() : null);
        edd.setPromotorCurp(obj[2] != null ? obj[2].toString() : null);
        edd.setPromotorTelefono(obj[3] != null ? obj[3].toString() : null);
        edd.setPromotorIne(obj[4] != null ? obj[4].toString() : null);
        edd.setSimpatizanteId(obj[5] != null ? (Long) obj[5] : null);
        edd.setSimpatizante(obj[6] != null ? obj[6].toString() : null);
        edd.setCurp(obj[7] != null ? obj[7].toString() : null);
        edd.setTelefono(obj[8] != null ? obj[8].toString() : null);
        edd.setIne(obj[9] != null ? obj[9].toString() : null);
        edd.setEntidadResidenciaId(obj[10] != null ? (Integer) obj[10] : null);
        edd.setMunicipioResidenciaId(obj[11] != null ? (Integer) obj[11] : null);
        edd.setSeccion(obj[12] != null ? obj[12].toString() : null);
        edd.setColonia(obj[13] != null ? obj[13].toString() : null);
        edd.setCalle(obj[14] != null ? obj[14].toString() : null);
        edd.setNumeroExterior(obj[15] != null ? obj[15].toString() : null);
        edd.setNumeroInterno(obj[16] != null ? obj[16].toString() : null);
        edd.setCodigoPostal(obj[17] != null ? obj[17].toString() : null);
        edd.setLatitud(obj[18] != null ? obj[18].toString() : null);
        edd.setLongitud(obj[19] != null ? obj[19].toString() : null);
        edd.setRolFamiliar(obj[20] != null ? obj[20].toString() : null);
        edd.setEstadoCivil(obj[21] != null ? obj[21].toString() : null);
        edd.setSexo(obj[22] != null ? obj[22].toString() : null);
        edd.setPersonasInvitadas(obj[23] != null ? obj[23].toString() : null);
        edd.setRepresentanteMesasElectorales(obj[24] != null ? obj[24].toString() : null);
        edd.setParticipacionComite(obj[25] != null ? obj[25].toString() : null);
        edd.setAdultoMayor(obj[26] != null ? obj[26].toString() : null);
        edd.setTelefonoCompatibleWhatsapp(obj[27] != null ? obj[27].toString() : null);
        edd.setApoyaMovimiento(obj[28] != null ? obj[28].toString() : null);
        edd.setIneVencimiento(obj[29] != null ? (Integer) obj[29] : null);
        edd.setFechaCreacion(obj[30] != null ? (Date) obj[30] : null);
        edd.setFechaActualizacion(obj[31] != null ? (Date) obj[31] : null);
        edd.setCordinatorZoneAuthId(obj[32] != null ? obj[32].toString() : null);
        result.add(edd);
      }
    } catch (Exception e) {
      System.err.println("Error al obtener el reporte:" + e.getMessage());
    }
    return result;
  }
}
