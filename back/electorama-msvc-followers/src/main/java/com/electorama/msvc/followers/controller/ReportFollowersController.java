package com.electorama.msvc.followers.controller;

import com.electorama.msvc.followers.model.dto.*;
import com.electorama.msvc.followers.service.ReportFollowersService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
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
@RequestMapping(value = "/reportFollowers")
public class ReportFollowersController {

  @Autowired ReportFollowersService reportFollowersService;

  @PostMapping("/reportFollowersByCoordinatorZone/{municipalityId}")
  public ResponseEntity<?> reportFollowersByCoordinatorZone(
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

    List<ReportFollowersByCoordinatorZoneDto> reportVisitedByMunicipalCoordinatorDtoList =
        reportFollowersService.findReportFollowersByCoordinatorZone(
            fechasDto, typeRol, municipalityId, authIdHeader);
    return ResponseEntity.ok(reportVisitedByMunicipalCoordinatorDtoList);
  }

  @PostMapping("/reportFollowersByReferent")
  public ResponseEntity<?> reportFollowersByReferent(
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

    List<ReportFollowersByReferentDto> reportFollowersByReferent =
        reportFollowersService.reportFollowersByReferent(fechasDto, typeRol, authIdHeader);
    return ResponseEntity.ok(reportFollowersByReferent);
  }

  @PostMapping("/reportFollowersByMunicipality")
  public ResponseEntity<?> reportFollowersByMunicipality(
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

    List<ReportFollowersByMunicipalityDto> reportFollowersByMunicipalityDtos =
        reportFollowersService.reportFollowersByMunicipality(fechasDto, typeRol, authIdHeader);
    return ResponseEntity.ok(reportFollowersByMunicipalityDtos);
  }

  @PostMapping("/downloadFollowersByCordinator")
  public ResponseEntity<byte[]> downloadFollowersByCordinator(
      @RequestHeader("X-Auth-Id") String authIdHeader, HttpServletRequest servletRequest)
      throws IOException {
    String typeRol = "";
    if (servletRequest.isUserInRole("ADMIN")) {
      typeRol = "ADMIN";
    } else if (servletRequest.isUserInRole("CORDINATOR_MUNICIPAL")) {
      typeRol = "CORDINATOR_MUNICIPAL";
    } else if (servletRequest.isUserInRole("CORDINATOR_ZONE")) {
      typeRol = "CORDINATOR_ZONE";
    }
    if (servletRequest.isUserInRole("CORDINATOR_ZONE")
        || servletRequest.isUserInRole("CORDINATOR_MUNICIPAL")
        || servletRequest.isUserInRole("ADMIN")) {

      List<DownloadFollowersDto> reportMap =
          reportFollowersService.downloadFollowersByCordinator(authIdHeader, typeRol);
      // Genera el archivo CSV
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      BufferedWriter writer =
          new BufferedWriter(new OutputStreamWriter(baos, StandardCharsets.ISO_8859_1));

      CSVFormat csvFormat =
          CSVFormat.DEFAULT.withHeader(
              "PROMOTOR ID",
              "NOMBRE PROMOTOR",
              "CURP PROMOTOR",
              "TELEFONO PROMOTOR",
              "INE PROMOTOR",
              "SIMPATIZANTE ID",
              "NOMBRE SIMPATIZANTE",
              "CURP",
              "TELEFONO",
              "INE",
              "ENTIDAD RESIDENCIA ID",
              "ENTIDAD",
              "MUNICIPIO RESIDENCIA ID",
              "MUNICIPIO",
              "SECCION",
              "COLONIA",
              "CALLE",
              "NÚMERO EXTERIOR",
              "NÚMERO INTERNO",
              "CÓDIGO POSTAL",
              "LATITUD",
              "LONGITUD",
              "ROL FAMILIAR",
              "ESTADO CIVIL",
              "SEXO",
              "NÚM. PERSONAS INVITADAS",
              "REPRESENTANTE MESAS ELECTORALES",
              "PARTICIPACION COMITE",
              "ADULTO MAYOR",
              "TELEFONO COMPATIBLE WHATSAPP",
              "APOYA MOVIMIENTO",
              "INE VENCIMIENTO",
              "FECHA CREACION",
              "FECHA ACTUALIZACIÓN",
              "COORDINADOR DE ZONA"); // Define las columnas del CSV

      try (CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
        for (DownloadFollowersDto entity : reportMap) {
          csvPrinter.printRecord(
              entity.getPromotorId(),
              entity.getPromotor(),
              entity.getPromotorCurp(),
              entity.getPromotorTelefono(),
              entity.getPromotorIne(),
              entity.getSimpatizanteId(),
              entity.getSimpatizante(),
              entity.getCurp(),
              entity.getTelefono(),
              entity.getIne(),
              entity.getEntidadResidenciaId(),
              entity.getNombreEntidad(),
              entity.getMunicipioResidenciaId(),
              entity.getNombreMunicipio(),
              entity.getSeccion(),
              entity.getColonia(),
              entity.getCalle(),
              entity.getNumeroExterior(),
              entity.getNumeroInterno(),
              entity.getCodigoPostal(),
              entity.getLatitud(),
              entity.getLongitud(),
              entity.getRolFamiliar(),
              entity.getEstadoCivil(),
              entity.getSexo(),
              entity.getPersonasInvitadas(),
              entity.getRepresentanteMesasElectorales(),
              entity.getParticipacionComite(),
              entity.getAdultoMayor(),
              entity.getTelefonoCompatibleWhatsapp(),
              entity.getApoyaMovimiento(),
              entity.getIneVencimiento(),
              entity.getFechaCreacion(),
              entity.getFechaActualizacion(),
              entity.getCordinatorZoneName());
        }
      }

      // Configura los encabezados de la respuesta
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.TEXT_PLAIN);
      // headers.setContentDispositionFormData("attachment", "data.csv");
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      LocalDate fechaHoy = LocalDate.now();

      headers.setContentDispositionFormData(
          "attachment", "Reporte_Simpatizantes_" + fechaHoy + ".csv");

      // Devuelve el archivo CSV como respuesta
      return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
    }
    return null;
  }

  @GetMapping("/getReportMobilizationBySection/{seccionId}")
  public ResponseEntity<?> listFollowerByCoordinatorId(
      @PathVariable Long seccionId,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    ReportFollowersMobilzationDto listFollower;
    if (servletRequest.isUserInRole("SAFE_HOUSE_OPERATOR")
        || servletRequest.isUserInRole("ADMIN")) {
      try {
        listFollower = reportFollowersService.reporteFollowersMobilizationBySection(seccionId);

      } catch (Exception e) {
        return new ResponseEntity<>(e, HttpStatus.CONFLICT);
      }
      return ResponseEntity.ok(listFollower);
    }
    return ResponseEntity.ok(new ArrayList<>());
  }
}
