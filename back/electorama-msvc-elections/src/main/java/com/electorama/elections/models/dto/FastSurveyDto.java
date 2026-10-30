package com.electorama.elections.models.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class FastSurveyDto {
  private Long id;
  private LocalDateTime dateCollected;
  private LocalDateTime surveyEndTime;
  private LocalDateTime uploadTime;
  private Integer duration;
  private String gobernatura;
  private String usuario;
  private String latitud;
  private String longitude;
  private String canditate;
  private String balancan;
  private String cardenas;
  private String centla;
  private String centro;
  private String comalcalco;
  private String cunduacan;
  private String emilianoZapata;
  private String huimanguillo;
  private String jalapa;
  private String jalpaDeMendez;
  private String jonuta;
  private String macuspana;
  private String nacajuca;
  private String paraiso;
  private String tacotalpa;
  private String teapa;
  private String tenosique;
}
