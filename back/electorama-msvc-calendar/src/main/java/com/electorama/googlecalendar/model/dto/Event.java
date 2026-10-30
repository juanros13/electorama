package com.electorama.googlecalendar.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import lombok.Data;

@Data
public class Event {

  private Long id;

  @NotNull private Integer calendarId;

  @NotNull private Integer recurringEventId;

  @NotNull private Boolean isFirstInstance;

  @NotBlank private String title;

  @NotBlank private String description;

  private Date start;

  private Date end;

  private Boolean allDay;

  private String recurrence;
}
