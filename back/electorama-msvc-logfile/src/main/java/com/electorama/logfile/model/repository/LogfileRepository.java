package com.electorama.logfile.model.repository;

import com.electorama.logfile.model.entity.LogfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogfileRepository extends JpaRepository<LogfileEntity, Long> {}
