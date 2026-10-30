package com.electorama.msvc.canvasandwalls.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportCanvasAndWallsAllDto {
  private String canvasId;
  private String referente;
  private String coordinadorZona;
  private String municipio;
  private String seccion;
  private String entidad;
  private String proyecto;
  private String categoria;
  private String fechaPuesta;
  private String fechaRegistro;
  private String latitud;
  private String longitud;
  private String path;
}
