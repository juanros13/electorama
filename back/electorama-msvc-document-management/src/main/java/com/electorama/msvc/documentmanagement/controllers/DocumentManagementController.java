package com.electorama.msvc.documentmanagement.controllers;

import com.electorama.msvc.documentmanagement.models.dto.DocumentDto;
import com.electorama.msvc.documentmanagement.service.DocumentManagementService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping()
public class DocumentManagementController {

  @Autowired private DocumentManagementService documentManagementService;

  @GetMapping("/{id}")
  public ResponseEntity<?> byId(
      @RequestHeader("X-Auth-Id") String authIdHeader,
      @PathVariable("id") Long id,
      HttpServletRequest servletRequest) {
    Optional<DocumentDto> municipalityOptional = documentManagementService.byId(id);
    if (municipalityOptional.isPresent()) {
      return ResponseEntity.ok(municipalityOptional.get());
    }
    return ResponseEntity.notFound().build();
  }
}
