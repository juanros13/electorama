package com.electorama.geography.model.repository;

import com.electorama.geography.model.entity.PostalCodeByMunicipalityEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostalCodeByMunicipalityRepository
    extends JpaRepository<PostalCodeByMunicipalityEntity, Long> {

  List<PostalCodeByMunicipalityEntity> findByMunicipioId(Long municipalityId);
}
