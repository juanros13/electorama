package com.electorama.msvc.elections.structure.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "routes")
public class RouteEntity {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private Integer key;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "route")
  @JsonIgnoreProperties(
      value = {"route", "hibernateLazyInitializer", "handler"},
      allowSetters = false)
  private List<RouteAssignmentEntity> routeAssignment;

  @OneToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(
      name = "polling_representative_general_id",
      referencedColumnName = "id",
      nullable = true)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private PollingRepresentativeEntity pollingRepresentativeGeneral;
}
