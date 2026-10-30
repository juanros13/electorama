package com.electorama.geography.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/report")
public class ReportController {

  //    @GetMapping("/getTotalReport")
  //    public ResponseEntity<?> detail(
  //            @RequestHeader("X-Auth-Id") String authIdHeader
  //    ) {
  //        Optional<Seccion> municipalityOptional = seccionService.byId(id);
  //        if (municipalityOptional.isPresent()) {
  //            return ResponseEntity.ok(municipalityOptional.get());
  //        }
  //        return ResponseEntity.notFound().build();
  //    }

}
