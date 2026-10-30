package com.electorama.logfile.service;

import com.electorama.logfile.model.dto.GoalsDto;
import com.electorama.logfile.model.dto.IndicatorsDto;
import com.electorama.logfile.model.dto.InstrumentsDto;
import com.electorama.logfile.model.dto.OdsDto;
import com.electorama.logfile.model.entity.GoalsEntity;
import com.electorama.logfile.model.entity.IndicatorsEntity;
import com.electorama.logfile.model.entity.InstrumentsEntity;
import com.electorama.logfile.model.entity.OdsEntity;
import com.electorama.logfile.model.mapper.GoalsMapper;
import com.electorama.logfile.model.mapper.IndicatorsMapper;
import com.electorama.logfile.model.mapper.InstrumentsMapper;
import com.electorama.logfile.model.mapper.OdsMapper;
import com.electorama.logfile.model.repository.GoalsRepository;
import com.electorama.logfile.model.repository.IndicatorsRepository;
import com.electorama.logfile.model.repository.InstrumentRepository;
import com.electorama.logfile.model.repository.OdsRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OdsServiceImpl implements OdsService {

  @Autowired GoalsRepository goalsRepository;

  @Autowired IndicatorsRepository indicatorsRepository;

  @Autowired InstrumentRepository instrumentRepository;

  @Autowired OdsRepository odsRepository;

  private final GoalsMapper goalsMapper = new GoalsMapper();
  private final OdsMapper osdMapper = new OdsMapper();
  private final IndicatorsMapper indicatorsMapper = new IndicatorsMapper();

  private final InstrumentsMapper instrumentsMapper = new InstrumentsMapper();

  @Override
  public List<OdsDto> getOdsList() {
    List<OdsEntity> odsEntityList = odsRepository.findAll();
    return odsEntityList.stream().map(osdMapper::convertToDto).toList();
  }

  @Override
  public Optional<OdsDto> getOdsById(Long id) {
    Optional<OdsEntity> odsEntity = odsRepository.findById(id);
    return odsEntity.map(osdMapper::convertToDto);
  }

  @Override
  public List<GoalsDto> getGoalsList() {
    List<GoalsEntity> goalsEntityList = goalsRepository.findAll();
    return goalsEntityList.stream().map(goalsMapper::convertToDto).toList();
  }

  @Override
  public Optional<GoalsDto> getGoalsById(Long id) {
    Optional<GoalsEntity> goalsEntity = goalsRepository.findById(id);
    return goalsEntity.map(goalsMapper::convertToDto);
  }

  @Override
  public List<GoalsDto> getGoalsByOdsId(Long id) {
    List<GoalsEntity> goalsEntityList = goalsRepository.findByOdsId(id);
    return goalsEntityList.stream().map(goalsMapper::convertToDto).toList();
  }

  @Override
  public List<IndicatorsDto> getIndicatorsList() {
    List<IndicatorsEntity> goalsEntityList = indicatorsRepository.findAll();
    return goalsEntityList.stream().map(indicatorsMapper::convertToDto).toList();
  }

  @Override
  public Optional<IndicatorsDto> getIndicatorsById(Long id) {
    Optional<IndicatorsEntity> goalsEntity = indicatorsRepository.findById(id);
    return goalsEntity.map(indicatorsMapper::convertToDto);
  }

  @Override
  public List<IndicatorsDto> getIndicatorsByGoalsId(Long id) {
    List<IndicatorsEntity> goalsEntityList = indicatorsRepository.findByGoalId(id);
    return goalsEntityList.stream().map(indicatorsMapper::convertToDto).toList();
  }

  @Override
  public List<InstrumentsDto> getInstrumentList() {
    List<InstrumentsEntity> goalsEntityList = instrumentRepository.findAll();
    return goalsEntityList.stream().map(instrumentsMapper::convertToDto).toList();
  }

  @Override
  public Optional<InstrumentsDto> getInstrumentsById(Long id) {
    Optional<InstrumentsEntity> goalsEntity = instrumentRepository.findById(id);
    return goalsEntity.map(instrumentsMapper::convertToDto);
  }

  @Override
  public List<InstrumentsDto> getInstrumentsByIndicatorsId(Long id) {
    List<InstrumentsEntity> goalsEntityList = instrumentRepository.findByIndicatorId(id);
    return goalsEntityList.stream().map(instrumentsMapper::convertToDto).toList();
  }
}
