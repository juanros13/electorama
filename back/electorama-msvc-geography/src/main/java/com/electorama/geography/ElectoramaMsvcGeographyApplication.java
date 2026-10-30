package com.electorama.geography;

import com.electorama.geography.service.rest.UsersRestClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableCaching
@EnableFeignClients(clients = {UsersRestClient.class})
public class ElectoramaMsvcGeographyApplication {

  public static void main(String[] args) {
    SpringApplication.run(ElectoramaMsvcGeographyApplication.class, args);
  }

  /**
   * Caché en memoria para los catálogos territoriales (datos casi inmutables). Mitiga el N+1 de los
   * microservicios que consultan geography por id en bucle (ej. canvas-walls al enriquecer cada
   * fila con municipio/localidad/sección): la 1ª lectura va a BD, las siguientes salen de memoria.
   * Las escrituras de localidades evictan su caché (ver LocalitiesSectionServiceImpl).
   */
  @Bean
  public CacheManager cacheManager() {
    return new ConcurrentMapCacheManager(
        "municipalityById",
        "municipalitiesByEntity",
        "allMunicipalities",
        "localitiesSectionById",
        "allLocalitiesSection",
        "localitiesBySection");
  }
}
