package com.electorama.msvc.users.controller;

import com.electorama.msvc.users.model.dto.AccountRequest;
import com.electorama.msvc.users.model.dto.AccountResponse;
import com.electorama.msvc.users.model.dto.AccountSummary;
import com.electorama.msvc.users.model.dto.PasswordChange;
import com.electorama.msvc.users.model.dto.RoleGrant;
import com.electorama.msvc.users.service.AccountAccess;
import com.electorama.msvc.users.service.AccountService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cuentas de Keycloak para quien se nombra en la estructura. ADMIN, y el referente sobre las
 * cuentas de su rama ({@link AccountAccess}); msvc-structure la llama con el token de quien está
 * armando la estructura.
 *
 * <ul>
 *   <li>201 con {@code {authId, username}};
 *   <li>409 {@code ACCOUNT_EXISTS} con el {@code authId} existente en {@code detail}, para ofrecer
 *       ligarla en vez de crear otra;
 *   <li>400 con un rol que no es de estructura o datos incompletos; 502 si Keycloak falla.
 * </ul>
 *
 * <p>El perfil del organigrama lee la cuenta del titular ({@code GET /{authId}}) y le cambia la
 * contraseña ({@code PUT /{authId}/password}, 204; 404 si la cuenta no existe).
 */
@RestController
@RequestMapping("/v2/accounts")
@RequiredArgsConstructor
@PreAuthorize(
    "hasAnyRole('ADMIN', 'CORDINATOR_MUNICIPAL', 'CORDINATOR_MUNICIPAL_ALT', 'CORDINATOR_ZONE', 'CORDINATOR_ZONE_ALT', 'PROMOTER')")
public class AccountController {

  private final AccountService accounts;
  private final AccountAccess access;

  @PostMapping
  public ResponseEntity<AccountResponse> create(@Valid @RequestBody AccountRequest request) {
    access.checkCreate(request.role());
    return ResponseEntity.status(HttpStatus.CREATED).body(accounts.create(request));
  }

  @GetMapping("/{authId}")
  public AccountSummary summary(@PathVariable String authId) {
    access.checkManage(authId);
    return accounts.summary(authId);
  }

  @PutMapping("/{authId}/password")
  public ResponseEntity<Void> changePassword(
      @PathVariable String authId,
      @Valid @RequestBody PasswordChange request,
      Principal principal) {
    access.checkManage(authId);
    accounts.changePassword(authId, request.password(), principal.getName());
    return ResponseEntity.noContent().build();
  }

  /**
   * Agrega un rol de estructura a la cuenta (204); el referente, sólo de promotor o movilizador.
   */
  @PostMapping("/{authId}/roles")
  public ResponseEntity<Void> grantRole(
      @PathVariable String authId, @Valid @RequestBody RoleGrant request) {
    access.checkCreate(request.role());
    accounts.grantRole(authId, request.role());
    return ResponseEntity.noContent().build();
  }

  /**
   * Quita un rol de estructura (204). El referente, sólo de promotor o movilizador y sólo a
   * titulares de su rama.
   */
  @DeleteMapping("/{authId}/roles/{role}")
  public ResponseEntity<Void> revokeRole(
      @PathVariable String authId, @PathVariable String role, Principal principal) {
    access.checkCreate(role);
    access.checkManage(authId);
    accounts.revokeRole(authId, role, principal.getName());
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{authId}")
  public ResponseEntity<Void> delete(@PathVariable String authId) {
    access.checkDelete(authId);
    accounts.delete(authId);
    return ResponseEntity.noContent().build();
  }
}
