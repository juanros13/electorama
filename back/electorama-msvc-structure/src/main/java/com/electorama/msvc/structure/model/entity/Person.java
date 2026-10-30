package com.electorama.msvc.structure.model.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * El ser humano, una sola vez. En el modelo viejo la misma persona podía existir a la vez en {@code
 * users}, {@code promoters} y {@code followers} sin vínculo entre las tres filas.
 *
 * <p>Que alguien sea convencido, promotor o las dos cosas no es un atributo de la persona: es una
 * relación con vigencia ({@link Membership} / {@link Assignment}).
 */
@Entity
@Table(name = "person")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Person {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Llave natural y de deduplicación. Única cuando existe. */
  private String curp;

  /** No es único: hay credenciales repetidas entre personas con CURP distinta. */
  private String ine;

  /** Nullable: hay cuentas cuyo perfil vive en Keycloak y no en la base. */
  private String names;

  @Column(name = "last_name")
  private String lastName;

  @Column(name = "sur_name")
  private String surName;

  @Column(name = "birthdate")
  private LocalDate birthdate;

  private String sex;
  private String cellPhoneNumber;
  private String email;
  private String street;

  @Column(name = "external_number")
  private String externalNumber;

  @Column(name = "internal_number")
  private String internalNumber;

  private String neighborhood;

  @Column(name = "zip_code")
  private String zipCode;

  /** De qué tabla del modelo viejo vino, para reconciliar después del backfill. */
  @Column(name = "legacy_source")
  private String legacySource;

  @Column(name = "legacy_id")
  private Long legacyId;

  /** UUID de Keycloak. Sólo lo tienen los operadores; los convencidos no. */
  @Column(name = "auth_id")
  private String authId;

  @Column(name = "created", insertable = false, updatable = false)
  private OffsetDateTime created;

  @Column(name = "updated")
  private OffsetDateTime updated;
}
