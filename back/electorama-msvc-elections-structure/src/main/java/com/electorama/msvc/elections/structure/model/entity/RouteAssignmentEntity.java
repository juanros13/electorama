package com.electorama.msvc.elections.structure.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "routes_assignment")
public class RouteAssignmentEntity {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "route_id", referencedColumnName = "id", nullable = true)
  @JsonIgnoreProperties({
    "routeAssignment",
    "route_assignment_id",
    "hibernateLazyInitializer",
    "handler"
  })
  private RouteEntity route;

  private Long pollingBoothId;
}
