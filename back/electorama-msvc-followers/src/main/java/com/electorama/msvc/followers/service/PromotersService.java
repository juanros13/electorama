package com.electorama.msvc.followers.service;

import com.electorama.msvc.followers.model.dto.PromoterGoalsDto;
import com.electorama.msvc.followers.model.dto.PromotersDto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface PromotersService {
  Page<PromotersDto> list(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String authId);

  Page<PromotersDto> listAdmin(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String authId);

}
