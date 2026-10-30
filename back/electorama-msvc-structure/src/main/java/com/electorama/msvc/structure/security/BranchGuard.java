package com.electorama.msvc.structure.security;

import com.electorama.msvc.structure.model.repository.PositionRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Quién puede tocar qué rama del organigrama. Se usa desde {@code @PreAuthorize} como {@code
 * @branch}.
 *
 * <ul>
 *   <li>ADMIN: todo.
 *   <li>Quien ocupa un puesto de la estructura (referente, promotor o movilizador, y sus _ALT): su
 *       rama son los puestos que ocupa y todo lo que cuelga de ellos. Ahí lee todo, nombra
 *       titulares en las vacantes y deja vacantes los puestos de abajo, nunca los suyos. No crea
 *       puestos, no genera vacantes, no cambia metas ni topes ni cierra puestos: eso sigue siendo
 *       sólo ADMIN. Para cambiar a un titular, primero lo deja vacante.
 *   <li>Cualquier otro rol: nada de la estructura.
 * </ul>
 *
 * <p>La rama se calcula en la base desde el nombramiento vigente ({@code assignment.auth_id}), no
 * desde el token: si a alguien lo quitan del puesto, pierde el acceso en ese momento.
 */
@Component("branch")
@RequiredArgsConstructor
public class BranchGuard {

  static final Set<String> STRUCTURE_ROLES =
      Set.of(
          "ROLE_CORDINATOR_MUNICIPAL",
          "ROLE_CORDINATOR_MUNICIPAL_ALT",
          "ROLE_CORDINATOR_ZONE",
          "ROLE_CORDINATOR_ZONE_ALT",
          "ROLE_PROMOTER");

  private final PositionRepository positions;

  /** Si quien llama es ADMIN. */
  public boolean isAdmin() {
    return hasAny(Set.of("ROLE_ADMIN"));
  }

  /** Si quien llama tiene un rol de la estructura (el puesto concreto lo dice la base). */
  public boolean isStructureMember() {
    return hasAny(STRUCTURE_ROLES);
  }

  /** El authId (UUID de Keycloak) de quien llama; null sin token. */
  public String callerAuthId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth instanceof JwtAuthenticationToken jwt) {
      return jwt.getToken().getClaimAsString("uid");
    }
    return null;
  }

  /** Ver el puesto, sus hijos, su avance, su actividad y su historial. */
  @Transactional(readOnly = true)
  public boolean canRead(Long positionId) {
    if (isAdmin()) {
      return true;
    }
    String authId = callerAuthId();
    return isStructureMember()
        && authId != null
        && positionId != null
        && positions.inHeldBranch(authId, positionId, true);
  }

  /**
   * Nombrar titular: en una VACANTE debajo de un puesto suyo. Cambiar a un titular es dejarlo
   * vacante primero.
   */
  @Transactional(readOnly = true)
  public boolean canAppoint(Long positionId) {
    if (isAdmin()) {
      return true;
    }
    String authId = callerAuthId();
    return isStructureMember()
        && authId != null
        && positionId != null
        && positions.inHeldBranch(authId, positionId, false)
        && positions
            .summary(positionId)
            .map(p -> p.getHolderPersonId() == null && p.getHolderAuthId() == null)
            .orElse(false);
  }

  /** Dejar vacante: un puesto debajo de los suyos, nunca uno suyo. */
  @Transactional(readOnly = true)
  public boolean canVacate(Long positionId) {
    if (isAdmin()) {
      return true;
    }
    String authId = callerAuthId();
    return isStructureMember()
        && authId != null
        && positionId != null
        && positions.inHeldBranch(authId, positionId, false);
  }

  /**
   * Si la cuenta es titular de un puesto debajo de los de quien llama. Lo pregunta msvc-users antes
   * de enseñar la cuenta o dejar cambiarle la contraseña.
   */
  @Transactional(readOnly = true)
  public boolean holderInBranch(String targetAuthId) {
    if (isAdmin()) {
      return true;
    }
    String authId = callerAuthId();
    return isStructureMember()
        && authId != null
        && targetAuthId != null
        && !targetAuthId.equals(authId)
        && positions.holdsInHeldBranch(authId, targetAuthId);
  }

  private boolean hasAny(Set<String> roles) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) {
      return false;
    }
    for (GrantedAuthority a : auth.getAuthorities()) {
      if (roles.contains(a.getAuthority())) {
        return true;
      }
    }
    return false;
  }
}
