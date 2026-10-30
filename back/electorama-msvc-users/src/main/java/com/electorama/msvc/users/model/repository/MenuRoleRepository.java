package com.electorama.msvc.users.model.repository;

import com.electorama.msvc.users.model.entity.MenuRoleEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuRoleRepository extends JpaRepository<MenuRoleEntity, Long> {
  List<MenuRoleEntity> findByMenuGroupId(Long menuGroupId);

  /** Los roles de varios grupos de una vez: la pantalla de accesos los pide todos juntos. */
  List<MenuRoleEntity> findByMenuGroupIdIn(List<Long> menuGroupIds);

  void deleteByMenuGroupId(Long menuGroupId);

  /** Los roles distintos que ya se usan, para no escribirlos a mano en el front. */
  @Query("select distinct r.role from MenuRoleEntity r where r.role is not null order by r.role")
  List<String> findDistinctRoles();
}
