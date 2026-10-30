package com.electorama.msvc.structure.model.repository;

import com.electorama.msvc.structure.model.entity.Person;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * La persona existe una sola vez, y esto es lo que rompe el ciclo {@code users ⇄ followers}: hoy
 * {@code msvc-users} sale por HTTP a {@code followers/findByCurp} para deduplicar a un humano.
 */
@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

  Optional<Person> findByCurpIgnoreCase(String curp);

  Optional<Person> findByAuthId(String authId);

  List<Person> findByIne(String ine);

  /**
   * Busca por nombre partido, que es como el front la busca.
   *
   * <p>Sin acentos ni mayúsculas: el dato viejo trae de todo.
   */
  @Query(
      value =
          "SELECT * FROM person"
              + " WHERE unaccent(lower(coalesce(names, ''))) LIKE unaccent(lower(concat('%', :q, '%')))"
              + "    OR unaccent(lower(coalesce(last_name, ''))) LIKE unaccent(lower(concat('%', :q, '%')))"
              + "    OR unaccent(lower(coalesce(sur_name, ''))) LIKE unaccent(lower(concat('%', :q, '%')))"
              + " ORDER BY last_name, sur_name, names LIMIT 50",
      nativeQuery = true)
  List<Person> search(@Param("q") String q);

  /**
   * La misma persona ocupando puestos: para saber si un humano ya está en la estructura antes de
   * darlo de alta otra vez.
   */
  @Query(
      value =
          "SELECT count(*) FROM assignment a"
              + " WHERE a.person_id = :personId AND upper(a.validity) IS NULL",
      nativeQuery = true)
  long countCurrentAssignments(@Param("personId") Long personId);
}
