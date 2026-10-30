package com.electorama.msvc.elections.structure.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pollings_representatives_coordinators")
public class PollingRepresentativeCoordinatorEntity {
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(
      name = "polling_representative_general_id",
      referencedColumnName = "id",
      nullable = true)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private PollingRepresentativeEntity pollingRepresentativeGeneral;

  @OneToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(
      name = "polling_representative_coordinator_id",
      referencedColumnName = "id",
      nullable = true)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private PollingRepresentativeEntity pollingRepresentativeCoordinator;
}
