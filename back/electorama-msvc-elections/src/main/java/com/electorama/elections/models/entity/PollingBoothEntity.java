package com.electorama.elections.models.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "polling_booths")
public class PollingBoothEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Integer seccionNumber;

  private Integer seccionId;

  private String pollingBoothId;

  private String state;

  private Integer stateId;

  private String municipality;

  private Integer municipalityId;

  private Integer federalDistrict;

  private String federalDistrictName;

  private Integer localDistrict;

  private String address;

  private String referenceAddress;

  private Integer votersRegistry;

  private Integer votersList;

  private String approvedBoard;

  private String approvedCommittee;

  private String pollingBoothKey;

  private String pollingBoothNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "polling_booth_address_type_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private PollingBoothAddressTypeEntity pollingBoothAddressType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "polling_booth_type_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private PollingBoothTypeEntity pollingBoothType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "polling_booth_category_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private PollingBoothCategoryEntity pollingBoothCategory;

  private LocalDateTime dateStatusOpen;
  private LocalDateTime dateClosed;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "polling_booth_status_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private PollingBoothStatusEntity pollingBoothStatus;

  private String latitude;
  private String longitude;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "pollingBoothId", cascade = CascadeType.MERGE)
  @JsonIgnoreProperties(
      value = {"issues", "hibernateLazyInitializer", "handler"},
      allowSetters = true)
  private List<PollingBoothIssuesEntity> pollingBoothIssuesList;
}
