package com.electorama.logfile.model.dto;

import java.util.Date;
import lombok.Data;

@Data
public class LogfileDto {

  private Long id;

  private String action;

  private Integer quantity;

  private String message;

  private String data;

  private Date createdAt;

  private String userAuthId;
  private Long accionId;
  private String module;
  private String ip;
}
