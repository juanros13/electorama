package com.electorama.msvc.users.controller;

import com.electorama.msvc.users.model.dto.UserSeccionDto;
import com.electorama.msvc.users.service.UserSeccionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Las secciones de una cuenta. Lo usa la app de bardas y lonas para su ámbito.
 *
 * <p>{@code users_seccions} ya es una vista de compatibilidad sobre msvc-structure (V3d). Antes
 * este endpoint no pedía ningún rol; ahora sólo la propia cuenta o ADMIN. El resto (altas, cambios
 * y bajas de secciones) se quitó: el ámbito se decide con los puestos de la estructura.
 */
@RestController
@RequestMapping("/v1.0.0/userSeccion")
@RequiredArgsConstructor
public class UserSeccionController {

  private final UserSeccionService userSeccionService;

  @GetMapping("/{authId}")
  @PreAuthorize("hasRole('ADMIN') or #authId == principal.getClaimAsString('uid')")
  public List<UserSeccionDto> sections(@PathVariable String authId) {
    return userSeccionService.findByUser(authId);
  }
}
