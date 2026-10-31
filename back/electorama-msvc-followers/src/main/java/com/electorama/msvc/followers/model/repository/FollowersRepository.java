package com.electorama.msvc.followers.model.repository;

import com.electorama.msvc.followers.model.entity.FollowersEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface FollowersRepository
    extends JpaRepository<FollowersEntity, Long>, JpaSpecificationExecutor<FollowersEntity> {

  Optional<FollowersEntity> findById(Long id);

  Integer countAllByPromoterAuthIdAndPromoterAuthIdIsNotNull(String userId);

  List<FollowersEntity> findByIdInAndPromoterAuthIdIsNotNull(List<Long> idsFollowers);

  // void updateVoteFlagById(boolean voteFlag,Long id);

  @Transactional
  @Modifying
  @Query(
      "UPDATE FollowersEntity f SET f.voteFlag = :voteFlag, f.voteFlagTimestamp=CURRENT_TIMESTAMP WHERE f.id = :followerId")
  void updateVoteFlagById(
      @Param("followerId") Long followerId, @Param("voteFlag") boolean voteFlag);

  @Query(
      value =
          "SELECT  "
              + "COUNT(followers.id) filter (where followers.vote_flag) as progress_mobilization, "
              + "COUNT(followers.id) as goal_mobilization "
              + "FROM followers "
              + "LEFT JOIN promoters V2_0 ON V2_0.promoter_auth_id = followers.promoter_auth_id  "
              + "where  V2_0.municipality_id IS NOT NULL AND followers.promoter_auth_id IS NOT NULL  "
              + "AND  V2_0.municipality_id <2000",
      nativeQuery = true)
  List<Object[]> getStateMobilization();

  @Query(
      value =
          "SELECT  "
              + " V2_0.municipality_id,  "
              + " COUNT(followers.id) filter (where followers.vote_flag) as progress_mobilization,  "
              + " COUNT(followers.id) as goal_mobilization  "
              + " FROM followers  "
              + " LEFT JOIN promoters V2_0 ON V2_0.promoter_auth_id = followers.promoter_auth_id   "
              + " where  V2_0.municipality_id IS NOT NULL  AND followers.promoter_auth_id IS NOT NULL  "
              + " AND V2_0.municipality_id <2000  "
              + " GROUP BY V2_0.municipality_id ",
      nativeQuery = true)
  List<Object[]> getMunicipalityMobilization();

  @Query(
      value =
          "SELECT  "
              + " V2_0.municipality_id, "
              + "     COUNT(followers.id) filter (where followers.vote_flag) as progress_mobilization, "
              + " COUNT(followers.id) as goal_mobilization, "
              + " V2_0.referent_auth_id "
              + "FROM followers "
              + "LEFT JOIN promoters V2_0 ON V2_0.promoter_auth_id = followers.promoter_auth_id  "
              + "where  V2_0.municipality_id IS NOT NULL AND followers.promoter_auth_id IS NOT NULL  "
              + "AND  V2_0.municipality_id <2000 "
              + "GROUP BY  V2_0.municipality_id, V2_0.referent_auth_id ",
      nativeQuery = true)
  List<Object[]> getMobilizationProgressByReferent();

  @Query(
      value =
          "SELECT  "
              + " V2_0.municipality_id, "
              + " followers.section_number, "
              + "     COUNT(followers.id) filter (where followers.vote_flag) as progress_mobilization, "
              + " COUNT(followers.id) as goal_mobilization "
              + "FROM followers "
              + "LEFT JOIN promoters V2_0 ON V2_0.promoter_auth_id = followers.promoter_auth_id  "
              + "where  V2_0.municipality_id IS NOT NULL AND followers.promoter_auth_id IS NOT NULL  "
              + "AND  V2_0.municipality_id <2000 "
              + "GROUP BY  V2_0.municipality_id, followers.section_number",
      nativeQuery = true)
  List<Object[]> getMobilizationSectionByMunicipality();

  @Query(
      value =
          "SELECT  "
              + " V2_0.municipality_id, "
              + " COUNT(followers.id) filter (where followers.vote_flag) as progress_mobilization, "
              + " COUNT(followers.id) as goal_mobilization, "
              + " V2_0.referent_auth_id, "
              + " V2_0.cordinator_zone_auth_id,"
              + " followers.section_id, "
              + " followers.section_number "
              + "FROM followers "
              + "LEFT JOIN promoters V2_0 ON V2_0.promoter_auth_id = followers.promoter_auth_id  "
              + "where  V2_0.municipality_id IS NOT NULL AND followers.promoter_auth_id IS NOT NULL  "
              + " AND  V2_0.municipality_id <2000 "
              + "GROUP BY  V2_0.municipality_id, V2_0.referent_auth_id, V2_0.cordinator_zone_auth_id, followers.section_id, followers.section_number",
      nativeQuery = true)
  List<Object[]> getMunicipalityByReferentAndCoordinator();

  @Query(
      value =
          "SELECT  "
              + "   V2_0.municipality_id, "
              + "     COUNT(followers.id) filter (where followers.vote_flag) as progress_mobilization, "
              + "   COUNT(followers.id) as goal_mobilization, "
              + "   V2_0.referent_auth_id, "
              + "   V2_0.cordinator_zone_auth_id, "
              + " followers.section_id, "
              + " followers.section_number "
              + "FROM followers "
              + "LEFT JOIN promoters V2_0 ON V2_0.promoter_auth_id = followers.promoter_auth_id  "
              + "where  V2_0.municipality_id IS NOT NULL AND followers.promoter_auth_id IS NOT NULL  "
              + " AND  V2_0.municipality_id <2000  AND followers.section_id IS NOT NULL "
              + " AND  V2_0.referent_auth_id = :authIdHeader "
              + "GROUP BY  V2_0.municipality_id, V2_0.referent_auth_id, V2_0.cordinator_zone_auth_id, followers.section_id, followers.section_number",
      nativeQuery = true)
  List<Object[]> getMunicipalityByReferentAndCoordinator(String authIdHeader);

  @Query(
      value =
          "SELECT  "
              + "   V2_0.municipality_id, "
              + "     COUNT(followers.id) filter (where followers.vote_flag) as progress_mobilization, "
              + "   COUNT(followers.id) as goal_mobilization, "
              + "   V2_0.referent_auth_id, "
              + "   V2_0.cordinator_zone_auth_id, "
              + " followers.section_id, "
              + " followers.section_number "
              + "FROM followers "
              + "LEFT JOIN promoters V2_0 ON V2_0.promoter_auth_id = followers.promoter_alt_auth_id  "
              + "where  V2_0.municipality_id IS NOT NULL AND followers.promoter_alt_auth_id IS NOT NULL  "
              + " AND  V2_0.municipality_id <2000  AND followers.section_id IS NOT NULL "
              + " AND  V2_0.referent_auth_id = :authIdHeader "
              + "GROUP BY  V2_0.municipality_id, V2_0.referent_auth_id, V2_0.cordinator_zone_auth_id, followers.section_id, followers.section_number",
      nativeQuery = true)
  List<Object[]> getMunicipalityByReferentAndCoordinatorAlt(String authIdHeader);

  @Query(
      value =
          "SELECT  "
              + "   V2_0.municipality_id, "
              + "     COUNT(followers.id) filter (where followers.vote_flag) as progress_mobilization, "
              + "   COUNT(followers.id) as goal_mobilization, "
              + "   V2_0.referent_auth_id, "
              + "   followers.section_number "
              + "FROM followers "
              + "LEFT JOIN promoters V2_0 ON V2_0.promoter_auth_id = followers.promoter_auth_id  "
              + "where  V2_0.municipality_id IS NOT NULL AND followers.promoter_auth_id IS NOT NULL  "
              + " AND  V2_0.municipality_id <2000 "
              + "GROUP BY  V2_0.municipality_id, followers.section_number, V2_0.referent_auth_id",
      nativeQuery = true)
  List<Object[]> getFederalDistrictByReferentMobilization();

  @Query(
      value =
          "SELECT "
              + " followers.municipality_id, "
              + " COUNT ( followers.ID ) FILTER ( WHERE followers.vote_flag ) AS progress_mobilization, "
              + " COUNT ( followers.ID ) AS goal_mobilization, "
              + " promoters.referent_auth_id, "
              + " followers.section_number, "
              + " followers.section_id, "
              + " followers.municipality_name, "
              + " promoters.referent_auth_id, "
              + " followers.federal_district_id, "
              + " followers.federal_district_name, "
              + " followers.local_district_id, "
              + " followers.local_district_name "
              + "FROM "
              + " followers "
              + " LEFT JOIN promoters ON promoters.promoter_auth_id = followers.promoter_auth_id  "
              + "WHERE "
              + " promoters.municipality_id IS NOT NULL  "
              + " AND promoters.promoter_auth_id IS NOT NULL  "
              + " AND followers.municipality_id = :municipalityId "
              + "GROUP BY "
              + " followers.municipality_id, "
              + " followers.municipality_name, "
              + " promoters.referent_auth_id, "
              + " promoters.referent_auth_id, "
              + " followers.section_number, "
              + " followers.section_id, "
              + " followers.federal_district_id, "
              + " followers.federal_district_name, "
              + " followers.local_district_id, "
              + " followers.local_district_name "
              + " ",
      nativeQuery = true)
  List<Object[]> getMunicipalityBySectionAndReferentMobilization(
      @Param("municipalityId") Long municipalityId);

  @Query(
      value =
          "SELECT   "
              + "V2_0.municipality_id,  "
              + "COUNT(followers.id) filter (where followers.vote_flag) as progress_mobilization,  "
              + "COUNT(followers.id) as goal_mobilization,  "
              + "followers.section_number  "
              + "FROM followers  "
              + "LEFT JOIN promoters V2_0 ON V2_0.promoter_auth_id = followers.promoter_auth_id   "
              + "where  V2_0.municipality_id IS NOT NULL  AND followers.promoter_auth_id IS NOT NULL  "
              + "AND  V2_0.municipality_id <2000  "
              + "GROUP BY  V2_0.municipality_id, followers.section_number ",
      nativeQuery = true)
  List<Object[]> getFederalDistrictMobilization();

  @Query(
      value =
          "SELECT   "
              + "followers.federal_district_name,  "
              + "COUNT(followers.id) filter (where followers.vote_flag) as progress_mobilization,  "
              + "COUNT(followers.id) as goal_mobilization  "
              + "FROM followers  "
              + "LEFT JOIN promoters V2_0 ON V2_0.promoter_auth_id = followers.promoter_auth_id   "
              + "where  V2_0.municipality_id IS NOT NULL  AND followers.promoter_auth_id IS NOT NULL  "
              + "AND  V2_0.municipality_id <2000  "
              + "GROUP BY  followers.federal_district_name ",
      nativeQuery = true)
  List<Object[]> getFederalDistrictMobilizationNuevo();

  @Query(
      value =
          "SELECT   "
              + "followers.local_district_name,  "
              + "COUNT(followers.id) filter (where followers.vote_flag) as progress_mobilization,  "
              + "COUNT(followers.id) as goal_mobilization  "
              + "FROM followers  "
              + "LEFT JOIN promoters V2_0 ON V2_0.promoter_auth_id = followers.promoter_auth_id   "
              + "where  V2_0.municipality_id IS NOT NULL  AND followers.promoter_auth_id IS NOT NULL  "
              + "AND  V2_0.municipality_id <2000  "
              + "GROUP BY  followers.local_district_name "
              + " ORDER BY followers.local_district_name::::int ",
      nativeQuery = true)
  List<Object[]> getLocalDistrictMobilizationNuevo();

  @Query(
      value =
          "SELECT  "
              + " V2_0.municipality_id, "
              + " COUNT(followers.id) filter (where followers.vote_flag) as progress_mobilization, "
              + " COUNT(followers.id) as goal_mobilization, "
              + " V2_0.referent_auth_id, "
              + " V2_0.cordinator_zone_auth_id,"
              + " followers.section_id, "
              + " followers.section_number "
              + "FROM followers "
              + "LEFT JOIN promoters V2_0 ON V2_0.promoter_auth_id = followers.promoter_auth_id  "
              + "where  V2_0.municipality_id IS NOT NULL AND followers.promoter_auth_id IS NOT NULL  "
              + " AND  V2_0.municipality_id <2000 "
              + " AND V2_0.municipality_id = :municipalityId "
              + "GROUP BY  V2_0.municipality_id, V2_0.referent_auth_id, V2_0.cordinator_zone_auth_id, followers.section_id, followers.section_number",
      nativeQuery = true)
  List<Object[]> getMunicipality(Integer municipalityId);

  @Query(
      value =
          "SELECT  "
              + " V2_0.municipality_id, "
              + " COUNT(followers.id) filter (where followers.vote_flag) as progress_mobilization, "
              + " COUNT(followers.id) as goal_mobilization, "
              + " V2_0.referent_auth_id, "
              + " V2_0.cordinator_zone_auth_id,"
              + " followers.section_id, "
              + " followers.section_number "
              + "FROM followers "
              + "LEFT JOIN promoters V2_0 ON V2_0.promoter_auth_id = followers.promoter_auth_id  "
              + "where  V2_0.municipality_id IS NOT NULL AND followers.promoter_auth_id IS NOT NULL  "
              + " AND  V2_0.municipality_id <2000 "
              + " AND V2_0.referent_auth_id = :referentId "
              + "GROUP BY  V2_0.municipality_id, V2_0.referent_auth_id, V2_0.cordinator_zone_auth_id, followers.section_id, followers.section_number",
      nativeQuery = true)
  List<Object[]> getReferent(String referentId);

  @Query(
      value =
          "SELECT  "
              + " V2_0.municipality_id, "
              + " COUNT(followers.id) filter (where followers.vote_flag) as progress_mobilization, "
              + " COUNT(followers.id) as goal_mobilization, "
              + " V2_0.referent_auth_id, "
              + " V2_0.cordinator_zone_auth_id, "
              + " followers.section_id, "
              + " followers.section_number "
              + "FROM followers "
              + "LEFT JOIN promoters V2_0 ON V2_0.promoter_auth_id = followers.promoter_alt_auth_id  "
              + "where  V2_0.municipality_id IS NOT NULL AND followers.promoter_alt_auth_id IS NOT NULL  "
              + " AND  V2_0.municipality_id <2000 and V2_0.referent_auth_id IN ('94f8e5f4-bdf6-4914-ada0-a17dd5e4e509','a1121895-32c0-48e4-8e18-f0205506a289', '092978b9-2bd2-4ec0-a992-f5bd43dafc73')  "
              + "GROUP BY  V2_0.municipality_id, V2_0.referent_auth_id, V2_0.cordinator_zone_auth_id, followers.section_id, followers.section_number",
      nativeQuery = true)
  List<Object[]> getReferentAltAdm();

  @Query(
      value =
          "SELECT  "
              + " V2_0.municipality_id, "
              + " COUNT(followers.id) filter (where followers.vote_flag) as progress_mobilization, "
              + " COUNT(followers.id) as goal_mobilization, "
              + " V2_0.referent_auth_id, "
              + " V2_0.cordinator_zone_auth_id, "
              + " followers.section_id, "
              + " followers.section_number "
              + "FROM followers "
              + "LEFT JOIN promoters V2_0 ON V2_0.promoter_auth_id = followers.promoter_alt_auth_id  "
              + "where  V2_0.municipality_id IS NOT NULL AND followers.promoter_alt_auth_id IS NOT NULL  "
              + " AND  V2_0.municipality_id <2000 and V2_0.referent_auth_id = :referentId "
              + "GROUP BY  V2_0.municipality_id, V2_0.referent_auth_id, V2_0.cordinator_zone_auth_id, followers.section_id, followers.section_number",
      nativeQuery = true)
  List<Object[]> getReferentAlt(String referentId);

  @Query(
      value =
          "SELECT  "
              + "V2_0.referent_auth_id, "
              + "COUNT(followers.id) filter (where followers.vote_flag) as progress, "
              + "COUNT(followers.id) as goal "
              + "FROM followers "
              + "LEFT JOIN promoters V2_0 ON V2_0.promoter_auth_id = followers.promoter_alt_auth_id  "
              + "where  V2_0.municipality_id IS NOT NULL AND followers.promoter_alt_auth_id IS NOT NULL  "
              + " AND  V2_0.municipality_id <2000 and V2_0.referent_auth_id IN ('94f8e5f4-bdf6-4914-ada0-a17dd5e4e509','a1121895-32c0-48e4-8e18-f0205506a289', '092978b9-2bd2-4ec0-a992-f5bd43dafc73') "
              + "GROUP BY V2_0.referent_auth_id  ",
      nativeQuery = true)
  List<Object[]> getMobilizationAltAdm();

  @Query(
      value =
          "SELECT  "
              + "V2_0.referent_auth_id, "
              + "COUNT(followers.id) filter (where followers.vote_flag) as progress, "
              + "COUNT(followers.id) as goal "
              + "FROM followers "
              + "LEFT JOIN promoters V2_0 ON V2_0.promoter_auth_id = followers.promoter_alt_auth_id  "
              + "where  V2_0.municipality_id IS NOT NULL AND followers.promoter_alt_auth_id IS NOT NULL  "
              + " AND  V2_0.municipality_id <2000 and V2_0.referent_auth_id =  :referentId "
              + "GROUP BY V2_0.referent_auth_id  ",
      nativeQuery = true)
  List<Object[]> getMobilizationAlt(String referentId);

  @Query(
      value =
          "SELECT   "
              + "                V2_0.municipality_id,  "
              + "                COUNT(followers.id) filter (where followers.vote_flag) as progress_mobilization,  "
              + "                COUNT(followers.id) as goal_mobilization,  "
              + "                V2_0.referent_auth_id,  "
              + "                V2_0.cordinator_zone_auth_id, "
              + "                followers.section_id,  "
              + "                followers.section_number,  "
              + "                followers.federal_district_name "
              + "               FROM followers  "
              + "               LEFT JOIN promoters V2_0 ON V2_0.promoter_auth_id = followers.promoter_auth_id   "
              + "               where  V2_0.municipality_id IS NOT NULL AND followers.promoter_auth_id IS NOT NULL   "
              + "                AND  V2_0.municipality_id <2000  "
              + "                AND followers.federal_district_name = :federalDistrict "
              + "               GROUP BY  V2_0.municipality_id, V2_0.referent_auth_id, V2_0.cordinator_zone_auth_id, followers.section_id, followers.section_number, "
              + "              followers.federal_district_name       ",
      nativeQuery = true)
  List<Object[]> getFederalDistrict(String federalDistrict);

  @Query(
      value =
          "SELECT   "
              + "                V2_0.municipality_id,  "
              + "                COUNT(followers.id) filter (where followers.vote_flag) as progress_mobilization,  "
              + "                COUNT(followers.id) as goal_mobilization,  "
              + "                V2_0.referent_auth_id,  "
              + "                V2_0.cordinator_zone_auth_id, "
              + "                followers.section_id,  "
              + "                followers.section_number,  "
              + "                followers.local_district_name "
              + "               FROM followers  "
              + "               LEFT JOIN promoters V2_0 ON V2_0.promoter_auth_id = followers.promoter_auth_id   "
              + "               where  V2_0.municipality_id IS NOT NULL AND followers.promoter_auth_id IS NOT NULL   "
              + "                AND  V2_0.municipality_id <2000  "
              + "                AND followers.local_district_name = :localDistrict "
              + "               GROUP BY  V2_0.municipality_id, V2_0.referent_auth_id, V2_0.cordinator_zone_auth_id, followers.section_id, followers.section_number, "
              + "              followers.local_district_name       ",
      nativeQuery = true)
  List<Object[]> getLocalDistrict(String localDistrict);

  @Query(
      value =
          " SELECT   "
              + "V2_0.municipality_id,  "
              + "COUNT(followers.id) filter (where followers.vote_flag) as progress_mobilization,  "
              + "COUNT(followers.id) as goal_mobilization,  "
              + "V2_0.referent_auth_id,  "
              + "V2_0.cordinator_zone_auth_id, "
              + "followers.section_id,  "
              + "followers.section_number  "
              + "FROM followers  "
              + "LEFT JOIN promoters V2_0 ON V2_0.promoter_auth_id = followers.promoter_alt_auth_id   "
              + "where  V2_0.municipality_id IS NOT NULL AND followers.promoter_alt_auth_id IS NOT NULL   "
              + "AND  V2_0.municipality_id <2000  "
              + "AND V2_0.referent_auth_id = :referentAltId "
              + "GROUP BY  V2_0.municipality_id, V2_0.referent_auth_id, V2_0.cordinator_zone_auth_id, followers.section_id, followers.section_number ",
      nativeQuery = true)
  List<Object[]> getReferentAlt2(String referentAltId);
}
