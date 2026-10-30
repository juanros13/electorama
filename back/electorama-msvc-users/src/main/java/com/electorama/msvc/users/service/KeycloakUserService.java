package com.electorama.msvc.users.service;

import com.electorama.msvc.users.exception.ApiException;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

/**
 * Las operaciones sobre cuentas de Keycloak que siguen vivas.
 *
 * <p>Lo que cambió respecto al modelo viejo:
 *
 * <ul>
 *   <li>La existencia de una cuenta se pregunta con búsqueda EXACTA por username. La vieja usaba
 *       {@code search(texto)}, que busca por subcadena en usuario, correo y nombres: rechazaba
 *       {@code ana@x.com} porque existía {@code mariana@x.com}.
 *   <li>Un 409 de Keycloak al crear se reporta como 409; antes se convertía en {@code null} y el
 *       llamador lo contaba como "imposible crear".
 *   <li>Keycloak caído es 502, no 404.
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakUserService {

  private final RealmResource realm;

  /** La cuenta por su id; 404 si no existe. */
  public UserRepresentation readUser(String authId) {
    try {
      return realm.users().get(authId).toRepresentation();
    } catch (NotFoundException e) {
      throw ApiException.notFound("No existe la cuenta " + authId);
    } catch (ProcessingException | WebApplicationException e) {
      throw ApiException.keycloak("No se pudo leer la cuenta en Keycloak: " + e.getMessage());
    }
  }

  /** Los roles de realm de la cuenta, por nombre. */
  public List<String> realmRoles(String authId) {
    try {
      return realm.users().get(authId).roles().realmLevel().listEffective().stream()
          .map(RoleRepresentation::getName)
          .toList();
    } catch (NotFoundException e) {
      throw ApiException.notFound("No existe la cuenta " + authId);
    } catch (ProcessingException | WebApplicationException e) {
      throw ApiException.keycloak("No se pudieron leer los roles en Keycloak: " + e.getMessage());
    }
  }

  /** Le pone una contraseña nueva, definitiva (no le pide cambiarla al entrar). */
  public void resetPassword(String authId, String password) {
    CredentialRepresentation credential = new CredentialRepresentation();
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setValue(password);
    credential.setTemporary(false);
    try {
      realm.users().get(authId).resetPassword(credential);
    } catch (NotFoundException e) {
      throw ApiException.notFound("No existe la cuenta " + authId);
    } catch (BadRequestException e) {
      // la política de contraseñas del realm la rechazó (largo, historial, etc.)
      throw ApiException.badRequest("Keycloak rechazó la contraseña por su política");
    } catch (ProcessingException | WebApplicationException e) {
      throw ApiException.keycloak(
          "No se pudo cambiar la contraseña en Keycloak: " + e.getMessage());
    }
  }

  /** La cuenta cuyo username es exactamente ése (aquí el username es el correo). */
  public Optional<UserRepresentation> findByUsername(String username) {
    try {
      return realm.users().search(username, true).stream().findFirst();
    } catch (ProcessingException | WebApplicationException e) {
      throw ApiException.keycloak("No se pudo buscar la cuenta en Keycloak: " + e.getMessage());
    }
  }

  /** Crea la cuenta y devuelve su id (lo lee del {@code Location} del 201). */
  public String create(UserRepresentation user) {
    try (Response response = realm.users().create(user)) {
      int status = response.getStatus();
      if (status == 409) {
        throw ApiException.conflict(
            "ACCOUNT_EXISTS", "Ya existe una cuenta con ese usuario o correo", null);
      }
      if (status != 201) {
        throw ApiException.keycloak("Keycloak rechazó la cuenta (HTTP " + status + ")");
      }
      return CreatedResponseUtil.getCreatedId(response);
    } catch (ProcessingException e) {
      throw ApiException.keycloak("No se pudo crear la cuenta en Keycloak: " + e.getMessage());
    }
  }

  /** Le pone un rol del realm. */
  public void addRealmRole(String authId, String roleName) {
    try {
      RoleRepresentation role = realm.roles().get(roleName).toRepresentation();
      realm.users().get(authId).roles().realmLevel().add(List.of(role));
    } catch (NotFoundException e) {
      throw ApiException.badRequest("El rol " + roleName + " no existe en el realm");
    } catch (ProcessingException | WebApplicationException e) {
      throw ApiException.keycloak("No se pudo asignar el rol en Keycloak: " + e.getMessage());
    }
  }

  /** Le quita un rol del realm; no tenerlo ya se toma como hecho. */
  public void removeRealmRole(String authId, String roleName) {
    try {
      RoleRepresentation role = realm.roles().get(roleName).toRepresentation();
      realm.users().get(authId).roles().realmLevel().remove(List.of(role));
    } catch (NotFoundException e) {
      throw ApiException.notFound("No existe la cuenta " + authId + " o el rol " + roleName);
    } catch (ProcessingException | WebApplicationException e) {
      throw ApiException.keycloak("No se pudo quitar el rol en Keycloak: " + e.getMessage());
    }
  }

  /** Borra la cuenta. No existir ya se toma como hecho. */
  public void delete(String authId) {
    try (Response response = realm.users().delete(authId)) {
      int status = response.getStatus();
      if (status != 204 && status != 404) {
        throw ApiException.keycloak("Keycloak no borró la cuenta (HTTP " + status + ")");
      }
    } catch (ProcessingException e) {
      throw ApiException.keycloak("No se pudo borrar la cuenta en Keycloak: " + e.getMessage());
    }
  }
}
