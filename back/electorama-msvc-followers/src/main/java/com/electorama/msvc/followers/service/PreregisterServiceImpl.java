package com.electorama.msvc.followers.service;

import com.electorama.msvc.followers.exeptions.EntityNotFoundException;
import com.electorama.msvc.followers.exeptions.InvalidEntityException;
import com.electorama.msvc.followers.model.dto.PreregisterDto;
import com.electorama.msvc.followers.model.entity.PreregisterEntity;
import com.electorama.msvc.followers.model.mapper.PreregisterMapper;
import com.electorama.msvc.followers.model.repository.PreRegisterRepository;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PreregisterServiceImpl implements PreregisterService {

  @Autowired private PreRegisterRepository preRegisterRepository;

  @Autowired private PreregisterMapper preregisterMapper;

  @Override
  public PreregisterDto save(PreregisterDto preregisterDto) {
    if (preregisterDto == null) {
      throw new InvalidEntityException("PreregisterDto cannot be null");
    }

    Date date;
    try {
      date = new SimpleDateFormat("yyyy-MM-dd").parse(preregisterDto.getDate());
    } catch (ParseException e) {
      throw new InvalidEntityException("Invalid date format");
    }

    PreregisterEntity entity = preregisterMapper.toEntity(preregisterDto);
    entity.setCreated(date);
    entity = preRegisterRepository.save(entity);
    return preregisterMapper.toDto(entity);
  }

  @Override
  public PreregisterDto findById(Long id) {
    PreregisterEntity entity =
        preRegisterRepository
            .findById(id)
            .orElseThrow(
                () -> new EntityNotFoundException("PreregisterEntity not found with id " + id));
    return preregisterMapper.toDto(entity);
  }

  @Override
  public Page<PreregisterDto> findAll(Pageable pageable) {
    return preRegisterRepository.findAll(pageable).map(preregisterMapper::toDto);
  }

  @Override
  public Page<PreregisterDto> findByDeletedIsNull(Pageable pageable) {
    return preRegisterRepository.findByDeletedIsNull(pageable).map(preregisterMapper::toDto);
  }

  @Override
  public Page<PreregisterDto> findByDeletedIsNotNull(Pageable pageable) {
    return preRegisterRepository.findByDeletedIsNotNull(pageable).map(preregisterMapper::toDto);
  }

  @Override
  public void delete(Long id) {
    if (!preRegisterRepository.existsById(id)) {
      throw new EntityNotFoundException("PreregisterEntity not found with id " + id);
    }
    preRegisterRepository.deleteById(id);
  }
}
