package com.electorama.msvc.survey.controller;

import com.electorama.msvc.survey.service.SurveyCatalogsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/catalogs")
public class SurveyCatalogsController {
  @Autowired private SurveyCatalogsService surveyCatalogsService;

  @GetMapping(value = "/getSurveyHousing")
  public ResponseEntity getSurveyHousing() {

    return ResponseEntity.ok(surveyCatalogsService.getHousing());
  }

  @GetMapping(value = "/getSurveyHousingMaterialFloor")
  public ResponseEntity getSurveyHousingMaterialFloor() {

    return ResponseEntity.ok(surveyCatalogsService.getHousingMaterialFloor());
  }

  @GetMapping(value = "/getSurveyHousingMaterialRoof")
  public ResponseEntity getSurveyHousingMaterialRoof() {

    return ResponseEntity.ok(surveyCatalogsService.getHousingMaterialRoof());
  }

  @GetMapping(value = "/getSurveyHousingMaterialWall")
  public ResponseEntity getSurveyHousingMaterialWall() {

    return ResponseEntity.ok(surveyCatalogsService.getHousingMaterialWall());
  }

  @GetMapping(value = "/getSurveyHousingCondition")
  public ResponseEntity getSurveyHousingCondition() {

    return ResponseEntity.ok(surveyCatalogsService.getHousingCondition());
  }

  @GetMapping(value = "/getSurveyHousingDocument")
  public ResponseEntity getSurveyHousingDocument() {

    return ResponseEntity.ok(surveyCatalogsService.getHousingDocument());
  }

  @GetMapping(value = "/getSurveyCulturalActivity")
  public ResponseEntity getSurveyCulturalActivity() {

    return ResponseEntity.ok(surveyCatalogsService.getSurveyCulturalActivity());
  }

  @GetMapping(value = "/getSurveyJob")
  public ResponseEntity getSurveyJob() {

    return ResponseEntity.ok(surveyCatalogsService.getSurveyJob());
  }

  @GetMapping(value = "/getSurveyProfession")
  public ResponseEntity getSurveyProfession() {

    return ResponseEntity.ok(surveyCatalogsService.getSurveyProfession());
  }

  @GetMapping(value = "/getSurveyFederalProgram")
  public ResponseEntity getSurveyFederalProgram() {

    return ResponseEntity.ok(surveyCatalogsService.getSurveyFederalProgram());
  }

  @GetMapping(value = "/getDisability")
  public ResponseEntity getDisability() {

    return ResponseEntity.ok(surveyCatalogsService.getDisability());
  }

  @GetMapping(value = "/getRelationship")
  public ResponseEntity getRelationship() {

    return ResponseEntity.ok(surveyCatalogsService.getRelationship());
  }

  @GetMapping(value = "/getAllIndigenousLanguages")
  public ResponseEntity getAllIndigenousLanguages() {

    return ResponseEntity.ok(surveyCatalogsService.getAllIndigenousLanguages());
  }

  @GetMapping(value = "/getAllOriginsOfDisability")
  public ResponseEntity getAllOriginsOfDisability() {

    return ResponseEntity.ok(surveyCatalogsService.getAllOriginsOfDisability());
  }

  @GetMapping(value = "/getAllDisabilityInformation")
  public ResponseEntity getAllDisabilityInformation() {

    return ResponseEntity.ok(surveyCatalogsService.getAllDisabilityInformation());
  }

  @GetMapping(value = "/getAllTypesOfDisability")
  public ResponseEntity getAllTypesOfDisability() {

    return ResponseEntity.ok(surveyCatalogsService.getAllTypesOfDisability());
  }

  @GetMapping(value = "/getAllCausesOfDisability")
  public ResponseEntity getAllCausesOfDisability() {

    return ResponseEntity.ok(surveyCatalogsService.getAllCausesOfDisability());
  }

  @GetMapping(value = "/getAllTutorsOrAssistants")
  public ResponseEntity getAllTutorsOrAssistants() {

    return ResponseEntity.ok(surveyCatalogsService.getAllTutorsOrAssistants());
  }

  @GetMapping(value = "/getAllRequestingOrAuxiliaryDocuments")
  public ResponseEntity getAllRequestingOrAuxiliaryDocuments() {

    return ResponseEntity.ok(surveyCatalogsService.getAllRequestingOrAuxiliaryDocuments());
  }

  @GetMapping(value = "/getSurveyHousingService")
  public ResponseEntity getSurveyHousingService() {

    return ResponseEntity.ok(surveyCatalogsService.getSurveyHousingService());
  }
}
