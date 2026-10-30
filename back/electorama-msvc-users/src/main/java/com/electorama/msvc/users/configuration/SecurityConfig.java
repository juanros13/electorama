package com.electorama.msvc.users.configuration;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @org.springframework.beans.factory.annotation.Value("${app.security.validate-auth-id:true}")
  private boolean validateAuthId;

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    // Validate tokens through configured OpenID Provider
    http.cors().and().csrf().disable();
    http.headers().frameOptions().sameOrigin();
    // Require a valid JWT for every request. Only the k8s liveness/readiness probes
    // (actuator health/info) are public. Antes faltaba esta regla: sin ella Spring
    // Security 6 validaba el token solo si venía, pero NO obligaba a enviarlo, dejando
    // accesibles sin autenticación endpoints con PII (CURP/INE) y el borrado de usuarios.
    http.authorizeHttpRequests(
        auth ->
            auth.requestMatchers("/actuator/health/**", "/actuator/info")
                .permitAll()
                .anyRequest()
                .authenticated());
    http.oauth2ResourceServer().jwt().jwtAuthenticationConverter(jwtAuthenticationConverter());

    // Después de autenticar el bearer token, valida que X-Auth-Id == UUID del token.
    http.addFilterAfter(
        new AuthIdHeaderValidationFilter(validateAuthId), BearerTokenAuthenticationFilter.class);

    return http.build();
  }

  private JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    // Convert realm_access.roles claims to granted authorities, for use in access decisions
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
    return jwtAuthenticationConverter;
  }

  @Bean
  JwtDecoder jwtDecoderByIssuerUri(OAuth2ResourceServerProperties properties) {
    String issuerUri = properties.getJwt().getIssuerUri();
    NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuerUri);
    // Use preferred_username from claims as authentication name, instead of UUID subject
    jwtDecoder.setClaimSetConverter(new UsernameSubClaimAdapter());
    return jwtDecoder;
  }
}

// As per:
// https://docs.spring.io/spring-security/site/docs/5.2.x/reference/html5/#oauth2resourceserver-jwt-claimsetmapping-rename
class UsernameSubClaimAdapter implements Converter<Map<String, Object>, Map<String, Object>> {

  private final MappedJwtClaimSetConverter delegate =
      MappedJwtClaimSetConverter.withDefaults(Collections.emptyMap());

  @Override
  public Map<String, Object> convert(Map<String, Object> claims) {
    Map<String, Object> convertedClaims = this.delegate.convert(claims);
    // Preserva el UUID original (sub de Keycloak) en 'uid' antes de reemplazar sub por el
    // username, para poder validar el header X-Auth-Id contra la identidad real del token.
    convertedClaims.put("uid", convertedClaims.get("sub"));
    String username = (String) convertedClaims.get("preferred_username");
    convertedClaims.put("sub", username);
    return convertedClaims;
  }
}

class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
  Logger logger = LoggerFactory.getLogger(KeycloakRealmRoleConverter.class);

  @Override
  @SuppressWarnings("unchecked")
  public Collection<GrantedAuthority> convert(final Jwt jwt) {
    final Map<String, Object> realmAccess =
        (Map<String, Object>) jwt.getClaims().get("realm_access");

    return ((List<String>) realmAccess.get("roles"))
        .stream()
            .map(
                roleName -> {
                  // logger.info(roleName);
                  return "ROLE_" + roleName;
                })
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
  }
}
