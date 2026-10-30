package com.electorama.googlecalendar.model.repository;

import com.electorama.googlecalendar.model.entity.CalendarEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalendarRepository extends JpaRepository<CalendarEntity, Long> {

  List<CalendarEntity> findByAuthId(String authId);

  Optional<CalendarEntity> findByIdAndAuthId(Long id, String authId);
}
