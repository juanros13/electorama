package com.electorama.msvc.canvasandwalls.model.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.io.Serializable;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@Entity
public class ReportPDFDto implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id private Long id;
  private String referente;
  private String coordinadorZona;
  private String entidad;
  private String municipio;
  private String localidad;
  private String seccion;
  private String proyecto;
  private String categoria;
  private String fechaRegistro;
  private String latitud;
  private String longitud;
  private String pathEvidencePhoto;
}
