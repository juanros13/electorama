package com.electorama.msvc.survey.model.repository;

import com.electorama.msvc.survey.model.entity.SurveySectionGoalEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveySectionGoalRepository extends JpaRepository<SurveySectionGoalEntity, Long> {

  @Query(
      value =
          " SELECT    "
              + "    COALESCE(cedulas.municipaly_id, ssm.municipality_id) AS municipality_id,   "
              + "    COALESCE(cedulas.municipality_name, ssm.municipality_name) AS municipality_name,   "
              + "    COALESCE(ssm.sections, 0) AS section,   "
              + "    COALESCE(ssm.total_homes, 0) AS goal_homes,   "
              + "    COALESCE(cedulas.folio_captured, 0) AS folio_captured,   "
              + "    COALESCE(cedulas.form_captured, 0) AS form_captured   "
              + "FROM (   "
              + "    SELECT    "
              + "        su.municipaly_id,     "
              + "        su.municipality_name,   "
              + "        COUNT(su.municipality_name) as folio_captured,   "
              + "        COUNT(su.municipality_name) filter (where s.street IS NOT NULL) as form_captured   "
              + "    FROM survey s   "
              + "    LEFT JOIN survey_user su ON s.survey_user_id = su.id "
              + "    GROUP BY su.municipaly_id, su.municipality_name    "
              + ") AS cedulas   "
              + "FULL JOIN (   "
              + "    SELECT    "
              + "        ssm.municipality_id,    "
              + "        ssm.municipality_name,   "
              + "        COUNT(*) AS sections,   "
              + "        SUM(total_homes) AS total_homes   "
              + "    FROM survey_section_goal ssm   "
              + "    GROUP BY ssm.municipality_id, ssm.municipality_name    "
              + ") AS ssm ON cedulas.municipaly_id = ssm.municipality_id   "
              + "ORDER BY ssm.municipality_name;  ",
      nativeQuery = true)
  List<Object[]> getSurveyReportMunicipality();

  @Query(
      value =
          "         SELECT   "
              + "                   COALESCE(cedulas.municipaly_id, ssm.municipality_id) AS municipality_id,   "
              + "                   COALESCE(cedulas.municipality_name, ssm.municipality_name) AS municipality_name,  "
              + "                   COALESCE(ssm.total_homes, 0) AS goal_homes,  "
              + "                   COALESCE(cedulas.form_captured, 0) AS form_captured,  "
              + "COALESCE(cedulas.form_to_capture, 0) AS form_to_capture"
              + "               FROM (  "
              + "                   SELECT   "
              + " su.municipaly_id,     "
              + "                       su.municipality_name,  "
              + "                       COUNT(su.municipality_name) AS folio_captured,  "
              + "                       COUNT(su.municipality_name)  AS form_captured,  "
              + " COUNT(su.municipality_name) filter (where s.street IS NOT NULL) as form_to_capture   "
              + "                   FROM survey s  "
              + "                   LEFT JOIN survey_user su ON s.survey_user_id = su.id  "
              + "                   GROUP BY su.municipaly_id, su.municipality_name  "
              + "               ) AS cedulas  "
              + "               FULL JOIN (  "
              + "                   SELECT   "
              + " ssm.municipality_id, "
              + "                       ssm.municipality_name,  "
              + "                       COUNT(*) AS sections,  "
              + "                       SUM(total_homes) AS total_homes  "
              + "                   FROM survey_section_goal ssm  "
              + "                   GROUP BY ssm.municipality_id, ssm.municipality_name    "
              + "               ) AS ssm ON cedulas.municipality_name = ssm.municipality_name  "
              + "               ORDER BY ssm.municipality_name ",
      nativeQuery = true)
  List<Object[]> getDashMunicipality();

  List<SurveySectionGoalEntity> findByMunicipalityId(Long municipalityId);

  @Query(
      value =
          "SELECT  "
              + "    s.survey_user_id, "
              + "    s.latitude, "
              + "    s.longitude "
              + "FROM  "
              + "    survey_user su "
              + "LEFT JOIN  "
              + "    survey s ON s.survey_user_id = su.id "
              + "WHERE  "
              + "    su.municipaly_id IS NOT NULL "
              + "  AND s.latitude IS NOT NULL "
              + "  AND s.longitude IS NOT NULL "
              + "    AND su.municipaly_id = :municipalityId    ",
      nativeQuery = true)
  List<Object[]> getCoordinatesMunicipality(@Param("municipalityId") Long municipalityId);

  @Query(
      value =
          "SELECT  "
              + "s.survey_user_id, "
              + "s.latitude, "
              + "s.longitude "
              + "FROM  "
              + "survey_user su "
              + "LEFT JOIN  "
              + "survey s ON s.survey_user_id = su.id "
              + "WHERE  "
              + "su.municipaly_id IS NOT NULL "
              + "AND s.latitude IS NOT NULL "
              + "AND s.longitude IS NOT NULL "
              + "AND  su.id = :userId    ",
      nativeQuery = true)
  List<Object[]> getUserId(@Param("userId") Long userId);

  @Query(
      value =
          "  SELECT     "
              + "                   COALESCE(cedulas.municipaly_id, ssm.municipality_id) AS municipality_id,    "
              + "                   COALESCE(cedulas.municipality_name, ssm.municipality_name) AS municipality_name,    "
              + "                   COALESCE(ssm.sections, 0) AS state_server,    "
              + "                   COALESCE(ssm.total_homes, 0) AS goal_homes,    "
              + "                   COALESCE(cedulas.folio_captured, 0) AS folio_captured,    "
              + "                   COALESCE(cedulas.form_captured, 0) AS form_captured,    "
              + "          COALESCE(ssm.total_homes, 0) - COALESCE(cedulas.form_captured, 0) AS homes_to_be_censused "
              + "               FROM (    "
              + "                   SELECT     "
              + "                       su.municipaly_id,      "
              + "                       su.municipality_name,    "
              + "                       COUNT(su.municipality_name) as folio_captured,    "
              + "                       COUNT(su.municipality_name) filter (where s.street IS NOT NULL) as form_captured    "
              + "                   FROM survey s    "
              + "                   LEFT JOIN survey_user su ON s.survey_user_id = su.id  "
              + "                   GROUP BY su.municipaly_id, su.municipality_name     "
              + "               ) AS cedulas    "
              + "               FULL JOIN (    "
              + "                   SELECT     "
              + "                       ssm.municipality_id,     "
              + "                       ssm.municipality_name,    "
              + "                       COUNT(*) AS sections,    "
              + "                       SUM(total_homes) AS total_homes    "
              + "                   FROM survey_section_goal ssm    "
              + "                   GROUP BY ssm.municipality_id, ssm.municipality_name     "
              + "               ) AS ssm ON cedulas.municipaly_id = ssm.municipality_id "
              + "        WHERE COALESCE(cedulas.municipaly_id, ssm.municipality_id) = :municipalityId "
              + "               ORDER BY ssm.municipality_name;",
      nativeQuery = true)
  List<Object[]> getSurveyReportMunicipalityId(@Param("municipalityId") Long municipalityId);

  @Query(
      value =
          " SELECT   "
              + "    COALESCE(cedulas.municipaly_id, ssm.municipality_id) AS municipality_id,  "
              + "    COALESCE(cedulas.municipality_name, ssm.municipality_name) AS municipality_name, "
              + "    COALESCE(cedulas.section_number, 0) AS section_number,  "
              + "    ssm.total_homes AS goal_homes,  "
              + "    COALESCE(cedulas.folio_captured, 0) AS folio_captured,  "
              + "    COALESCE(cedulas.form_captured, 0) AS form_captured, "
              + "  cedulas.auth_id, "
              + "    cedulas.name "
              + "FROM (  "
              + "    SELECT   "
              + "        su.municipaly_id,    "
              + "        su.municipality_name,  "
              + "    su.survey_municipality_auth_id, "
              + "    su.section_number, "
              + "     su.auth_id, "
              + "        su.name, "
              + "        COUNT(su.municipality_name) as folio_captured,  "
              + "        COUNT(su.municipality_name) filter (where s.street IS NOT NULL) as form_captured  "
              + "    FROM survey s  "
              + "    LEFT JOIN survey_user su ON s.survey_user_id = su.id                             "
              + "     WHERE su.survey_municipality_auth_id = :autId "
              + "    GROUP BY su.municipaly_id, su.municipality_name, su.survey_municipality_auth_id,  su.auth_id, su.name, su.section_number  "
              + ") AS cedulas  "
              + "INNER JOIN (  "
              + "    SELECT     "
              + "    ssm.municipality_id,  "
              + "    ssm.municipality_name,  "
              + "        ssm.section as section, "
              + "        ssm.total_homes as total_homes  "
              + "    FROM survey_section_goal ssm  "
              + "    WHERE (:sectionNumber IS NULL OR ssm.section = :sectionNumber) "
              + "    GROUP BY municipality_id, municipality_name, ssm.section, ssm.total_homes "
              + ") AS ssm ON cedulas.section_number = ssm.section "
              + "ORDER BY ssm.section;    ",
      nativeQuery = true)
  List<Object[]> getSurveyReportCoordinateMunicipality(
      @Param("autId") String autId, @Param("sectionNumber") Integer sectionNumber);

  @Query(
      value =
          "            SELECT     "
              + "              COALESCE(cedulas.municipaly_id, ssm.municipality_id) AS municipality_id,    "
              + "              COALESCE(cedulas.municipality_name, ssm.municipality_name) AS municipality_name,   "
              + "              COALESCE(cedulas.section_number, ssm.section) AS section_number,    "
              + "              ssm.total_homes AS goal_homes,    "
              + "              COALESCE(cedulas.folio_captured, 0) AS folio_captured,    "
              + "              COALESCE(cedulas.form_captured, 0) AS form_captured,   "
              + "            cedulas.auth_id,   "
              + "              cedulas.name   "
              + "          FROM (    "
              + "              SELECT     "
              + "                  su.municipaly_id,      "
              + "                  su.municipality_name,    "
              + "              su.survey_municipality_auth_id,   "
              + "              su.section_number,   "
              + "               su.auth_id,   "
              + "                  su.name,   "
              + "                  COUNT(su.municipality_name) as folio_captured,    "
              + "                  COUNT(su.municipality_name) filter (where s.street IS NOT NULL) as form_captured    "
              + "              FROM survey s    "
              + "              LEFT JOIN survey_user su ON s.survey_user_id = su.id                               "
              + "              GROUP BY su.municipaly_id, su.municipality_name, su.survey_municipality_auth_id,  su.auth_id, su.name, su.section_number    "
              + "          ) AS cedulas    "
              + "           FULL JOIN (    "
              + "              SELECT       "
              + "              ssm.municipality_id,    "
              + "              ssm.municipality_name,    "
              + "                  ssm.section as section,   "
              + "                  ssm.total_homes as total_homes    "
              + "              FROM survey_section_goal ssm    "
              + "              GROUP BY municipality_id, municipality_name, ssm.section, ssm.total_homes   "
              + "          ) AS ssm ON cedulas.section_number = ssm.section   "
              + "          ORDER BY ssm.section",
      nativeQuery = true)
  List<Object[]> getSurveyReportAdminCoordinator(@Param("autId") String autId);

  @Query(
      value =
          "SELECT "
              + " COALESCE(cedulas.municipaly_id, ssm.municipality_id) AS municipality_id, "
              + " COALESCE(cedulas.municipality_name, ssm.municipality_name) AS municipality_name, "
              + " COALESCE(cedulas.section_number, ssm.section) AS section_number, "
              + " ssm.total_homes AS goal_homes, "
              + " COALESCE(cedulas.folio_captured, 0) AS folio_captured, "
              + " COALESCE(cedulas.form_captured, 0) AS form_captured, "
              + " cedulas.auth_id, "
              + " cedulas.name "
              + " FROM ( "
              + " SELECT "
              + " su.municipaly_id, "
              + " su.municipality_name, "
              + " su.survey_municipality_auth_id, "
              + " su.section_number, "
              + " su.auth_id, "
              + " su.name, "
              + " COUNT(su.municipality_name) as folio_captured, "
              + " COUNT(su.municipality_name) filter (where s.street IS NOT NULL) as form_captured "
              + " FROM survey s "
              + " LEFT JOIN survey_user su ON s.survey_user_id = su.id "
              + " WHERE (:name IS NULL OR :name = '' OR su.name ILIKE %:name%) " // Filtrado basado
              // en el campo name
              // con ILIKE
              + " GROUP BY su.municipaly_id, su.municipality_name, su.survey_municipality_auth_id, su.auth_id, su.name, su.section_number "
              + " ) AS cedulas "
              + " INNER JOIN ( " // Cambié a INNER JOIN para obtener solo coincidencias
              + " SELECT "
              + " ssm.municipality_id, "
              + " ssm.municipality_name, "
              + " ssm.section as section, "
              + " ssm.total_homes as total_homes "
              + " FROM survey_section_goal ssm "
              + " GROUP BY municipality_id, municipality_name, ssm.section, ssm.total_homes "
              + " ) AS ssm ON cedulas.section_number = ssm.section "
              + " ORDER BY ssm.section",
      nativeQuery = true)
  List<Object[]> getSearchSurveyReportAdminCoordinator(@Param("name") String name);
}
