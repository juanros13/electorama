package com.electorama.msvc.visits.model.repository;

import com.electorama.msvc.visits.model.entity.VisitedPeopleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitedPeopleRepository extends JpaRepository<VisitedPeopleEntity, Long> {

  VisitedPeopleEntity findByUuid(String Uuid);

  Page<VisitedPeopleEntity>
      findByVisitedEntity_CoordinateZoneIdAndPhoneOrVisitedEntity_CoordinateZoneIdOrVisitedEntity_SeccionId(
          String authIdHeader,
          String phone,
          String cordianteZoneId,
          Long seccionId,
          Pageable pageable);

  Page<VisitedPeopleEntity> findByVisitedEntity_CoordinateZoneIdOrVisitedEntity_ReferenceAuthId(
      String authIdHeader, String authIdHeader2, Pageable pageable);

  Page<VisitedPeopleEntity> findByVisitedEntity_CoordinateZoneIdAndPhoneOrNamesOrLastNameOrSurName(
      String authIdHeader,
      String phone,
      String name,
      String lastName,
      String surName,
      Pageable pageable);

  @Query(
      value =
          "select "
              + "    v1_0. * "
              + "    from "
              + "        visited_people v1_0  "
              + "    left join "
              + "        visited v2_0  "
              + "            on v2_0.id=v1_0.visited_id  "
              + "    where "
              + "        ( v2_0.coordinate_zone_id= :authIdHeader or v2_0.reference_auth_id= :authIdHeader)"
              + "        and (v1_0.phone= :phone "
              + "        or CONCAT (v1_0.names, ' ', v1_0.last_name, ' ', v1_0.sur_name) LIKE CONCAT('%',:name,'%'))",
      nativeQuery = true)
  Page<VisitedPeopleEntity> findByParametros(
      @Param("authIdHeader") String authIdHeader,
      @Param("phone") String phone,
      @Param("name") String name,
      Pageable pageable);

  @Query(
      value =
          "select "
              + "    v1_0. * "
              + "    from "
              + "        visited_people v1_0  "
              + "    left join "
              + "        visited v2_0  "
              + "            on v2_0.id=v1_0.visited_id  "
              + "    where "
              + "        v1_0.phone= :phone "
              + "        or CONCAT (v1_0.names, ' ', v1_0.last_name, ' ', v1_0.sur_name) LIKE CONCAT('%',:name,'%')",
      nativeQuery = true)
  Page<VisitedPeopleEntity> findAllByParametros(
      @Param("phone") String phone, @Param("name") String name, Pageable pageable);
}
