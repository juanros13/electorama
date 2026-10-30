package com.electorama.msvc.elections.structure.model.repository;

import com.electorama.msvc.elections.structure.model.entity.PollingRepresentativeCoordinatorEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PollingRepresentativeCoordinatorRepository
    extends JpaRepository<PollingRepresentativeCoordinatorEntity, Long> {

  Optional<PollingRepresentativeCoordinatorEntity> findByPollingRepresentativeCoordinator_Id(
      Long id);

  @Query(
      value =
          "select "
              + "    crg. * "
              + "    from "
              + "        pollings_representatives_coordinators crg  "
              + "LEFT JOIN pollings_representatives rg ON rg.id = crg.polling_representative_coordinator_id "
              + "    where crg.polling_representative_coordinator_id = :idPollingRepresentativeGeneral",
      nativeQuery = true)
  Optional<PollingRepresentativeCoordinatorEntity> findByCordinator(
      @Param("idPollingRepresentativeGeneral") Long idPollingRepresentativeGeneral);
}
