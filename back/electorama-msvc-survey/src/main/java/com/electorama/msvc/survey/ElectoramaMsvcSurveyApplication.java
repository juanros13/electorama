package com.electorama.msvc.survey;

import com.electorama.msvc.survey.service.rest.UsersRestClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(clients = {UsersRestClient.class})
public class ElectoramaMsvcSurveyApplication {

  public static void main(String[] args) {
    SpringApplication.run(ElectoramaMsvcSurveyApplication.class, args);
  }
}
