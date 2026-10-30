package com.electorama.logfile.service;

import com.electorama.logfile.model.dto.GoalsDto;
import com.electorama.logfile.model.dto.IndicatorsDto;
import com.electorama.logfile.model.dto.InstrumentsDto;
import com.electorama.logfile.model.dto.OdsDto;
import java.util.List;
import java.util.Optional;

public interface OdsService {

  List<OdsDto> getOdsList();

  Optional<OdsDto> getOdsById(Long id);

  List<GoalsDto> getGoalsList();

  Optional<GoalsDto> getGoalsById(Long id);

  List<GoalsDto> getGoalsByOdsId(Long id);

  List<IndicatorsDto> getIndicatorsList();

  Optional<IndicatorsDto> getIndicatorsById(Long id);

  List<IndicatorsDto> getIndicatorsByGoalsId(Long id);

  List<InstrumentsDto> getInstrumentList();

  Optional<InstrumentsDto> getInstrumentsById(Long id);

  List<InstrumentsDto> getInstrumentsByIndicatorsId(Long id);
}
