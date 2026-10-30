package com.electorama.msvc.users.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.electorama.msvc.users.configuration.SecurityConfig;
import com.electorama.msvc.users.exception.ApiException;
import com.electorama.msvc.users.model.dto.MenuGroupDto;
import com.electorama.msvc.users.model.dto.User;
import com.electorama.msvc.users.service.AccountService;
import com.electorama.msvc.users.service.MenuAdminService;
import com.electorama.msvc.users.service.MenuService;
import com.electorama.msvc.users.service.UserSeccionService;
import com.electorama.msvc.users.service.UserService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Quién puede qué, sin base ni Keycloak: la configuración de seguridad real con los servicios
 * simulados. El {@code contextLoads} de antes necesitaba las dos cosas en vivo.
 */
@WebMvcTest
@Import(SecurityConfig.class)
class ApiSecurityTest {

  @Autowired MockMvc mvc;

  @MockBean JwtDecoder jwtDecoder;
  @MockBean UserService userService;
  @MockBean UserSeccionService userSeccionService;
  @MockBean AccountService accountService;
  @MockBean MenuService menuService;
  @MockBean MenuAdminService menuAdminService;

  private static final String ANA = "11111111-1111-1111-1111-111111111111";
  private static final String BETO = "22222222-2222-2222-2222-222222222222";

  private static org.springframework.test.web.servlet.request.RequestPostProcessor as(
      String uid, String... roles) {
    return jwt()
        .jwt(j -> j.claim("uid", uid))
        .authorities(
            java.util.Arrays.stream(roles)
                .<org.springframework.security.core.GrantedAuthority>map(
                    r -> new SimpleGrantedAuthority("ROLE_" + r))
                .toList());
  }

  @Test
  void sinTokenEs401() throws Exception {
    mvc.perform(get("/v1.0.0/readUser/" + ANA)).andExpect(status().isUnauthorized());
  }

  @Test
  void unaCuentaLeeSuPerfilPeroNoElDeOtra() throws Exception {
    when(userService.findByAuthId(ANA)).thenReturn(new User());
    mvc.perform(get("/v1.0.0/readUser/" + ANA).with(as(ANA, "CORDINATOR_ZONE")))
        .andExpect(status().isOk());
    mvc.perform(get("/v1.0.0/readUser/" + BETO).with(as(ANA, "CORDINATOR_ZONE")))
        .andExpect(status().isForbidden());
    mvc.perform(get("/v1.0.0/userSeccion/" + BETO).with(as(ANA, "CORDINATOR_ZONE")))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminLeeCualquierPerfil() throws Exception {
    when(userService.findByAuthId(BETO)).thenReturn(new User());
    mvc.perform(get("/v1.0.0/readUser/" + BETO).with(as(ANA, "ADMIN"))).andExpect(status().isOk());
  }

  @Test
  void crearCuentaEsSoloDeAdminYElDuplicadoEs409() throws Exception {
    String body =
        "{\"email\":\"ana@x.com\",\"password\":\"secreta123\",\"firstName\":\"Ana\","
            + "\"lastName\":\"López\",\"role\":\"CORDINATOR_ZONE\"}";
    mvc.perform(
            post("/v2/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(as(ANA, "CORDINATOR_ZONE")))
        .andExpect(status().isForbidden());

    when(accountService.create(any()))
        .thenThrow(ApiException.conflict("ACCOUNT_EXISTS", "ya existe", Map.of("authId", BETO)));
    mvc.perform(
            post("/v2/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(as(ANA, "ADMIN")))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("ACCOUNT_EXISTS"))
        .andExpect(jsonPath("$.detail.authId").value(BETO));
  }

  @Test
  void elMenuSaleDeLosRolesDelToken() throws Exception {
    MenuGroupDto tablero = new MenuGroupDto();
    tablero.setLink("/admin/structure/dashboard");
    when(menuService.menuFor(anyCollection())).thenReturn(List.of(tablero));
    mvc.perform(get("/menu/v1.0.0/getMenuByRol").with(as(ANA, "ADMIN")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].link").value("/admin/structure/dashboard"))
        .andExpect(jsonPath("$[0].children").doesNotExist());
  }

  @Test
  void administrarElMenuSinSerAdminEs403() throws Exception {
    mvc.perform(get("/menu/v1.0.0/admin/menus").with(as(ANA, "CORDINATOR_ZONE")))
        .andExpect(status().isForbidden());
  }
}
