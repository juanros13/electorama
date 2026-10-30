package com.electorama.msvc.users.controller;

import com.electorama.msvc.users.model.dto.MenuGroupDto;
import com.electorama.msvc.users.service.MenuService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * El menú de quien entra, según los roles del realm de su token. Lo pide el front al iniciar sesión
 * ({@code core/access/access.service.ts}). El que devolvía todo el menú sin mirar roles ({@code GET
 * /menu/v1.0.0/}) se quitó.
 */
@RestController
@RequestMapping("/menu/v1.0.0")
@RequiredArgsConstructor
public class MenuController {

  private final MenuService menuService;

  @GetMapping("/getMenuByRol")
  public List<MenuGroupDto> menuByRole(Authentication authentication) {
    List<String> roles =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(a -> a.startsWith("ROLE_"))
            .map(a -> a.substring("ROLE_".length()))
            .toList();
    return menuService.menuFor(roles);
  }
}
