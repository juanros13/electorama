package com.electorama.elections.models.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PollingBoothChangeStatusDto {
  private Long id;

  private PollingBoothStatusDto status;
  private Long pollingBootId;
  private LocalDateTime dateStatusOpen;
  private LocalDateTime dateClosed;
}
