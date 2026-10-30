package com.electorama.msvc.elections.structure.model.rest.response;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountResponse {
  private String number;
  private BigDecimal actualBalance;
  private Integer id;
  private String type;
  private String status;
  private BigDecimal availableBalance;
}
