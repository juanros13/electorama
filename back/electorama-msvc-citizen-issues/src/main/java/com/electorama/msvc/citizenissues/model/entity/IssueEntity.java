package com.electorama.msvc.citizenissues.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "issues")
public class IssueEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String invoice;

  private Date registrationDate;

  private Date deletedDate;

  private String fullName;

  private String phone;

  private Integer municipalityId;

  private Integer localityId;

  private String localityOther;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "issue", cascade = CascadeType.MERGE)
  @JsonIgnoreProperties(
      value = {"issue", "hibernateLazyInitializer", "handler"},
      allowSetters = true)
  private List<IssueDetailEntity> issueDetailEntities;
}
