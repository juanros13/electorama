package com.electorama.msvc.gateway.config;

import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfiguration {

  // setting auth-id for API requests.

  private static final String HTTP_HEADER_AUTH_USER_ID = "X-Auth-Id";
  private static final String UNAUTHORIZED_USER_NAME = "SYSTEM USER";
  Logger logger = LoggerFactory.getLogger(GatewayConfiguration.class);

  @Bean
  public GlobalFilter customGlobalFilter() {

    return (exchange, chain) ->
        exchange
            .getPrincipal()
            .map(Principal::getName)
            .defaultIfEmpty(UNAUTHORIZED_USER_NAME)
            .map(
                principal -> {
                  // adds header to proxied request
                  logger.info(principal);
                  exchange
                      .getRequest()
                      .mutate()
                      .header(HTTP_HEADER_AUTH_USER_ID, principal)
                      .build();
                  return exchange;
                })
            .flatMap(chain::filter)
            .then(Mono.fromRunnable(() -> {}));
  }
}
