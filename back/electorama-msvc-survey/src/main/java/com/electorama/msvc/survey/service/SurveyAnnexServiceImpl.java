package com.electorama.msvc.survey.service;

import com.electorama.msvc.survey.model.dto.SurveyAnnexDto;
import com.electorama.msvc.survey.model.dto.SurveyAnnexListDto;
import com.electorama.msvc.survey.model.dto.request.RequestSurveyAnexAllFile;
import com.electorama.msvc.survey.model.dto.request.RequestSurveyAnexFile;
import com.electorama.msvc.survey.model.entity.SurveyAnnexEntity;
import com.electorama.msvc.survey.model.mapper.*;
import com.electorama.msvc.survey.model.repository.SurveyAnnexRepository;
import com.electorama.msvc.survey.model.repository.SurveyUserRepository;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class SurveyAnnexServiceImpl implements SurveyAnnexService {

  @Autowired private SurveyAnnexRepository surveyAnnexRepository;

  @Autowired private DropboxService dropboxService;

  @Autowired private SurveyUserRepository surveyUserRepository;

  private final SurveyAnnexMapper surveyAnnexMapper = new SurveyAnnexMapper();
  private final SurveyCulturalActivityMapper surveyCulturalActivityMapper =
      new SurveyCulturalActivityMapper();
  private final SurveyHousingServiceMapper surveyHousingServiceMapper =
      new SurveyHousingServiceMapper();
  private final SurveyUserFolioMapper surveyUserFolioMapper = new SurveyUserFolioMapper();

  @Override
  public SurveyAnnexListDto save(SurveyAnnexListDto surveyAnnexListDtoList) {
    List<SurveyAnnexDto> resultList = new ArrayList<>();
    SurveyAnnexListDto result = new SurveyAnnexListDto();

    for (SurveyAnnexDto surveyAnnexListDto : surveyAnnexListDtoList.getSurveyAnnexList()) {

      Optional<SurveyAnnexEntity> findAnnex =
          surveyAnnexRepository.findFirstBySurveyPersonIdOrderBySurveyPersonIdDesc(
              surveyAnnexListDto.getSurveyPersonId());

      SurveyAnnexEntity entity;
      if (findAnnex.isPresent()) {
        entity = findAnnex.get();
        BeanUtils.copyProperties(surveyAnnexListDto, entity, "id");
      } else {
        entity = surveyAnnexMapper.convertToEntity(surveyAnnexListDto);
      }

      SurveyAnnexEntity updatedEntity = surveyAnnexRepository.save(entity);
      // Convierte la entidad guardada de nuevo a DTO y la agrega a la lista de resultados
      SurveyAnnexDto dto = surveyAnnexMapper.convertToDto(updatedEntity);
      resultList.add(dto);
    }
    result.setSurveyAnnexList(resultList);
    return result;
  }

  public Page<SurveyAnnexDto> list(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String deviceId,
      String surveyorName,
      Date sgeSurveyDate,
      Boolean statusChange) {
    Pageable paging =
        PageRequest.of(
            pageNo,
            pageSize,
            orderBy.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending());
    String formattedDate = "";
    if (sgeSurveyDate != null) {
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      formattedDate = formatter.format(sgeSurveyDate);
    }

    Page<SurveyAnnexEntity> entityPage =
        surveyAnnexRepository.searchByMultipleParameters(deviceId, paging);
    return entityPage.map(surveyAnnexMapper::convertToDto);
  }

  @Override
  public Page<SurveyAnnexDto> listFolio(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String deviceId,
      String surveyorName,
      Date sgeSurveyDate,
      Boolean statusChange,
      String authIdHeader) {
    Pageable paging =
        PageRequest.of(
            pageNo,
            pageSize,
            orderBy.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending());
    String formattedDate = "";
    if (sgeSurveyDate != null) {
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      formattedDate = formatter.format(sgeSurveyDate);
    }

    Page<SurveyAnnexEntity> entityPage =
        surveyAnnexRepository.searchByAuthIdAndNotStreet(authIdHeader, paging);
    return entityPage.map(surveyAnnexMapper::convertToDto);
  }

  @Override
  public Page<SurveyAnnexDto> list(
      Integer pageNo, Integer pageSize, String sortBy, String orderBy, String search) {
    Pageable paging;

    if (orderBy.equals("desc")) {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
    } else {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
    }
    Page<SurveyAnnexDto> dtoPage = null;

    if (search.length() >= 1) {
      dtoPage =
          surveyAnnexMapper.convertToDtoPage(
              surveyAnnexRepository.findByParameters(search, search, search, paging));
    } else {
      dtoPage = surveyAnnexMapper.convertToDtoPage(surveyAnnexRepository.findAll(paging));
    }

    dtoPage =
        new PageImpl<>(
            dtoPage.stream()
                .map(
                    page -> {
                      try {
                      } catch (Exception e) {

                      }
                      return page;
                    })
                .collect(Collectors.toList()),
            dtoPage.getPageable(),
            dtoPage.getTotalElements());

    return dtoPage;
  }

  @Override
  public Optional<SurveyAnnexDto> findById(Long id) {
    return surveyAnnexRepository.findById(id).map(surveyAnnexMapper::convertToDto);
  }

  @Override
  public Boolean uploadFile(RequestSurveyAnexFile requestSurveyAnexFile) {
    Optional<SurveyAnnexEntity> surveyAnnexEntity =
        surveyAnnexRepository.findFirstBySurveyPersonIdOrderBySurveyPersonIdDesc(
            requestSurveyAnexFile.getSurveyPersonId());

    if (surveyAnnexEntity.isEmpty()) {
      return false;
    }
    String path = "/CENSO2024/ARCHIVOSANEXO/";
    LocalDate currentDate = LocalDate.now();
    Random random = new Random();

    List<String> contentTypes =
        Arrays.asList("image/jpg", "image/jpeg", "image/png", "application/pdf");

    int randomWithNextInt = random.nextInt() & Integer.MAX_VALUE;
    // INE ADVERSO
    String ext =
        requestSurveyAnexFile
            .getFile()
            .getOriginalFilename()
            .substring(requestSurveyAnexFile.getFile().getOriginalFilename().lastIndexOf("."));
    //    private String pathFileIdentificationApplicant;
    //    private String pathFileBirthCertificateApplicant;
    //    private String pathFileCurpApplicant;
    //    private String pathFileAddressApplicant;
    //
    //
    //    private String pathFileIdentificationAssistant;
    //    private String pathFileBirthCertificateAssistant;
    //    private String pathFileCurpAssistant;
    //    private String pathFileAddressAssistant;
    String fileName = "";

    // if (surveyAnnexEntity.get().getPathFileIdentificationApplicant() != null) {
    // }

    if (requestSurveyAnexFile.getCategory().equals("identificationApplicant")) {
      fileName = "IDENTIFICATION_APPLICANT_" + currentDate + "_" + randomWithNextInt + ext;
    } else if (requestSurveyAnexFile.getCategory().equals("birthCertificateApplicant")) {
      fileName = "BIRTH_CERTIFICATE_APPLICANT_" + currentDate + "_" + randomWithNextInt + ext;
    } else if (requestSurveyAnexFile.getCategory().equals("curpApplicant")) {
      fileName = "CURP_APPLICANT_" + currentDate + "_" + randomWithNextInt + ext;
    } else if (requestSurveyAnexFile.getCategory().equals("addressApplicant")) {
      fileName = "ADDRESS_APPLICANT_" + currentDate + "_" + randomWithNextInt + ext;
    } else if (requestSurveyAnexFile.getCategory().equals("identificationAssistant")) {
      fileName = "IDENTIFICATION_ASSISTANT_" + currentDate + "_" + randomWithNextInt + ext;
    } else if (requestSurveyAnexFile.getCategory().equals("birthCertificateAssistant")) {
      fileName = "BIRTH_CERTIFICATE_ASSISTANT_" + currentDate + "_" + randomWithNextInt + ext;
    } else if (requestSurveyAnexFile.getCategory().equals("curpAssistant")) {
      fileName = "CURP_ASSISTANT_" + currentDate + "_" + randomWithNextInt + ext;
    } else if (requestSurveyAnexFile.getCategory().equals("addressAssistant")) {
      fileName = "ADDRESS_ASSISTANT_" + currentDate + "_" + randomWithNextInt + ext;
    } else {
      return false;
    }

    String file = "";
    try {
      file =
          dropboxService.uploadFile(requestSurveyAnexFile.getFile(), fileName, path, contentTypes);
    } catch (Exception e) {
      try {
        file =
            dropboxService.uploadFile(
                requestSurveyAnexFile.getFile(), fileName, path, contentTypes);
      } catch (Exception e2) {
        try {
          file =
              dropboxService.uploadFile(
                  requestSurveyAnexFile.getFile(), fileName, path, contentTypes);
        } catch (Exception e3) {
          return false;
        }
      }
    }
    if (requestSurveyAnexFile.getCategory().equals("identificationApplicant")) {
      if (surveyAnnexEntity.get().getPathFileIdentificationApplicant() == null) {
        surveyAnnexEntity.get().setPathFileIdentificationApplicant(file);
      }
    } else if (requestSurveyAnexFile.getCategory().equals("birthCertificateApplicant")) {
      if (surveyAnnexEntity.get().getPathFileBirthCertificateApplicant() == null) {
        surveyAnnexEntity.get().setPathFileBirthCertificateApplicant(file);
      }
    } else if (requestSurveyAnexFile.getCategory().equals("curpApplicant")) {
      if (surveyAnnexEntity.get().getPathFileCurpApplicant() == null) {
        surveyAnnexEntity.get().setPathFileCurpApplicant(file);
      }
    } else if (requestSurveyAnexFile.getCategory().equals("addressApplicant")) {
      if (surveyAnnexEntity.get().getPathFileAddressApplicant() == null) {
        surveyAnnexEntity.get().setPathFileAddressApplicant(file);
      }
    } else if (requestSurveyAnexFile.getCategory().equals("identificationAssistant")) {
      if (surveyAnnexEntity.get().getPathFileIdentificationAssistant() == null) {
        surveyAnnexEntity.get().setPathFileIdentificationAssistant(file);
      }
    } else if (requestSurveyAnexFile.getCategory().equals("birthCertificateAssistant")) {
      if (surveyAnnexEntity.get().getPathFileBirthCertificateAssistant() == null) {
        surveyAnnexEntity.get().setPathFileBirthCertificateAssistant(file);
      }
    } else if (requestSurveyAnexFile.getCategory().equals("curpAssistant")) {
      if (surveyAnnexEntity.get().getPathFileCurpAssistant() == null) {
        surveyAnnexEntity.get().setPathFileCurpAssistant(file);
      }
    } else if (requestSurveyAnexFile.getCategory().equals("addressAssistant")) {
      if (surveyAnnexEntity.get().getPathFileAddressAssistant() == null) {
        surveyAnnexEntity.get().setPathFileAddressAssistant(file);
      }
    }
    surveyAnnexRepository.save(surveyAnnexEntity.get());
    return true;
  }

  @Override
  public Boolean uploadFileAll(RequestSurveyAnexAllFile requestSurveyAnexFile) {
    Optional<SurveyAnnexEntity> surveyAnnexEntity =
        surveyAnnexRepository.findFirstBySurveyPersonIdOrderBySurveyPersonIdDesc(
            requestSurveyAnexFile.getSurveyPersonId());

    if (surveyAnnexEntity.isEmpty()) {
      return false;
    }
    String path = "/CENSO2024/ARCHIVOSANEXO/";
    LocalDate currentDate = LocalDate.now();
    Random random = new Random();

    List<String> contentTypes =
        Arrays.asList("image/jpg", "image/jpeg", "image/png", "application/pdf");

    int randomWithNextInt = random.nextInt() & Integer.MAX_VALUE;
    // identificationApplicantFile
    if (requestSurveyAnexFile.getIdentificationApplicantFile() != null) {
      String ext =
          requestSurveyAnexFile
              .getIdentificationApplicantFile()
              .getOriginalFilename()
              .substring(
                  requestSurveyAnexFile
                      .getIdentificationApplicantFile()
                      .getOriginalFilename()
                      .lastIndexOf("."));
      String fileName = "IDENTIFICATION_APPLICANT_" + currentDate + "_" + randomWithNextInt + ext;
      String identificationApplicant = "";
      try {
        identificationApplicant =
            dropboxService.uploadFile(
                requestSurveyAnexFile.getIdentificationApplicantFile(),
                fileName,
                path,
                contentTypes);
      } catch (Exception e) {
        try {
          identificationApplicant =
              dropboxService.uploadFile(
                  requestSurveyAnexFile.getIdentificationApplicantFile(),
                  fileName,
                  path,
                  contentTypes);
        } catch (Exception e2) {
          try {
            identificationApplicant =
                dropboxService.uploadFile(
                    requestSurveyAnexFile.getIdentificationApplicantFile(),
                    fileName,
                    path,
                    contentTypes);
          } catch (Exception e3) {
            return false;
          }
        }
      }
      if (surveyAnnexEntity.get().getPathFileIdentificationApplicant() == null) {
        surveyAnnexEntity.get().setPathFileIdentificationApplicant(identificationApplicant);
      }
    }
    // birthCertificateApplicant
    if (requestSurveyAnexFile.getBirthCertificateApplicantFile() != null) {
      String extBirthCertificateApplicant =
          requestSurveyAnexFile
              .getBirthCertificateApplicantFile()
              .getOriginalFilename()
              .substring(
                  requestSurveyAnexFile
                      .getBirthCertificateApplicantFile()
                      .getOriginalFilename()
                      .lastIndexOf("."));
      String fileNameBirthCertificateApplicant =
          "BIRTH_CERTIFICATE_APPLICANT_"
              + currentDate
              + "_"
              + randomWithNextInt
              + extBirthCertificateApplicant;
      String birthCertificateApplicant = "";
      try {
        birthCertificateApplicant =
            dropboxService.uploadFile(
                requestSurveyAnexFile.getBirthCertificateApplicantFile(),
                fileNameBirthCertificateApplicant,
                path,
                contentTypes);
      } catch (Exception e) {
        try {
          birthCertificateApplicant =
              dropboxService.uploadFile(
                  requestSurveyAnexFile.getBirthCertificateApplicantFile(),
                  fileNameBirthCertificateApplicant,
                  path,
                  contentTypes);
        } catch (Exception e2) {
          try {
            birthCertificateApplicant =
                dropboxService.uploadFile(
                    requestSurveyAnexFile.getBirthCertificateApplicantFile(),
                    fileNameBirthCertificateApplicant,
                    path,
                    contentTypes);
          } catch (Exception e3) {
            return false;
          }
        }
      }
      if (surveyAnnexEntity.get().getPathFileBirthCertificateApplicant() == null) {
        surveyAnnexEntity.get().setPathFileBirthCertificateApplicant(birthCertificateApplicant);
      }
    }
    // curpApplicant
    if (requestSurveyAnexFile.getCurpApplicantFile() != null) {
      String extCurpApplicant =
          requestSurveyAnexFile
              .getCurpApplicantFile()
              .getOriginalFilename()
              .substring(
                  requestSurveyAnexFile
                      .getCurpApplicantFile()
                      .getOriginalFilename()
                      .lastIndexOf("."));
      String fileCurpApplicant =
          "CURP_APPLICANT_" + currentDate + "_" + randomWithNextInt + extCurpApplicant;
      String curpApplicant = "";
      try {
        curpApplicant =
            dropboxService.uploadFile(
                requestSurveyAnexFile.getCurpApplicantFile(),
                fileCurpApplicant,
                path,
                contentTypes);
      } catch (Exception e) {
        try {
          curpApplicant =
              dropboxService.uploadFile(
                  requestSurveyAnexFile.getCurpApplicantFile(),
                  fileCurpApplicant,
                  path,
                  contentTypes);
        } catch (Exception e2) {
          try {
            curpApplicant =
                dropboxService.uploadFile(
                    requestSurveyAnexFile.getCurpApplicantFile(),
                    fileCurpApplicant,
                    path,
                    contentTypes);
          } catch (Exception e3) {
            return false;
          }
        }
      }
      if (surveyAnnexEntity.get().getPathFileCurpApplicant() == null) {
        surveyAnnexEntity.get().setPathFileCurpApplicant(curpApplicant);
      }
    }

    // addressApplicant
    if (requestSurveyAnexFile.getAddressApplicantFile() != null) {
      String extAddressApplicant =
          requestSurveyAnexFile
              .getAddressApplicantFile()
              .getOriginalFilename()
              .substring(
                  requestSurveyAnexFile
                      .getAddressApplicantFile()
                      .getOriginalFilename()
                      .lastIndexOf("."));
      String fileAddressApplicant =
          "CURP_APPLICANT_" + currentDate + "_" + randomWithNextInt + extAddressApplicant;
      String addressApplicant = "";
      try {
        addressApplicant =
            dropboxService.uploadFile(
                requestSurveyAnexFile.getAddressApplicantFile(),
                fileAddressApplicant,
                path,
                contentTypes);
      } catch (Exception e) {
        try {
          addressApplicant =
              dropboxService.uploadFile(
                  requestSurveyAnexFile.getAddressApplicantFile(),
                  fileAddressApplicant,
                  path,
                  contentTypes);
        } catch (Exception e2) {
          try {
            addressApplicant =
                dropboxService.uploadFile(
                    requestSurveyAnexFile.getAddressApplicantFile(),
                    fileAddressApplicant,
                    path,
                    contentTypes);
          } catch (Exception e3) {
            return false;
          }
        }
      }
      if (surveyAnnexEntity.get().getPathFileAddressApplicant() == null) {
        surveyAnnexEntity.get().setPathFileAddressApplicant(addressApplicant);
      }
    }
    // identificationAssistant
    if (requestSurveyAnexFile.getIdentificationAssistantFile() != null) {
      String extIdentificationAssistant =
          requestSurveyAnexFile
              .getIdentificationAssistantFile()
              .getOriginalFilename()
              .substring(
                  requestSurveyAnexFile
                      .getIdentificationAssistantFile()
                      .getOriginalFilename()
                      .lastIndexOf("."));
      String fileIdentificationAssistant =
          "IDENTIFICATION_ASSISTANT_"
              + currentDate
              + "_"
              + randomWithNextInt
              + extIdentificationAssistant;
      String identificationAssistant = "";
      try {
        identificationAssistant =
            dropboxService.uploadFile(
                requestSurveyAnexFile.getIdentificationAssistantFile(),
                fileIdentificationAssistant,
                path,
                contentTypes);
      } catch (Exception e) {
        try {
          identificationAssistant =
              dropboxService.uploadFile(
                  requestSurveyAnexFile.getIdentificationAssistantFile(),
                  fileIdentificationAssistant,
                  path,
                  contentTypes);
        } catch (Exception e2) {
          try {
            identificationAssistant =
                dropboxService.uploadFile(
                    requestSurveyAnexFile.getIdentificationAssistantFile(),
                    fileIdentificationAssistant,
                    path,
                    contentTypes);
          } catch (Exception e3) {
            return false;
          }
        }
      }
      if (surveyAnnexEntity.get().getPathFileIdentificationAssistant() == null) {
        surveyAnnexEntity.get().setPathFileIdentificationAssistant(identificationAssistant);
      }
    }
    // birthCertificateAssistant
    if (requestSurveyAnexFile.getBirthCertificateAssistantFile() != null) {
      String extBirthCertificateAssistant =
          requestSurveyAnexFile
              .getBirthCertificateAssistantFile()
              .getOriginalFilename()
              .substring(
                  requestSurveyAnexFile
                      .getBirthCertificateAssistantFile()
                      .getOriginalFilename()
                      .lastIndexOf("."));
      String fileBirthCertificateAssistant =
          "BIRTH_CERTIFICATE_ASSISTANT_"
              + currentDate
              + "_"
              + randomWithNextInt
              + extBirthCertificateAssistant;
      String birthCertificateAssistant = "";
      try {
        birthCertificateAssistant =
            dropboxService.uploadFile(
                requestSurveyAnexFile.getBirthCertificateAssistantFile(),
                fileBirthCertificateAssistant,
                path,
                contentTypes);
      } catch (Exception e) {
        try {
          birthCertificateAssistant =
              dropboxService.uploadFile(
                  requestSurveyAnexFile.getBirthCertificateAssistantFile(),
                  fileBirthCertificateAssistant,
                  path,
                  contentTypes);
        } catch (Exception e2) {
          try {
            birthCertificateAssistant =
                dropboxService.uploadFile(
                    requestSurveyAnexFile.getBirthCertificateAssistantFile(),
                    fileBirthCertificateAssistant,
                    path,
                    contentTypes);
          } catch (Exception e3) {
            return false;
          }
        }
      }
      if (surveyAnnexEntity.get().getPathFileBirthCertificateAssistant() == null) {
        surveyAnnexEntity.get().setPathFileBirthCertificateAssistant(birthCertificateAssistant);
      }
    }
    // curpAssistant
    if (requestSurveyAnexFile.getCurpAssistantFile() != null) {
      String extCurpAssistant =
          requestSurveyAnexFile
              .getCurpAssistantFile()
              .getOriginalFilename()
              .substring(
                  requestSurveyAnexFile
                      .getCurpAssistantFile()
                      .getOriginalFilename()
                      .lastIndexOf("."));
      String fileCurpAssistant =
          "CURP_ASSISTANT_" + currentDate + "_" + randomWithNextInt + extCurpAssistant;
      String curpAssistant = "";
      try {
        curpAssistant =
            dropboxService.uploadFile(
                requestSurveyAnexFile.getCurpAssistantFile(),
                fileCurpAssistant,
                path,
                contentTypes);
      } catch (Exception e) {
        try {
          curpAssistant =
              dropboxService.uploadFile(
                  requestSurveyAnexFile.getCurpAssistantFile(),
                  fileCurpAssistant,
                  path,
                  contentTypes);
        } catch (Exception e2) {
          try {
            curpAssistant =
                dropboxService.uploadFile(
                    requestSurveyAnexFile.getCurpAssistantFile(),
                    fileCurpAssistant,
                    path,
                    contentTypes);
          } catch (Exception e3) {
            return false;
          }
        }
      }
      if (surveyAnnexEntity.get().getPathFileCurpAssistant() == null) {
        surveyAnnexEntity.get().setPathFileCurpAssistant(curpAssistant);
      }
    }
    // addressAssistant
    if (requestSurveyAnexFile.getAddressAssistantFile() != null) {
      String extAddressAssistant =
          requestSurveyAnexFile
              .getAddressAssistantFile()
              .getOriginalFilename()
              .substring(
                  requestSurveyAnexFile
                      .getAddressAssistantFile()
                      .getOriginalFilename()
                      .lastIndexOf("."));
      String fileAddressAssistant =
          "ADDRESS_ASSISTANT_" + currentDate + "_" + randomWithNextInt + extAddressAssistant;
      String addressAssistant = "";
      try {
        addressAssistant =
            dropboxService.uploadFile(
                requestSurveyAnexFile.getCurpAssistantFile(),
                fileAddressAssistant,
                path,
                contentTypes);
      } catch (Exception e) {
        try {
          addressAssistant =
              dropboxService.uploadFile(
                  requestSurveyAnexFile.getCurpAssistantFile(),
                  fileAddressAssistant,
                  path,
                  contentTypes);
        } catch (Exception e2) {
          try {
            addressAssistant =
                dropboxService.uploadFile(
                    requestSurveyAnexFile.getCurpAssistantFile(),
                    fileAddressAssistant,
                    path,
                    contentTypes);
          } catch (Exception e3) {
            return false;
          }
        }
      }
      if (surveyAnnexEntity.get().getPathFileAddressAssistant() == null) {
        surveyAnnexEntity.get().setPathFileAddressAssistant(addressAssistant);
      }
    }

    surveyAnnexRepository.save(surveyAnnexEntity.get());
    return true;
  }

  @Override
  public Optional<SurveyAnnexDto> update(Long id, SurveyAnnexDto requestSurveys) {
    return surveyAnnexRepository
        .findById(id)
        .map(
            existingSurvey -> {
              existingSurvey = surveyAnnexMapper.convertToEntity(requestSurveys);
              SurveyAnnexEntity updatedEntity = surveyAnnexRepository.save(existingSurvey);
              SurveyAnnexDto updatedRequest = new SurveyAnnexDto();
              updatedRequest = surveyAnnexMapper.convertToDto(updatedEntity);
              return updatedRequest;
            });
  }

  @Override
  public List<SurveyAnnexDto> findBySurveyId(Long surveyId) {
    List<SurveyAnnexEntity> annexEntities = surveyAnnexRepository.findBySurveyId(surveyId);
    return annexEntities.stream().map(surveyAnnexMapper::convertToDto).collect(Collectors.toList());
  }
}
