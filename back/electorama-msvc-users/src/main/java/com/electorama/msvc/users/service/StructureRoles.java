package com.electorama.msvc.users.service;

import com.electorama.msvc.users.model.dto.RoleSpec;
import com.electorama.msvc.users.model.entity.MenuRoleEntity;
import com.electorama.msvc.users.model.entity.StructureRoleEntity;
import com.electorama.msvc.users.model.repository.MenuRoleRepository;
import com.electorama.msvc.users.model.repository.StructureRoleRepository;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Qué roles da la estructura y su antigüedad. Antes era una lista fija de cinco roles; con los
 * niveles por campaña (V3) cada nivel trae el suyo y msvc-structure lo registra aquí.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StructureRoles {

  private final StructureRoleRepository roles;
  private final MenuRoleRepository menuRoles;
  private final KeycloakUserService keycloak;

  @Transactional(readOnly = true)
  public boolean isStructureRole(String role) {
    return role != null && roles.existsById(role);
  }

  /** La antigüedad del rol (mayor es más alto); vacío si no es de la estructura. */
  @Transactional(readOnly = true)
  public Optional<Integer> seniority(String role) {
    return role == null
        ? Optional.empty()
        : roles.findById(role).map(r -> r.getSeniority().intValue());
  }

  /** Todos los roles de estructura con su antigüedad. */
  @Transactional(readOnly = true)
  public Map<String, Integer> all() {
    return roles.findAll().stream()
        .collect(Collectors.toMap(StructureRoleEntity::getRole, r -> r.getSeniority().intValue()));
  }

  /**
   * Deja el rol listo para usarse: lo crea en el realm si falta, lo registra con su antigüedad y,
   * si es nuevo y pide copiar menús, le da los mismos que el rol heredado. Un rol que ya existe de
   * otra campaña (o heredado) conserva su antigüedad: la comparten varias campañas.
   */
  @Transactional
  public void ensure(RoleSpec spec) {
    keycloak.ensureRealmRole(spec.role(), spec.description());
    StructureRoleEntity row = roles.findById(spec.role()).orElse(null);
    boolean isNew = row == null;
    if (isNew) {
      row = new StructureRoleEntity();
      row.setRole(spec.role());
      row.setCampaignId(spec.campaignId());
    }
    if (isNew || Objects.equals(row.getCampaignId(), spec.campaignId())) {
      row.setSeniority((short) spec.seniority());
      row.setDescription(spec.description());
      roles.save(row);
    }
    if (isNew && spec.copyMenusFrom() != null && !spec.copyMenusFrom().equals(spec.role())) {
      Set<Long> groups =
          menuRoles.findAll().stream()
              .filter(m -> spec.copyMenusFrom().equals(m.getRole()))
              .map(MenuRoleEntity::getMenuGroupId)
              .collect(Collectors.toSet());
      for (Long group : groups) {
        MenuRoleEntity copy = new MenuRoleEntity();
        copy.setMenuGroupId(group);
        copy.setRole(spec.role());
        menuRoles.save(copy);
      }
      log.info("rol {}: {} menús copiados de {}", spec.role(), groups.size(), spec.copyMenusFrom());
    }
    log.info("rol de estructura {} listo (antigüedad {})", spec.role(), spec.seniority());
  }
}
