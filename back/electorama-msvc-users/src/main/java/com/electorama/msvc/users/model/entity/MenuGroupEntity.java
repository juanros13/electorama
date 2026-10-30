package com.electorama.msvc.users.model.entity;

import com.electorama.msvc.users.model.dto.TypeMenu;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "menus_groups")
public class MenuGroupEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long pk;

  private String id;

  private String title;
  private String subtitle;
  private String icon;
  private String link;

  private Integer order;

  @Enumerated(EnumType.STRING)
  private TypeMenu type;
}
