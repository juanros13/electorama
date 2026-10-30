package com.electorama.msvc.citizenissues.controller;

import com.dropbox.core.DbxException;
import com.electorama.msvc.citizenissues.model.dto.DatesRequestDto;
import com.electorama.msvc.citizenissues.model.dto.DownloadIssuesDto;
import com.electorama.msvc.citizenissues.model.dto.IssueDetailDto;
import com.electorama.msvc.citizenissues.model.dto.IssueDto;
import com.electorama.msvc.citizenissues.services.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.*;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
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
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping(value = "/issue")
public class IssueController {

  @Autowired private IssuesService issueService;

  @Autowired private DownloadIssuesService downloadIssuesService;

  @PostMapping
  public ResponseEntity createIssue(
      @RequestHeader("X-Auth-Id") String authIdHeader, @RequestBody IssueDto issue) {
    issue.setUserCreateId(authIdHeader);
    return ResponseEntity.ok(issueService.save(issue));
  }

  @PostMapping("/uploadFile")
  public String uploadFile(@RequestParam("file") MultipartFile file)
      throws IOException, DbxException {
    uploadFile(file);
    return "Archivo subido exitosamente a Dropbox";
  }

  @PutMapping
  public ResponseEntity updateIssue(
      @RequestHeader("X-Auth-Id") String authIdHeader, @RequestBody IssueDto issue) {
    issue.setUserCreateId(authIdHeader);
    return ResponseEntity.ok(issueService.save(issue));
  }

  @GetMapping("/findIssueById/{id}")
  public ResponseEntity getIssue(
      @RequestHeader("X-Auth-Id") String authIdHeader, @PathVariable("id") Long id) {

    return ResponseEntity.ok(issueService.findById(id));
  }

  @DeleteMapping("/deleteIssueById/{id}")
  public ResponseEntity deleteIssue(
      @RequestHeader("X-Auth-Id") String authIdHeader, @PathVariable("id") Long id) {

    return ResponseEntity.ok(issueService.deleteById(id));
  }

  @GetMapping("/findAllIssues")
  public ResponseEntity getAllIssues(
      @RequestParam(defaultValue = "0") Integer pageNo,
      @RequestParam(defaultValue = "15") Integer pageSize,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "desc") String orderBy,
      @RequestParam(defaultValue = "") String search,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {

    if (servletRequest.isUserInRole("ADMIN")) {
      return ResponseEntity.ok(issueService.findAll(pageNo, pageSize, sortBy, orderBy, search));
    } else {
      return ResponseEntity.ok(
          issueService.findByCreator(pageNo, pageSize, sortBy, orderBy, search, authIdHeader));
    }
  }

