package com.electorama.msvc.users.service;

import com.electorama.msvc.users.model.dto.MenuGroupDto;
import com.electorama.msvc.users.model.entity.MenuGroupEntity;
import com.electorama.msvc.users.model.repository.MenuGroupRepository;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * El menú en una consulta. Antes eran dos más una por grupo y una por menú (hijos EAGER), y a cada
 * entrada se le intercalaba un "divider" que el front tiraba junto con los hijos.
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

  private final MenuGroupRepository menuGroups;

  @Override
  @Transactional(readOnly = true)
  public List<MenuGroupDto> menuFor(Collection<String> roles) {
    if (roles == null || roles.isEmpty()) {
      return List.of();
    }
    return menuGroups.findVisibleTo(roles).stream().map(MenuServiceImpl::toDto).toList();
  }

  private static MenuGroupDto toDto(MenuGroupEntity e) {
    MenuGroupDto dto = new MenuGroupDto();
    dto.setPk(e.getPk());
    dto.setId(e.getId());
    dto.setTitle(e.getTitle());
    dto.setSubtitle(e.getSubtitle());
    dto.setIcon(e.getIcon());
    dto.setType(e.getType());
    dto.setLink(e.getLink());
    dto.setOrder(e.getOrder());
    return dto;
  }
}
