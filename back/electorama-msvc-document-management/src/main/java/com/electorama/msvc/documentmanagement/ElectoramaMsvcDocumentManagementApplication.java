package com.electorama.msvc.documentmanagement;

import com.electorama.msvc.documentmanagement.service.rest.GeographyRestClient;
import com.electorama.msvc.documentmanagement.service.rest.UsersRestClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(clients = {UsersRestClient.class, GeographyRestClient.class})
public class ElectoramaMsvcDocumentManagementApplication {

  public static void main(String[] args) {
    SpringApplication.run(ElectoramaMsvcDocumentManagementApplication.class, args);
  }
}
