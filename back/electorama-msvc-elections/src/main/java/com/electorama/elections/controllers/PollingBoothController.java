package com.electorama.elections.controllers;

import com.electorama.elections.models.dto.*;
import com.electorama.elections.services.PollingBoothService;
import com.electorama.elections.services.ScheduleService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/pollingBooth")
public class PollingBoothController {

  @Autowired PollingBoothService pollingBoothService;
  @Autowired private ScheduleService scheduleService;

  @GetMapping()
  public ResponseEntity<?> list(
      @RequestParam(defaultValue = "0") Integer pageNo,
      @RequestParam(defaultValue = "30") Integer pageSize,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "desc") String orderBy,
      @RequestParam(defaultValue = "") String search,
      @RequestParam(defaultValue = "0") Integer status,
      @RequestParam(defaultValue = "0") Integer categoryId,
      @RequestParam(defaultValue = "0") Integer typeId,
      @RequestParam(defaultValue = "0") Integer addressTypeId,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {

    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "pollingBooths",
          pollingBoothService.list(
              pageNo,
              pageSize,
              sortBy,
              orderBy,
              search,
              status,
              categoryId,
              typeId,
              addressTypeId,
              authIdHeader));

      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/findAllPollingBoothImproved")
  public ResponseEntity<?> findAllPollingBoothImproved() {

    Map<String, Object> response = new HashMap<>();
    try {
      response.put("findAllPollingBooth", pollingBoothService.findAllPollingBoothImproved());
      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/catPollingBoothStatus")
  public ResponseEntity<?> catPollingBoothStatus() {

    Map<String, Object> response = new HashMap<>();
    try {
      response.put("catPollingBoothStatus", pollingBoothService.catPollingBoothStatus());
      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/catPollingBoothAddressType")
  public ResponseEntity<?> catPollingBoothAddressType() {

    Map<String, Object> response = new HashMap<>();
    try {
      response.put("catPollingBoothAddressType", pollingBoothService.catPollingBoothAddressType());
      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/catPollingBoothCategory")
  public ResponseEntity<?> catPollingBoothCategory() {

    Map<String, Object> response = new HashMap<>();
    try {
      response.put("catPollingBoothCategory", pollingBoothService.catPollingBoothCategory());
      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/catPollingBoothType")
  public ResponseEntity<?> catPollingBoothType() {

    Map<String, Object> response = new HashMap<>();
    try {
      response.put("catPollingBoothType", pollingBoothService.catPollingBoothType());
      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/findByFederalAndLocalDistrictPaging")
  public ResponseEntity<?> findByFederalAndLocalDistrictPaging(
      @RequestParam(defaultValue = "0") Integer pageNo,
      @RequestParam(defaultValue = "30") Integer pageSize,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "desc") String orderBy,
      @RequestParam(defaultValue = "") String search,
      @RequestParam(defaultValue = "") Integer federalDistrictId,
      @RequestParam(defaultValue = "") Integer localDistrictId,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "findByFederalAndLocalDistrictPaging",
          pollingBoothService.findByFederalAndLocalDistrictPaging(
              pageNo,
              pageSize,
              sortBy,
              orderBy,
              search,
              federalDistrictId,
              localDistrictId,
              authIdHeader));

      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/findByFederalDistrict/{federalDistricId}")
  public ResponseEntity<?> findByFederalDistrict(@PathVariable Integer federalDistricId) {

    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "findByFederalDistrict", pollingBoothService.findByFederalDistrict(federalDistricId));
      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/findByMunicipalityPaging")
  public ResponseEntity<?> findByMunicipalityPaging(
      @RequestParam(defaultValue = "0") Integer pageNo,
      @RequestParam(defaultValue = "30") Integer pageSize,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "desc") String orderBy,
      @RequestParam(defaultValue = "") String search,
      @RequestParam(defaultValue = "") Integer municipalityId,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "findByMunicipalityPaging",
          pollingBoothService.findByMunicipalityPaging(
              pageNo, pageSize, sortBy, orderBy, search, municipalityId, authIdHeader));

      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/findByMunicipality/{municipalityId}")
  public ResponseEntity<?> findByMunicipality(@PathVariable Integer municipalityId) {

    Map<String, Object> response = new HashMap<>();
    try {
      response.put("findByMunicipality", pollingBoothService.findByMunicipality(municipalityId));
      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping("/v2.0/changeStatus")
  public ResponseEntity<?> changeStatus(
      @RequestBody PollingBoothChangeStatusDto datesRequestDto,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    JSONObject jsonResponse = new JSONObject();

    if (scheduleService.isExecutionAllowed()) {
      Optional<PollingBoothDto> o =
          pollingBoothService.byId(datesRequestDto.getPollingBootId(), authIdHeader);
      try {

        if (o.isPresent()) {
          PollingBoothDto pollingBoothDB = o.get();
          if (datesRequestDto.getStatus() != null && datesRequestDto.getStatus().getId() == 1) {
            pollingBoothDB.setPollingBoothStatus(datesRequestDto.getStatus());
            if (pollingBoothDB.getDateStatusOpen() == null) {
              pollingBoothDB.setDateStatusOpen(datesRequestDto.getDateStatusOpen());
            }

          } else if (datesRequestDto.getStatus() != null
              && datesRequestDto.getStatus().getId() == 2) {
            pollingBoothDB.setPollingBoothStatus(datesRequestDto.getStatus());
            pollingBoothDB.setDateClosed(datesRequestDto.getDateClosed());
          }

          return ResponseEntity.status(HttpStatus.CREATED)
              .body(pollingBoothService.save(pollingBoothDB, authIdHeader));
        } else {
          return ResponseEntity.notFound().build();
        }
      } catch (EntityNotFoundException e) {
        return ResponseEntity.notFound().build();
      }

    } else {
      // No ejecutar la lógica del negocio
      jsonResponse.put("message", "Tarea no permitida en este horario.");
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(jsonResponse);
    }
  }

  @GetMapping("/getReportMunicipalities")
  public ResponseEntity<?> getReportMunicipalities() {

    List<ResponseMunicipalityReportDto> municipalitiesReport =
        pollingBoothService.getReportMunicipalitiesByEntity();

    return ResponseEntity.ok(municipalitiesReport);
  }

  @GetMapping("/getReportSectionsByMunicipalityId/{municipalityId}")
  public ResponseEntity<?> getReportSectionByMunicipalityId(@PathVariable Long municipalityId) {

    List<ResponseSectionReportDto> municipalitiesReport =
        pollingBoothService.getReportSectionByMunicipalityId(municipalityId);

    return ResponseEntity.ok(municipalitiesReport);
  }

  @GetMapping("/getReportSectionsByDistrictFederalId/{districtFederalId}")
  public ResponseEntity<?> getReportSectionByDistrictFederalId(
      @PathVariable Long districtFederalId) {

    List<ResponseSectionReportByDistrictFederalDto> municipalitiesReport =
        pollingBoothService.getReportSectionByDistrictFederalId(districtFederalId);

    return ResponseEntity.ok(municipalitiesReport);
  }

  @GetMapping("/{pollingBoothId}")
  public ResponseEntity<?> getById(@PathVariable Long pollingBoothId) {

    Optional<PollingBoothDto> pollingBoothDtoOptional =
        pollingBoothService.findById(pollingBoothId);
    if (pollingBoothDtoOptional.isPresent()) {
      return ResponseEntity.ok(pollingBoothDtoOptional.get());
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/getPollingsBoothsByFederalDistrict")
  public ResponseEntity<?> getPollingsBoothsByFederalDistrict(
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getPollingsBoothsByFederalDistrict",
          pollingBoothService.getPollingsBoothsByFederalDistrict());
      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/getPollingsBoothsByLocalDistrict")
  public ResponseEntity<?> getPollingsBoothsByLocalDistrict(
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getPollingsBoothsByLocalDistrict",
          pollingBoothService.getPollingsBoothsByLocalDistrict());
      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/getPollingsBoothsByMunicipality")
  public ResponseEntity<?> getPollingsBoothsByMunicipality(
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getPollingsBoothsByMunicipality", pollingBoothService.getPollingsBoothsByMunicipality());
      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/getReportSectionByDistrictLocalId/{localDistrictId}")
  public ResponseEntity<?> getReportSectionByDistrictLocalId(
      @PathVariable Long localDistrictId, @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getReportSectionByDistrictLocalId",
          pollingBoothService.getReportSectionByDistrictLocalId(localDistrictId));
      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/getPollingBoothBySection/{sectionNumber}")
  public ResponseEntity<?> getPollingBoothBySection(
      @PathVariable Long sectionNumber, @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getPollingBoothBySection", pollingBoothService.getPollingBoothBySection(sectionNumber));
      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/getPollingBooths")
  public ResponseEntity<?> getPollingBooths(
      @RequestParam(defaultValue = "0") Integer pageNo,
      @RequestParam(defaultValue = "30") Integer pageSize,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "desc") String orderBy,
      @RequestParam(defaultValue = "") Long municipalityId,
      @RequestParam(defaultValue = "") Integer sectionNum,
      @RequestParam(defaultValue = "") Long federalDistrictId,
      @RequestParam(defaultValue = "") Long localDistrictId,
      @RequestParam(defaultValue = "") Long electionTypeId,
      @RequestParam(defaultValue = "") Long electionPositionId,
      @RequestParam(defaultValue = "") Long statusValidateId,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    Map<String, Object> response = new HashMap<>();
    try {
      //  if (servletRequest.isUserInRole("CORDINATOR_MUNICIPAL")) {
      response.put(
          "getPollingBooths",
          pollingBoothService.getPollingBooths(
              pageNo,
              pageSize,
              sortBy,
              orderBy,
              municipalityId,
              sectionNum,
              federalDistrictId,
              localDistrictId,
              electionTypeId,
              electionPositionId,
              statusValidateId));
      //   }
      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/getStatusValidate")
  public ResponseEntity<?> getStatusValidate(@RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    JSONObject jsonResponse = new JSONObject();
    try {
      response.put("statusValidateCat", pollingBoothService.getAllStatusValidate());
      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/getPollingBoothsStatus")
  public ResponseEntity<?> getPollingBoothsStattus(
      @RequestParam(defaultValue = "0") Integer pageNo,
      @RequestParam(defaultValue = "30") Integer pageSize,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "desc") String orderBy,
      @RequestParam(defaultValue = "") Long municipalityId,
      @RequestParam(defaultValue = "") Integer sectionNum,
      @RequestParam(defaultValue = "") Long federalDistrictId,
      @RequestParam(defaultValue = "") Long localDistrictId,
      @RequestParam(defaultValue = "") Long statusId,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    Map<String, Object> response = new HashMap<>();
    try {
      //  if (servletRequest.isUserInRole("CORDINATOR_MUNICIPAL")) {
      response.put(
          "getPollingBooths",
          pollingBoothService.getPollingBoothsStatus(
              pageNo,
              pageSize,
              sortBy,
              orderBy,
              municipalityId,
              sectionNum,
              federalDistrictId,
              localDistrictId,
              statusId));
      //   }
      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/getCountPollingsBooths")
  public ResponseEntity<?> getCountPollingsBooths(
      @RequestParam(defaultValue = "") Long municipalityId,
      @RequestParam(defaultValue = "") Integer sectionNum,
      @RequestParam(defaultValue = "") Long federalDistrictId,
      @RequestParam(defaultValue = "") Long localDistrictId,
      @RequestParam(defaultValue = "") Long statusId,
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getCountPollingsBooths",
          pollingBoothService.getCountPollingsBooths(
              municipalityId, sectionNum, federalDistrictId, localDistrictId, statusId));
      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/getValideSheet")
  public ResponseEntity<?> getValideSheet(
      @RequestParam(defaultValue = "") Long municipalityId,
      @RequestParam(defaultValue = "") Integer sectionNum,
      @RequestParam(defaultValue = "") Long federalDistrictId,
      @RequestParam(defaultValue = "") Long localDistrictId,
      @RequestParam(defaultValue = "") Long statusValidateId,
      @RequestParam(defaultValue = "") Long electionPositionId,
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getValideSheet",
          pollingBoothService.getValideSheet(
              municipalityId,
              sectionNum,
              federalDistrictId,
              localDistrictId,
              statusValidateId,
              electionPositionId));
      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
