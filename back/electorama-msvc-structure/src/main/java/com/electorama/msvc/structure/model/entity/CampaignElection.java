package com.electorama.msvc.structure.model.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Un cargo en disputa de la campaña: qué se elige, en qué ámbito y cuántos (V6). */
@Entity
@Table(name = "campaign_election")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignElection {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "campaign_id", nullable = false)
  private Long campaignId;

  /** PRESIDENT, SENATOR, FEDERAL_DEPUTY, GOVERNOR, LOCAL_DEPUTY o MAYOR. */
  @Column(nullable = false)
  private String office;

  /** STATE, FEDERAL_DISTRICT, LOCAL_DISTRICT o MUNICIPALITY. */
  @Column(nullable = false)
  private String scope;

  @Column(nullable = false)
  private Integer seats;

  @Column(name = "election_day", nullable = false)
  private LocalDate electionDay;
}
