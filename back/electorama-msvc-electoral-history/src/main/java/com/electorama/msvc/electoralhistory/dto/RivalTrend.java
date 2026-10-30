package com.electorama.msvc.electoralhistory.dto;

import java.util.List;

/** Los votos de un rival en ayuntamientos, elección por elección. */
public record RivalTrend(String code, String label, String color, List<Point> points) {

  /**
   * @param pct sobre la votación total estatal de ayuntamientos
   */
  public record Point(int year, long votes, double pct) {}
}
