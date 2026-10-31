package com.electorama.msvc.structure.service;

import com.electorama.msvc.structure.exception.StructureException;
import com.electorama.msvc.structure.model.dto.CampaignRequests.LevelData;
import com.electorama.msvc.structure.model.entity.CampaignLevel;
import com.electorama.msvc.structure.model.enums.Level;
import com.electorama.msvc.structure.model.enums.TerritoryType;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Las reglas de los niveles de una campaña, sin base: se prueban solas.
 *
 * <ul>
 *   <li>de 1 a {@value #MAX_LEVELS} niveles, cada uno con nombre singular y plural;
 *   <li>el código es único en la campaña (si no viene, sale del nombre);
 *   <li>cada función heredada va a lo más una vez y en su orden: REFERENT arriba de
 *       ZONE_COORDINATOR arriba de PROMOTER;
 *   <li>un nivel con puestos no se quita ni cambia de rango, territorio o función.
 * </ul>
 */
public final class LevelRules {

  public static final int MAX_LEVELS = 10;

  private static final Pattern ROLE = Pattern.compile("^[A-Z][A-Z0-9_]{0,79}$");

  private LevelRules() {}

  /** Un nivel ya validado, con su rango (1 = cúspide) y sus roles resueltos. */
  public record Planned(
      Long id,
      short rank,
      String code,
      String nameSingular,
      String namePlural,
      TerritoryType territoryType,
      Integer defaultGoal,
      Integer defaultChildLimit,
      Level function,
      String roleName,
      String alternateRoleName) {}

  /**
   * Valida la lista completa (en orden) contra los niveles actuales y cuántos puestos tiene cada
   * uno. {@code rolePrefix} arma el rol por defecto: {@code <PREFIJO>_<CODIGO>}.
   */
  public static List<Planned> plan(
      List<LevelData> requested,
      List<CampaignLevel> current,
      Map<Long, Long> positionsByLevel,
      String rolePrefix) {
    if (requested == null || requested.isEmpty()) {
      throw StructureException.badRequest("la campaña necesita al menos un nivel");
    }
    if (requested.size() > MAX_LEVELS) {
      throw StructureException.badRequest("a lo más " + MAX_LEVELS + " niveles por campaña");
    }
    Map<Long, CampaignLevel> byId = new HashMap<>();
    current.forEach(l -> byId.put(l.getId(), l));

    List<Planned> out = new ArrayList<>();
    Set<String> codes = new HashSet<>();
    Set<Long> kept = new HashSet<>();
    Level lastFunction = null;
    for (int i = 0; i < requested.size(); i++) {
      LevelData d = requested.get(i);
      short rank = (short) (i + 1);
      String singular = trimToNull(d.nameSingular());
      if (singular == null) {
        throw StructureException.badRequest("el nivel " + rank + " no tiene nombre");
      }
      String plural = trimToNull(d.namePlural());
      if (plural == null) {
        plural = defaultPlural(singular);
      }
      String code = slug(trimToNull(d.code()) == null ? singular : d.code());
      if (!codes.add(code)) {
        throw StructureException.badRequest("hay dos niveles con el código " + code);
      }
      TerritoryType territory = parseEnum(TerritoryType.class, d.territoryType(), "territorio");
      Level function = parseEnum(Level.class, d.function(), "función");
      if (function != null) {
        if (lastFunction != null && function.orden() <= lastFunction.orden()) {
          throw StructureException.badRequest(
              "la función "
                  + function
                  + " debe ir debajo de "
                  + lastFunction
                  + " y cada función una sola vez");
        }
        lastFunction = function;
      }
      requireNonNegative(d.defaultGoal(), "la meta de " + singular);
      requireNonNegative(d.defaultChildLimit(), "el tope de " + singular);
      String role = trimToNull(d.roleName());
      role = role == null ? slug(rolePrefix + "_" + code) : role.toUpperCase(Locale.ROOT);
      String alternate = trimToNull(d.alternateRoleName());
      alternate = alternate == null ? null : alternate.toUpperCase(Locale.ROOT);
      requireRole(role, singular);
      if (alternate != null) {
        requireRole(alternate, singular);
      }

      if (d.id() != null) {
        CampaignLevel existing = byId.get(d.id());
        if (existing == null) {
          throw StructureException.badRequest("el nivel " + d.id() + " no es de esta campaña");
        }
        kept.add(d.id());
        if (positionsByLevel.getOrDefault(d.id(), 0L) > 0
            && (existing.getRank() != rank
                || existing.getTerritoryType() != territory
                || existing.getLevelFunction() != function)) {
          throw StructureException.conflict(
              "el nivel "
                  + existing.getNameSingular()
                  + " ya tiene puestos: sólo se cambian nombre, meta, tope y roles");
        }
      }
      out.add(
          new Planned(
              d.id(),
              rank,
              code,
              singular,
              plural,
              territory,
              d.defaultGoal(),
              d.defaultChildLimit(),
              function,
              role,
              alternate));
    }
    for (CampaignLevel l : current) {
      if (!kept.contains(l.getId()) && positionsByLevel.getOrDefault(l.getId(), 0L) > 0) {
        throw StructureException.conflict(
            "el nivel " + l.getNameSingular() + " ya tiene puestos: no se puede quitar");
      }
    }
    return out;
  }

  /** Mayúsculas sin acentos, con guión bajo: "Coordinador de Zona" → COORDINADOR_DE_ZONA. */
  public static String slug(String text) {
    String plain =
        Normalizer.normalize(Objects.requireNonNullElse(text, ""), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toUpperCase(Locale.ROOT)
            .replaceAll("[^A-Z0-9]+", "_")
            .replaceAll("^_+|_+$", "");
    if (plain.isEmpty()) {
      return "NIVEL";
    }
    if (!Character.isLetter(plain.charAt(0))) {
      plain = "N_" + plain;
    }
    return plain.length() > 40 ? plain.substring(0, 40).replaceAll("_+$", "") : plain;
  }

  /** Plural en español para cuando no lo mandan: Promotor → Promotores, Enlace → Enlaces. */
  static String defaultPlural(String singular) {
    String[] words = singular.split(" ", 2);
    String head = words[0];
    String last = head.substring(head.length() - 1).toLowerCase(Locale.ROOT);
    String plural = "aeiouáéó".contains(last) ? head + "s" : head + "es";
    return words.length > 1 ? plural + " " + words[1] : plural;
  }

  private static void requireRole(String role, String level) {
    if (!ROLE.matcher(role).matches()) {
      throw StructureException.badRequest(
          "el rol de " + level + " debe ir en MAYÚSCULAS, sin espacios ni acentos: " + role);
    }
  }

  private static void requireNonNegative(Integer value, String what) {
    if (value != null && value < 0) {
      throw StructureException.badRequest(what + " no puede ser negativa");
    }
  }

  private static <E extends Enum<E>> E parseEnum(Class<E> type, String value, String what) {
    String v = trimToNull(value);
    if (v == null) {
      return null;
    }
    try {
      return Enum.valueOf(type, v.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      throw StructureException.badRequest(what + " desconocido: " + value);
    }
  }

  private static String trimToNull(String s) {
    return s == null || s.isBlank() ? null : s.trim();
  }
}
