package com.electorama.elections.models.entity;

import jakarta.persistence.*;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(name = "polling_booths_election_association")
public class PollingBoothElectionAssociationEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long pollingBoothId;

  private Long electionId;

  private Boolean isSynchronized;

  private String pathSheet;

  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private Date pathSheetCreated;

  @UpdateTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private Date pathSheetUpdated;

  private Integer isValidatedSheet;
}
