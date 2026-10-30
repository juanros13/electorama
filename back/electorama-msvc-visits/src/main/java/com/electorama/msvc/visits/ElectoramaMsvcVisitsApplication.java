package com.electorama.msvc.visits;

import com.electorama.msvc.visits.service.rest.GeographyRestClient;
import com.electorama.msvc.visits.service.rest.UsersRestClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(clients = {UsersRestClient.class, GeographyRestClient.class})
public class ElectoramaMsvcVisitsApplication {

  public static void main(String[] args) {
    SpringApplication.run(ElectoramaMsvcVisitsApplication.class, args);
  }
}
