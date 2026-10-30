package com.electorama.googlecalendar.model.rest.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
  private Long id;

  private String email;

  private String password;

  private String authId;

  private String status;

  private String firstName;

  private String lastName;

  private String avatar;

  private String tokenGoogleCalendar;
  private String idGoogleCalendar;
}
