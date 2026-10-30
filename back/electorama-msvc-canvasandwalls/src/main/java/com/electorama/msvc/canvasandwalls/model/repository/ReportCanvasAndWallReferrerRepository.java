package com.electorama.msvc.canvasandwalls.model.repository;

import com.electorama.msvc.canvasandwalls.model.entity.ReportCanvasAndWallReferrerEntity;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportCanvasAndWallReferrerRepository
    extends JpaRepository<ReportCanvasAndWallReferrerEntity, Integer> {

  @Query(
      value =
          "SELECT total.*, COALESCE(g.goal, 0) FROM "
              + "( "
              + "   SELECT "
              + "   row_number() over() as id, "
              + "   canvas_and_walls.municipality_id, "
              + "   canvas_and_walls.reference_auth_id, "
              + "   canvas_and_walls.canvas_and_wall_category_id, "
              + "   canvas_and_walls_category.name AS category, "
              + "   canvas_and_walls.canvas_and_wall_proyect_id, "
              + "   canvas_and_walls_proyects.name AS proyect, "
              + "   count(*) as total "
              + "   from canvas_and_walls "
              + "   LEFT JOIN canvas_and_walls_category on canvas_and_walls_category.id = canvas_and_walls.canvas_and_wall_category_id "
              + "   LEFT JOIN canvas_and_walls_proyects on canvas_and_walls_proyects.id = canvas_and_walls.canvas_and_wall_proyect_id "
              + "   WHERE canvas_and_walls.created BETWEEN :dateStart AND :dateEnd and canvas_and_walls.deleted is null "
              + "   GROUP BY "
              + "   canvas_and_walls.municipality_id, "
              + "   canvas_and_walls.reference_auth_id, "
              + "   canvas_and_walls.canvas_and_wall_category_id, "
              + "   canvas_and_walls_category.name, "
              + "   canvas_and_walls.canvas_and_wall_proyect_id, "
              + "   canvas_and_walls_proyects.name "
              + "   ORDER BY canvas_and_walls.municipality_id, canvas_and_walls_category.name, canvas_and_walls_proyects.name "
              + ") total "
              + "LEFT JOIN ( "
              + "   SELECT "
              + "   municipality_id, "
              + "   canvas_and_walls_goals.reference_auth_id, "
              + "   canvas_and_walls_goals.canvas_and_wall_category_id, "
              + "   canvas_and_walls_goals.canvas_and_wall_proyect_id, "
              + "   sum(goal) as goal "
              + "   FROM canvas_and_walls_goals "
              + "   group by canvas_and_walls_goals.municipality_id, canvas_and_walls_goals.reference_auth_id, canvas_and_wall_category_id, canvas_and_wall_proyect_id "
              + "   ORDER BY canvas_and_walls_goals.municipality_id "
              + ") g on g.municipality_id = total.municipality_id and g.reference_auth_id = total.reference_auth_id and "
              + "g.canvas_and_wall_category_id = total.canvas_and_wall_category_id and "
              + "g.canvas_and_wall_proyect_id = total.canvas_and_wall_proyect_id "
              + "ORDER BY total.municipality_id",
      nativeQuery = true)
  List<ReportCanvasAndWallReferrerEntity> findAllByReferrer(
      @Param("dateStart") Date dateStart, @Param("dateEnd") Date dateEnd);
}
