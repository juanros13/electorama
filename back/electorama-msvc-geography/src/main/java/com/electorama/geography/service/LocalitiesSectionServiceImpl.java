package com.electorama.geography.service;

import com.electorama.geography.exception.EntityNotFoundException;
import com.electorama.geography.model.dto.LocalitiesSection;
import com.electorama.geography.model.dto.SectionLocalities;
import com.electorama.geography.model.entity.LocalitiesSectionEntity;
import com.electorama.geography.model.mapper.LocalitiesSectionMapper;
import com.electorama.geography.model.mapper.SectionLocalitiesMapper;
import com.electorama.geography.model.repository.LocalitiesSectionRepository;
import com.electorama.geography.model.repository.SectionLocalitiesRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

@Service
public class LocalitiesSectionServiceImpl implements LocalitiesSectionService {

  @Autowired LocalitiesSectionRepository localitiesSectionRepository;

  @Autowired SectionLocalitiesRepository sectionLocalitiesRepository;

  private final LocalitiesSectionMapper localitiesSectionMapper = new LocalitiesSectionMapper();

  private final SectionLocalitiesMapper sectionLocalitiesMapper = new SectionLocalitiesMapper();

  // Catálogo de localidades: cacheable para evitar el N+1 (consultas por id en bucle).
  @Override
  @Cacheable("localitiesSectionById")
  public Optional<LocalitiesSection> byId(Long id) {
    return Optional.of(
        localitiesSectionMapper.convertToDto(
            localitiesSectionRepository.findById(id).orElseThrow(EntityNotFoundException::new)));
  }

  @Override
  @Cacheable("allLocalitiesSection")
  public List<LocalitiesSection> getAll() {
    return localitiesSectionMapper.convertToDtoList(localitiesSectionRepository.findAll());
  }

  @Override
  @Cacheable("localitiesBySection")
  public List<LocalitiesSection> findBySectionId(Long id) {
    return new ArrayList<>(
        localitiesSectionMapper.convertToDto(localitiesSectionRepository.findBySeccionId(id)));
  }

  // Las escrituras invalidan la caché de localidades para no servir datos obsoletos.
  @Caching(
      evict = {
        @CacheEvict(value = "localitiesSectionById", allEntries = true),
        @CacheEvict(value = "allLocalitiesSection", allEntries = true),
        @CacheEvict(value = "localitiesBySection", allEntries = true)
      })
  public LocalitiesSection createLocation(LocalitiesSection localitiesSection) {
    LocalitiesSectionEntity localitiesSectionEntity =
        localitiesSectionMapper.convertToEntity(localitiesSection);

    return localitiesSectionMapper.convertToDto(
        localitiesSectionRepository.save(localitiesSectionEntity));
  }

  @Caching(
      evict = {
        @CacheEvict(value = "localitiesSectionById", allEntries = true),
        @CacheEvict(value = "allLocalitiesSection", allEntries = true),
        @CacheEvict(value = "localitiesBySection", allEntries = true)
      })
  public LocalitiesSection save(LocalitiesSection localitiesSection) {

    LocalitiesSectionEntity localitiesSectionEntity =
        localitiesSectionMapper.convertToEntity(localitiesSection);

    return localitiesSectionMapper.convertToDto(
        localitiesSectionRepository.save(localitiesSectionEntity));
  }

  @Override
  @Caching(
      evict = {
        @CacheEvict(value = "localitiesSectionById", allEntries = true),
        @CacheEvict(value = "allLocalitiesSection", allEntries = true),
        @CacheEvict(value = "localitiesBySection", allEntries = true)
      })
  public void delete(Long id) {

    localitiesSectionRepository.deleteById(id);
  }

  @Override
  public List<SectionLocalities> findBySection(Long id) {
    return new ArrayList<>(
        sectionLocalitiesMapper.convertToDto(sectionLocalitiesRepository.findByIdSeccion(id)));
  }
}
