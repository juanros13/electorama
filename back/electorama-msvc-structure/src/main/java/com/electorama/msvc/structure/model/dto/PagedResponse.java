package com.electorama.msvc.structure.model.dto;

import java.util.List;

/**
 * Una página, con su total contado con el mismo criterio que el contenido.
 *
 * <p>No se devuelve {@code Page} de Spring Data: su forma JSON no es estable entre versiones y del
 * otro lado hay un cliente Feign que tiene que deserializarla.
 */
public record PagedResponse<T>(List<T> content, int page, int size, long totalElements) {

  public static <T> PagedResponse<T> of(List<T> content, int page, int size, long total) {
    return new PagedResponse<>(content, page, size, total);
  }
}
