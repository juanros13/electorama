package com.electorama.msvc.elections.structure;

import com.electorama.msvc.elections.structure.service.rest.ElectionsRestClient;
import com.electorama.msvc.elections.structure.service.rest.GeographyRestClient;
import com.electorama.msvc.elections.structure.service.rest.UsersRestClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(
    clients = {UsersRestClient.class, GeographyRestClient.class, ElectionsRestClient.class})
public class ElectoramaMsvcElectionsStructureApplication {

  public static void main(String[] args) {
    SpringApplication.run(ElectoramaMsvcElectionsStructureApplication.class, args);
  }
}
