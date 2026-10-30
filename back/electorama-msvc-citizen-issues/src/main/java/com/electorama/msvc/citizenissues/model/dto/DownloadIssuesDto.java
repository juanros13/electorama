package com.electorama.msvc.citizenissues.model.dto;

import java.util.Date;
import lombok.Data;

@Data
public class DownloadIssuesDto {

  private String oficio;
  private String nombreCompletoSolicitante;
  private String telefonoSolicitante;
  private Date fechaLevantamiento;
  private String comentarios;
  private String tipo;
  private String categoria;
  private String estatus;
  private String nombreBeneficiario;
  private String apellidoPaternoBeneficiario;
  private String apellidoMaternoBeneficiario;
  private String otroNombreLocalidad;
  private Long municipalityId;
  private Long localityId;
  private String localidadOtra;
  private String localidad;
  private String municipio;
  private String usuarioCreoId;
  private String usuarioCreo;
  private String curp;
  private String recurso;

  private String urgente;
}
