package com.electorama.msvc.users.service;

import com.electorama.msvc.users.exception.ApiException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Qué cuentas puede tocar quien llama a {@code /v2/accounts}.
 *
 * <ul>
 *   <li>ADMIN: cualquiera.
 *   <li>Quien ocupa un puesto de la estructura: sólo cuentas de niveles DEBAJO del suyo (el
 *       referente, de promotor y movilizador; el promotor, de movilizador; el movilizador, de
 *       nadie), nunca ADMIN. Para verlas o cambiarles la contraseña, además, tienen que ser
 *       titulares de un puesto de su rama, según msvc-structure. Puede crear cuentas de esos roles
 *       (al nombrar en su rama; msvc-structure valida la rama antes de llamar) y borrar una recién
 *       creada (msvc-structure la borra si el nombramiento falla).
 * </ul>
 *
 * <p>La revisión de roles de la cuenta destino va aunque msvc-structure diga que es de su rama: un
 * nombramiento con LINK podría haber ligado una cuenta de más arriba a un puesto de abajo.
 */
@Component
@RequiredArgsConstructor
public class AccountAccess {

  /** El nivel de cada rol de estructura: sólo se administra lo que queda debajo. */
  static final Map<String, Integer> RANK =
      Map.of(
          "CORDINATOR_MUNICIPAL", 3,
          "CORDINATOR_MUNICIPAL_ALT", 3,
          "CORDINATOR_ZONE", 2,
          "CORDINATOR_ZONE_ALT", 2,
          "PROMOTER", 1);

  /** Cuánto dura una cuenta "recién creada" para poder deshacerla. */
  static final Duration UNDO_WINDOW = Duration.ofMinutes(10);

  private final KeycloakUserService keycloak;
  private final StructureBranchClient structure;

  public boolean isAdmin() {
    return hasRole("ADMIN");
  }

  /** Crear una cuenta con ese rol (o dárselo o quitárselo a una existente). */
  public void checkCreate(String role) {
    if (isAdmin()) {
      return;
    }
    int mine = requireStructureRank();
    if (RANK.getOrDefault(role, Integer.MAX_VALUE) >= mine) {
      throw ApiException.forbidden("Sólo se administran cuentas de niveles debajo del tuyo");
    }
  }

  /** Ver la cuenta o cambiarle la contraseña. */
  public void checkManage(String targetAuthId) {
    if (isAdmin()) {
      return;
    }
    int mine = requireStructureRank();
    requireOnlyBelow(targetAuthId, mine);
    if (!structure.holderInBranch(targetAuthId, callerToken())) {
      throw ApiException.forbidden("La cuenta no ocupa ningún puesto de tu rama");
    }
  }

  /** Borrar: sólo para deshacer un alta que no se completó. */
  public void checkDelete(String targetAuthId) {
    if (isAdmin()) {
      return;
    }
    int mine = requireStructureRank();
    requireOnlyBelow(targetAuthId, mine);
    UserRepresentation user = keycloak.readUser(targetAuthId);
    Long created = user.getCreatedTimestamp();
    if (created == null || System.currentTimeMillis() - created > UNDO_WINDOW.toMillis()) {
      throw ApiException.forbidden("Sólo se puede deshacer una cuenta recién creada");
    }
  }

  private void requireOnlyBelow(String targetAuthId, int mine) {
    List<String> roles = keycloak.realmRoles(targetAuthId);
    boolean protectedRole =
        roles.stream().anyMatch(r -> "ADMIN".equals(r) || RANK.getOrDefault(r, 0) >= mine);
    if (protectedRole) {
      throw ApiException.forbidden("Esa cuenta la administra alguien de más arriba");
    }
  }

  /** El nivel más alto de quien llama; 403 si no tiene rol de estructura. */
  private int requireStructureRank() {
    int mine =
        RANK.entrySet().stream()
            .filter(e -> hasRole(e.getKey()))
            .mapToInt(Map.Entry::getValue)
            .max()
            .orElse(0);
    if (mine == 0) {
      throw ApiException.forbidden("Tu rol no administra cuentas");
    }
    return mine;
  }

  private boolean hasRole(String role) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) {
      return false;
    }
    for (GrantedAuthority a : auth.getAuthorities()) {
      if (("ROLE_" + role).equals(a.getAuthority())) {
        return true;
      }
    }
    return false;
  }

  private String callerToken() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth instanceof JwtAuthenticationToken jwt) {
      return jwt.getToken().getTokenValue();
    }
    throw ApiException.forbidden("Sin token");
  }
}
