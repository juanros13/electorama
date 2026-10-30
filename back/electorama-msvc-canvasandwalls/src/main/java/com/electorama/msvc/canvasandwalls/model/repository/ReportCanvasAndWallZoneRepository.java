package com.electorama.msvc.canvasandwalls.model.repository;

import com.electorama.msvc.canvasandwalls.model.entity.ReportCanvasAndWallZoneEntity;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportCanvasAndWallZoneRepository
    extends JpaRepository<ReportCanvasAndWallZoneEntity, Integer> {

  @Query(
      value =
          "SELECT "
              + "row_number() over() as id, "
              + "canvas_and_walls.reference_auth_id, "
              + "canvas_and_walls.coordinate_zone_id, "
              + "municipality_id, "
              + "canvas_and_walls.seccion_id as section_id, "
              + "canvas_and_walls.canvas_and_wall_category_id, "
              + "canvas_and_walls_category.name AS category, "
              + "canvas_and_walls.canvas_and_wall_proyect_id, "
              + "canvas_and_walls_proyects.name AS proyect, "
              + "count(*) as total "
              + "from canvas_and_walls "
              + "LEFT JOIN canvas_and_walls_category on canvas_and_walls_category.id = canvas_and_walls.canvas_and_wall_category_id "
              + "LEFT JOIN canvas_and_walls_proyects on canvas_and_walls_proyects.id = canvas_and_walls.canvas_and_wall_proyect_id "
              + "WHERE canvas_and_walls.created BETWEEN :dateStart AND :dateEnd and canvas_and_walls.deleted is null "
              + "GROUP BY "
              + "canvas_and_walls.reference_auth_id, "
              + "canvas_and_walls.coordinate_zone_id, "
              + "canvas_and_walls.municipality_id, "
              + "canvas_and_walls.seccion_id, "
              + "canvas_and_walls.canvas_and_wall_category_id, "
              + "canvas_and_walls_category.name, "
              + "canvas_and_walls.canvas_and_wall_proyect_id, "
              + "canvas_and_walls_proyects.name "
              + "ORDER BY canvas_and_walls.reference_auth_id, canvas_and_walls.municipality_id, canvas_and_walls.seccion_id, canvas_and_walls_category.name, canvas_and_walls_proyects.name",
      nativeQuery = true)
  List<ReportCanvasAndWallZoneEntity> findAllByMunicipality(
      @Param("dateStart") Date dateStart, @Param("dateEnd") Date dateEnd);
}
