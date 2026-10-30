package com.electorama.msvc.electionsresults;

import com.electorama.msvc.electionsresults.service.rest.ElectionsRestClient;
import com.electorama.msvc.electionsresults.service.rest.GeographyRestClient;
import com.electorama.msvc.electionsresults.service.rest.UsersRestClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(
    clients = {UsersRestClient.class, GeographyRestClient.class, ElectionsRestClient.class})
public class ElectoramaMsvcElectionsResultsApplication {

  public static void main(String[] args) {
    SpringApplication.run(ElectoramaMsvcElectionsResultsApplication.class, args);
  }
}
