package com.electorama.msvc.structure.model.repository;

import com.electorama.msvc.structure.model.entity.Territory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TerritoryRepository extends JpaRepository<Territory, Long> {

  /** Los territorios que pueden ser el alcance de una campaña: estados y municipios vigentes. */
  @Query(
      value =
          "SELECT t.id AS id, t.type AS type, t.code AS code, t.name AS name,"
              + "       t.parent_territory_id AS parentId"
              + "  FROM territory t"
              + " WHERE t.valid_to IS NULL AND t.type IN ('STATE', 'MUNICIPALITY')"
              + " ORDER BY CASE t.type WHEN 'STATE' THEN 0 ELSE 1 END, t.code",
      nativeQuery = true)
  List<TerritoryOption> campaignScopes();

  interface TerritoryOption {
    Long getId();

    String getType();

    String getCode();

    String getName();

    Long getParentId();
  }
}
