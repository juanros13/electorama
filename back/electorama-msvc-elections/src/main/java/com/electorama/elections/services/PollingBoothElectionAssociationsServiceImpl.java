package com.electorama.elections.services;

import com.electorama.elections.models.dto.*;
import com.electorama.elections.models.entity.ElectionEntity;
import com.electorama.elections.models.entity.PollingBoothElectionAssociationEntity;
import com.electorama.elections.models.mapper.*;
import com.electorama.elections.models.repository.*;
import com.electorama.elections.models.rest.ElectionResultResponse;
import com.electorama.elections.services.rest.ElectionsResultsRestClient;
import jakarta.persistence.EntityNotFoundException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class PollingBoothElectionAssociationsServiceImpl
    implements PollingBoothElectionAssociationService {
  @Autowired PollingBoothElectionAssociationRepository pollingBoothElectionAssociationRepository;

  @Autowired ElectionRepository electionRepository;
  @Autowired CandidateRepository candidateRepository;
  @Autowired PoliticalPartyRepository politicalPartyRepository;
  @Autowired CandidatePartyAssociationRepository candidatePartyAssociationRepository;
  @Autowired PollingBoothRepository pollingBoothRepository;

  @Autowired private DropboxService dropboxService;
  private final PollingBoothElectionAssociationMapper pollingBoothElectionAssociationMapper =
      new PollingBoothElectionAssociationMapper();

  private final ElectionYearMapper electionYearMapper = new ElectionYearMapper();
  private final ElectionPositionMapper electionPositionMapper = new ElectionPositionMapper();

  private final ElectionTypeMapper electionTypeMapper = new ElectionTypeMapper();

  private final CandidateMapper candidateMapper = new CandidateMapper();

  private final CandidatePartyAssociationMapper candidatePartyAssociationMapper =
      new CandidatePartyAssociationMapper();

  private final PoliticanPartyMapper politicanPartyMapper = new PoliticanPartyMapper();

  private final PollingBoothMapper pollingBoothMapper = new PollingBoothMapper();

  @Autowired private ElectionsResultsRestClient electionsResultsRestClient;

  @Override
  public List<ElectionByPollingBoothsDto> getElectionsByPollingBooths(List<Long> pollingBooths) {
    List<ElectionByPollingBoothsDto> result = new ArrayList<>();
    AtomicLong idCounter = new AtomicLong(1);

    List<PollingBoothElectionAssociationDto> associationDto =
        pollingBoothElectionAssociationMapper.convertToDtoList(
            pollingBoothElectionAssociationRepository.findByPollingBoothIdIn(pollingBooths));

    for (PollingBoothElectionAssociationDto association : associationDto) {

      Optional<PollingBoothDto> pollingBoothDto =
          pollingBoothMapper.convertToDtoOptional(
              pollingBoothRepository.findById(association.getPollingBoothId()));

      Map<Long, ElectionDto> electionMap = new HashMap<>();
      ElectionDto electionDtoList =
          electionMap.getOrDefault(association.getPollingBoothId(), new ElectionDto());

      Optional<ElectionEntity> electionEntity =
          electionRepository.findById(association.getElectionId());

      ElectionDto electionDto = null;
      if (electionEntity != null) {
        List<CandidateDto> candidateDtoList =
            candidateMapper.convertToDtoList(
                candidateRepository.findByElection_Id(electionEntity.get().getId()));

        List<Long> idsCandidates =
            candidateDtoList.stream().map(CandidateDto::getId).collect(Collectors.toList());

        // Fetching partyAssociationDtoList based on candidates' IDs
        List<CandidatePartyAssociationDto> partyAssociationDtoList =
            candidatePartyAssociationMapper.convertToDtoList(
                candidatePartyAssociationRepository.findByCandidate_IdIn(idsCandidates));

        List<Long> idsPoliticalParties =
            partyAssociationDtoList.stream()
                .map(
                    CandidatePartyAssociationDto ->
                        CandidatePartyAssociationDto.getPoliticalParty().getId())
                .collect(Collectors.toList());

        List<PoliticalPartyDto> politicalPartyDtoList =
            politicanPartyMapper.convertToDtoList(
                politicalPartyRepository.findByIdIn(idsPoliticalParties));

        ElectionAssociationIdsDto electionAssociationIdsDto = new ElectionAssociationIdsDto();

        List<Long> idsElectionAssociationIds = new ArrayList<>();
        idsElectionAssociationIds.add(association.getId());
        electionAssociationIdsDto.setElectionAssociationIds(idsElectionAssociationIds);

        List<ElectionResultResponse> resultResponses =
            electionsResultsRestClient.findElectionsAssociationIn(electionAssociationIdsDto);

        for (PoliticalPartyDto politicalPartyDto : politicalPartyDtoList) {
          List<CandidateDto> candidatesForParty = new ArrayList<>();
          for (CandidatePartyAssociationDto associationDto1 : partyAssociationDtoList) {
            if (associationDto1.getPoliticalParty().getId().equals(politicalPartyDto.getId())) {
              politicalPartyDto.setSoftorder(associationDto1.getSortOrder());
              for (ElectionResultResponse resultResponse : resultResponses) {
                if (associationDto1
                        .getPoliticalParty()
                        .getId()
                        .equals(resultResponse.getPoliticalPartyId())
                    && association.getId().equals(resultResponse.getElectionAssociationId())) {
                  politicalPartyDto.setResult(resultResponse.getResult());
                }
              }

              // Find the corresponding CandidateDto using candidateId from associationDto
              for (CandidateDto candidateDto : candidateDtoList) {
                if (candidateDto.getId().equals(associationDto1.getCandidate().getId())) {
                  candidatesForParty.add(candidateDto);
                  break; // Stop searching for candidates once found
                }
              }
            }
          }
          politicalPartyDto.setCandidate(candidatesForParty);
        }

        electionDto = new ElectionDto();
        electionDto.setId(electionEntity.get().getId());
        electionDto.setName(electionEntity.get().getName());
        electionDto.setElectionYear(
            electionYearMapper.convertToDto(electionEntity.get().getElectionYear()));
        electionDto.setElectionPosition(
            electionPositionMapper.convertToDto(electionEntity.get().getElectionPosition()));
        electionDto.setElectionType(
            electionTypeMapper.convertToDto(electionEntity.get().getElectionType()));
        electionDto.setPoliticalPartyList(politicalPartyDtoList);
      }

      electionMap.put(association.getPollingBoothId(), electionDtoList);

      ElectionByPollingBoothsDto dto = new ElectionByPollingBoothsDto();
      dto.setId(idCounter.getAndIncrement());
      dto.setElectionAssociationId(association.getId());
      dto.setIsSynchronized(association.getIsSynchronized());
      dto.setVotersList(pollingBoothDto.get().getVotersList());
      dto.setVotersRegistry(pollingBoothDto.get().getVotersRegistry());
      dto.setPollingBoothId(association.getPollingBoothId());
      dto.setElection(electionDto);
      result.add(dto);
    }

    return result;
  }

  @Transactional
  public void updateSynchronized(Long id) {
    Optional<PollingBoothElectionAssociationEntity> associationOptional =
        pollingBoothElectionAssociationRepository.findById(id);

    if (associationOptional.isPresent()) {
      PollingBoothElectionAssociationEntity association = associationOptional.get();
      association.setIsSynchronized(true);

      if (association.getIsValidatedSheet() == 1) {
        association.setIsValidatedSheet(4);
      }

      pollingBoothElectionAssociationRepository.save(association);
    } else {
      throw new EntityNotFoundException(
          "No se encontró ninguna asociación con el ID proporcionado: " + id);
    }
  }

  @Override
  public PollingBoothElectionAssociationDto save(Long electionAssociationId, MultipartFile file) {
    String ext;
    Random random = new Random();
    int randomWithNextInt = random.nextInt() & Integer.MAX_VALUE;
    LocalDate currentDate = LocalDate.now();
    List<String> contentTypes = Arrays.asList("image/jpg", "image/jpeg", "image/png");

    ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));

    PollingBoothElectionAssociationEntity associationEntity =
        pollingBoothElectionAssociationRepository
            .findById(electionAssociationId)
            .orElse(new PollingBoothElectionAssociationEntity());
    if (associationEntity.getIsValidatedSheet() == 1
        || associationEntity.getIsValidatedSheet() == 4) {
      associationEntity.setIsValidatedSheet(5);
    }

    Optional<PollingBoothDto> pollingBoothDto =
        pollingBoothMapper.convertToDtoOptional(
            pollingBoothRepository.findById(associationEntity.getPollingBoothId()));

    String path = "/SOMOSLAMAYORIA/DEFENSA/" + pollingBoothDto.get().getPollingBoothKey() + "/";
    String fileName =
        "EVIDENCIA_"
            + associationEntity.getPollingBoothId()
            + "_"
            + currentDate
            + "_"
            + randomWithNextInt
            + ext;

    if (associationEntity.getPathSheet() != null) {
      fileName =
          associationEntity
              .getPathSheet()
              .substring(associationEntity.getPathSheet().lastIndexOf("/") + 1);
      path = associationEntity.getPathSheet().replace(fileName, "");
    }

    try {
      // Rotar la imagen horizontalmente
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      Thumbnails.of(file.getInputStream())
          .rotate(-90) // girar horizontalmente
          .outputFormat(
              "jpg") // especificar el formato de salida, puedes cambiarlo según tus necesidades
          .scale(1)
          .toOutputStream(outputStream);

      byte[] rotatedImage = outputStream.toByteArray();

      MultipartFile rotatedFile =
          new ByteArrayMultipartFile(
              rotatedImage, file.getName(), file.getOriginalFilename(), file.getContentType());
      // Subir el archivo rotado a Dropbox
      dropboxService.uploadFile(rotatedFile, fileName, path, contentTypes);
      associationEntity.setPathSheet(path + fileName);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return pollingBoothElectionAssociationMapper.convertToDto(
        pollingBoothElectionAssociationRepository.save(associationEntity));
  }

  @Override
  public Optional<PollingBoothElectionAssociationDto> getPollingBoothsElectionAssociation(Long id) {
    Optional<PollingBoothElectionAssociationEntity> associationEntity =
        pollingBoothElectionAssociationRepository.findById(id);
    if (associationEntity.isPresent()) {
      return Optional.of(
          pollingBoothElectionAssociationMapper.convertToDto(associationEntity.get()));
    }
    return Optional.empty();
  }

  @Transactional
  public void updateValidate(Long id, Integer statusId) {
    Optional<PollingBoothElectionAssociationEntity> associationOptional =
        pollingBoothElectionAssociationRepository.findById(id);

    if (associationOptional.isPresent()) {
      PollingBoothElectionAssociationEntity association = associationOptional.get();
      association.setIsValidatedSheet(statusId);
      pollingBoothElectionAssociationRepository.save(association);
    } else {
      throw new EntityNotFoundException(
          "No se encontró ninguna asociación con el ID proporcionado: " + id);
    }
  }
}
