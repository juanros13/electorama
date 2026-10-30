package com.electorama.msvc.users.service;

import com.electorama.msvc.users.exception.ApiException;
import com.electorama.msvc.users.model.dto.MenuAdminDto;
import com.electorama.msvc.users.model.dto.TypeMenu;
import com.electorama.msvc.users.model.entity.MenuGroupEntity;
import com.electorama.msvc.users.model.entity.MenuRoleEntity;
import com.electorama.msvc.users.model.repository.MenuGroupRepository;
import com.electorama.msvc.users.model.repository.MenuRoleRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuAdminServiceImpl implements MenuAdminService {

  private final MenuGroupRepository menuGroupRepository;
  private final MenuRoleRepository menuRoleRepository;

  @Override
  @Transactional(readOnly = true)
  public List<MenuAdminDto> findAll() {
    List<MenuGroupEntity> groups = menuGroupRepository.findAllByOrderByOrderAscPkAsc();
    if (groups.isEmpty()) {
      return List.of();
    }
    List<Long> ids = groups.stream().map(MenuGroupEntity::getPk).collect(Collectors.toList());

    // Los roles de todos los grupos en una consulta: uno por grupo serían 29
    // idas y vueltas para pintar una tabla.
    Map<Long, List<String>> rolesByGroup =
        menuRoleRepository.findByMenuGroupIdIn(ids).stream()
            .collect(
                Collectors.groupingBy(
                    MenuRoleEntity::getMenuGroupId,
                    Collectors.mapping(MenuRoleEntity::getRole, Collectors.toList())));

    return groups.stream()
        .map(group -> toDto(group, rolesByGroup.getOrDefault(group.getPk(), List.of())))
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<String> roles() {
    return menuRoleRepository.findDistinctRoles();
  }

  @Override
  @Transactional
  public MenuAdminDto create(MenuAdminDto menu) {
    MenuGroupEntity entity = new MenuGroupEntity();
    apply(entity, menu);
    MenuGroupEntity saved = menuGroupRepository.save(entity);
    List<String> roles = saveRoles(saved.getPk(), menu.getRoles());
    log.info("menú creado: {} ({}), roles {}", saved.getTitle(), saved.getPk(), roles);
    return toDto(saved, roles);
  }

  @Override
  @Transactional
  public MenuAdminDto update(Long pk, MenuAdminDto menu) {
    MenuGroupEntity entity =
        menuGroupRepository
            .findById(pk)
            .orElseThrow(() -> ApiException.notFound("No existe el menú " + pk));
    apply(entity, menu);
    MenuGroupEntity saved = menuGroupRepository.save(entity);

    // Si la petición no trae roles, no se tocan: actualizar el título no debería
    // dejar la entrada sin nadie que la vea.
    List<String> roles =
        menu.getRoles() == null ? currentRoles(pk) : saveRoles(pk, menu.getRoles());
    return toDto(saved, roles);
  }

  @Override
  @Transactional
  public void delete(Long pk) {
    if (!menuGroupRepository.existsById(pk)) {
      throw ApiException.notFound("No existe el menú " + pk);
    }
    menuRoleRepository.deleteByMenuGroupId(pk);
    menuGroupRepository.deleteById(pk);
    log.info("menú {} borrado con sus roles", pk);
  }

  @Override
  @Transactional
  public MenuAdminDto setRoles(Long pk, List<String> roles) {
    MenuGroupEntity entity =
        menuGroupRepository
            .findById(pk)
            .orElseThrow(() -> ApiException.notFound("No existe el menú " + pk));
    return toDto(entity, saveRoles(pk, roles));
  }

  // ---------------------------------------------------------------------------

  /** Reemplaza los roles de la entrada. Sin duplicados y sin vacíos. */
  private List<String> saveRoles(Long menuGroupId, List<String> roles) {
    menuRoleRepository.deleteByMenuGroupId(menuGroupId);
    if (roles == null || roles.isEmpty()) {
      return List.of();
    }
    Set<String> limpios =
        roles.stream()
            .filter(role -> role != null && !role.isBlank())
            .map(String::trim)
            .collect(Collectors.toCollection(HashSet::new));

    List<MenuRoleEntity> nuevos = new ArrayList<>(limpios.size());
    for (String role : limpios) {
      MenuRoleEntity entity = new MenuRoleEntity();
      entity.setMenuGroupId(menuGroupId);
      entity.setRole(role);
      nuevos.add(entity);
    }
    menuRoleRepository.saveAll(nuevos);
    return limpios.stream().sorted().collect(Collectors.toList());
  }

  private List<String> currentRoles(Long menuGroupId) {
    return menuRoleRepository.findByMenuGroupId(menuGroupId).stream()
        .map(MenuRoleEntity::getRole)
        .sorted()
        .collect(Collectors.toList());
  }

  private void apply(MenuGroupEntity entity, MenuAdminDto dto) {
    entity.setId(dto.getId());
    entity.setTitle(dto.getTitle());
    entity.setSubtitle(dto.getSubtitle());
    entity.setIcon(dto.getIcon());
    entity.setLink(dto.getLink());
    entity.setType(dto.getType() == null ? TypeMenu.basic : dto.getType());
    entity.setOrder(dto.getOrder() == null ? 1 : dto.getOrder());
  }

  private MenuAdminDto toDto(MenuGroupEntity entity, List<String> roles) {
    MenuAdminDto dto = new MenuAdminDto();
    dto.setPk(entity.getPk());
    dto.setId(entity.getId());
    dto.setTitle(entity.getTitle());
    dto.setSubtitle(entity.getSubtitle());
    dto.setIcon(entity.getIcon());
    dto.setLink(entity.getLink());
    dto.setType(entity.getType());
    dto.setOrder(entity.getOrder());
    dto.setRoles(roles);
    return dto;
  }
}
