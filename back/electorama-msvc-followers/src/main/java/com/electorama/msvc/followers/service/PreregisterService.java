package com.electorama.msvc.followers.service;

import com.electorama.msvc.followers.model.dto.PreregisterDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PreregisterService {
  PreregisterDto save(PreregisterDto preregisterDto);

  PreregisterDto findById(Long id);

  Page<PreregisterDto> findAll(Pageable pageable);

  Page<PreregisterDto> findByDeletedIsNull(Pageable pageable);

  Page<PreregisterDto> findByDeletedIsNotNull(Pageable pageable);

  void delete(Long id);
}
