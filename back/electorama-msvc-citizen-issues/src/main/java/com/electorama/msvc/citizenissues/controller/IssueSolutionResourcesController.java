package com.electorama.msvc.citizenissues.controller;

import com.electorama.msvc.citizenissues.model.entity.IssueSolutionResourceEntity;
import com.electorama.msvc.citizenissues.services.ResourcesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/resources")
public class IssueSolutionResourcesController {

  @Autowired private ResourcesService resourcesService;

  @GetMapping
  public ResponseEntity getResourses() {

    return ResponseEntity.ok(resourcesService.findAll());
  }

  @GetMapping("/getResoursesByCategoryId/{id}")
  public ResponseEntity getResoursesByCategoryId(@PathVariable("id") Long id) {

    return ResponseEntity.ok(resourcesService.findByCategoryId(id));
  }

  @PostMapping
  public ResponseEntity createResourses(@RequestBody IssueSolutionResourceEntity resourcesEntity) {

    return ResponseEntity.ok(resourcesService.save(resourcesEntity));
  }

  @GetMapping("/findResourcesById/{id}")
  public ResponseEntity getResoursesById(
      @RequestHeader("X-Auth-Id") String authIdHeader, @PathVariable("id") Long id) {

    return ResponseEntity.ok(resourcesService.findById(id));
  }
}
