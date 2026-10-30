package com.electorama.msvc.structure.service.rest;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Las cuentas de Keycloak viven en msvc-users ({@code /v2/accounts}). Se llama con el token de
 * quien está armando la estructura ({@link com.electorama.msvc.structure.config
 * .FeignAuthForwarding}), así que el permiso lo decide msvc-users, no esta cuenta de servicio.
 */
@FeignClient(name = "msvc-users-accounts", url = "${app.config.usuarios-url}")
public interface UsersClient {

  @PostMapping("/v2/accounts")
  AccountResponse create(@RequestBody AccountRequest request);

  @DeleteMapping("/v2/accounts/{authId}")
  void delete(@PathVariable("authId") String authId);

  /** Le agrega un rol de estructura a una cuenta existente; no quita los que ya tiene. */
  @PostMapping("/v2/accounts/{authId}/roles")
  void grantRole(@PathVariable("authId") String authId, @RequestBody RoleGrant request);

  record RoleGrant(String role) {}

  /** Le quita un rol de estructura (al pasar a otro puesto de la misma rama). */
  @DeleteMapping("/v2/accounts/{authId}/roles/{role}")
  void revokeRole(@PathVariable("authId") String authId, @PathVariable("role") String role);

  record AccountRequest(
      String email, String password, String firstName, String lastName, String role) {

    @Override
    public String toString() {
      return "AccountRequest[email=" + email + ", role=" + role + "]";
    }
  }

  record AccountResponse(String authId, String username) {}
}
