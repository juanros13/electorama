package com.electorama.msvc.structure.model.dto;

import com.electorama.msvc.structure.model.enums.Level;
import java.util.List;

/** Lo que se manda para armar la estructura: crear puestos, generar vacantes y nombrar. */
public final class BuildRequests {

  private BuildRequests() {}

  /**
   * Un puesto nuevo.
   *
   * <ul>
   *   <li>REFERENT: sin padre, sobre un municipio ({@code municipalityGeoId}).
   *   <li>ZONE_COORDINATOR (el "promotor" del Excel): bajo un referente, sobre una sección de su
   *       municipio ({@code sectionGeoId}).
   *   <li>PROMOTER (el "movilizador"): bajo un coordinador de zona; si no se dice sección, la de su
   *       coordinador.
   * </ul>
   *
   * @param goal si no viene: referente = la meta que mande el front, promotor 200, movilizador 20
   */
  public record CreatePosition(
      Long structureId,
      Level level,
      Long parentPositionId,
      Integer sectionGeoId,
      Integer municipalityGeoId,
      Integer goal,
      Integer childLimit) {}

  /**
   * Generar las vacantes que pide la meta debajo de un puesto.
   *
   * @param sections sólo para un referente: la meta 2027 de cada sección de su municipio (el front
   *     la tiene en el catálogo de geography)
   */
  public record Vacancies(List<SectionMeta> sections) {}

  public record SectionMeta(Integer sectionGeoId, Integer meta) {}

  /** Cuántos puestos pide la meta en una sección, cuántos había y cuántos se crearon. */
  public record VacancyResult(Integer sectionGeoId, int needed, int existing, int created) {}

  /**
   * Nombrar al titular de un puesto.
   *
   * @param person la persona: por {@code personId}, o sus datos (si la CURP ya existe se reutiliza)
   * @param account CREATE crea su cuenta con el rol de su nivel; LINK usa una existente ({@code
   *     authId}); NONE la deja sin cuenta
   * @param replaceInBranch con LINK, confirma que si la cuenta ya ocupa otro puesto en la misma
   *     rama, ese puesto queda vacante y se le quitan los roles que ya no necesita. Sin esto el
   *     nombramiento responde 409 BRANCH_CONFLICT con el plan, para que el front pida confirmar.
   */
  /**
   * {@code wholeSection}: en un promotor, la persona ocupa también los demás puestos vacantes de
   * promotor de la misma sección (A1, A2, A3…) y queda con toda su meta.
   */
  public record Appoint(
      PersonData person,
      AccountData account,
      String reason,
      Boolean replaceInBranch,
      Boolean wholeSection) {}

  public record PersonData(
      Long personId,
      String names,
      String lastName,
      String surName,
      String curp,
      String ine,
      String phone,
      String email) {}

  public enum AccountMode {
    CREATE,
    LINK,
    NONE
  }

  public record AccountData(AccountMode mode, String email, String password, String authId) {

    @Override
    public String toString() {
      return "AccountData[mode=" + mode + ", email=" + email + ", authId=" + authId + "]";
    }
  }

  /**
   * El nombramiento hecho: quién, con qué cuenta y si la cuenta se creó ahora; con LINK, además,
   * los puestos de la misma rama que quedaron vacantes y los roles que se le quitaron.
   */
  public record Appointed(
      Long positionId,
      Long personId,
      String authId,
      boolean accountCreated,
      String role,
      List<Long> vacatedPositions,
      List<String> revokedRoles,
      List<Long> alsoAppointed) {}

  /** Un puesto vacante de promotor de la misma sección (para «toda la sección»). */
  public record SectionSeat(Long positionId, String sectionKey, Integer goal) {}

  /** Un puesto que la cuenta ocupa en la misma rama y que quedaría vacante. */
  public record BranchSeat(Long positionId, String level, String label) {}

  /**
   * Qué pasa al ligar una cuenta a un puesto: el rol que recibe y, si ya ocupa otros puestos en la
   * misma rama, cuáles quedan vacantes y qué roles se le quitan.
   */
  public record LinkPlan(String roleToGrant, List<BranchSeat> vacates, List<String> rolesToRevoke) {

    public boolean hasConflict() {
      return !vacates.isEmpty();
    }
  }
}
