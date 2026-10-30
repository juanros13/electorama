package com.electorama.elections.models.repository;

import com.electorama.elections.models.entity.PollingBoothIssuesEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PollingBoothIssuesRepository
    extends JpaRepository<PollingBoothIssuesEntity, Long> {

  List<PollingBoothIssuesEntity> findByPollingBoothIdOrderByIdAsc(Long pollingBoothId);

  List<PollingBoothIssuesEntity> findByPollingBoothIdIsIn(List<Long> pollingBoothsIds);

  PollingBoothIssuesEntity findByPollingBoothId(Long pollingBoothId);

  PollingBoothIssuesEntity findFirstByPollingBoothId(Long pollingBoothId);

  @Query(
      value =
          "SELECT     "
              + " V2_0.municipality,  "
              + " V2_0.federal_district,  "
              + " V2_0.local_district,  "
              + " V2_0.seccion_number,  "
              + " V2_0.id as polling_booths_id,  "
              + " cat.name as name_cat,  "
              + " subcat.name as name_sub,  "
              + " polling_booths_issues.description,  "
              + " polling_booths_issues.created,  "
              + " UPPER(concat(polling_booths_types.name,' ',V2_0.polling_booth_number)) as polling_booth_name  "
              + " FROM polling_booths_issues    "
              + " LEFT JOIN polling_booths V2_0 ON V2_0.id = polling_booths_issues.polling_booth_id  "
              + "LEFT JOIN polling_booths_issues_sub_categories subcat ON subcat.id = polling_booths_issues.sub_category_id  "
              + "LEFT JOIN polling_booths_issues_categories cat ON cat.id = subcat.category_id  "
              + "LEFT JOIN polling_booths_types on polling_booths_types.id = V2_0.polling_booth_type_id  "
              + " where  V2_0.municipality_id IS NOT NULL   ",
      nativeQuery = true)
  List<Object[]> downloadIssues();
}
