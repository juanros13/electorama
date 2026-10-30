package com.electorama.msvc.users.controller;

import com.electorama.msvc.users.model.dto.MenuAdminDto;
import com.electorama.msvc.users.service.MenuAdminService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * Administración del menú: crear entradas y decidir qué roles las ven.
 *
 * <p>Sólo ADMIN ({@code @PreAuthorize} de clase: sin el rol responde 403).
 *
 * <p>La lectura del menú propio sigue en {@link MenuController}: esto es el lado de escritura, que
 * antes no existía y obligaba a tocar `menus_groups` y `menus_roles` a mano en la base.
 */
@Slf4j
@RestController
@RequestMapping(value = "/menu/v1.0.0/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class MenuAdminController {

  private final MenuAdminService menuAdminService;

  @GetMapping("/menus")
  public ResponseEntity<?> findAll() {
    return ResponseEntity.ok(menuAdminService.findAll());
  }

  /** Los roles que ya se usan en algún menú, para no teclearlos a mano. */
  @GetMapping("/roles")
  public ResponseEntity<?> roles() {
    return ResponseEntity.ok(menuAdminService.roles());
  }

  @PostMapping("/menus")
  public ResponseEntity<?> create(@RequestBody MenuAdminDto menu) {
    return ResponseEntity.status(HttpStatus.CREATED).body(menuAdminService.create(menu));
  }

  @PutMapping("/menus/{pk}")
  public ResponseEntity<?> update(@PathVariable Long pk, @RequestBody MenuAdminDto menu) {
    return ResponseEntity.ok(menuAdminService.update(pk, menu));
  }

  /** Reemplaza los roles de una entrada. Lista vacía = no la ve nadie. */
  @PutMapping("/menus/{pk}/roles")
  public ResponseEntity<?> setRoles(@PathVariable Long pk, @RequestBody List<String> roles) {
    return ResponseEntity.ok(menuAdminService.setRoles(pk, roles));
  }

  @DeleteMapping("/menus/{pk}")
  public ResponseEntity<?> delete(@PathVariable Long pk) {
    menuAdminService.delete(pk);
    return ResponseEntity.noContent().build();
  }
}
