package com.electorama.geography.service;

import com.electorama.geography.model.dto.LocalitiesSection;
import com.electorama.geography.model.entity.LocalitiesSectionEntity;
import com.electorama.geography.model.mapper.LocalitiesSectionMapper;
import com.electorama.geography.model.repository.LocalitiesSectionRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class LocalitiesServiceImpl implements LocalitiesService {

  @Autowired LocalitiesSectionRepository localitiesSectionRepository;

  private final LocalitiesSectionMapper localitiesSectionMapper = new LocalitiesSectionMapper();

  @Override
  public Page<LocalitiesSection> findByLocalities(
      Integer pageNo, Integer pageSize, String sortBy, String orderBy, String search) {
    Pageable paging;

    if (orderBy.equals("desc")) {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
    } else {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
    }
    Page<LocalitiesSectionEntity> localitiesSectionEntity = null;
    if (search.length() >= 1) {
      localitiesSectionEntity =
          localitiesSectionRepository
              .findByLocalidadIsContainingIgnoreCaseOrSeccionOrMunicipioContainingIgnoreCase(
                  search, search, search, paging);
    } else {
      localitiesSectionEntity = localitiesSectionRepository.findAll(paging);
    }

    Page<LocalitiesSection> dtoPage =
        localitiesSectionEntity.map(
            entity -> {
              LocalitiesSection dto = new LocalitiesSection();

              if (entity != null) {
                dto = localitiesSectionMapper.convertToDto(entity);
              }
              return dto;
            });

    return dtoPage;
  }

  @Override
  public List<LocalitiesSectionEntity> findByLocalitiesByMunicipality(String id) {
    return localitiesSectionRepository.findByMunicipioIgnoreCase(id);
  }
}
