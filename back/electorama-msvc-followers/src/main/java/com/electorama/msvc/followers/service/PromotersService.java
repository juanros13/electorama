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

  PromotersDto save(PromotersDto promotersDto, String authId);

  Optional<PromotersDto> byId(Long id, String authId);

  Optional<PromotersDto> deleteByPromoters(Long id, String authId);

  Optional<PromotersDto> byIdAdmin(Long id, String authId);

  void delete(Long id);

  PromoterGoalsDto listBySection(Integer sectionId, String authId);

  List<PromotersDto> listPromoterBySection(Integer SectionId, String authId);

  List<PromotersDto> listPromoters(String authId);

  PromotersDto updatePromoterKeycloak(PromotersDto promotersDto, String authId);

  PromotersDto update(PromotersDto promotersDto, Long id, String authIdHeader);

  PromotersDto findByCurp(String curp);

  PromotersDto findByIne(String ine);
}
