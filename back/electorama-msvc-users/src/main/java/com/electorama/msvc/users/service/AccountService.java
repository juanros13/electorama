package com.electorama.msvc.users.service;

import com.electorama.msvc.users.exception.ApiException;
import com.electorama.msvc.users.model.dto.AccountRequest;
import com.electorama.msvc.users.model.dto.AccountResponse;
import com.electorama.msvc.users.model.dto.AccountSummary;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

/**
 * Alta de cuentas para la estructura nueva (msvc-structure nombra; aquí se crea el acceso).
 *
 * <p>A diferencia de las altas viejas ({@code createCordinatorZone} y compañía), no escribe las
 * tablas {@code users} ni {@code users_seccions}: el territorio y la jerarquía viven en el puesto
 * de msvc-structure. Y no deja cuentas a medias: si no se puede poner el rol, la cuenta se borra.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

  /** Los roles que se pueden dar desde la estructura. Los nombres van con sus erratas del realm. */
  public static final Set<String> STRUCTURE_ROLES =
      Set.of(
          "CORDINATOR_MUNICIPAL",
          "CORDINATOR_MUNICIPAL_ALT",
          "CORDINATOR_ZONE",
          "CORDINATOR_ZONE_ALT",
          "PROMOTER");

  private final KeycloakUserService keycloak;

  public AccountResponse create(AccountRequest request) {
    if (!STRUCTURE_ROLES.contains(request.role())) {
      throw ApiException.badRequest("El rol debe ser uno de " + STRUCTURE_ROLES);
    }
    String username = request.email().trim().toLowerCase();

    keycloak
        .findByUsername(username)
        .ifPresent(
            existing -> {
              throw ApiException.conflict(
                  "ACCOUNT_EXISTS",
                  "Ya existe una cuenta con el usuario " + username,
                  Map.of("authId", existing.getId(), "username", existing.getUsername()));
            });

    UserRepresentation user = new UserRepresentation();
    user.setUsername(username);
    user.setEmail(username);
    user.setFirstName(request.firstName().trim());
    user.setLastName(request.lastName().trim());
    user.setEnabled(true);
    user.setEmailVerified(true);
    CredentialRepresentation password = new CredentialRepresentation();
    password.setType(CredentialRepresentation.PASSWORD);
    password.setValue(request.password());
    password.setTemporary(false);
    user.setCredentials(List.of(password));

    String authId = keycloak.create(user);
    try {
      keycloak.addRealmRole(authId, request.role());
    } catch (RuntimeException e) {
      // sin rol la cuenta no sirve: se borra para no dejar basura en el realm
      log.warn("no se pudo poner el rol {} a {}; se borra la cuenta", request.role(), username);
      keycloak.delete(authId);
      throw e;
    }
    log.info("cuenta creada: {} ({}) con rol {}", username, authId, request.role());
    return new AccountResponse(authId, username);
  }

  /**
   * Le agrega un rol de estructura a una cuenta que ya existe, sin quitarle los que tiene. Lo usa
   * msvc-structure al ligar (LINK) una cuenta a un puesto: sin el rol del nivel, la cuenta ocupa el
   * puesto pero no ve sus pantallas.
   */
  public void grantRole(String authId, String role) {
    if (!STRUCTURE_ROLES.contains(role)) {
      throw ApiException.badRequest("El rol debe ser uno de " + STRUCTURE_ROLES);
    }
    keycloak.readUser(authId);
    if (keycloak.realmRoles(authId).contains(role)) {
      return;
    }
    keycloak.addRealmRole(authId, role);
    log.info("rol {} agregado a la cuenta {}", role, authId);
  }

  /**
   * Le quita un rol de estructura. Lo usa msvc-structure cuando la cuenta pasa a otro puesto de la
   * misma rama y el anterior queda vacante.
   */
  public void revokeRole(String authId, String role, String changedBy) {
    if (!STRUCTURE_ROLES.contains(role)) {
      throw ApiException.badRequest("El rol debe ser uno de " + STRUCTURE_ROLES);
    }
    keycloak.removeRealmRole(authId, role);
    log.info("rol {} quitado a la cuenta {} por {}", role, authId, changedBy);
  }

  /** Los datos de la cuenta para el perfil del organigrama. */
  public AccountSummary summary(String authId) {
    UserRepresentation user = keycloak.readUser(authId);
    List<String> roles =
        keycloak.realmRoles(authId).stream()
            .filter(r -> STRUCTURE_ROLES.contains(r) || "ADMIN".equals(r))
            .sorted()
            .toList();
    return new AccountSummary(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        Boolean.TRUE.equals(user.isEnabled()),
        user.getCreatedTimestamp(),
        roles);
  }

  /**
   * Un administrador le pone contraseña nueva a una cuenta. Queda en el log quién lo hizo y a
   * quién, nunca la contraseña.
   */
  public void changePassword(String authId, String password, String changedBy) {
    keycloak.resetPassword(authId, password);
    log.info("contraseña cambiada: cuenta {} por {}", authId, changedBy);
  }

  /** Borra una cuenta: lo usa msvc-structure para deshacer un nombramiento que no se completó. */
  public void delete(String authId) {
    keycloak.delete(authId);
    log.info("cuenta borrada: {}", authId);
  }
}
