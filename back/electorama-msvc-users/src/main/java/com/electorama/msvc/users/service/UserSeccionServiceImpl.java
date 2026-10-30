package com.electorama.msvc.users.service;

import com.electorama.msvc.users.model.dto.UserSeccionDto;
import com.electorama.msvc.users.model.mapper.UserSeccionMapper;
import com.electorama.msvc.users.model.repository.UserSeccionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSeccionServiceImpl implements UserSeccionService {

  private final UserSeccionRepository userSeccionRepository;
  private final UserSeccionMapper userSeccionMapper = new UserSeccionMapper();

  @Override
  @Transactional(readOnly = true)
  public List<UserSeccionDto> findByUser(String authId) {
    return userSeccionMapper.convertToDtoList(userSeccionRepository.findByUserAuthId(authId));
  }
}
