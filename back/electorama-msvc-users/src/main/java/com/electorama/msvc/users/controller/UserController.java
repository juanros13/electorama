package com.electorama.msvc.users.controller;

import com.electorama.msvc.users.model.dto.User;
import com.electorama.msvc.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * El perfil de una cuenta. Lo usa la app de bardas y lonas al iniciar sesión.
 *
 * <p>Sólo la propia cuenta o ADMIN: antes cualquier coordinador podía leer CURP, INE y domicilio de
 * cualquier authId. El resto de los endpoints de este controller (altas, bajas y lecturas del
 * modelo viejo) se quitaron: la estructura vive en msvc-structure y las cuentas se crean en {@link
 * AccountController}.
 */
@RestController
@RequestMapping("/v1.0.0")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping("/readUser/{authId}")
  @PreAuthorize("hasRole('ADMIN') or #authId == principal.getClaimAsString('uid')")
  public User readUser(@PathVariable String authId) {
    return userService.findByAuthId(authId);
  }
}
