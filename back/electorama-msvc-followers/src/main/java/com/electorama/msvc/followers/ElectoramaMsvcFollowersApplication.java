package com.electorama.msvc.followers;

import com.electorama.msvc.followers.service.rest.ElectionsRestClient;
import com.electorama.msvc.followers.service.rest.GeographyRestClient;
import com.electorama.msvc.followers.service.rest.StructureRestClient;
import com.electorama.msvc.followers.service.rest.UsersRestClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
// La lista es explícita, así que un cliente nuevo no se registra solo por estar
// en el paquete: hay que añadirlo aquí. StructureRestClient es el que habla con
// el modelo de estructura (fase 4 de la migración).
@EnableFeignClients(
    clients = {
      UsersRestClient.class,
      GeographyRestClient.class,
      ElectionsRestClient.class,
      StructureRestClient.class
    })
public class ElectoramaMsvcFollowersApplication {

  public static void main(String[] args) {
    SpringApplication.run(ElectoramaMsvcFollowersApplication.class, args);
  }
}
