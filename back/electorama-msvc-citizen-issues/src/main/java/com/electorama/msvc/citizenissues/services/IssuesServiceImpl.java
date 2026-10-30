package com.electorama.msvc.citizenissues.services;

import com.electorama.msvc.citizenissues.facade.ReportIssueStatusFacade;
import com.electorama.msvc.citizenissues.model.dto.IssueDetailDto;
import com.electorama.msvc.citizenissues.model.dto.IssueDto;
import com.electorama.msvc.citizenissues.model.dto.ReportIssueStatusDto;
import com.electorama.msvc.citizenissues.model.entity.*;
import com.electorama.msvc.citizenissues.model.mapper.*;
import com.electorama.msvc.citizenissues.model.repository.*;
import com.electorama.msvc.citizenissues.model.rest.MunicipalitiesResponse;
import com.electorama.msvc.citizenissues.model.specification.IssueDetailSpecification;
import com.electorama.msvc.citizenissues.services.rest.GeographyRestClient;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class IssuesServiceImpl implements IssuesService {

  @Autowired private IssueRepository issueRepository;

  @Autowired private IssueDetailRepository issueDetailRepository;

  @Autowired private IssueCategoryRepository issueCategoryRepository;

  @Autowired private IssuesStatusRepository issuesStatusRepository;

  @Autowired private IssuesTypesRepository issuesTypesRepository;

  @Autowired private GeographyRestClient geographyRestClient;

  @Autowired private ReportIssueStatusFacade reportIssueStatusFacade;

  @Autowired private UploadFileService uploadFileService;
  private final IssueSolutionResourceMapper issueSolutionResourceMapper =
      new IssueSolutionResourceMapper();

  private IssueDetailMapper issueDetailMapper = new IssueDetailMapper();

  private IssueMapper issueMapper = new IssueMapper();

  @Override
  public Page<IssueDto> findAll(
      Integer pageNo, Integer pageSize, String sortBy, String orderBy, String search) {
    Pageable paging;

    List<MunicipalitiesResponse> municipalitiesResponse =
        geographyRestClient.readMunicipalitiesByEntity();

    if (orderBy.equals("desc")) {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
    } else {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
    }

    if (search != null && !search.isEmpty()) {
      Page<IssueDto> issueEntityList =
          issueMapper.convertToDtoPage(
              issueRepository.findAllByDeletedDateIsNullAndInvoiceContainingIgnoreCase(
                  paging = PageRequest.of(pageNo, pageSize), search));
      issueEntityList.forEach(
          issueEntity -> {
            Optional<MunicipalitiesResponse> muni =
                municipalitiesResponse.stream()
                    .filter(
                        municipio -> {
                          return issueEntity
                              .getMunicipalityId()
                              .equals(Integer.parseInt(municipio.getId().toString()));
                        })
                    .findFirst();

            String nombreMunicipio = muni.map(MunicipalitiesResponse::getNomMun).orElse("");
            issueEntity.setMunicipalityName(nombreMunicipio);
            List<IssueDetailEntity> issueDetailEntityList =
                issueDetailRepository.findByIssueId(issueEntity.getId());
            issueEntity.setIssueDetailEntities(
                issueDetailMapper.convertToDtoList(issueDetailEntityList));
          });
      return issueEntityList;
    } else {
      Page<IssueDto> issueEntityList =
          issueMapper.convertToDtoPage(
              issueRepository.findAllByDeletedDateIsNull(
                  paging = PageRequest.of(pageNo, pageSize)));
      issueEntityList.forEach(
          issueEntity -> {
            Optional<MunicipalitiesResponse> muni =
                municipalitiesResponse.stream()
                    .filter(
                        municipio -> {
                          return issueEntity
                              .getMunicipalityId()
                              .equals(Integer.parseInt(municipio.getId().toString()));
                        })
                    .findFirst();

            String nombreMunicipio = muni.map(MunicipalitiesResponse::getNomMun).orElse("");
            issueEntity.setMunicipalityName(nombreMunicipio);
            List<IssueDetailEntity> issueDetailEntityList =
                issueDetailRepository.findByIssueId(issueEntity.getId());
            issueEntity.setIssueDetailEntities(
                issueDetailMapper.convertToDtoList(issueDetailEntityList));
          });
      return issueEntityList;
    }
  }

  @Override
  public Page<IssueDetailDto> findAllDetails(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      Integer municipalityId,
      String userCreateId,
      Integer categoryId,
      Integer statusId,
      Integer invoice,
      String isUrgent) {
    Pageable paging;
    List<MunicipalitiesResponse> municipalitiesResponse =
        geographyRestClient.readMunicipalitiesByEntity();

    Page<IssueDetailEntity> issueDetailDtoPage;
    Specification<IssueDetailEntity> spec =
        IssueDetailSpecification.withFilters(
            municipalityId, userCreateId, categoryId, statusId, search, true, invoice, isUrgent);

    issueDetailDtoPage =
        issueDetailRepository.findAll(spec, paging = PageRequest.of(pageNo, pageSize));

    Page<IssueDetailDto> issueDetailDtoList =
        issueDetailMapper.convertToDtoPage(issueDetailDtoPage);

    issueDetailDtoList.stream()
        .map(
            issueDetailDto -> {
              IssueEntity issueEntity =
                  issueRepository
                      .findById(issueDetailDto.getIssue().getId())
                      .orElseThrow(() -> new RuntimeException("Issue not found"));
              issueDetailDto.setIssue(issueMapper.convertToDto(issueEntity));
              Optional<MunicipalitiesResponse> muni =
                  municipalitiesResponse.stream()
                      .filter(
                          municipio ->
                              issueEntity
                                  .getMunicipalityId()
                                  .equals(Integer.parseInt(municipio.getId().toString())))
                      .findFirst();

              String nombreMunicipio = muni.map(MunicipalitiesResponse::getNomMun).orElse("");
              issueDetailDto.getIssue().setMunicipalityName(nombreMunicipio);
              if (issueEntity.getLocalityId() > 0) {
                issueDetailDto
                    .getIssue()
                    .setLocalityName(
                        geographyRestClient
                            .readLocalityById(issueEntity.getLocalityId())
                            .getLocalidad());
              }

              return issueDetailDto;
            })
        .toList();

    return issueDetailDtoList;
  }

  @Override
  public Page<IssueEntity> findByCreator(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String authId) {
    Pageable paging;
    Page<IssueEntity> issueEntityList =
        issueRepository.findAll(paging = PageRequest.of(pageNo, pageSize));
    issueEntityList.forEach(
        issueEntity -> {
          List<IssueDetailEntity> issueDetailEntityList =
              issueDetailRepository.findByIssueId(issueEntity.getId());
          issueEntity.setIssueDetailEntities(issueDetailEntityList);
        });

    return null;
  }

  @Override
  public IssueDto findById(Long id) {

    IssueEntity issueEntity =
        issueRepository.findById(id).orElseThrow(() -> new RuntimeException("Issue not found"));
    List<IssueDetailEntity> issueDetailEntityList =
        issueDetailRepository.findByIssueId(issueEntity.getId());
    issueEntity.setIssueDetailEntities(issueDetailEntityList);
    IssueDto issueDto = issueMapper.convertToDto(issueEntity);
    issueDto.setIssueDetailEntities(issueDetailMapper.convertToDtoList(issueDetailEntityList));
    issueDto.setMunicipalityName(
        geographyRestClient.readMunicipalityById(issueEntity.getMunicipalityId()).getNomMun());
    if (issueEntity.getLocalityId() != 0)
      issueDto.setLocalityName(
          geographyRestClient.readLocalityById(issueEntity.getLocalityId()).getLocalidad());
    return issueDto;
  }

  @Override
  public IssueDetailDto findDetailById(Long id) {
    IssueDetailEntity issueDetailEntity =
        issueDetailRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Issue not found"));
    IssueEntity issueEntity =
        issueRepository
            .findById(issueDetailEntity.getIssue().getId())
            .orElseThrow(() -> new RuntimeException("Issue not found"));
    issueDetailEntity.setIssue(issueEntity);

    IssueDetailDto issueDetailDto = issueDetailMapper.convertToDto(issueDetailEntity);
    issueDetailDto.setIssue(issueMapper.convertToDto(issueEntity));
    issueDetailDto
        .getIssue()
        .setMunicipalityName(
            geographyRestClient.readMunicipalityById(issueEntity.getMunicipalityId()).getNomMun());
    if (issueEntity.getLocalityId() != 0)
      issueDetailDto
          .getIssue()
          .setLocalityName(
              geographyRestClient.readLocalityById(issueEntity.getLocalityId()).getLocalidad());
    return issueDetailDto;
  }

  @Override
  public Page<IssueDetailDto> findDetailByUserCreateId(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String authId,
      Integer municipalityId,
      Integer categoryId,
      Integer statusId,
      Integer invoice,
      String isUrgent) {
    Pageable paging;
    List<MunicipalitiesResponse> municipalitiesResponse =
        geographyRestClient.readMunicipalitiesByEntity();

    Page<IssueDetailEntity> issueDetailDtoPage;
    Specification<IssueDetailEntity> spec =
        IssueDetailSpecification.withFilters(
            municipalityId, authId, categoryId, statusId, search, false, invoice, isUrgent);

    issueDetailDtoPage =
        issueDetailRepository.findAll(spec, paging = PageRequest.of(pageNo, pageSize));

    Page<IssueDetailDto> issueDetailDtoList =
        issueDetailMapper.convertToDtoPage(issueDetailDtoPage);
    //    Page<IssueDetailDto> issueDetailDtoList = issueDetailMapper.convertToDtoPage
    //
    // (issueDetailRepository.findByUserCreateIdIsNullAndUserCreateIdContainingIgnoreCaseAndDeletedDateIsNull
    //                    (  authId, paging = PageRequest.of(pageNo, pageSize)));

    issueDetailDtoList.stream()
        .map(
            issueDetailDto -> {
              IssueEntity issueEntity =
                  issueRepository
                      .findById(issueDetailDto.getIssue().getId())
                      .orElseThrow(() -> new RuntimeException("Issue not found"));
              issueDetailDto.setIssue(issueMapper.convertToDto(issueEntity));
              Optional<MunicipalitiesResponse> muni =
                  municipalitiesResponse.stream()
                      .filter(
                          municipio ->
                              issueEntity
                                  .getMunicipalityId()
                                  .equals(Integer.parseInt(municipio.getId().toString())))
                      .findFirst();

              String nombreMunicipio = muni.map(MunicipalitiesResponse::getNomMun).orElse("");
              issueDetailDto.getIssue().setMunicipalityName(nombreMunicipio);
              if (issueEntity.getLocalityId() != 0)
                issueDetailDto
                    .getIssue()
                    .setLocalityName(
                        geographyRestClient
                            .readLocalityById(issueEntity.getLocalityId())
                            .getLocalidad());
              return issueDetailDto;
            })
        .toList();

    return issueDetailDtoList;
  }

  @Override
  public IssueEntity save(IssueDto issue) {
    IssueEntity issueVo = new IssueEntity();
    MunicipalitiesResponse municipality =
        geographyRestClient.readMunicipalityById(issue.getMunicipalityId());

    issueVo = issueMapper.convertToEntity(issue);
    Integer conteo = issueRepository.countByMunicipalityId(Math.toIntExact(municipality.getId()));
    conteo++;
    if (issue.getId() == null)
      issueVo.setInvoice(municipality.getNomMun().substring(0, 5) + "-" + conteo);
    final IssueEntity issueResponse = issueRepository.save(issueVo);
    List<IssueDetailEntity> issueDetailEntityList =
        issue.getIssueDetailEntities().stream()
            .map(
                issueDetailDto -> {
                  IssueDetailEntity issueDetailEntity =
                      issueDetailMapper.convertToEntity(issueDetailDto);
                  if ((issue.getId() != null)) {
                    issueDetailEntity.setId(issue.getIssueDetailEntities().get(0).getId());
                  } else {
                    issueDetailEntity.setId(null);
                  }
                  issueDetailEntity.setIssue(issueResponse);
                  issueDetailEntity.setIssueCategory(
                      issueCategoryRepository
                          .findById(issueDetailDto.getIssueCategory().getId())
                          .orElseThrow(() -> new RuntimeException("Issue Category not found")));
                  issueDetailEntity.setIssueStatus(
                      issuesStatusRepository
                          .findById(issueDetailDto.getIssueStatus().getId())
                          .orElseThrow(() -> new RuntimeException("Issue Status not found")));
                  issueDetailEntity.setIssueType(
                      issuesTypesRepository
                          .findById(issueDetailDto.getIssueType().getId())
                          .orElseThrow(() -> new RuntimeException("Issue Type not found")));
                  issueDetailEntity.setIssueSolutionResourceTmp(
                      issueDetailDto.getIssueSolutionResourceTmp() != null
                          ? issueSolutionResourceMapper.convertToEntity(
                              issueDetailDto.getIssueSolutionResourceTmp())
                          : null);
                  issueDetailEntity.setRegistrationDate(new Date());
                  issueDetailEntity.setUserCreateId(issue.getUserCreateId());
                  if (issueDetailDto.getIssueSolution() == null) {
                    issueDetailEntity.setIssueSolution(null);
                  }
                  issueDetailDto.setPathFile(issueDetailDto.getPathFile());
                  return issueDetailEntity;
                })
            .toList();
    issueDetailRepository.saveAll(issueDetailEntityList);
    issueResponse.setIssueDetailEntities(issueDetailEntityList);

    return issueResponse;
  }

  @Override
  public IssueEntity deleteById(Long id) {
    IssueEntity issueEntity =
        issueRepository.findById(id).orElseThrow(() -> new RuntimeException("Issue not found"));
    issueEntity.setDeletedDate(new Date());
    issueRepository.save(issueEntity);

    return issueEntity;
  }

  @Override
  public IssueDetailDto deleteIssueDetailById(Long id) {
    IssueDetailEntity issueDetailEntity =
        issueDetailRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Issue not found"));
    issueDetailEntity.setDeletedDate(new Date());
    return issueDetailMapper.convertToDto(issueDetailRepository.save(issueDetailEntity));
  }

  @Override
  public IssueDetailDto hardDeleteIssueDetailById(Long id) {
    IssueDetailEntity issueDetailEntity =
        issueDetailRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Issue not found"));
    IssueEntity issuesEntity = issueRepository.getById(issueDetailEntity.getIssue().getId());
    issueDetailRepository.deleteById(id);
    if (issuesEntity.getIssueDetailEntities().size() == 0) {
      issueRepository.deleteById(issuesEntity.getId());
    }
    return issueDetailMapper.convertToDto(issueDetailEntity);
  }

  @Override
  public IssueDetailDto updateDetail(IssueDetailDto issueDetail) {
    IssueEntity ichu =
        issueRepository
            .findById(issueDetail.getIssue().getId())
            .orElseThrow(() -> new RuntimeException("Issue not found"));
    if (issueDetail.getIssueSolution() == null) {
      issueDetail.setIssueSolution(null);
    }
    if (issueDetail.getIssueSolutionResourceTmp() == null) {
      issueDetail.setIssueSolutionResourceTmp(null);
    }
    IssueDetailDto detailVo = new IssueDetailDto();
    ichu.setId(issueDetail.getIssue().getId());
    ichu.setInvoice(issueDetail.getIssue().getInvoice());
    ichu.setFullName(issueDetail.getIssue().getFullName());
    ichu.setPhone(issueDetail.getIssue().getPhone());
    ichu.setMunicipalityId(issueDetail.getIssue().getMunicipalityId());
    ichu.setRegistrationDate(issueDetail.getIssue().getRegistrationDate());
    ichu.setLocalityId(issueDetail.getIssue().getLocalityId());
    ichu.setLocalityOther(issueDetail.getIssue().getLocalityOther());
    issueRepository.save(ichu);
    if (detailVo != null) {

      detailVo =
          issueDetailMapper.convertToDto(
              issueDetailRepository.save(issueDetailMapper.convertToEntity(issueDetail)));
      detailVo
          .getIssue()
          .setMunicipalityName(
              geographyRestClient.readMunicipalityById(ichu.getMunicipalityId()).getNomMun());
      if (ichu.getLocalityId() != 0)
        detailVo
            .getIssue()
            .setLocalityName(
                geographyRestClient.readLocalityById(ichu.getLocalityId()).getLocalidad());
      detailVo.setPathFile(issueDetail.getPathFile());
    }
    return detailVo;
  }

  public ReportIssueStatusDto getReportIssueCategory(
      Integer municipalityId,
      String authId,
      Integer categoryId,
      Integer statusId,
      String search,
      Boolean isAdmin,
      Integer invoice,
      String isUrgent) {

    return reportIssueStatusFacade.getReporteAllVisitsByAdmin(
        municipalityId, authId, categoryId, statusId, search, isAdmin, invoice, isUrgent);
  }

  @Override
  public String uploadFile(
      MultipartFile file, Long issueDetailId, Integer issueSolutionDocumentsCategory, String uuid) {
    return uploadFileService.uploadFile(file, issueDetailId, issueSolutionDocumentsCategory, uuid);
  }
}
