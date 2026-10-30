package com.electorama.msvc.canvasandwalls.service;

import com.electorama.msvc.canvasandwalls.model.dto.CanvasAndWallCategoryDto;
import com.electorama.msvc.canvasandwalls.model.dto.CanvasAndWallProyectDto;
import com.electorama.msvc.canvasandwalls.model.dto.CanvasAndWallsDto;
import com.electorama.msvc.canvasandwalls.model.dto.ReportPDFDto;
import com.electorama.msvc.canvasandwalls.model.entity.*;
import com.electorama.msvc.canvasandwalls.model.mapper.CanvasAndWallsCategoryMapper;
import com.electorama.msvc.canvasandwalls.model.mapper.CanvasAndWallsMapper;
import com.electorama.msvc.canvasandwalls.model.mapper.CanvasAndWallsProyectMapper;
import com.electorama.msvc.canvasandwalls.model.repository.CanvasAndWallsCategoryRepository;
import com.electorama.msvc.canvasandwalls.model.repository.CanvasAndWallsProyectRepository;
import com.electorama.msvc.canvasandwalls.model.repository.CanvasAndWallsRepository;
import com.electorama.msvc.canvasandwalls.model.repository.ReportPDFRepository;
import com.electorama.msvc.canvasandwalls.model.rest.LocalityResponse;
import com.electorama.msvc.canvasandwalls.model.rest.MunicipalitiesResponse;
import com.electorama.msvc.canvasandwalls.model.rest.SeccionResponse;
import com.electorama.msvc.canvasandwalls.model.rest.UserProfileResponse;
import com.electorama.msvc.canvasandwalls.service.rest.GeographyRestClient;
import com.electorama.msvc.canvasandwalls.service.rest.UsersRestClient;
import com.itextpdf.text.*;
import jakarta.persistence.EntityNotFoundException;
import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CanvasAndWallServiceImpl implements CanvasAndWallService {

  @Autowired CanvasAndWallsRepository canvasAndWallsRepository;

  @Autowired CanvasAndWallsProyectRepository canvasAndWallsProyectRepository;

  @Autowired CanvasAndWallsCategoryRepository canvasAndWallsCategoryRepository;

  @Autowired GeographyRestClient geographyRestClient;

  @Autowired private DropboxService dropboxService;

  @Autowired private ReportPDFRepository reportPDFRepository;

  @Autowired UsersRestClient usuariosRestClient;

  private final CanvasAndWallsMapper canvasAndWallsMapper = new CanvasAndWallsMapper();

  private final CanvasAndWallsProyectMapper canvasAndWallsProyectMapper =
      new CanvasAndWallsProyectMapper();

  private final CanvasAndWallsCategoryMapper canvasAndWallsCategoryMapper =
      new CanvasAndWallsCategoryMapper();

  @Override
  public Page<CanvasAndWallsDto> listAll(
      Integer pageNo, Integer pageSize, String sortBy, String orderBy, String search) {
    Pageable paging;

    if (orderBy.equals("desc")) {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
    } else {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
    }
    Page<CanvasAndWallsDto> dtoPage = null;
    Page<CanvasAndWallsEntity> entities;
    if (search.length() >= 1) {
      entities =
          canvasAndWallsRepository.findByMunicipalitynameOrSeccionNumberOrLocalityNameOrId(
              search, search, search, search, paging);
    } else {
      entities = canvasAndWallsRepository.findAll(paging);
    }
    dtoPage = canvasAndWallsMapper.convertToDtoPage(entities);
    List<UserProfileResponse> userProfileResponse = usuariosRestClient.readUserAllCoorZone();

    List<UserProfileResponse> userProfileResponseMun = usuariosRestClient.readUserAllCoorMun();

    dtoPage.stream()
        .map(
            dto -> {
              Optional<UserProfileResponse> useProfileOptional =
                  userProfileResponse.stream()
                      .filter(response -> dto.getCoordinateZoneId().equals(response.getAuthId()))
                      .findFirst();

              String firstName =
                  useProfileOptional.map(UserProfileResponse::getFirstName).orElse("");
              String lastName = useProfileOptional.map(UserProfileResponse::getLastName).orElse("");
              dto.setCoordinateZone((firstName + " " + lastName).trim());
              useProfileOptional =
                  userProfileResponseMun.stream()
                      .filter(response -> dto.getReferenceAuthId().equals(response.getAuthId()))
                      .findFirst();

              firstName = useProfileOptional.map(UserProfileResponse::getFirstName).orElse("");
              lastName = useProfileOptional.map(UserProfileResponse::getLastName).orElse("");
              dto.setReference((firstName + " " + lastName).trim());

              return dto;
            })
        .collect(Collectors.toList());
    return dtoPage;
  }

  @Override
  public Page<CanvasAndWallsDto> listByRole(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String authIdHeader,
      String roleType) {
    Pageable paging;

    if (orderBy.equals("desc")) {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
    } else {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
    }
    Page<CanvasAndWallsDto> dtoPage = null;
    Page<CanvasAndWallsEntity> entities;

    if (search.length() >= 1) {
      if (roleType.equals("CORDINATOR_MUNICIPAL")) {
        entities =
            canvasAndWallsRepository
                .findByReferenceAuthIdAndMunicipalitynameOrSeccionNumberOrLocalityName(
                    authIdHeader, search, search, search, search, paging);
      } else {
        entities =
            canvasAndWallsRepository
                .findByCoordinateZoneIdAndMunicipalitynameOrSeccionNumberOrLocalityNameOrId(
                    authIdHeader, search, search, search, search, paging);
      }

    } else {
      entities =
          canvasAndWallsRepository.findByReferenceAuthIdOrCoordinateZoneId(
              authIdHeader, authIdHeader, paging);
    }

    dtoPage = canvasAndWallsMapper.convertToDtoPage(entities);
    // List<UserProfileResponse> userProfileResponse = usuariosRestClient.readUserAllCoorZone();

    List<UserProfileResponse> userProfileResponse = new ArrayList<>();
    List<UserProfileResponse> userProfileResponseMun;
    if (roleType.equals("CORDINATOR_ZONE_ALT")) {
      userProfileResponseMun = usuariosRestClient.readUserAllCoorMunAlt();
    } else {
      userProfileResponseMun = usuariosRestClient.readUserAllCoorMun();
    }

    dtoPage.stream()
        .map(
            dto -> {
              for (UserProfileResponse userProfile : userProfileResponse) {
                if (userProfile.getAuthId() == dto.getCoordinateZoneId()) {
                  String firstName = userProfile.getFirstName();
                  String lastName = userProfile.getLastName();

                  dto.setCoordinateZone((firstName + " " + lastName).trim());
                }
              }
              if (dto.getCoordinateZone() == null) {
                UserProfileResponse userProfileResponseAdd =
                    usuariosRestClient.readUserXAuthId(dto.getCoordinateZoneId());
                if (userProfileResponseAdd != null && userProfileResponseAdd.getId() != null) {
                  userProfileResponse.add(userProfileResponseAdd);
                  String firstName = userProfileResponseAdd.getFirstName();
                  String lastName = userProfileResponseAdd.getLastName();

                  dto.setCoordinateZone((firstName + " " + lastName).trim());
                }
              }

              //              Optional<UserProfileResponse> useProfileOptional =
              //                  userProfileResponse.stream()
              //                      .filter(
              //                          userProfileResponse1 ->
              //
              // dto.getCoordinateZoneId().equals(userProfileResponse1.getAuthId()))
              //                      .findFirst();
              //
              //              String firstName =
              //
              // useProfileOptional.map(UserProfileResponse::getFirstName).orElse("");
              //              String lastName =
              // useProfileOptional.map(UserProfileResponse::getLastName).orElse("");
              //              dto.setCoordinateZone((firstName + " " + lastName).trim());

              Optional<UserProfileResponse> useProfileOptional =
                  userProfileResponseMun.stream()
                      .filter(
                          userProfileResponse1 ->
                              dto.getReferenceAuthId().equals(userProfileResponse1.getAuthId()))
                      .findFirst();

              String firstName =
                  useProfileOptional.map(UserProfileResponse::getFirstName).orElse("");
              String lastName = useProfileOptional.map(UserProfileResponse::getLastName).orElse("");
              dto.setReference((firstName + " " + lastName).trim());

              return dto;
            })
        .collect(Collectors.toList());
    return dtoPage;
  }

  @Override
  public CanvasAndWallsDto save(
      CanvasAndWallsDto canvasAndWallsDto, MultipartFile file, String authId) {
    CanvasAndWallsEntity canvasAndWallsEntity = new CanvasAndWallsEntity();

    String ext;
    Random random = new Random();
    int randomWithNextInt = random.nextInt() & Integer.MAX_VALUE;
    LocalDate currentDate = LocalDate.now();
    List<String> contentTypes =
        Arrays.asList("image/jpg", "image/jpeg", "image/png", "application/pdf");

    ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
    String path = "/SOMOSLAMAYORIA/BARDAS_LONAS/";
    String fileName = "EVIDENCIA_" + currentDate + "_" + randomWithNextInt + ext;

    CanvasAndWallsEntity canvasAndWallsExistente =
        canvasAndWallsRepository
            .findFirstByUuid(canvasAndWallsDto.getUuid())
            .orElse(new CanvasAndWallsEntity());

    if (canvasAndWallsExistente.getUuid() == null) {
      canvasAndWallsEntity = canvasAndWallsMapper.convertToEntity(canvasAndWallsDto, authId);
    } else {
      canvasAndWallsEntity = canvasAndWallsMapper.convertToEntity(canvasAndWallsDto, authId);
      canvasAndWallsEntity.setId(canvasAndWallsExistente.getId());
    }

    if (canvasAndWallsDto.getPathEvidencePhoto() != null) {
      fileName =
          canvasAndWallsDto
              .getPathEvidencePhoto()
              .substring(canvasAndWallsDto.getPathEvidencePhoto().lastIndexOf("/") + 1);
      path = canvasAndWallsDto.getPathEvidencePhoto().replace(fileName, "");
    }

    dropboxService.uploadFile(file, fileName, path, contentTypes);
    canvasAndWallsEntity.setPathEvidencePhoto(path + fileName);

    SeccionResponse seccion =
        geographyRestClient.readSeccionById(canvasAndWallsEntity.getSeccionId());

    MunicipalitiesResponse municipality =
        geographyRestClient.readMunicipalityById(canvasAndWallsEntity.getMunicipalityId());

    LocalityResponse locality =
        geographyRestClient.readLocalityById(canvasAndWallsEntity.getLocalityId());
    canvasAndWallsEntity.setSeccionNumber(seccion.getSeccion().toString());
    canvasAndWallsEntity.setMunicipalityname(municipality.getNomMun());
    canvasAndWallsEntity.setLocalityName(locality.getLocalidad());

    return canvasAndWallsMapper.convertToDto(canvasAndWallsRepository.save(canvasAndWallsEntity));
  }

  @Override
  public CanvasAndWallsDto saveDelete(CanvasAndWallsDto canvasAndWallsDto, String authId) {

    CanvasAndWallsEntity canvasAndWallsEntity =
        canvasAndWallsMapper.convertToEntity(canvasAndWallsDto, authId);
    canvasAndWallsEntity.setUserCreatedId(authId);

    return canvasAndWallsMapper.convertToDto(canvasAndWallsRepository.save(canvasAndWallsEntity));
  }

  @Override
  public List<CanvasAndWallProyectDto> getProyects() {
    List<CanvasAndWallsProyectEntity> canvasAndWallsEntity =
        canvasAndWallsProyectRepository.findAll();
    return canvasAndWallsProyectMapper.convertToDtoList(canvasAndWallsEntity);
  }

  @Override
  public List<CanvasAndWallCategoryDto> getCategories() {
    List<CanvasAndWallsCategoryEntity> canvasAndWallsEntity =
        canvasAndWallsCategoryRepository.findAll();
    return canvasAndWallsCategoryMapper.convertToDtoList(canvasAndWallsEntity);
  }

  @Override
  public Optional<CanvasAndWallsDto> byId(Long id, String authId) {
    CanvasAndWallsEntity canvasAndWallsEntity =
        canvasAndWallsRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    return Optional.of(canvasAndWallsMapper.convertToDto(canvasAndWallsEntity));
  }

  @Override
  public List<ReportPDFDto> armaPDFS() {
    List<ReportPDFDto> canvasAndWalls = reportPDFRepository.findAllByParametros();

    return canvasAndWalls;
  }
}
