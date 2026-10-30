package com.electorama.msvc.users;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Cuentas y menú.
 *
 * <p>Lo que queda después de pasar la estructura a msvc-structure: el menú del front por rol (y su
 * administración), el alta de cuentas de Keycloak para quien se nombra en un puesto, y las dos
 * lecturas que todavía usa la app de bardas y lonas (perfil y secciones de una cuenta).
 */
@SpringBootApplication
public class ElectoramaMsvcUsersApplication {

  public static void main(String[] args) {
    SpringApplication.run(ElectoramaMsvcUsersApplication.class, args);
  }
}
