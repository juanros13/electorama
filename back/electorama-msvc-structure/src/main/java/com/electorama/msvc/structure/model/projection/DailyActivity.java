package com.electorama.msvc.structure.model.projection;

/** Un día de la gráfica de actividad de un puesto: cuántos convencidos se capturaron. */
public interface DailyActivity {

  /** El día en hora de México, ISO: "2024-03-26". */
  String getDay();

  Long getCaptured();
}
