package com.electorama.googlecalendar.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "events")
public class EventEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull private Integer calendarId;

  @NotNull private Integer recurringEventId;

  @NotNull private Boolean isFirstInstance;

  @NotBlank private String title;

  @NotBlank private String description;

  @Column(name = "start_date")
  private Date start;

  @Column(name = "end_date")
  private Date end;

  private Boolean allDay;

  private String recurrence;

  @NotBlank private String authId;
}
