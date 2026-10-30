package com.electorama.msvc.followers.model.repository;

import com.electorama.msvc.followers.model.entity.FollowerMarkEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface FollowersMarkRepository extends JpaRepository<FollowerMarkEntity, Long> {

  List<FollowerMarkEntity> findByPollingBoothIdIsIn(List<Long> pollingBoothsIds);

  @Transactional
  @Modifying
  @Query(
      "UPDATE FollowerMarkEntity f SET f.markFlag = :markFlag , f.isSynchronized = :isSynchronized WHERE f.id = :id")
  void updateIsSyncronizedById(
      @Param("id") Long followerId,
      @Param("markFlag") boolean markFlag,
      @Param("isSynchronized") boolean isSynchronized);

  @Query(
      value =
          " SELECT  "
              + "follower_mark.polling_booth_id, "
              + "count(*) progress_vote "
              + "FROM follower_mark "
              + "WHERE follower_mark.mark_flag = true "
              + "AND follower_mark.polling_booth_id in (:pollingBoothsIds) "
              + "GROUP BY follower_mark.polling_booth_id",
      nativeQuery = true)
  List<Object[]> getProgressByFollowerMark(@Param("pollingBoothsIds") List<Long> pollingBoothsIds);

  @Query(
      value =
          "SELECT  "
              + "count(*) progress_vote "
              + "FROM follower_mark "
              + "WHERE follower_mark.mark_flag = true "
              + "AND follower_mark.polling_booth_id in (:pollingBoothsIds) ",
      nativeQuery = true)
  Object[] getProgressVote(@Param("pollingBoothsIds") List<Long> pollingBoothsIds);

  @Query(
      value =
          "select municipality_id, municipality, SUM(CASE  "
              + "  WHEN follower_mark.mark_flag = true THEN "
              + "    1 "
              + "  ELSE "
              + "    0 "
              + "END) as num_voters, "
              + "sum(CASE  "
              + "  WHEN follower_mark.mark_flag = false THEN "
              + "    1 "
              + "  ELSE "
              + "    0 "
              + "END) as num_voters_pending "
              + "from follower_mark group by municipality, municipality_id "
              + "order by municipality_id ASC;",
      nativeQuery = true)
  List<Object[]> getVotersByMunicipality();

  @Query(
      value =
          "select federal_district, federal_district_name, SUM(CASE  "
              + " WHEN follower_mark.mark_flag = true THEN "
              + "  1 "
              + " ELSE "
              + "  0 "
              + "END) as num_voters, "
              + "sum(CASE  "
              + " WHEN follower_mark.mark_flag = false THEN "
              + "  1 "
              + " ELSE "
              + "  0 "
              + "END) as num_voters_pending "
              + "from follower_mark group by federal_district, federal_district_name "
              + "order by federal_district ASC;",
      nativeQuery = true)
  List<Object[]> getVotersByFederalDistrict();

  @Query(
      value =
          "select local_district, SUM(CASE  "
              + " WHEN follower_mark.mark_flag = true THEN "
              + "  1 "
              + " ELSE "
              + "  0 "
              + "END) as num_voters, "
              + "sum(CASE  "
              + " WHEN follower_mark.mark_flag = false THEN "
              + "  1 "
              + " ELSE "
              + "  0 "
              + "END) as num_voters_pending "
              + "from follower_mark group by local_district "
              + "order by local_district::::integer ASC;",
      nativeQuery = true)
  List<Object[]> getVotersByLocalDistrict();
}
