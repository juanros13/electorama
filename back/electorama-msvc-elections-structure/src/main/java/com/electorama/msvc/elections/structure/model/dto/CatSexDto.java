package com.electorama.msvc.elections.structure.model.dto;

import jakarta.persistence.*;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CatSexDto implements Serializable {

  private Long id;

  private String nombre;
}
