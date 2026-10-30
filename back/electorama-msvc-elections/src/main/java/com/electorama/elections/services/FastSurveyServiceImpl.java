package com.electorama.elections.services;

import com.electorama.elections.models.dto.CandidateAndMunicipalityDto;
import com.electorama.elections.models.dto.SurveyResultsDto;
import com.electorama.elections.models.repository.FastSurveyRepository;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FastSurveyServiceImpl implements FastSurveyService {

  @Autowired FastSurveyRepository fastSurveyRepository;

  @Override
  public List<SurveyResultsDto> getReportFastSurvey(Integer id) {
    List<Object[]> objects = new ArrayList<>();

    switch (id) {
        // BALANCAN
      case 1974:
        log.info("BALANCAN...");
        objects = fastSurveyRepository.getFastSurveyBalancan();
        break;
      case 1975:
        // CARDENAS
        log.info("CARDENAS...");
        objects = fastSurveyRepository.getFastSurveyCardenas();
        break;
      case 1976:
        // CENTLA
        log.info("CENTLA...");
        objects = fastSurveyRepository.getFastSurveyCentla();
        break;
      case 1977:
        // CENTRO
        log.info("CENTRO...");
        objects = fastSurveyRepository.getFastSurveyCentro();
        break;
      case 1978:
        // COMALCALCO
        log.info("COMALCALCO...");
        objects = fastSurveyRepository.getFastSurveyComalcalco();
        break;
      case 1979:
        // CUNDUACAN
        log.info("CUNDUACAN...");
        objects = fastSurveyRepository.getFastSurveyCunduacan();
        break;
      case 1980:
        // EMILIANO ZAPATA
        log.info("EMILIANO ZAPATA...");
        objects = fastSurveyRepository.getFastSurveyEmilianoZapata();
        break;
      case 1981:
        // HUIMANGUILLO
        log.info("HUIMANGUILLO...");
        objects = fastSurveyRepository.getFastSurveyHuimanguillo();
        break;
      case 1982:
        // JALAPA
        log.info("JALAPA...");
        objects = fastSurveyRepository.getFastSurveyJalapa();
        break;
      case 1983:
        // JALPA DE MENDEZ
        log.info("JALPA DE MENDEZ...");
        objects = fastSurveyRepository.getFastSurveyJalpa();
        break;
      case 1984:
        // JONUTA
        log.info("JONUTA...");
        objects = fastSurveyRepository.getFastSurveyJonuta();
        break;
      case 1985:
        // MACUSPANA
        log.info("MACUSPANA...");
        objects = fastSurveyRepository.getFastSurveyMacuspana();
        break;
      case 1986:
        // NACAJUCA
        log.info("NACAJUCA...");
        objects = fastSurveyRepository.getFastSurveyNacajuca();
        break;
      case 1987:
        // PARAISO
        log.info("PARAISO...");
        objects = fastSurveyRepository.getFastSurveyParaiso();
        break;
      case 1988:
        // TACOTALPA
        log.info("TACOTALPA...");
        objects = fastSurveyRepository.getFastSurveyTacotalpa();
        break;
      case 1989:
        // TEAPA
        log.info("TEAPA...");
        objects = fastSurveyRepository.getFastSurveyTeapa();
        break;
      case 1990:
        // TENOSIQUE
        log.info("TENOSIQUE...");
        objects = fastSurveyRepository.getFastSurveyTenosique();
        break;
      case 0:
        // GOBERNADOR
        log.info("GOERNADOR...");
        objects = fastSurveyRepository.getFastSurveyGobernador();
        break;
      default:
        break;
    }

    int idCounter = 1;
    List<SurveyResultsDto> result = new ArrayList<>();

    for (Object[] objArray : objects) {
      Timestamp dateCo = (Timestamp) objArray[0];
      LocalDateTime dateCollected = dateCo != null ? dateCo.toLocalDateTime() : null;

      CandidateAndMunicipalityDto dto = new CandidateAndMunicipalityDto();
      dto.setCandidate((String) objArray[1]);
      dto.setTotalCount(((Long) objArray[2]).intValue());

      // Buscar si ya existe un SurveyResultsDto con la misma fecha
      SurveyResultsDto edd = findSurveyResultsDtoByDate(result, dateCollected);
      if (edd == null) {
        edd = new SurveyResultsDto();
        edd.setDateCollected(dateCollected);
        edd.setResult(new ArrayList<>()); // Inicializar la lista de resultados
        result.add(edd);
      }

      // Agregar dto a la lista de resultados en SurveyResultsDto
      edd.getResult().add(dto);
    }
    return result;
  }

  private static SurveyResultsDto findSurveyResultsDtoByDate(
      List<SurveyResultsDto> result, LocalDateTime dateCollected) {
    for (SurveyResultsDto edd : result) {
      if (edd.getDateCollected().equals(dateCollected)) {
        return edd;
      }
    }
    return null;
  }
}
