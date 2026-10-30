package com.electorama.msvc.gateway.config.security;

import com.electorama.msvc.gateway.config.GatewayConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
@Configuration
public class SecurityConfiguration {
  private static final String FRONTEND_LOCALHOST = "http://localhost:4200";
  private static final String FRONTEND_STAGING = "https://somehost.github.io";
  Logger logger = LoggerFactory.getLogger(GatewayConfiguration.class);

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    http.cors();
    http.authorizeExchange()
        // ALLOWING REGISTER API FOR DIRECT ACCESS
        .pathMatchers("/api/logfile/**")
        .permitAll()
        .pathMatchers("/actuator/*")
        .permitAll()
        .pathMatchers("/api/survey/surveyUser/register")
        .permitAll()
        .pathMatchers("/api/survey/createFolio")
        .permitAll()
        // ALL OTHER APIS ARE AUTHENTICATED
        .anyExchange()
        .authenticated()
        .and()
        .cors()
        .and()
        .csrf()
        .disable()
        .oauth2Login()
        .and()
        .oauth2ResourceServer()
        .jwt();
    return http.build();
  }
}
