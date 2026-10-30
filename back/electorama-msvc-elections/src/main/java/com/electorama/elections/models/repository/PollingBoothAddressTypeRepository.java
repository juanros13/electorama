package com.electorama.elections.models.repository;

import com.electorama.elections.models.entity.PollingBoothAddressTypeEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PollingBoothAddressTypeRepository
    extends JpaRepository<PollingBoothAddressTypeEntity, Long> {

  List<PollingBoothAddressTypeEntity> findAllByOrderByIdAsc();
}
