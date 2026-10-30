package com.electorama.logfile.service;

import com.electorama.logfile.model.dto.LogfileDto;
import com.electorama.logfile.model.entity.LogfileEntity;
import com.electorama.logfile.model.mapper.LogfileMapper;
import com.electorama.logfile.model.repository.LogfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogfileServiceImpl implements LogfileService {

  @Autowired private LogfileRepository logfileRepository;
  private final LogfileMapper logfileMapper = new LogfileMapper();

  public LogfileDto save(LogfileDto logfile, String authId) {

    LogfileEntity followersEntity = logfileMapper.convertToEntity(logfile, authId);
    followersEntity.setUserAuthId(authId);
    return logfileMapper.convertToDto(logfileRepository.save(followersEntity));
  }
}
