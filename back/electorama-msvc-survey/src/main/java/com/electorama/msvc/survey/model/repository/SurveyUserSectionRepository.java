package com.electorama.msvc.survey.model.repository;

import com.electorama.msvc.survey.model.entity.SurveyUserSectionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyUserSectionRepository extends JpaRepository<SurveyUserSectionEntity, Long> {

  List<SurveyUserSectionEntity> findBySurveyUserId(Long id);

  Optional<SurveyUserSectionEntity> findBySurveyUserIdAndSectionNumber(
      Long userId, Integer sectionNumber);

  List<SurveyUserSectionEntity> findBySectionNumber(Integer sectionNumber);

  Optional<SurveyUserSectionEntity> deleteBySectionNumber(Integer sectionNumber);
}
