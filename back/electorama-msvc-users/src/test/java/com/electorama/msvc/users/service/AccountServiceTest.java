package com.electorama.msvc.users.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.electorama.msvc.users.exception.ApiException;
import com.electorama.msvc.users.model.dto.AccountRequest;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;

class AccountServiceTest {

  private KeycloakUserService keycloak;
  private AccountService accounts;

  private static final AccountRequest ANA =
      new AccountRequest("Ana@X.com", "secreta123", "Ana", "López", "CORDINATOR_ZONE");

  @BeforeEach
  void setUp() {
    keycloak = mock(KeycloakUserService.class);
    accounts = new AccountService(keycloak);
  }

  @Test
  void creaLaCuentaConElCorreoEnMinusculasYLePoneElRol() {
    when(keycloak.findByUsername("ana@x.com")).thenReturn(Optional.empty());
    when(keycloak.create(any())).thenReturn("id-ana");

    var created = accounts.create(ANA);

    assertThat(created.authId()).isEqualTo("id-ana");
    assertThat(created.username()).isEqualTo("ana@x.com");
    verify(keycloak).addRealmRole("id-ana", "CORDINATOR_ZONE");
  }

  @Test
  void siYaExisteResponde409ConSuAuthIdYNoCreaOtra() {
    UserRepresentation existing = new UserRepresentation();
    existing.setId("id-vieja");
    existing.setUsername("ana@x.com");
    when(keycloak.findByUsername("ana@x.com")).thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> accounts.create(ANA))
        .isInstanceOfSatisfying(
            ApiException.class,
            e -> {
              assertThat(e.getStatus()).isEqualTo(HttpStatus.CONFLICT);
              assertThat(e.getDetail())
                  .isEqualTo(Map.of("authId", "id-vieja", "username", "ana@x.com"));
            });
    verify(keycloak, never()).create(any());
  }

  @Test
  void siNoSePuedePonerElRolBorraLaCuenta() {
    when(keycloak.findByUsername("ana@x.com")).thenReturn(Optional.empty());
    when(keycloak.create(any())).thenReturn("id-ana");
    doThrow(ApiException.keycloak("caído"))
        .when(keycloak)
        .addRealmRole("id-ana", "CORDINATOR_ZONE");

    assertThatThrownBy(() -> accounts.create(ANA)).isInstanceOf(ApiException.class);
    verify(keycloak).delete("id-ana");
  }

  @Test
  void rechazaUnRolQueNoEsDeEstructura() {
    AccountRequest admin = new AccountRequest("x@x.com", "secreta123", "X", "Y", "ADMIN");

    assertThatThrownBy(() -> accounts.create(admin))
        .isInstanceOfSatisfying(
            ApiException.class, e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    verify(keycloak, never()).create(any());
  }
}
