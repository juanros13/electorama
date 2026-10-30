package com.electorama.logfile.service;

import com.electorama.logfile.model.dto.LogfileDto;

public interface LogfileService {

  LogfileDto save(LogfileDto logfile, String authId);
}
