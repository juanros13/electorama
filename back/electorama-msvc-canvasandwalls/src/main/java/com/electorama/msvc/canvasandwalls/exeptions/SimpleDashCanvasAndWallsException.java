package com.electorama.msvc.canvasandwalls.exeptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleDashCanvasAndWallsException extends RuntimeException {
  private String code;
  private String message;

  public SimpleDashCanvasAndWallsException(String message) {
    this.message = message;
  }
}
