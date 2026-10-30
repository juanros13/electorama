package com.electorama.msvc.canvasandwalls.controller;

import com.electorama.msvc.canvasandwalls.model.dto.*;
import com.electorama.msvc.canvasandwalls.service.ReportCanvasAndWallMunicipalityService;
import com.electorama.msvc.canvasandwalls.service.ReportCanvasAndWallReferrerService;
import com.electorama.msvc.canvasandwalls.service.ReportCanvasAndWallZoneService;
import com.electorama.msvc.canvasandwalls.service.ReportCanvasAndWallsAllService;
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
@RequestMapping(value = "/reportCanvasAndWall")
public class ReportCanvasAndWallController {

  @Autowired ReportCanvasAndWallMunicipalityService reportCanvasAndWallMunicipalityService;

  @Autowired ReportCanvasAndWallReferrerService reportCanvasAndWallReferrerService;

  @Autowired ReportCanvasAndWallZoneService reportCanvasAndWallZoneService;

  @Autowired ReportCanvasAndWallsAllService reportCanvasAndWallsAllService;

  @PostMapping("/byMunicipality")
  public ResponseEntity byMunicipality(
      @RequestHeader("X-Auth-Id") String authIdHeader,
      @RequestBody ReportCanvasAndWallDto requ,
      HttpServletRequest servletRequest) {
    if (servletRequest.isUserInRole("ADMIN")) {
      if (requ.getCategory() == 0 && requ.getProyect() == 0) {
        log.info("Encuentra por todo  " + requ.getCategory());
        return ResponseEntity.ok(
            reportCanvasAndWallMunicipalityService.findAllByMunicipality(requ));
      } else {
        if (requ.getCategory() != 0 && requ.getProyect() == 0) {
          log.info("Encuentra por categoria " + requ.getCategory());
          return ResponseEntity.ok(
              reportCanvasAndWallMunicipalityService.findByCategoryMunicipality(requ));
        } else if (requ.getCategory() == 0 && requ.getProyect() != 0) {
          log.info("Encuentra por proyecto " + requ.getProyect());
          return ResponseEntity.ok(
              reportCanvasAndWallMunicipalityService.findByProjectMunicipality(requ));
        } else {
          log.info(
              "Encuentra por proyecto y categoria "
                  + requ.getProyect()
                  + " Categoria "
                  + requ.getCategory());
          return ResponseEntity.ok(
              reportCanvasAndWallMunicipalityService.findByProjectAndCategoryMunicipality(requ));
        }
      }
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/byReferrer")
  public ResponseEntity byReferrer(
      @RequestHeader("X-Auth-Id") String authIdHeader,
      @RequestBody ReportCanvasAndWallDto requ,
      HttpServletRequest servletRequest) {
    if (servletRequest.isUserInRole("ADMIN")) {
      if (requ.getCategory() == 0 && requ.getProyect() == 0) {
        log.info("Encuentra por todo  " + requ.getCategory());
        return ResponseEntity.ok(reportCanvasAndWallReferrerService.findAllByMunicipality(requ));
      } else {
        if (requ.getCategory() != 0 && requ.getProyect() == 0) {
          log.info("Encuentra por categoria " + requ.getCategory());
          return ResponseEntity.ok(reportCanvasAndWallReferrerService.findByCategory(requ));
        } else if (requ.getCategory() == 0 && requ.getProyect() != 0) {
          log.info("Encuentra por proyecto " + requ.getProyect());
          return ResponseEntity.ok(reportCanvasAndWallReferrerService.findByProject(requ));
        } else {
          log.info(
              "Encuentra por proyecto y categoria "
                  + requ.getProyect()
                  + " Categoria "
                  + requ.getCategory());
          return ResponseEntity.ok(
              reportCanvasAndWallReferrerService.findByProjectAndCategory(requ));
        }
      }
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/byZone")
  public ResponseEntity byZone(
      @RequestHeader("X-Auth-Id") String authIdHeader,
      @RequestBody ReportCanvasAndWallDto requ,
      HttpServletRequest servletRequest) {
    if (servletRequest.isUserInRole("CORDINATOR_MUNICIPAL")) {
      requ.setReferenceAuthId(authIdHeader);
    }
    if (servletRequest.isUserInRole("CORDINATOR_ZONE")) {
      requ.setCoordinatorZoneAuthId(authIdHeader);
    }
    if (requ.getMunicipality() == 0) {
      if (requ.getCategory() == 0 && requ.getProyect() == 0) {
        log.info("Encuentra por todo  " + requ.getCategory());
        return ResponseEntity.ok(reportCanvasAndWallZoneService.findAllByZone(requ));
      } else {
        if (requ.getCategory() != 0 && requ.getProyect() == 0) {
          log.info("Encuentra por categoria " + requ.getCategory());
          return ResponseEntity.ok(reportCanvasAndWallZoneService.findByCategory(requ));
        } else if (requ.getCategory() == 0 && requ.getProyect() != 0) {
          log.info("Encuentra por proyecto " + requ.getProyect());
          return ResponseEntity.ok(reportCanvasAndWallZoneService.findByProject(requ));
        } else {
          log.info(
              "Encuentra por proyecto y categoria "
                  + requ.getProyect()
                  + " Categoria "
                  + requ.getCategory());
          return ResponseEntity.ok(reportCanvasAndWallZoneService.findByProjectAndCategory(requ));
        }
      }
    } else {
      if (requ.getCategory() != 0 && requ.getProyect() == 0) {
        log.info("Encuentra por categoria y por municipio " + requ.getCategory());
        return ResponseEntity.ok(
            reportCanvasAndWallZoneService.findByCategoryAndMunicipality(requ));
      } else if (requ.getCategory() == 0 && requ.getProyect() != 0) {
        log.info("Encuentra por proyecto y municipio" + requ.getProyect());
        return ResponseEntity.ok(reportCanvasAndWallZoneService.findByProjectAndMunicipality(requ));
      } else if (requ.getCategory() == 0 && requ.getProyect() == 0) {
        log.info("Encuentra por proyecto y municipio" + requ.getProyect());
        return ResponseEntity.ok(reportCanvasAndWallZoneService.findByMunicipality(requ));
      } else {
        log.info(
            "Encuentra por proyecto  categoria y municipio "
                + requ.getProyect()
                + " Categoria "
                + requ.getCategory());
        return ResponseEntity.ok(
            reportCanvasAndWallZoneService.findByProjectAndCategoryAndMunicipality(requ));
      }
    }
  }

  @PostMapping("/reportCanvasAndWallsAll")
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

    List<ReportCanvasAndWallsAllDto> reportMap =
        reportCanvasAndWallsAllService.reportVisitsAll(datesRequestDto, typeRol, authIdHeader);
    if (!reportMap.isEmpty()) {
      return ResponseEntity.ok(reportMap);
    }

    return ResponseEntity.notFound().build();
  }

  @PostMapping("/reportCanvasAndWallsAllToCsv")
  public ResponseEntity<?> reportVisitedAllToCsv(
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
    } else if (servletRequest.isUserInRole("CORDINATOR_ZONE_ALT")) {
      typeRol = "CORDINATOR_ZONE_ALT";
    }

    List<ReportCanvasAndWallsAllDto> reportMap =
        reportCanvasAndWallsAllService.reportVisitsAll(datesRequestDto, typeRol, authIdHeader);
    if (reportMap.isEmpty()) {
      return ResponseEntity.ok(reportMap);
    }
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
            "CATEGORIA",
            "PROYECTO",
            "FECHA PUESTA",
            "FECHA REGISTRO",
            "LATITUD",
            "LONGITUD",
            "IMAGEN"); // Define las columnas del CSV

    try (CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
      for (ReportCanvasAndWallsAllDto entity : reportMap) {
        csvPrinter.printRecord(
            entity.getMunicipio(),
            entity.getReferente(),
            entity.getCoordinadorZona(),
            entity.getSeccion(),
            entity.getCategoria(),
            entity.getProyecto(),
            entity.getFechaPuesta(),
            entity.getFechaRegistro(),
            entity.getLatitud(),
            entity.getLongitud(),
            "https://somoslamayoria.com/canvas-walls" + entity.getPath());
      }
    }

    // Configura los encabezados de la respuesta
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.TEXT_PLAIN);
    headers.setContentDispositionFormData(
        "attachment",
        "LonasBardas_"
            + datesRequestDto.getStartDate()
            + "_"
            + datesRequestDto.getEndDate()
            + ".csv");

    // Devuelve el archivo CSV como respuesta
    return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
  }

