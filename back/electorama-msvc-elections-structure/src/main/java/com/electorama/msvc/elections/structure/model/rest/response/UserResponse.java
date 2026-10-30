package com.electorama.msvc.elections.structure.model.rest.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
  private String firstName;
  private String lastName;
  private List<AccountResponse> bankAccounts;
  private String identificationNumber;
  private Integer id;
  private String email;
}
