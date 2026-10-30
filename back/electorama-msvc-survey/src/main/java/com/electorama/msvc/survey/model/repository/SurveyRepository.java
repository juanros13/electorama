package com.electorama.msvc.survey.model.repository;

import com.electorama.msvc.survey.model.entity.SurveyEntity;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyRepository extends JpaRepository<SurveyEntity, Long> {

  Optional<SurveyEntity> findBySurveyUserFolio_FolioAndSurveyUserFolio_SurveyUserId(
      String folio, Long surveyUserId);

  List<SurveyEntity> findBySurveyUserFolio_FolioIn(List<String> folios);

  @Query(
      value =
          " select sv.* from survey sv "
              + "where (CONCAT(sv.names, ' ', sv.last_name, ' ', sv.sur_name) ILIKE CONCAT('%', :name,'%') "
              + "OR CONCAT( sv.street) ILIKE CONCAT('%',:address,'%') "
              + "OR sv.device_id ILIKE CONCAT('%',:deviceId,'%') "
              + ")",
      nativeQuery = true)
  Page<SurveyEntity> findByParameters(
      @Param("address") String address,
      @Param("name") String name,
      @Param("deviceId") String deviceId,
      Pageable pageable);

  @Query(
      value =
          "SELECT * FROM survey su WHERE su.device_id = :deviceId ORDER BY su.folio DESC LIMIT 1",
      nativeQuery = true)
  Optional<SurveyEntity> getMaxFolioFromDevice(String deviceId);

  // Optional<SurveyEntity> findByIdAndDeletedIsNull(Long id);

  @Query(value = "SELECT * FROM survey su ", nativeQuery = true)
  Page<SurveyEntity> findAll(Pageable pageable);

  @Query(
      value =
          " select dge_survet_date, count(*) from survey "
              + "where dge_survet_date BETWEEN :startDate AND :endDate "
              + "group by dge_survet_date;",
      nativeQuery = true)
  Page<SurveyEntity> findByWeek(
      @Param("startDate") String startDate, @Param("endDate") String endDate, Pageable pageable);

  @Query(
      value =
          "SELECT sv.* FROM survey sv "
              + "WHERE ( sv.device_id ILIKE CONCAT('%', :deviceId, '%')) ",
      //        + "OR ( sv.interview_name ILIKE CONCAT('%', :surveyor_name, '%'))",
      // "OR ( sv.dge_survet_date = :sge_survet_date) " +
      // "OR ( sv.status_change = :status_change)",
      nativeQuery = true)
  Page<SurveyEntity> searchByMultipleParameters(
      @Param("deviceId") String deviceId,
      // @Param("surveyor_name") String surveyor_name,
      // @Param("sge_survet_date") String sge_survet_date,
      // @Param("status_change") Boolean status_change,
      Pageable pageable);

  @Query(
      value =
          "SELECT sv.* FROM survey sv"
              + " LEFT JOIN survey_user su ON su.id = sv.survey_user_id "
              + " WHERE su.auth_id = :authId  AND sv.street IS NULL ",
      nativeQuery = true)
  Page<SurveyEntity> searchByAuthIdAndNotStreet(@Param("authId") String authId, Pageable pageable);

  @Query(
      value =
          "SELECT sv.* FROM survey sv"
              + " LEFT JOIN survey_user su ON su.id = sv.survey_user_id "
              + " WHERE su.auth_id = :authId  AND sv.street IS NOT NULL ",
      nativeQuery = true)
  Page<SurveyEntity> searchByAuthIdAndStreet(@Param("authId") String authId, Pageable pageable);

  @Query(
      value =
          "SELECT sv.* FROM survey sv"
              + " LEFT JOIN survey_user su ON su.id = sv.survey_user_id "
              + " WHERE su.survey_municipality_auth_id = :authId  AND sv.street IS NOT NULL ",
      nativeQuery = true)
  Page<SurveyEntity> searchBySurveyMunicipalityAuthIdAndStreet(
      @Param("authId") String authId, Pageable pageable);

  @Query(
      "SELECT s.surveyDate, COUNT(s) FROM SurveyEntity s WHERE s.surveyDate BETWEEN :startDate AND :endDate GROUP BY s.surveyDate")
  List<Object[]> findTotalSurveysPerWeek(
      @Param("startDate") Date startDate, @Param("endDate") Date endDate);

  @Query("SELECT s.municipality, COUNT(s) FROM SurveyEntity s GROUP BY s.municipality")
  List<Object[]> findTotalSurveysPerMunicipality();

  @Query(
      "SELECT s.municipality, s.deviceId, COUNT(s) FROM SurveyEntity s GROUP BY s.municipality, s.deviceId")
  List<Object[]> findTotalSurveysPerMunicipalityAndUser();

  @Query(
      value =
          " WITH folio_groups AS ( "
              + "    SELECT "
              + "        folio::::int AS folio_num, "
              + "        folio::::int - ROW_NUMBER() OVER (ORDER BY folio::::int) AS grp "
              + "    FROM "
              + "        survey_user_folio "
              + "    WHERE "
              + "        survey_user_id = :authId  "
              + " ), "
              + " grouped_folios AS ( "
              + "    SELECT "
              + "        MIN(folio_num) AS start_folio, "
              + "        MAX(folio_num) AS end_folio "
              + "    FROM "
              + "        folio_groups "
              + "    GROUP BY "
              + "        grp "
              + " ) "
              + " SELECT "
              + "    CASE "
              + "        WHEN start_folio = end_folio THEN start_folio::::TEXT "
              + "        ELSE start_folio::::TEXT || '-' || end_folio::::TEXT "
              + "    END AS folio_range "
              + " FROM "
              + "    grouped_folios "
              + " ORDER BY "
              + "    start_folio",
      nativeQuery = true)
  List<Object[]> getRangeFolioByAuthId(@Param("authId") Integer authId);

  @Query(
      value =
          " WITH folio_groups AS ( "
              + "    SELECT "
              + "        folio::::int AS folio_num, "
              + "        folio::::int - ROW_NUMBER() OVER (ORDER BY folio::::int) AS grp "
              + "    FROM "
              + "        survey_user_folio "
              + "    LEFT JOIN survey ON survey.survey_user_folio_id = survey_user_folio.id"
              + "    WHERE "
              + "        survey_user_folio.survey_user_id = :authId AND survey.id IS NULL"
              + " ), "
              + " grouped_folios AS ( "
              + "    SELECT "
              + "        MIN(folio_num) AS start_folio, "
              + "        MAX(folio_num) AS end_folio "
              + "    FROM "
              + "        folio_groups "
              + "    GROUP BY "
              + "        grp "
              + " ) "
              + " SELECT "
              + "    CASE "
              + "        WHEN start_folio = end_folio THEN start_folio::::TEXT "
              + "        ELSE start_folio::::TEXT || '-' || end_folio::::TEXT "
              + "    END AS folio_range "
              + " FROM "
              + "    grouped_folios "
              + " ORDER BY "
              + "    start_folio",
      nativeQuery = true)
  List<Object[]> getRangeFolioAvailablesByAuthId(@Param("authId") Integer authId);

  List<SurveyEntity> findBySurveyUser_Id(Long id);

  @Query(
      value =
          " SELECT sv.* FROM survey sv"
              + " LEFT JOIN secciones_ok so on sv.section_number = so.seccion "
              + " LEFT JOIN survey_user su on su.id = sv.survey_user_id "
              + " WHERE sv.street is null AND so.municipio = 'NACAJUCA'  AND so.municipio != su.municipality_name ",
      nativeQuery = true)
  Page<SurveyEntity> searchSurveyNacajucaWithoutSearch(Pageable pageable);

  @Query(
      value =
          " SELECT s.* FROM survey s "
              + " LEFT JOIN secciones_ok so on s.section_number::::int = so.seccion::::int "
              + " LEFT JOIN survey_user su on su.id = s.survey_user_id "
              + " LEFT JOIN survey_user_folio suf on suf.id = s.survey_user_folio_id "
              + " WHERE s.street is null AND suf.folio = :search "
              + " AND so.municipio = 'NACAJUCA' AND so.municipio != su.municipality_name",
      nativeQuery = true)
  Page<SurveyEntity> getSurveyNacajucaWithSearch(@Param("search") String search, Pageable pageable);

  @Query(
      value =
          " SELECT s.* FROM survey s "
              + " LEFT JOIN secciones_ok so on s.section_number::::int = so.seccion::::int "
              + " LEFT JOIN survey_user su on su.id = s.survey_user_id "
              + " LEFT JOIN survey_user_folio suf on suf.id = s.survey_user_folio_id "
              + " WHERE s.street is null AND suf.folio = :search "
              + " AND so.municipio = 'CENTRO' AND so.municipio != su.municipality_name",
      nativeQuery = true)
  Page<SurveyEntity> getSurveyCentroWithSearch(@Param("search") String search, Pageable pageable);

  @Query(
      value =
          " SELECT s.* FROM survey s"
              + " LEFT JOIN secciones_ok so on s.section_number = so.seccion "
              + " LEFT JOIN survey_user su on su.id = s.survey_user_id "
              + " WHERE s.street is null AND so.municipio = 'CENTRO' AND so.municipio != su.municipality_name",
      nativeQuery = true)
  Page<SurveyEntity> getSurveyCentroWithoutSearch(Pageable pageable);

  @Query(
      value =
          " SELECT s.* FROM survey s "
              + " LEFT JOIN secciones_ok so on s.section_number::::int = so.seccion::::int "
              + " LEFT JOIN survey_user su on su.id = s.survey_user_id "
              + " LEFT JOIN survey_user_folio suf on suf.id = s.survey_user_folio_id "
              + " WHERE s.street is not null AND suf.folio = :search "
              + " AND so.municipio = 'NACAJUCA' AND so.municipio != su.municipality_name",
      nativeQuery = true)
  Page<SurveyEntity> searchSurveyNacajucaWithoutSearchComplete(Pageable pageable);

  @Query(
      value =
          " SELECT s.* FROM survey s "
              + " LEFT JOIN secciones_ok so on s.section_number::::int = so.seccion::::int "
              + " LEFT JOIN survey_user su on su.id = s.survey_user_id "
              + " LEFT JOIN survey_user_folio suf on suf.id = s.survey_user_folio_id "
              + " WHERE s.street is not null AND suf.folio = :search "
              + " AND so.municipio = 'NACAJUCA' AND so.municipio != su.municipality_name",
      nativeQuery = true)
  Page<SurveyEntity> getSurveyNacajucaWithSearchComplete(
      @Param("search") String search, Pageable pageable);

  @Query(
      value =
          " SELECT s.* FROM survey s "
              + " LEFT JOIN secciones_ok so on s.section_number::::int = so.seccion::::int "
              + " LEFT JOIN survey_user su on su.id = s.survey_user_id "
              + " LEFT JOIN survey_user_folio suf on suf.id = s.survey_user_folio_id "
              + " WHERE s.street is not null AND suf.folio = :search   AND so.municipio = 'CENTRO' AND so.municipio != survey_user.municipality_name",
      nativeQuery = true)
  Page<SurveyEntity> getSurveyCentroWithSearchComplete(
      @Param("search") String search, Pageable pageable);

  @Query(
      value =
          " SELECT s.* FROM survey s"
              + " LEFT JOIN secciones_ok so on s.section_number = so.seccion "
              + " LEFT JOIN survey_user su on su.id = s.survey_user_id "
              + " WHERE s.street is not null AND so.municipio = 'CENTRO' AND so.municipio != su.municipality_name",
      nativeQuery = true)
  Page<SurveyEntity> getSurveyCentroWithoutSearchComplete(Pageable pageable);

  Page<SurveyEntity> findBySurveyUser_AuthIdAndSurveyUserFolio_FolioAndStreetIsNull(
      String authId, String search, Pageable pageable);

  Page<SurveyEntity> findBySurveyUser_AuthIdAndSurveyUserFolio_FolioAndStreetIsNotNull(
      String authId, String search, Pageable pageable);

  Page<SurveyEntity>
      findBySurveyUser_SurveyMunicipalityAuthIdAndSurveyUserFolio_FolioAndStreetIsNotNull(
          String authId, String search, Pageable pageable);
}
