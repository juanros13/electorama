package com.electorama.msvc.followers.model.rest;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** Una pagina de msvc-structure: contenido y total contado con el mismo filtro. */
@Getter
@Setter
public class StructurePageResponse<T> {

  private List<T> content;
  private int page;
  private int size;
  private long totalElements;
}
