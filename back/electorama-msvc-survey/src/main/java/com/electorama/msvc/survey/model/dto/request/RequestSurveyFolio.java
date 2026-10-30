package com.electorama.msvc.survey.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class RequestSurveyFolio {
  private MultipartFile pathPhotoIdentificationAdverse;
  private MultipartFile pathPhotoIdentificationReverse;
  private MultipartFile pathPhotoFacadeQr;
  @NotBlank private String deviceId;
  @NotBlank private String folio;
  @NotBlank private String folioHouse;
  @NotBlank private String surveyDate;
  @NotBlank private String latitude;
  @NotBlank private String longitude;
  @NotBlank private String userAuthId;
  @NotBlank private String sectionId;
  @NotBlank private String sectionNumber;
  @NotBlank private String municipality;
  @NotBlank private Integer municipalityId;
}
