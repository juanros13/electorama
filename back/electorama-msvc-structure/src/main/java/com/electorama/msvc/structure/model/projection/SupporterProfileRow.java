package com.electorama.msvc.structure.model.projection;

/** Una barra del perfil: cuántos convencidos dieron cada respuesta a cada pregunta. */
public interface SupporterProfileRow {

  /** La pregunta o dimensión: supportsMovement, sex, ageGroup, capturedMonth… */
  String getDimension();

  /** La respuesta tal como se capturó ("SI", "masdecinco", "2024-03"…); "" si no se contestó. */
  String getValue();

  Long getCount();
}
