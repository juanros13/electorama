package com.electorama.msvc.followers.service;

import com.electorama.msvc.followers.model.schedule.ScheduleConfig;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScheduleService {

  @Autowired private ScheduleConfig scheduleConfig;

  public boolean isExecutionAllowed() {
    LocalDateTime now = LocalDateTime.now();
    DayOfWeek currentDay = now.getDayOfWeek();
    LocalTime currentTime = now.toLocalTime();

    boolean isDayAllowed = currentDay.getValue() == scheduleConfig.getDay();
    boolean isTimeAllowed =
        !currentTime.isBefore(scheduleConfig.getStartTime())
            && !currentTime.isAfter(scheduleConfig.getEndTime());

    return isDayAllowed && isTimeAllowed;
  }
}
