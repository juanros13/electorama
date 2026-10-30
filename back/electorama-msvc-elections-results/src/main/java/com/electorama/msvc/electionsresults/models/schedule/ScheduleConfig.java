package com.electorama.msvc.electionsresults.models.schedule;

import java.time.LocalTime;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "schedule")
public class ScheduleConfig {
  private LocalTime startTime;
  private LocalTime endTime;
  private Integer day;

  // Getters y Setters
  public LocalTime getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = LocalTime.parse(startTime);
  }

  public LocalTime getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = LocalTime.parse(endTime);
  }

  public Integer getDay() {
    return day;
  }

  public void setDay(Integer day) {
    this.day = day;
  }
}