  @GetMapping("/findAllIssueDetails")
  public ResponseEntity findAllIssueDetails(
      @RequestParam(defaultValue = "0") Integer pageNo,
      @RequestParam(defaultValue = "15") Integer pageSize,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "desc") String orderBy,
      @RequestParam(defaultValue = "") String search,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      @RequestParam(defaultValue = "0") Integer municipalityId,
      @RequestParam(defaultValue = "0") String userCreateId,
      @RequestParam(defaultValue = "0") Integer categoryId,
      @RequestParam(defaultValue = "0") Integer statusId,
      @RequestParam(defaultValue = "0") Integer invoice,
      @RequestParam(defaultValue = "0") String urgent,
      HttpServletRequest servletRequest) {

    if (servletRequest.isUserInRole("ADMIN")
        || servletRequest.isUserInRole("SOLUTION_TRACKING")
        || servletRequest.isUserInRole("ISSUES_SUPEVISOR")) {
      return ResponseEntity.ok(
          issueService.findAllDetails(
              pageNo,
              pageSize,
              sortBy,
              orderBy,
              search,
              municipalityId,
              userCreateId,
              categoryId,
              statusId,
              invoice,
              urgent));
    } else {
      return ResponseEntity.ok(
          issueService.findDetailByUserCreateId(
              pageNo,
              pageSize,
              sortBy,
              orderBy,
              search,
              authIdHeader,
              municipalityId,
              categoryId,
              statusId,
              invoice,
              urgent));
    }
  }

  @GetMapping("/findDetailById/{id}")
  public ResponseEntity finddetailById(
      @RequestHeader("X-Auth-Id") String authIdHeader, @PathVariable("id") Long id) {

    return ResponseEntity.ok(issueService.findDetailById(id));
  }

  @GetMapping("/getReportIssuesStatus")
  public ResponseEntity getReportIssuesStatus(
      @RequestHeader("X-Auth-Id") String authIdHeader,
      @RequestParam(defaultValue = "0") Integer municipalityId,
      @RequestParam(defaultValue = "0") String userCreateId,
      @RequestParam(defaultValue = "0") Integer categoryId,
      @RequestParam(defaultValue = "0") Integer statusId,
      @RequestParam(defaultValue = "") String search,
      @RequestParam(defaultValue = "0") Integer invoice,
      @RequestParam(defaultValue = "0") String urgent,
      HttpServletRequest servletRequest) {
    if (servletRequest.isUserInRole("ADMIN")
        || servletRequest.isUserInRole("SOLUTION_TRACKING")
        || servletRequest.isUserInRole("ISSUES_SUPEVISOR")) {

      return ResponseEntity.ok(
          issueService.getReportIssueCategory(
              municipalityId, userCreateId, categoryId, statusId, search, true, invoice, urgent));

    } else {
      return ResponseEntity.ok(
          issueService.getReportIssueCategory(
              municipalityId, authIdHeader, categoryId, statusId, search, false, invoice, urgent));
    }
  }

  @PostMapping("/downloadIssuesByTime")
  public ResponseEntity<byte[]> downloadIssuesByTime(
      @RequestBody DatesRequestDto datesRequestDto,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest)
      throws IOException {
    if (servletRequest.isUserInRole("ADMIN")
        || servletRequest.isUserInRole("SOLUTION_TRACKING")
        || servletRequest.isUserInRole("ISSUES_SUPEVISOR")) {

      List<DownloadIssuesDto> reportMap =
          downloadIssuesService.downloadIssuesByTime(datesRequestDto);
      // Genera el archivo CSV
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      BufferedWriter writer =
          new BufferedWriter(new OutputStreamWriter(baos, StandardCharsets.ISO_8859_1));

      CSVFormat csvFormat =
          CSVFormat.DEFAULT.withHeader(
              "OFICIO",
              "MUNICIPIO",
              "NOMBRE COMPLETO SOLICITANTE",
              "TELEFONO SOLICITANTE",
              "FECHA LEVANTAMIENTO",
              "COMENTARIOS",
              "TIPO",
              "CATEGORIA",
              "ESTATUS",
              "NOMBRE BENEFICIARIOS",
              "APELLIDO PATERNO BENEFICIARIO",
              "APELLIDO MATERNO BENEFICIARIO",
              "LOCALIDAD",
              "LOCALIDAD OTRA",
              "USUARIO CREO",
              "CURP",
              "RECURSO",
              "URGENTE"); // Define las columnas del CSV

      try (CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
        for (DownloadIssuesDto entity : reportMap) {
          csvPrinter.printRecord(
              entity.getOficio(),
              entity.getMunicipio(),
              entity.getNombreCompletoSolicitante(),
              entity.getTelefonoSolicitante(),
              entity.getFechaLevantamiento(),
              entity.getComentarios(),
              entity.getTipo(),
              entity.getCategoria(),
              entity.getEstatus(),
              entity.getNombreBeneficiario(),
              entity.getApellidoPaternoBeneficiario(),
              entity.getApellidoMaternoBeneficiario(),
              entity.getLocalidad(),
              entity.getLocalidadOtra(),
              entity.getUsuarioCreo(),
              entity.getCurp(),
              entity.getRecurso(),
              entity.getUrgente());
        }
      }

      // Configura los encabezados de la respuesta
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.TEXT_PLAIN);
      // headers.setContentDispositionFormData("attachment", "data.csv");
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      String dateStart = dateFormat.format(datesRequestDto.getStartDate());
      String dateEnd = dateFormat.format(datesRequestDto.getEndDate());
      headers.setContentDispositionFormData(
          "attachment", "Atencion_ciudadana_" + dateStart + "_" + dateEnd + ".csv");

      // Devuelve el archivo CSV como respuesta
      return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
    }
    return null;
  }

  @PutMapping("/updateIssueDetail/{id}")
  public ResponseEntity updateIssueDetail(
      @RequestHeader("X-Auth-Id") String authIdHeader, @RequestBody IssueDetailDto detail) {
    detail.setUserCreateId(authIdHeader);
    return ResponseEntity.ok(issueService.updateDetail(detail));
  }

  @DeleteMapping("/deleteIssueDetailById/{id}")
  public ResponseEntity deleteIssueDetailById(
      @RequestHeader("X-Auth-Id") String authIdHeader, @PathVariable("id") Long id) {

    return ResponseEntity.ok(issueService.deleteIssueDetailById(id));
  }

  @DeleteMapping("/hardDeleteIssueDetailById/{id}")
  public ResponseEntity hardDeleteIssueDetailById(
      @RequestHeader("X-Auth-Id") String authIdHeader,
      @PathVariable("id") Long id,
      HttpServletRequest servletRequest) {
    if (servletRequest.isUserInRole("ADMIN") || servletRequest.isUserInRole("ISSUES_SUPEVISOR")) {
      return ResponseEntity.ok(issueService.hardDeleteIssueDetailById(id));
    }
    return ResponseEntity.notFound().build();
  }
}
