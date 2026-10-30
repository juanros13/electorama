package com.electorama.msvc.followers.facade;

import com.electorama.msvc.followers.model.dto.ReportCommitteesGoalsDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Meta y avance de comites.
 *
 * <p><b>El avance de este reporte siempre ha sido 0, y no por un error de aqui.</b> Se calculaba
 * contando promotores agrupados por {@code promoters.cat_committee}, y esa columna esta vacia en
 * las 49,355 filas de la tabla: cero promotores con comite asignado. El LEFT JOIN nunca encontraba
 * pareja, asi que el avance salia 0 contra una meta de 71,879 repartida en 5,653 comites.
 *
 * <p>La meta sigue saliendo de {@code cat_committee}, que es una tabla propia de esta base y no se
 * movio al modelo de estructura. La referencia a {@code promoters} se quita —era lo unico que ataba
 * este reporte a la vista de compatibilidad, y sin ella la fase 5 puede borrarla— y el avance se
 * declara 0 de forma explicita en vez de simularlo con una consulta que no mide nada.
 *
 * <p>Para que vuelva a medir hace falta una decision de producto, no codigo: donde vive la
 * pertenencia de un promotor a un comite. Cuando exista, el avance se cuenta con los puestos de
 * promotor del modelo nuevo.
 */
@Component
@Slf4j
public class ReportCommitteesGoalFacade {

  @PersistenceContext EntityManager em;

  public ReportCommitteesGoalsDto getReportCommitteesGoals(List<Integer> sections) {
    if (sections == null || sections.isEmpty()) {
      return vacio();
    }
    Query query =
        em.createNativeQuery(
            "SELECT COALESCE(SUM(promoters_goal), 0) FROM cat_committee"
                + " WHERE cat_section IN (:sections)");
    query.setParameter("sections", sections);
    return conMeta(query);
  }

  public ReportCommitteesGoalsDto getReportCommitteesGoalsByAdmin() {
    return conMeta(
        em.createNativeQuery("SELECT COALESCE(SUM(promoters_goal), 0) FROM cat_committee"));
  }

  private ReportCommitteesGoalsDto conMeta(Query query) {
    ReportCommitteesGoalsDto result = vacio();
    Object goal = query.getSingleResult();
    result.setGoal(goal == null ? 0 : ((Number) goal).intValue());
    return result;
  }

  private ReportCommitteesGoalsDto vacio() {
    ReportCommitteesGoalsDto result = new ReportCommitteesGoalsDto();
    result.setGoal(0);
    // El avance no se puede medir: ningun promotor tiene comite. Ver el javadoc.
    result.setProgress(0);
    return result;
  }
}
