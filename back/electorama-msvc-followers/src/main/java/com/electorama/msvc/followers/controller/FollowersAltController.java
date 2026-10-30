package com.electorama.msvc.followers.controller;

import com.electorama.msvc.followers.model.dto.FollowersDto;
import com.electorama.msvc.followers.model.dto.ResponseRelocateFollower;
import com.electorama.msvc.followers.service.FollowersAltService;
import com.electorama.msvc.followers.service.FollowersSirenaService;
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
@RequestMapping(value = "/followersAlt")
public class FollowersAltController {

  @Autowired private FollowersAltService followersServiceAlt;

  @Autowired private FollowersSirenaService followersSirenaService;

  @GetMapping()
  public ResponseEntity<?> list(
      @RequestParam(defaultValue = "0") Integer pageNo,
      @RequestParam(defaultValue = "30") Integer pageSize,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "desc") String orderBy,
      @RequestParam(defaultValue = "") String search,
      @RequestParam(defaultValue = "") String promoterAuthId,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    Map<String, Object> response = new HashMap<>();
    try {
      if (servletRequest.isUserInRole("CORDINATOR_MUNICIPAL_ALT")) {
        response.put(
            "followers",
            followersServiceAlt.listByReferent(
                pageNo, pageSize, sortBy, orderBy, search, authIdHeader));
      } else {
        response.put(
            "followers",
            followersServiceAlt.list(
                pageNo, pageSize, sortBy, orderBy, search, authIdHeader, promoterAuthId));
      }
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

  @PostMapping("/createNew")
  public ResponseEntity createNew(
      @Valid @RequestBody FollowersDto request,
      BindingResult result,
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    if (result.hasErrors()) {
      return validar(result);
    }

    log.info("Creating follower with {}", request.toString());

    FollowersDto followers = new FollowersDto();
    try {
      followers = followersServiceAlt.saveNew(request, authIdHeader);

    } catch (Exception e) {
      return new ResponseEntity<>(e, HttpStatus.CONFLICT);
    }
    return ResponseEntity.ok(followers);
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> editFollower(
      @Valid @RequestBody FollowersDto follower,
      BindingResult result,
      @PathVariable Long id,
      @RequestHeader("X-Auth-Id") String authIdHeader) {

    if (result.hasErrors()) {
      return validar(result);
    }

    Optional<FollowersDto> o = followersServiceAlt.byId(id, authIdHeader);
    if (o.isPresent()) {
      FollowersDto followerDB = o.get();

      followerDB.setNames(follower.getNames());
      followerDB.setSurName(follower.getSurName());
      followerDB.setLastName(follower.getLastName());
      followerDB.setCurp(follower.getCurp());
      followerDB.setIne(follower.getIne());
      followerDB.setCatSex(follower.getCatSex());
      followerDB.setBirthdate(follower.getBirthdate());
      followerDB.setCellPhoneNumber(follower.getCellPhoneNumber());
      followerDB.setCellPhoneNumberOther(follower.getCellPhoneNumberOther());
      followerDB.setStatus(follower.getStatus());
      followerDB.setEmail(follower.getEmail());
      followerDB.setCatMaritalStatus(follower.getCatMaritalStatus());
      followerDB.setBirthEntityId(follower.getBirthEntityId());
      followerDB.setCatFamilyRole(follower.getCatFamilyRole());
      followerDB.setSectionNumber(follower.getSectionNumber());

      followerDB.setValidity(follower.getValidity());
      followerDB.setIsAdult(follower.getIsAdult());

      followerDB.setIsAdult(follower.getIsAdult());
      followerDB.setIsInSync(true);
      followerDB.setStreet(follower.getStreet());
      followerDB.setNeighborhood(follower.getNeighborhood());

      followerDB.setExternalNumber(follower.getExternalNumber());
      followerDB.setInternalNumber(follower.getInternalNumber());
      followerDB.setResidenceEntityId(follower.getResidenceEntityId());
      followerDB.setResidenceMunicipalityId(follower.getResidenceMunicipalityId());
      followerDB.setSupportWhatsapp(follower.getSupportWhatsapp());

      followerDB.setSupportsMovement(follower.getSupportsMovement());
      followerDB.setCommitteeParticipation(follower.getCommitteeParticipation());
      followerDB.setPollinStationRepresentativeParticipation(
          follower.getPollinStationRepresentativeParticipation());
      followerDB.setInvitingOthersToParticipate(follower.getInvitingOthersToParticipate());
      followerDB.setNumberOfPeopleInvited(follower.getNumberOfPeopleInvited());

      followerDB.setLongitude(follower.getLongitude());
      followerDB.setLatitude(follower.getLatitude());
      followerDB.setSectionNumberIne(follower.getSectionNumberIne());

      return ResponseEntity.status(HttpStatus.CREATED)
          .body(followersServiceAlt.save(followerDB, authIdHeader));
    }
    return ResponseEntity.notFound().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteFollower(
      @PathVariable Long id, @RequestHeader("X-Auth-Id") String authIdHeader) {
    Optional<FollowersDto> o;
    try {
      o = followersServiceAlt.byId(id, authIdHeader);
    } catch (Exception e) {
      return new ResponseEntity<>(e, HttpStatus.CONFLICT);
    }

    if (o.isPresent()) {
      FollowersDto followersDto = o.get();

      long miliseconds = System.currentTimeMillis();
      Date date = new Date(miliseconds);

      followersDto.setDeleted(date);
      followersDto.setPromoterAltAuthId(null);
      followersDto.setUserCreateId(null);

      followersSirenaService.deleteByFollowerId(id, authIdHeader);

      return ResponseEntity.status(HttpStatus.OK)
          .body(followersServiceAlt.update(followersDto, authIdHeader));
    } else {
      return ResponseEntity.notFound().build();
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

  @GetMapping("/{id}")
  public ResponseEntity<?> detail(
      @PathVariable Long id, @RequestHeader("X-Auth-Id") String authIdHeader) {
    Optional<FollowersDto> followerOptional;
    try {
      followerOptional = followersServiceAlt.byId(id, authIdHeader);

    } catch (Exception e) {
      return new ResponseEntity<>(e, HttpStatus.CONFLICT);
    }

    if (followerOptional.isPresent()) {
      return ResponseEntity.ok(followerOptional.get());
    }
    return ResponseEntity.notFound().build();
  }

  @GetMapping("/getFollowerByPromoterId/{promoterAuthId}")
  public ResponseEntity<?> listFollowerByPromoterId(
      @PathVariable String promoterAuthId, @RequestHeader("X-Auth-Id") String authIdHeader) {
    List<FollowersDto> listFollower;
    try {
      listFollower = followersServiceAlt.byPromoterId(promoterAuthId, authIdHeader);

    } catch (Exception e) {
      return new ResponseEntity<>(e, HttpStatus.CONFLICT);
    }

    if (listFollower.size() > 0) {
      return ResponseEntity.ok(listFollower);
    }
    return ResponseEntity.ok(new ArrayList<>());
  }

  @PostMapping("/relocateFollowers")
  public ResponseEntity relocateFollower(
      @Valid @RequestBody ResponseRelocateFollower request,
      BindingResult result,
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    if (result.hasErrors()) {
      return validar(result);
    }
    Map<String, Object> response = new HashMap<>();

    log.info("Creating follower with {}", request.toString());

    FollowersDto followers = new FollowersDto();
    try {
      if (followersServiceAlt.relocateFollowers(request)) {
        response.put("success", "true");
        response.put("message", "Los simpatizantes fueron actualizados correctamente");

      } else {
        response.put("success", "false");
        response.put("message", "Error al realizar la actualizacion");
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
      }
    } catch (Exception e) {
      return new ResponseEntity<>(e, HttpStatus.CONFLICT);
    }
    return ResponseEntity.ok(response);
  }

  @GetMapping("/findByCurp/{curp}")
  public ResponseEntity<?> findByCurp(@PathVariable("curp") String curp) {
    return ResponseEntity.ok(followersServiceAlt.findByCurp(curp));
  }

  @GetMapping("/findByIne/{ine}")
  public ResponseEntity<?> findByIne(@PathVariable("ine") String ine) {
    return ResponseEntity.ok(followersServiceAlt.findByIne(ine));
  }

  @GetMapping("/findTotalFollowersAlt/{authId}")
  public ResponseEntity<?> findTotalFollowersAlt(@PathVariable("authId") String ine) {
    return ResponseEntity.ok(followersServiceAlt.findTotalFollowerAlt(ine));
  }
}
