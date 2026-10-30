package com.electorama.msvc.canvasandwalls.model.repository;

import com.electorama.msvc.canvasandwalls.model.dto.ReportPDFDto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportPDFRepository extends JpaRepository<ReportPDFDto, Long> {

  @Query(
      value =
          "select "
              + "canvas_and_walls.id as id, "
              + "ue.first_name as referente, "
              + "user_entity.first_name as coordinador_zona,  "
              + "'TABASCO' as entidad, "
              + "canvas_and_walls.municipalityname as municipio, "
              + "canvas_and_walls.locality_name as localidad, "
              + "canvas_and_walls.seccion_number as seccion, "
              + "canvas_and_walls_proyects. name  as proyecto, "
              + "canvas_and_walls_category. name  as categoria, "
              + "date(canvas_and_walls.created) as fecha_registro, "
              + "canvas_and_walls.latitude as latitud, "
              + "canvas_and_walls.longitude as longitud, "
              + "canvas_and_walls.path_evidence_photo as  path_evidence_photo "
              + "FROM "
              + "canvas_and_walls "
              + "LEFT JOIN user_entity on user_entity.id  = canvas_and_walls.coordinate_zone_id "
              + "LEFT JOIN canvas_and_walls_category on canvas_and_walls_category.id = canvas_and_walls.canvas_and_wall_category_id "
              + "LEFT JOIN canvas_and_walls_proyects on canvas_and_walls_proyects.id = canvas_and_walls.canvas_and_wall_proyect_id "
              + "LEFT JOIN user_entity ue on ue.id = canvas_and_walls.reference_auth_id "
              + "WHERE canvas_and_walls.deleted is null "
              + "ORDER BY "
              + "ue.first_name , "
              + "user_entity.id  , "
              + "user_entity.first_name, "
              + "date(canvas_and_walls.created)",
      nativeQuery = true)
  List<ReportPDFDto> findAllByParametros();
}
