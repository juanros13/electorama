package com.electorama.msvc.users.model.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Un rol del realm que da la estructura (V3), con su antigüedad: mayor es más alto y sólo se
 * administran cuentas de roles de menor antigüedad. Lo llena msvc-structure al guardar los niveles
 * de una campaña; los cinco roles heredados vienen sembrados.
 */
@Getter
@Setter
@Entity
@Table(name = "structure_role")
public class StructureRoleEntity {

  @Id private String role;

  private Short seniority;

  @Column(name = "campaign_id")
  private Long campaignId;

  private String description;

  @Column(insertable = false, updatable = false)
  private OffsetDateTime created;
}
