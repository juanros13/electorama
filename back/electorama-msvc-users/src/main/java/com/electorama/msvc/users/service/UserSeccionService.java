package com.electorama.msvc.users.service;

import com.electorama.msvc.users.model.dto.UserSeccionDto;
import java.util.List;

public interface UserSeccionService {

  List<UserSeccionDto> findByUser(String authId);
}
