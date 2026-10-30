package com.electorama.msvc.survey.model.repository;

import com.electorama.msvc.survey.model.entity.SurveyUserFolioEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SurveyUserFolioRepository extends JpaRepository<SurveyUserFolioEntity, Long> {

  Optional<SurveyUserFolioEntity> findByFolioAndSurveyUserId(String folio, Long surveyUserId);

  @Query(
      value = "SELECT suf.* FROM survey_user_folio suf" + " WHERE suf.survey_user_id  = :authId ",
      nativeQuery = true)
  List<SurveyUserFolioEntity> searchByAuthId(@Param("authId") Integer authId);

  @Query(
      value =
          "SELECT suf.* FROM survey_user_folio suf "
              + " LEFT JOIN survey s on s.survey_user_folio_id = suf.id"
              + " WHERE suf.survey_user_id  = :authId and s.id IS NULL",
      nativeQuery = true)
  List<SurveyUserFolioEntity> searchAvailablesByAuthId(@Param("authId") Integer authId);

  List<SurveyUserFolioEntity> findBySurveyUserIdAndFolioIn(
      Long surverUserId, List<String> foliosList);

  @Transactional
  @Modifying
  @Query(
      "UPDATE SurveyUserFolioEntity f SET f.surveyUserId = :userId  WHERE f.folio BETWEEN :folioStart AND :folioEnd ")
  void updateFolio(
      @Param("userId") Long userId,
      @Param("folioStart") String folioStart,
      @Param("folioEnd") String folioEnd);
}
