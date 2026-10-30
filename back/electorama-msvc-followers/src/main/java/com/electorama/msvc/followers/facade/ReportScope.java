package com.electorama.msvc.followers.facade;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Traduccion de los parametros que los facades reciben del servicio a lo que la API de
 * msvc-structure espera.
 *
 * <p>Las fechas llegan como cadenas ya entrecomilladas ("'2024-01-01'") porque antes se
 * concatenaban dentro del SQL. Se conservan asi para no tocar el servicio en este paso; cuando se
 * toque, deberian viajar como fechas.
 */
final class ReportScope {

  private ReportScope() {}

  private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");

  /**
   * Quita las comillas que traia la cadena y la convierte en el inicio del dia.
   *
   * <p>Se devuelve en UTC y no con el desplazamiento local a proposito: viaja como parametro de una
   * URL, y un desplazamiento positivo llevaria un '+' que se decodifica como espacio si algo por el
   * camino no lo escapa. Con 'Z' no hay nada que escapar.
   */
  static String from(String quotedDate) {
    return utc(parse(quotedDate).atStartOfDay(ZONA).toOffsetDateTime());
  }

  /**
   * El fin del rango, EXCLUSIVO y un dia despues.
   *
   * <p>El SQL viejo hacia {@code created BETWEEN '2024-01-01' AND '2024-12-31'} comparando
   * timestamps, asi que el ultimo dia del rango se perdia completo. Aqui se incluye: es lo que
   * quiere decir quien elige un rango de fechas.
   */
  static String to(String quotedDate) {
    return utc(parse(quotedDate).plusDays(1).atStartOfDay(ZONA).toOffsetDateTime());
  }

  private static String utc(OffsetDateTime moment) {
    return moment.atZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime().toString();
  }

  private static LocalDate parse(String quotedDate) {
    if (quotedDate == null) {
      throw new IllegalArgumentException("falta la fecha del reporte");
    }
    return LocalDate.parse(quotedDate.replace("'", "").trim());
  }

  /**
   * El ambito de quien consulta.
   *
   * <p>Un coordinador municipal ve lo que cuelga de su puesto de referente y un coordinador de zona
   * lo que cuelga del suyo: en el modelo nuevo es el mismo recorrido del organigrama, asi que basta
   * el authId. Cualquier otro rol ve todo.
   *
   * <p>Esto tambien arregla las dos ramas de rol del modelo viejo, que nombraban columnas
   * inexistentes (`reference_auth_id`, `coordinate_zone_auth_id`) y dejaban el reporte en blanco
   * sin avisar.
   */
  static String scope(String typeRol, String authIdHeader) {
    if (typeRol == null) {
      return null;
    }
    boolean acotado =
        typeRol.equalsIgnoreCase("CORDINATOR_MUNICIPAL")
            || typeRol.equalsIgnoreCase("CORDINATOR_ZONE");
    return acotado ? authIdHeader : null;
  }
}
