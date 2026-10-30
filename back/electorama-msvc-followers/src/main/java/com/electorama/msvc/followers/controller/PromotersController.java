package com.electorama.msvc.followers.controller;

import com.electorama.msvc.followers.model.dto.*;
import com.electorama.msvc.followers.service.FollowersService;
import com.electorama.msvc.followers.service.FollowersSirenaService;
import com.electorama.msvc.followers.service.PromotersService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/promoters")
public class PromotersController {

  @Autowired private PromotersService promotersService;

  @Autowired private FollowersService followersService;

  @Autowired private FollowersSirenaService followersSirenaService;

  @GetMapping()
  public ResponseEntity<?> list(
      @RequestParam(defaultValue = "0") Integer pageNo,
      @RequestParam(defaultValue = "30") Integer pageSize,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "desc") String orderBy,
      @RequestParam(defaultValue = "") String search,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    Map<String, Object> response = new HashMap<>();
    try {
      if (servletRequest.isUserInRole("ADMIN")) {
        response.put(
            "list",
            promotersService.listAdmin(pageNo, pageSize, sortBy, orderBy, search, authIdHeader));
        response.put("success", "true");
        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
      } else {
        response.put(
            "list", promotersService.list(pageNo, pageSize, sortBy, orderBy, search, authIdHeader));
        response.put("success", "true");
        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
      }
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<>(
          "No se encontraron datos con datos asociados el id: " + authIdHeader,
          HttpStatus.NOT_FOUND);
    }
  }

  @PostMapping
  public ResponseEntity create(
      @Valid @RequestBody PromotersDto request,
      BindingResult result,
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    if (result.hasErrors()) {
      return validar(result);
    }

    log.info("Creating promoters with {}", request.toString());
    PromotersDto promoters;
    try {
      promoters = promotersService.save(request, authIdHeader);

    } catch (Exception e) {
      return new ResponseEntity<>(e, HttpStatus.CONFLICT);
    }
    if (request.getFollowerSirenaId() != null) {
      Optional<FollowersSirenaDto> followersSirenaDto =
          followersSirenaService.byIdAdmin(request.getFollowerSirenaId(), authIdHeader);
      followersSirenaDto.get().setPromotersId(promoters.getId());

      Optional<FollowersSirenaDto> sirenaDto = followersSirenaService.save(followersSirenaDto);
    }

    if (promoters != null) {

      if (request.getFollowerSirenaId() != null) {
        Optional<FollowersSirenaDto> followersSirenaDto =
            followersSirenaService.byIdAdmin(request.getFollowerSirenaId(), authIdHeader);
        followersSirenaDto.get().setPromotersId(promoters.getId());

        Optional<FollowersSirenaDto> sirenaDto = followersSirenaService.save(followersSirenaDto);
      }

      ResponsePromoterCreateDto responsePromoterCreateDto = new ResponsePromoterCreateDto();
      responsePromoterCreateDto.setPromotersDto(promoters);

      return ResponseEntity.ok(responsePromoterCreateDto);
    } else {
      return new ResponseEntity<>(
          "No se pudo crear el promotor porque excede su meta", HttpStatus.CONFLICT);
    }
  }

  @PostMapping("/createByFollower/{id}")
  public ResponseEntity createByFollower(
      @PathVariable Long id,
      @Valid @RequestBody PromotersDto request,
      BindingResult result,
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    if (result.hasErrors()) {
      return validar(result);
    }
    Optional<FollowersDto> followersDtoOptional;
    try {
      followersDtoOptional = followersService.referenceFindById(id, authIdHeader);
    } catch (Exception e) {
      return new ResponseEntity<>(e, HttpStatus.CONFLICT);
    }
    if (followersDtoOptional.isPresent()) {
      FollowersDto followersDto = followersDtoOptional.get();

      long miliseconds = System.currentTimeMillis();
      Date date = new Date(miliseconds);

      followersDto.setDeleted(date);
      followersDto.setPromoterAuthId(null);
      followersDto.setUserCreateId(null);

      followersSirenaService.deleteByFollowerId(id, authIdHeader);
      followersService.update(followersDto, authIdHeader);
    } else {
      return new ResponseEntity<>("No se pudo eliminar el simpatizante", HttpStatus.CONFLICT);
    }

    PromotersDto promoters;
    try {
      promoters = promotersService.save(request, authIdHeader);

    } catch (Exception e) {
      FollowersDto followersDto = followersDtoOptional.get();
      followersService.update(followersDto, authIdHeader);
      return new ResponseEntity<>(e, HttpStatus.CONFLICT);
    }

    ResponsePromoterCreateDto responsePromoterCreateDto = new ResponsePromoterCreateDto();
    responsePromoterCreateDto.setPromotersDto(promoters);

    return ResponseEntity.ok(responsePromoterCreateDto);
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> editPromoters(
      @Valid @RequestBody PromotersDto promotersDto,
      BindingResult result,
      @PathVariable Long id,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {

    if (result.hasErrors()) {
      return validar(result);
    }
    PromotersDto dto = null;

    try {
      dto = promotersService.update(promotersDto, id, authIdHeader);
      if (dto != null) {
        return ResponseEntity.ok(dto);
      } else {
        return new ResponseEntity<>("No se pudo actualizar el promotor", HttpStatus.CONFLICT);
      }
    } catch (Exception e) {
      return new ResponseEntity<>(e, HttpStatus.CONFLICT);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deletePromoters(
      @PathVariable Long id, @RequestHeader("X-Auth-Id") String authIdHeader) {
    Optional<PromotersDto> promotersDtoOptional = promotersService.byIdAdmin(id, authIdHeader);
    try {
      Optional<PromotersDto> o = promotersService.deleteByPromoters(id, authIdHeader);
    } catch (Exception e) {
      return new ResponseEntity<>(e, HttpStatus.CONFLICT);
    }
    return ResponseEntity.ok(promotersDtoOptional.get());
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

  @GetMapping("/{id}")
  public ResponseEntity<?> detail(
      @PathVariable Long id,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    // Optional<PromotersDto> promotersDtoOptional = promotersService.byId(id, authIdHeader);
    Optional<PromotersDto> promotersDtoOptional = promotersService.byIdAdmin(id, authIdHeader);
    if (promotersDtoOptional.isPresent()) {
      return ResponseEntity.ok(promotersDtoOptional.get());
    }
    return new ResponseEntity<>(
        "No se encontraron datos con datos asociados el id: " + id, HttpStatus.NOT_FOUND);
  }

  @GetMapping("/listPromotersBySectionId/{sectionId}")
  public ResponseEntity<?> promotesBySectionId(
      @PathVariable Integer sectionId,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    // Optional<PromotersDto> promotersDtoOptional = promotersService.byId(id, authIdHeader);
    List<PromotersDto> promotersDtoOptional;
    promotersDtoOptional = promotersService.listPromoterBySection(sectionId, authIdHeader);
    if (promotersDtoOptional.size() > 0) {
      return ResponseEntity.ok(promotersDtoOptional);
    }
    return new ResponseEntity<>(
        "No se encontraron datos con datos asociados el id: " + sectionId, HttpStatus.NOT_FOUND);
  }

  @GetMapping("/listPromoters")
  public ResponseEntity<?> getPromoters(
      @RequestHeader("X-Auth-Id") String authIdHeader, HttpServletRequest servletRequest) {
    // Optional<PromotersDto> promotersDtoOptional = promotersService.byId(id, authIdHeader);
    List<PromotersDto> promotersDtoOptional = new ArrayList<>();
    promotersDtoOptional = promotersService.listPromoters(authIdHeader);
    if (!promotersDtoOptional.isEmpty()) {
      return ResponseEntity.ok(promotersDtoOptional);
    }
    return new ResponseEntity<>(
        "No se encontraron datos con datos asociados el id: " + authIdHeader, HttpStatus.NOT_FOUND);
  }

  @GetMapping("/listPromoters/{authId}")
  public ResponseEntity<?> getPromotersByAuthId(
      @RequestHeader("X-Auth-Id") String authIdHeader,
      @PathVariable String authId,
      HttpServletRequest servletRequest) {
    List<PromotersDto> promotersDtoOptional = new ArrayList<>();
    if (servletRequest.isUserInRole("CORDINATOR_MUNICIPAL")) {
      log.info("ENTRANDO");
      promotersDtoOptional = promotersService.listPromoters(authId);
    }
    if (!promotersDtoOptional.isEmpty()) {
      return ResponseEntity.ok(promotersDtoOptional);
    }
    return new ResponseEntity<>(
        "No se encontraron datos con datos asociados el id: " + authIdHeader, HttpStatus.NOT_FOUND);
  }

  @GetMapping("/reportPromotersBySection/{sectionId}")
  public ResponseEntity<?> getPromotersBySection(
      @PathVariable Integer sectionId, @RequestHeader("X-Auth-Id") String authIdHeader) {
    PromoterGoalsDto promoterGoalsDto = promotersService.listBySection(sectionId, authIdHeader);

    if (promoterGoalsDto != null) {
      return ResponseEntity.ok(promoterGoalsDto);
    }
    return new ResponseEntity<>(
        "No se encontraron promotores en esa sección", HttpStatus.NOT_FOUND);
  }

  @GetMapping("/findByCurp/{curp}")
  public ResponseEntity<?> findByCurp(@PathVariable("curp") String curp) {
    return ResponseEntity.ok(promotersService.findByCurp(curp));
  }

  @GetMapping("/findByIne/{ine}")
  public ResponseEntity<?> findByIne(@PathVariable("ine") String ine) {
    return ResponseEntity.ok(promotersService.findByIne(ine));
  }
}
