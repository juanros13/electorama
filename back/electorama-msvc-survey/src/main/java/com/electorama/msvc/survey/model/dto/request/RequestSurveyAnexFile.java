package com.electorama.msvc.survey.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class RequestSurveyAnexFile {
  @NotBlank private MultipartFile file;
  @NotBlank private String category;
  @NotBlank private Long surveyPersonId;
}