  @GetMapping("/reportCanvasAndWallAllResume")
  public ResponseEntity<?> reportTotalVisits(
      @RequestHeader("X-Auth-Id") String authIdHeader, HttpServletRequest servletRequest) {
    List<ReportCanvasAndWallsAllResumeDto> reportAllVisitsDtos = null;
    if (servletRequest.isUserInRole("ADMIN")) {
      reportAllVisitsDtos = reportCanvasAndWallsAllService.reportVisitsAllResume();
    }
    if (reportAllVisitsDtos != null) {
      return ResponseEntity.ok(reportAllVisitsDtos);
    }
    return ResponseEntity.notFound().build();
  }

  @PostMapping("/reportCanvasAndWallAllByTime/{categoryId}")
  public ResponseEntity<?> reportTotalCanvasAndWallsByTime(
      @RequestBody DatesRequestDto datesRequestDto,
      @PathVariable Integer categoryId,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    List<ReportCanvasAndWallsByTimeDto> reportAllVisitsDtos = null;
    if (servletRequest.isUserInRole("ADMIN")) {
      reportAllVisitsDtos =
          reportCanvasAndWallsAllService.findAllVisitsByTime(datesRequestDto, categoryId);
    }
    if (reportAllVisitsDtos != null) {
      return ResponseEntity.ok(reportAllVisitsDtos);
    }
    return ResponseEntity.notFound().build();
  }
}
