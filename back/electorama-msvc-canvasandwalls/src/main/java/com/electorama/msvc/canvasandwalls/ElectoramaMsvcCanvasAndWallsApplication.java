package com.electorama.msvc.canvasandwalls;

import com.electorama.msvc.canvasandwalls.service.rest.GeographyRestClient;
import com.electorama.msvc.canvasandwalls.service.rest.UsersRestClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(clients = {UsersRestClient.class, GeographyRestClient.class})
public class ElectoramaMsvcCanvasAndWallsApplication {

  public static void main(String[] args) {
    SpringApplication.run(ElectoramaMsvcCanvasAndWallsApplication.class, args);
  }
}
