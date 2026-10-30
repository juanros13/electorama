package com.electorama.msvc.followers.model.dto;

import java.util.Date;
import lombok.Data;

@Data
public class DownloadFollowersDto {

  private String promotorId;
  private String promotor;
  private String promotorCurp;
  private String promotorTelefono;
  private String promotorIne;
  private Long simpatizanteId;
  private String simpatizante;
  private String curp;
  private String telefono;
  private String ine;
  private Integer entidadResidenciaId;
  private String nombreEntidad;
  private Integer municipioResidenciaId;
  private String nombreMunicipio;
  private String seccion;
  private String colonia;
  private String calle;
  private String numeroExterior;
  private String numeroInterno;
  private String codigoPostal;
  private String latitud;
  private String longitud;
  private String rolFamiliar;
  private String estadoCivil;
  private String sexo;
  private String personasInvitadas;
  private String representanteMesasElectorales;
  private String participacionComite;
  private String adultoMayor;
  private String telefonoCompatibleWhatsapp;
  private String apoyaMovimiento;
  private Integer ineVencimiento;
  private Date fechaCreacion;
  private Date fechaActualizacion;
  private String cordinatorZoneAuthId;
  private String cordinatorZoneName;
}
