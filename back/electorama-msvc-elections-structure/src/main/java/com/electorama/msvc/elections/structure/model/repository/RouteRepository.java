package com.electorama.msvc.elections.structure.model.repository;

import com.electorama.msvc.elections.structure.model.entity.RouteEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository extends JpaRepository<RouteEntity, Long> {

  Optional<RouteEntity> findByPollingRepresentativeGeneralId(Long idPollingRepresentativeGeneral);

  @Query(
      value =
          "select "
              + "    route. * "
              + "    from "
              + "        routes route  "
              + "LEFT JOIN routes_assignment assigment ON assigment.route_id = route.id "
              + "    where assigment.polling_booth_id = :idPollingBooth",
      nativeQuery = true)
  Optional<RouteEntity> findByPollingBooth(@Param("idPollingBooth") Long idPollingBooth);

  Optional<RouteEntity> findByPollingRepresentativeGeneral_AuthId(
      String PollingRepresentativeGeneralAuthId);
}
