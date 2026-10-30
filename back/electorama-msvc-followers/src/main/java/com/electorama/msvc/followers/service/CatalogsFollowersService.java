package com.electorama.msvc.followers.service;

import com.electorama.msvc.followers.model.dto.CatFamilyRoleDto;
import com.electorama.msvc.followers.model.dto.CatMaritalStatusDto;
import com.electorama.msvc.followers.model.dto.CatSexDto;
import java.util.List;

public interface CatalogsFollowersService {

  List<CatFamilyRoleDto> findAllFamilyRole();

  List<CatMaritalStatusDto> findAllMaritalStatus();

  List<CatSexDto> findAllSex();
}
