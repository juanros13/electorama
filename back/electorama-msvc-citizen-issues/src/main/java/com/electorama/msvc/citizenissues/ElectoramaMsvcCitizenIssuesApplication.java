package com.electorama.msvc.citizenissues;

import com.electorama.msvc.citizenissues.services.rest.GeographyRestClient;
import com.electorama.msvc.citizenissues.services.rest.UsersRestClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(clients = {GeographyRestClient.class, UsersRestClient.class})
public class ElectoramaMsvcCitizenIssuesApplication {

  public static void main(String[] args) {
    SpringApplication.run(ElectoramaMsvcCitizenIssuesApplication.class, args);
  }
}
