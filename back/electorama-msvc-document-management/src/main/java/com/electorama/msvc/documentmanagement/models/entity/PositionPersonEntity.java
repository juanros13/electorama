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
@Table(name = "position_person")
public class PositionPersonEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "person_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private PersonEntity person;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "position_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private PositionEntity position;

  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private Date created;

  @UpdateTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private Date updated;

  private Date deleted;
}
