package com.electorama.elections.models.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "elections_voter_list")
public class ElectionVoterListEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String userAuthId;

  private Integer electionId;

  private Integer voterList;
}
