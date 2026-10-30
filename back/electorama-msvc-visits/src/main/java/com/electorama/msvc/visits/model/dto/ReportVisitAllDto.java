package com.electorama.msvc.visits.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportVisitAllDto {
  private String referente;
  private String coordinadorZona;
  private String municipio;
  private String seccion;
  private String visitadoId;
  private String fechaVisita;
  private String fechaSincronizacion;
  private String abrioPuerta;
  private String esAfin;
  private String aceptaMasInfo;
  private String visitadoNombre;
  private String visitadoPaterno;
  private String visitadoMaterno;
  private String telefono;
  private String edadPersonaAbrePuerta;
  private String sexoPersonaAbrePuerta;
  private String calle;
  private String numeroExterno;
  private String latitud;
  private String longitud;
}
