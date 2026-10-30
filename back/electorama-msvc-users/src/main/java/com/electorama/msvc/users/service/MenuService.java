package com.electorama.msvc.users.service;

import com.electorama.msvc.users.model.dto.MenuGroupDto;
import java.util.Collection;
import java.util.List;

/** El menú del front según los roles de quien entra. */
public interface MenuService {

  List<MenuGroupDto> menuFor(Collection<String> roles);
}
