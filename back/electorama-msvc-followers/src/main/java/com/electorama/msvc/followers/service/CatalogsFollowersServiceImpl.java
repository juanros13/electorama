package com.electorama.msvc.followers.service;

import com.electorama.msvc.followers.model.dto.CatFamilyRoleDto;
import com.electorama.msvc.followers.model.dto.CatMaritalStatusDto;
import com.electorama.msvc.followers.model.dto.CatSexDto;
import com.electorama.msvc.followers.model.mapper.CatFamilyRoleMapper;
import com.electorama.msvc.followers.model.mapper.CatMaritalStatusMapper;
import com.electorama.msvc.followers.model.mapper.CatSexMapper;
import com.electorama.msvc.followers.model.repository.CatFamilyRoleRepository;
import com.electorama.msvc.followers.model.repository.CatMaritalStatusRepository;
import com.electorama.msvc.followers.model.repository.CatSexRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CatalogsFollowersServiceImpl implements CatalogsFollowersService {

  @Autowired private CatFamilyRoleRepository catFamilyRoleRepository;

  @Autowired private CatMaritalStatusRepository catMaritalStatusRepository;

  @Autowired private CatSexRepository catSexRepository;

  private final CatSexMapper catSexMapper = new CatSexMapper();

  private final CatFamilyRoleMapper catFamilyRoleMapper = new CatFamilyRoleMapper();

  private final CatMaritalStatusMapper catMaritalStatusMapper = new CatMaritalStatusMapper();

  @Override
  public List<CatFamilyRoleDto> findAllFamilyRole() {
    return new ArrayList<>(catFamilyRoleMapper.convertToDto(catFamilyRoleRepository.findAll()));
  }

  @Override
  public List<CatMaritalStatusDto> findAllMaritalStatus() {
    return new ArrayList<>(
        catMaritalStatusMapper.convertToDto(catMaritalStatusRepository.findAll()));
  }

  @Override
  public List<CatSexDto> findAllSex() {
    return new ArrayList<>(catSexMapper.convertToDto(catSexRepository.findAll()));
  }
}
