package com.electorama.msvc.documentmanagement.models.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(name = "position")
public class PositionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  private String positionKey;

  private Integer statusId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "departament_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private DepartamentEntity departament;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "position_type_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private PositionTypeEntity positionType;

  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private Date created;

  @UpdateTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private Date updated;

  private Date deleted;
}
