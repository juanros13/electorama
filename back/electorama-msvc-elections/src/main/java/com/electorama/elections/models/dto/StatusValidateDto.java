package com.electorama.elections.models.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusValidateDto {

  private Long id;

  private String name;
}
