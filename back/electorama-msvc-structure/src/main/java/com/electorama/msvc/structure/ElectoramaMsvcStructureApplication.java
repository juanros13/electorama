package com.electorama.msvc.structure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ElectoramaMsvcStructureApplication {

  public static void main(String[] args) {
    SpringApplication.run(ElectoramaMsvcStructureApplication.class, args);
  }
}
