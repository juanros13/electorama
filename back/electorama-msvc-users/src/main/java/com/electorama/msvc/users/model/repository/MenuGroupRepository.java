package com.electorama.msvc.users.model.repository;

import com.electorama.msvc.users.model.entity.MenuGroupEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuGroupRepository extends JpaRepository<MenuGroupEntity, Long> {

  /** Las entradas que ve alguno de estos roles, en una sola consulta y en orden estable. */
  @Query(
      "select g from MenuGroupEntity g"
          + " where g.pk in (select r.menuGroupId from MenuRoleEntity r where r.role in :roles)"
          + " order by g.order, g.pk")
  List<MenuGroupEntity> findVisibleTo(@Param("roles") Collection<String> roles);

  List<MenuGroupEntity> findAllByOrderByOrderAscPkAsc();
}
