package com.electorama.msvc.survey.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class RequestSurveyAnexAllFile {
  private MultipartFile identificationApplicantFile;
  private MultipartFile birthCertificateApplicantFile;
  private MultipartFile curpApplicantFile;
  private MultipartFile addressApplicantFile;
  private MultipartFile identificationAssistantFile;
  private MultipartFile birthCertificateAssistantFile;
  private MultipartFile curpAssistantFile;
  private MultipartFile addressAssistantFile;

  @NotBlank private Long surveyPersonId;
}
