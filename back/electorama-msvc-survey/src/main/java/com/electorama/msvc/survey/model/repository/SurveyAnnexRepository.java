package com.electorama.msvc.survey.model.repository;

import com.electorama.msvc.survey.model.entity.SurveyAnnexEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyAnnexRepository extends JpaRepository<SurveyAnnexEntity, Long> {

  @Query(
      value =
          " select sv.* from survey sv "
              + "where (CONCAT(sv.names, ' ', sv.last_name, ' ', sv.sur_name) ILIKE CONCAT('%', :name,'%') "
              + "OR CONCAT( sv.street) ILIKE CONCAT('%',:address,'%') "
              + "OR sv.device_id ILIKE CONCAT('%',:deviceId,'%') "
              + ")",
      nativeQuery = true)
  Page<SurveyAnnexEntity> findByParameters(
      @Param("address") String address,
      @Param("name") String name,
      @Param("deviceId") String deviceId,
      Pageable pageable);

  Optional<SurveyAnnexEntity> findFirstBySurveyPersonIdOrderBySurveyPersonIdDesc(
      Long surveyPersonId);

  // Optional<SurveyAnnexEntity> findByIdAndDeletedIsNull(Long id);

  @Query(
      value =
          "SELECT sv.* FROM survey sv "
              + "WHERE ( sv.device_id ILIKE CONCAT('%', :deviceId, '%')) ",
      //        + "OR ( sv.interview_name ILIKE CONCAT('%', :surveyor_name, '%'))",
      // "OR ( sv.dge_survet_date = :sge_survet_date) " +
      // "OR ( sv.status_change = :status_change)",
      nativeQuery = true)
  Page<SurveyAnnexEntity> searchByMultipleParameters(
      @Param("deviceId") String deviceId,
      // @Param("surveyor_name") String surveyor_name,
      // @Param("sge_survet_date") String sge_survet_date,
      // @Param("status_change") Boolean status_change,
      Pageable pageable);

  @Query(
      value =
          "SELECT sv.* FROM survey sv"
              + " LEFT JOIN survey_user su ON su.id = sv.survey_user "
              + " WHERE su.auth_id = :authId  AND sv.street IS NULL ",
      nativeQuery = true)
  Page<SurveyAnnexEntity> searchByAuthIdAndNotStreet(
      @Param("authId") String authId, Pageable pageable);

  List<SurveyAnnexEntity> findBySurveyId(Long surveyId);
}
