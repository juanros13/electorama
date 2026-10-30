package com.electorama.msvc.followers.model.dto;

import lombok.Data;

@Data
public class FollowersListDto {
  private Long id;
  private String names;
  private String lastName;
  private String surName;
  private String curp;
  private String ine;
  private String neighborhood;
  private String sectionNumber;
  private Long politicalPartyId;
  private Integer markNumber;
  private boolean voteFlag;
  private String fullName;
}
