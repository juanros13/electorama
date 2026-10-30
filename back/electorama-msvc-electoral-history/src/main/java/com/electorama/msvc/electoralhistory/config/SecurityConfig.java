package com.electorama.msvc.electoralhistory.config;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Token de Keycloak obligatorio en todo menos health e info.
 *
 * <p>A diferencia de structure y de los servicios de elecciones, aquí la autenticación no depende
 * de que el gateway la pida: sin {@code authorizeHttpRequests} el servicio responde a cualquiera
 * que llegue directo a su puerto. Los roles se revisan con {@code @PreAuthorize} en los
 * controladores.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.cors()
        .and()
        .csrf()
        .disable()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    http.authorizeHttpRequests(
        auth ->
            auth.requestMatchers("/actuator/health/**", "/actuator/info")
                .permitAll()
                .anyRequest()
                .authenticated());
    http.oauth2ResourceServer().jwt().jwtAuthenticationConverter(jwtAuthenticationConverter());
    return http.build();
  }

  private JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
    return converter;
  }

  /** {@code realm_access.roles} de Keycloak → {@code ROLE_*}, como en el resto del back. */
  static class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> convert(Jwt jwt) {
      Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
      if (realmAccess == null || !(realmAccess.get("roles") instanceof List<?>)) {
        return List.of();
      }
      return ((List<String>) realmAccess.get("roles"))
          .stream()
              .<GrantedAuthority>map(role -> new SimpleGrantedAuthority("ROLE_" + role))
              .toList();
    }
  }
}
