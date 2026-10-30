package com.electorama.elections;

import com.electorama.elections.services.rest.ElectionsResultsRestClient;
import com.electorama.elections.services.rest.FollowersRestClient;
import com.electorama.elections.services.rest.GeographyRestClient;
import com.electorama.elections.services.rest.UsersRestClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(
    clients = {
      UsersRestClient.class,
      GeographyRestClient.class,
      ElectionsResultsRestClient.class,
      FollowersRestClient.class
    })
public class ElectoramaMsvcElectionsApplication {

  public static void main(String[] args) {
    SpringApplication.run(ElectoramaMsvcElectionsApplication.class, args);
  }
}
