package com.electorama.msvc.users.controller;

import com.electorama.msvc.users.model.dto.RoleSpec;
import com.electorama.msvc.users.service.StructureRoles;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Los roles que da la estructura (V3). msvc-structure llama {@code /ensure} al guardar los niveles
 * de una campaña, con el token del ADMIN que los configura.
 */
@RestController
@RequestMapping("/v2/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StructureRoleController {

  private final StructureRoles roles;

  /** Crea el rol en el realm si falta y lo registra con su antigüedad; idempotente. */
  @PostMapping("/ensure")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void ensure(@Valid @RequestBody RoleSpec spec) {
    roles.ensure(spec);
  }

  /** Cada rol de estructura con su antigüedad (mayor es más alto). */
  @GetMapping
  public Map<String, Integer> all() {
    return roles.all();
  }
}
