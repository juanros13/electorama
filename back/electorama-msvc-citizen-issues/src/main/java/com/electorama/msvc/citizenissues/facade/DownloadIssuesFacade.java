package com.electorama.msvc.citizenissues.facade;

import com.electorama.msvc.citizenissues.model.dto.DownloadIssuesDto;
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
public class DownloadIssuesFacade {
  @PersistenceContext EntityManager em;

  public List<DownloadIssuesDto> getAllIssuesByTime(String startDate, String endDate) {
    List<DownloadIssuesDto> result = new ArrayList<DownloadIssuesDto>();
    List<Object[]> objectRes = new ArrayList<>();
    String select =
        "SELECT    "
            + "issues.invoice as oficio,   "
            + "issues.full_name as nombre_completo_solicitante,   "
            + "issues.phone as telefono_solicitante,   "
            + "issues_details.registration_date AS fecha_levantamiento,   "
            + "issues_details.comments as comentarios,   "
            + "issues_types.name as tipo,   "
            + "issues_categories.name as categoria,   "
            + "issues_status.name as estatus,   "
            + "COALESCE(issues_details.name,'') AS nombre_beneficiario,   "
            + "COALESCE(issues_details.last_name,'') AS apellido_paterno_beneficiario,   "
            + "COALESCE(issues_details.sur_name,'') AS apellido_materno_beneficiario,   "
            + "COALESCE(issues_solutions_resources.name,''),   "
            + "issues.municipality_id,   "
            + "CASE   "
            + "  WHEN issues.locality_id = 0 THEN NULL   "
            + "  ELSE issues.locality_id   "
            + "END as localidad_id,   "
            + "COALESCE(issues.locality_other,'') as localidad_otro, "
            + "COALESCE(issues_details.user_create_id,'') as usuario_creo_id, "
            + "issues_details.curp, "
            + "COALESCE(issues_solutions_resources.name,'NO APLICA'), "
            + "CASE "
            + "  WHEN issues_details.urgent THEN 'URGENTE' "
            + "  ELSE 'NO URGENTE' "
            + "END as urgent "
            + "FROM issues   "
            + "LEFT JOIN issues_details ON issues_details.issue_id = issues.id   "
            + "LEFT JOIN issues_types ON issues_types.id = issues_details.issue_type_id   "
            + "LEFT JOIN issues_categories ON issues_categories.id = issues_details.issue_category_id   "
            + "LEFT JOIN issues_status ON issues_status.id = issues_details.issue_status_id   "
            + "LEFT JOIN issues_solutions_resources ON issues_solutions_resources.id = issues_details.issue_solution_resource_tmp_id   "
            + "WHERE issues_details.deleted_date IS NULL "
            + "AND issues_details.registration_date BETWEEN "
            + startDate
            + " AND "
            + endDate;

    log.info("Query: " + select);

    Query query = em.createNativeQuery(select);
    try {
      objectRes = query.getResultList();
      for (Object[] obj : objectRes) {
        DownloadIssuesDto edd = new DownloadIssuesDto();
        edd.setOficio(obj[0] != null ? obj[0].toString() : null);
        edd.setNombreCompletoSolicitante(obj[1] != null ? obj[1].toString() : null);
        edd.setTelefonoSolicitante(obj[2] != null ? obj[2].toString() : null);
        edd.setFechaLevantamiento(obj[3] != null ? (Date) obj[3] : null);
        edd.setComentarios(obj[4] != null ? obj[4].toString() : null);
        edd.setTipo(obj[5] != null ? obj[5].toString() : null);
        edd.setCategoria(obj[6] != null ? obj[6].toString() : null);
        edd.setEstatus(obj[7] != null ? obj[7].toString() : null);
        edd.setNombreBeneficiario(obj[8] != null ? obj[8].toString() : null);
        edd.setApellidoPaternoBeneficiario(obj[9] != null ? obj[9].toString() : null);
        edd.setApellidoMaternoBeneficiario(obj[10] != null ? obj[10].toString() : null);
        edd.setOtroNombreLocalidad(obj[11] != null ? obj[11].toString() : null);
        edd.setMunicipalityId(obj[12] != null ? Long.parseLong(obj[12].toString()) : null);
        edd.setLocalityId(obj[13] != null ? Long.parseLong(obj[13].toString()) : null);
        edd.setLocalidadOtra(obj[14] != null ? obj[14].toString() : null);
        edd.setUsuarioCreoId(obj[15] != null ? obj[15].toString() : null);
        edd.setCurp(obj[16] != null ? obj[16].toString() : null);
        edd.setRecurso(obj[17] != null ? obj[17].toString() : null);
        edd.setUrgente(obj[18] != null ? obj[18].toString() : null);
        result.add(edd);
      }

    } catch (Exception e) {
      System.err.println("Error al obtener el reporte:" + e.getMessage());
    }
    return result;
  }
}
