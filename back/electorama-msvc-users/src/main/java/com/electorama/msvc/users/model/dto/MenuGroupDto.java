package com.electorama.msvc.users.model.dto;

import lombok.Data;

/** Una entrada del menú, plana: el front sólo usa link, título, icono y orden. */
@Data
public class MenuGroupDto {
  private Long pk;
  private String id;
  private String title;
  private String subtitle;
  private String icon;
  private TypeMenu type;
  private String link;
  private Integer order;
}
