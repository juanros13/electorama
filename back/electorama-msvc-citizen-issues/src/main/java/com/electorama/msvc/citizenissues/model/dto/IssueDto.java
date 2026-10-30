package com.electorama.msvc.citizenissues.model.dto;

import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IssueDto {

  public IssueDto(
      Long id,
      String invoice,
      Date registrationDate,
      Date deletedDate,
      String fullName,
      String phone,
      Integer municipalityId,
      String municipalityName,
      Integer localityId,
      String localityName,
      String localityOther) {
    this.id = id;
    this.invoice = invoice;
    this.registrationDate = registrationDate;
    this.deletedDate = deletedDate;
    this.fullName = fullName;
    this.phone = phone;
    this.municipalityId = municipalityId;
    this.municipalityName = municipalityName;
    this.localityId = localityId;
    this.localityName = localityName;
    this.localityOther = localityOther;
  }

  private Long id;

  private String invoice;

  private Date registrationDate;

  private Date deletedDate;

  private String fullName;

  private String phone;

  private Integer municipalityId;

  private String municipalityName;

  private Integer localityId;

  private String localityName;

  private String localityOther;

  private String userCreateId;

  List<IssueDetailDto> issueDetailEntities;
}
