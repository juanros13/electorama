package com.electorama.msvc.visits.controller;

import com.electorama.msvc.visits.model.dto.*;
import com.electorama.msvc.visits.service.ReportVisitedService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/reportVisited")
public class ReportVisitedController {

  @Autowired ReportVisitedService reportVisitedByMunicipalCoordinatorService;

  @PostMapping("/reportVisitedByMunicipalCoordinator")
  public ResponseEntity<?> reportVisitedByMunicipalCoordinator(
      @RequestBody DatesRequestDto fechasDto,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {

    if (servletRequest.isUserInRole("ADMIN")) {
      log.info("Es Administrador");
      List<ReportVisitedByMunicipalCoordinatorDto> reportVisitedByMunicipalCoordinatorDtoList =
          reportVisitedByMunicipalCoordinatorService.findReportVisitedByMunicipalCoordinator(
              fechasDto);
      return ResponseEntity.ok(reportVisitedByMunicipalCoordinatorDtoList);

    } else {
      log.info("No eres administrador");
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/reportVisitsByMunicipality")
  public ResponseEntity<?> reportVisitsByMunicipality(
      @RequestBody DatesRequestDto fechasDto,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {

    if (servletRequest.isUserInRole("ADMIN")) {
      log.info("Es Administrador");

      List<ReportVisitsByMunicipalityDto> reportVisitsByMunicipalityDtos =
          reportVisitedByMunicipalCoordinatorService.reportVisitsByMunicipality(fechasDto);
      return ResponseEntity.ok(reportVisitsByMunicipalityDtos);
    } else {
      log.info("No eres administrador");
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/reportVisitsByCoordinatorZoneSectionRef/{municipalityId}")
  public ResponseEntity<?> reportVisitsByCoordinatorZoneSectionRef(
      @PathVariable Integer municipalityId,
      @RequestBody DatesRequestDto fechasDto,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {

    String typeRol = "";

    if (servletRequest.isUserInRole("ADMIN")) {
      typeRol = "ADMIN";
    } else if (servletRequest.isUserInRole("CORDINATOR_MUNICIPAL")) {
      typeRol = "CORDINATOR_MUNICIPAL";
    } else if (servletRequest.isUserInRole("CORDINATOR_ZONE")) {
      typeRol = "CORDINATOR_ZONE";
    }

    List<ReportVisitsByCoordinatorZoneSectionRefDto> reportVisitsByCoordinatorZoneSectionRefDtos =
        reportVisitedByMunicipalCoordinatorService.reportVisitsByCoordinatorZoneSectionRef(
            fechasDto, municipalityId, typeRol, authIdHeader);

    return ResponseEntity.ok(reportVisitsByCoordinatorZoneSectionRefDtos);
  }

  @PostMapping("/reportVisitedMap")
  public ResponseEntity<?> reportVisitedMap(
      @RequestHeader("X-Auth-Id") String authIdHeader, HttpServletRequest servletRequest) {
    String typeRol = "";
    if (servletRequest.isUserInRole("ADMIN")) {
      typeRol = "ADMIN";
    } else if (servletRequest.isUserInRole("CORDINATOR_MUNICIPAL")) {
      typeRol = "CORDINATOR_MUNICIPAL";
    } else if (servletRequest.isUserInRole("CORDINATOR_ZONE")) {
      typeRol = "CORDINATOR_ZONE";
    }

    List<ReportVisitMapDto> reportMap =
        reportVisitedByMunicipalCoordinatorService.reportVisitsMap(typeRol, authIdHeader);
    return ResponseEntity.ok(reportMap);
  }

  @PostMapping("/reportVisitedAll")
  public ResponseEntity<?> reportVisitedAll(
      @RequestBody DatesRequestDto datesRequestDto,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    String typeRol = "";
    if (servletRequest.isUserInRole("ADMIN")) {
      typeRol = "ADMIN";
    } else if (servletRequest.isUserInRole("CORDINATOR_MUNICIPAL")) {
      typeRol = "CORDINATOR_MUNICIPAL";
    } else if (servletRequest.isUserInRole("CORDINATOR_ZONE")) {
      typeRol = "CORDINATOR_ZONE";
    }

    List<ReportVisitAllDto> reportMap =
        reportVisitedByMunicipalCoordinatorService.reportVisitsAll(
            datesRequestDto, typeRol, authIdHeader);
    return ResponseEntity.ok(reportMap);
  }

  @PostMapping("/reportVisitedAllToCsv")
  public ResponseEntity<byte[]> reportVisitedAllToCsv(
      @RequestBody DatesRequestDto datesRequestDto,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest)
      throws IOException {
    String typeRol = "";
    if (servletRequest.isUserInRole("ADMIN")) {
      typeRol = "ADMIN";
    } else if (servletRequest.isUserInRole("CORDINATOR_MUNICIPAL")) {
      typeRol = "CORDINATOR_MUNICIPAL";
    } else if (servletRequest.isUserInRole("CORDINATOR_ZONE")) {
      typeRol = "CORDINATOR_ZONE";
    }

    List<ReportVisitAllDto> reportMap =
        reportVisitedByMunicipalCoordinatorService.reportVisitsAll(
            datesRequestDto, typeRol, authIdHeader);
    // Genera el archivo CSV
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BufferedWriter writer =
        new BufferedWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8));

    CSVFormat csvFormat =
        CSVFormat.DEFAULT.withHeader(
            "MUNICIPIO",
            "COORDINADOR DE MUNICIPIO",
            "COORDINADOR DE ZONA",
            "SECCION",
            "ID",
            "FECHA VISITA",
            "FECHA SINCRONIZACION",
            "CALLE",
            "NO. EXTERNO",
            "LATITUD",
            "LONGITUD",
            "ABRIO PUERTA",
            "ES AFIN",
            "ACEPTA MAS INFO",
            "NOMBRE",
            "APELLIDO PATERNO",
            "APELLIDO MATERNO",
            "TELEFONO",
            "EDAD",
            "SEXO"); // Define las columnas del CSV

    try (CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
      for (ReportVisitAllDto entity : reportMap) {
        csvPrinter.printRecord(
            entity.getMunicipio(),
            entity.getReferente(),
            entity.getCoordinadorZona(),
            entity.getSeccion(),
            entity.getVisitadoId(),
            entity.getFechaVisita(),
            entity.getFechaSincronizacion(),
            entity.getCalle(),
            entity.getNumeroExterno(),
            entity.getLatitud(),
            entity.getLongitud(),
            entity.getAbrioPuerta(),
            entity.getEsAfin(),
            entity.getAceptaMasInfo(),
            entity.getVisitadoNombre(),
            entity.getVisitadoPaterno(),
            entity.getVisitadoMaterno(),
            entity.getTelefono(),
            entity.getEdadPersonaAbrePuerta(),
            entity.getSexoPersonaAbrePuerta());
      }
    }

    // Configura los encabezados de la respuesta
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.TEXT_PLAIN);
    // headers.setContentDispositionFormData("attachment", "data.csv");
    headers.setContentDispositionFormData(
        "attachment",
        "Visitas_" + datesRequestDto.getStartDate() + "_" + datesRequestDto.getEndDate() + ".csv");

    // Devuelve el archivo CSV como respuesta
    return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
  }
}
