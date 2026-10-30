package com.electorama.msvc.structure.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Las llamadas a otros servicios salen con el token de quien hizo la petición: msvc-users decide
 * con SUS roles si puede crear cuentas. Sólo funciona en el hilo de la petición, que es donde se
 * usa.
 */
@Configuration
public class FeignAuthForwarding {

  @Bean
  RequestInterceptor forwardAuthorization() {
    return template -> {
      if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
        HttpServletRequest request = attrs.getRequest();
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null) {
          template.header(HttpHeaders.AUTHORIZATION, authorization);
        }
      }
    };
  }
}
