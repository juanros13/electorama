package com.electorama.msvc.users.service;

import com.electorama.msvc.users.model.dto.MenuAdminDto;
import java.util.List;

/**
 * Administración del menú: crear entradas y decidir qué roles las ven.
 *
 * <p>El lado de lectura ({@link MenuService}) ya existía y es el que consume el front al entrar.
 * Esto es el lado que faltaba para que el menú se pueda cambiar sin tocar la base a mano.
 */
public interface MenuAdminService {

  /** Todas las entradas con sus roles, para la pantalla de accesos. */
  List<MenuAdminDto> findAll();

  /** Los roles que ya se usan en algún menú. */
  List<String> roles();

  MenuAdminDto create(MenuAdminDto menu);

  MenuAdminDto update(Long pk, MenuAdminDto menu);

  void delete(Long pk);

  /** Reemplaza los roles de una entrada por los que se pasan. */
  MenuAdminDto setRoles(Long pk, List<String> roles);
}
