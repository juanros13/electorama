package com.electorama.msvc.visits.model.mapper;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

public abstract class BaseMapper<E, D> {
  public abstract E convertToEntity(D dto, Object... args);

  public abstract D convertToDto(E entity, Object... args);

  public Collection<E> convertToEntity(Collection<D> dto, Object... args) {
    return dto.stream().map(d -> convertToEntity(d, args)).collect(Collectors.toList());
  }

  public Collection<D> convertToDto(Collection<E> entity, Object... args) {
    return entity.stream().map(e -> convertToDto(e, args)).collect(Collectors.toList());
  }

  public Collection<D> convertToDtoFromPage(Page<E> entity, Object... args) {
    return entity.stream().map(e -> convertToDto(e, args)).collect(Collectors.toList());
  }

  public List<E> convertToEntityList(Collection<D> dto, Object... args) {
    return convertToEntity(dto, args).stream().collect(Collectors.toList());
  }

  public List<D> convertToDtoList(Collection<E> entity, Object... args) {
    return convertToDto(entity, args).stream().collect(Collectors.toList());
  }

  public Page<D> convertToDtoPage(Page<E> entity, Object... args) {
    List<D> dtoList = convertToDtoFromPage(entity, args).stream().collect(Collectors.toList());

    PageRequest pageRequest =
        PageRequest.of(entity.getNumber(), entity.getSize(), entity.getSort());
    return new PageImpl<>(dtoList, pageRequest, entity.getTotalElements());
  }

  public Set<E> convertToEntitySet(Collection<D> dto, Object... args) {
    return convertToEntity(dto, args).stream().collect(Collectors.toSet());
  }

  public Set<D> convertToDtoSet(Collection<E> entity, Object... args) {
    return convertToDto(entity, args).stream().collect(Collectors.toSet());
  }
}
