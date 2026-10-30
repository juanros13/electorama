package com.electorama.elections.controllers;

import com.electorama.elections.models.dto.PollingBoothIssuesDescriptionDto;
import com.electorama.elections.models.dto.PollingBoothIssuesDto;
import com.electorama.elections.models.dto.PollingBoothsIdsDto;
import com.electorama.elections.services.PollingBoothIssuesService;
import com.electorama.elections.services.ScheduleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/pollingBoothIssues")
public class PollingBoothIssuesController {

  @Autowired PollingBoothIssuesService pollingBoothIssuesService;
  @Autowired private ScheduleService scheduleService;

  @GetMapping("/getIssuesByPollingBooth/{pollingBoothId}")
  public ResponseEntity<?> getIssuesByPollingBooth(
      @PathVariable Long pollingBoothId, @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getIssuesByPollingBooth",
          pollingBoothIssuesService.getIssuesByPollingBooth(pollingBoothId));
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

  @PostMapping("/v2.0/addIssue")
  public ResponseEntity addIssue(
      @Valid @RequestBody PollingBoothIssuesDto request,
      BindingResult result,
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    JSONObject jsonResponse = new JSONObject();
    if (scheduleService.isExecutionAllowed()) {
      if (result.hasErrors()) {
        return validar(result);
      }
      log.info("Creating follower with {}", request.toString());
      PollingBoothIssuesDto issue = new PollingBoothIssuesDto();
      try {
        issue = pollingBoothIssuesService.addIssue(request, authIdHeader);

      } catch (Exception e) {
        return new ResponseEntity<>(e, HttpStatus.CONFLICT);
      }
      return ResponseEntity.ok(issue);
    } else {
      // No ejecutar la lógica del negocio
      jsonResponse.put("message", "Tarea no permitida en este horario.");
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(jsonResponse);
    }
  }

  private ResponseEntity<Map<String, String>> validar(BindingResult result) {
    Map<String, String> errores = new HashMap<>();
    result
        .getFieldErrors()
        .forEach(
            err -> {
              errores.put(
                  err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
            });
    return ResponseEntity.badRequest().body(errores);
  }

  @GetMapping("/v2.0/getIssuesByPollingBootList")
  public ResponseEntity<?> getIssuesByPollingBootList(
      @RequestBody PollingBoothsIdsDto pollingBoothsIdsDto,
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getIssuesByPollingBootList",
          pollingBoothIssuesService.getIssuesByPollingBootList(
              pollingBoothsIdsDto.getPollingBoothsIds()));
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

  @GetMapping("/v2.0/getCategory")
  public ResponseEntity<?> getCategory(@RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put("getIssuesByPollingBooth", pollingBoothIssuesService.getCategory());
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

  @GetMapping()
  public ResponseEntity<?> list(
      @RequestParam(defaultValue = "0") Integer pageNo,
      @RequestParam(defaultValue = "30") Integer pageSize,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "desc") String orderBy,
      @RequestParam(defaultValue = "") Long municipalityId,
      @RequestParam(defaultValue = "") Integer sectionNum,
      @RequestParam(defaultValue = "") Long federalDistrictId,
      @RequestParam(defaultValue = "") Long localDistrictId,
      @RequestParam(defaultValue = "") Long subCategoryId,
      @RequestParam(defaultValue = "") Long typeId,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    Map<String, Object> response = new HashMap<>();
    try {
      //  if (servletRequest.isUserInRole("CORDINATOR_MUNICIPAL")) {
      response.put(
          "list",
          pollingBoothIssuesService.list(
              pageNo,
              pageSize,
              sortBy,
              orderBy,
              municipalityId,
              sectionNum,
              federalDistrictId,
              localDistrictId,
              subCategoryId,
              typeId));
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

  @GetMapping("/listTotalIncidents")
  public ResponseEntity<?> listTotalIncidents(
      @RequestParam(defaultValue = "") Long municipalityId,
      @RequestParam(defaultValue = "") Integer sectionNum,
      @RequestParam(defaultValue = "") Long federalDistrictId,
      @RequestParam(defaultValue = "") Long localDistrictId,
      @RequestParam(defaultValue = "") Long subCategoryId,
      @RequestParam(defaultValue = "") Long typeId,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    Map<String, Object> response = new HashMap<>();
    try {
      //  if (servletRequest.isUserInRole("CORDINATOR_MUNICIPAL")) {
      response.put(
          "listTotalIncidents",
          pollingBoothIssuesService.listTotalIncidents(
              municipalityId,
              sectionNum,
              federalDistrictId,
              localDistrictId,
              subCategoryId,
              typeId));
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

  @PostMapping("/downloadIssues")
  public ResponseEntity<byte[]> downloadIssuesByTime(
      @RequestParam(defaultValue = "") Long municipalityId,
      @RequestParam(defaultValue = "") Integer sectionNum,
      @RequestParam(defaultValue = "") Long federalDistrictId,
      @RequestParam(defaultValue = "") Long localDistrictId,
      @RequestParam(defaultValue = "") Long subCategoryId,
      @RequestParam(defaultValue = "") Long typeId,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest)
      throws IOException {
    //    if (servletRequest.isUserInRole("ADMIN")
    //            || servletRequest.isUserInRole("ISSUES_SUPEVISOR")) {

    List<PollingBoothIssuesDescriptionDto> reportMap =
        pollingBoothIssuesService.downloadIssues(
            municipalityId, sectionNum, federalDistrictId, localDistrictId, subCategoryId, typeId);
    // Genera el archivo CSV
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BufferedWriter writer =
        new BufferedWriter(new OutputStreamWriter(baos, StandardCharsets.ISO_8859_1));

    CSVFormat csvFormat =
        CSVFormat.DEFAULT.withHeader(
            "MUNICIPIO",
            "DISTRITO FEDERAL",
            "DISTRITO LOCAL",
            "NÚMERO DE SECCIÓN",
            "NÚMERO DE CASILLA",
            "CATEGORIA",
            "SUBCATEGORIA",
            "DESCRIPCION",
            "FECHA Y HORA INCIDENTE",
            "NOMBRE CASILLA"); // Define las columnas del CSV

    try (CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
      for (PollingBoothIssuesDescriptionDto entity : reportMap) {
        csvPrinter.printRecord(
            entity.getMunicipality(),
            entity.getFederalDistrict(),
            entity.getLocalDistrict(),
            entity.getSectionNumber(),
            entity.getPollingBoothsId(),
            entity.getNameCat(),
            entity.getNameSub(),
            entity.getDescription(),
            entity.getCreated(),
            entity.getPollingBoothName());
      }
    }

    // Configura los encabezados de la respuesta
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.TEXT_PLAIN);
    Date date = new Date();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String formattedDate = dateFormat.format(date);
    headers.setContentDispositionFormData(
        "attachment", "Incidencias_Reportadas_" + formattedDate + ".csv");

    // Devuelve el archivo CSV como respuesta
    return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
    // }
  }
}
