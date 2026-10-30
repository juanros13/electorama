package com.electorama.msvc.electoralhistory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Resultados electorales oficiales de Tabasco (2018, 2021, 2024) para analizar cómo votó cada
 * municipio y sección. Sólo lee: los datos entran con los scripts versionados de {@code db/}.
 *
 * <p>No se mezcla con elections / elections-results: ésos son de la operación del día de la jornada
 * (casillas, captura de actas) y no tienen año ni cómputos oficiales.
 */
@SpringBootApplication
public class ElectoramaMsvcElectoralHistoryApplication {

  public static void main(String[] args) {
    SpringApplication.run(ElectoramaMsvcElectoralHistoryApplication.class, args);
  }
}
