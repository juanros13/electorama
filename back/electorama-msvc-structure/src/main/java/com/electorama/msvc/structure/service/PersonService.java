package com.electorama.msvc.structure.service;

import com.electorama.msvc.structure.exception.StructureException;
import com.electorama.msvc.structure.model.entity.Person;
import com.electorama.msvc.structure.model.repository.PersonRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * La persona, una sola vez.
 *
 * <p>Estos endpoints son los que dejan a {@code msvc-users} deduplicar sin llamar a {@code
 * msvc-followers}: es el lado de la flecha que rompe el ciclo {@code users ⇄ followers}.
 */
@Service
@RequiredArgsConstructor
public class PersonService {

  private final PersonRepository persons;

  @Transactional(readOnly = true)
  public Person get(Long id) {
    return persons.findById(id).orElseThrow(() -> StructureException.notFound("la persona", id));
  }

  @Transactional(readOnly = true)
  public Person byCurp(String curp) {
    return persons
        .findByCurpIgnoreCase(curp)
        .orElseThrow(() -> StructureException.notFound("la persona con CURP", curp));
  }

  @Transactional(readOnly = true)
  public List<Person> byIne(String ine) {
    return persons.findByIne(ine);
  }

  @Transactional(readOnly = true)
  public Person byAuthId(String authId) {
    return persons
        .findByAuthId(authId)
        .orElseThrow(() -> StructureException.notFound("la persona con cuenta", authId));
  }

  @Transactional(readOnly = true)
  public List<Person> search(String query) {
    if (query == null || query.trim().length() < 3) {
      throw StructureException.badRequest("la búsqueda necesita al menos 3 caracteres");
    }
    return persons.search(query.trim());
  }

  @Transactional
  public Person create(Person person) {
    if (person.getCurp() != null) {
      persons
          .findByCurpIgnoreCase(person.getCurp())
          .ifPresent(
              existing -> {
                // El UNIQUE parcial de la base también lo impide; aquí el mensaje dice cuál es.
                throw StructureException.conflict(
                    "la CURP ya está en la persona " + existing.getId());
              });
    }
    return persons.save(person);
  }
}
