package com.electorama.msvc.structure.controller;

import com.electorama.msvc.structure.model.entity.Person;
import com.electorama.msvc.structure.service.PersonService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * La persona. Es el endpoint que sustituye a {@code followers/findByCurp} para deduplicar: el
 * humano existe una sola vez y ser convencido o promotor es una relación con vigencia, no un
 * atributo.
 */
@RestController
@RequestMapping("/persons")
@RequiredArgsConstructor
public class PersonController {

  private final PersonService personService;

  @GetMapping("/{id}")
  public Person get(@PathVariable Long id) {
    return personService.get(id);
  }

  @GetMapping("/by-curp/{curp}")
  public Person byCurp(@PathVariable String curp) {
    return personService.byCurp(curp);
  }

  @GetMapping("/by-ine/{ine}")
  public List<Person> byIne(@PathVariable String ine) {
    return personService.byIne(ine);
  }

  @GetMapping("/by-auth/{authId}")
  public Person byAuthId(@PathVariable String authId) {
    return personService.byAuthId(authId);
  }

  @GetMapping("/search")
  public List<Person> search(@RequestParam("q") String query) {
    return personService.search(query);
  }

  @PostMapping
  public Person create(@RequestBody Person person) {
    return personService.create(person);
  }
}
