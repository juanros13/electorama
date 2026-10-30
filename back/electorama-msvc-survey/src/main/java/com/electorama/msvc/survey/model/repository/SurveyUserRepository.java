package com.electorama.msvc.survey.model.repository;

import com.electorama.msvc.survey.model.entity.SurveyUserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyUserRepository extends JpaRepository<SurveyUserEntity, Long> {

  Optional<SurveyUserEntity> findByAuthId(String authId);

  Optional<SurveyUserEntity> findByUserName(String userName);

  List<SurveyUserEntity> findBySurveyMunicipalityAuthId(String authId);

  Optional<SurveyUserEntity> findByAuthIdAndSurveyUserSection_SectionNumber(
      String authId, Integer sectionNumber);
}
