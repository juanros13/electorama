package com.electorama.msvc.users.configuration;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * El cliente de administración de Keycloak, como bean.
 *
 * <p>Antes era un {@code static} mutable que se armaba perezosamente sin sincronizar: dos
 * peticiones simultáneas al arrancar podían crear dos clientes. El {@link Keycloak} ya es seguro
 * entre hilos y renueva su token solo, así que basta un singleton de Spring.
 *
 * <p>Con secreto, entra como la cuenta de servicio de su cliente (client credentials) en el propio
 * realm de la aplicación: el cliente necesita los roles {@code manage-users}, {@code view-users},
 * {@code manage-realm} y {@code view-realm} de {@code realm-management}. Sin secreto, sigue como
 * antes: usuario y contraseña de administrador del realm {@code master}.
 */
@Configuration
public class KeycloakAdminConfig {

  @Bean(destroyMethod = "close")
  Keycloak keycloakAdmin(
      @Value("${app.config.keycloak.server-url}") String serverUrl,
      @Value("${app.config.keycloak.realm}") String realm,
      @Value("${app.config.keycloak.clientId}") String clientId,
      @Value("${app.config.keycloak.secret:}") String secret,
      @Value("${app.config.keycloak.user}") String user,
      @Value("${app.config.keycloak.pass}") String pass) {
    KeycloakBuilder builder =
        KeycloakBuilder.builder()
            .serverUrl(serverUrl)
            .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build());
    if (!secret.isBlank()) {
      return builder
          .realm(realm)
          .clientId(clientId)
          .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
          .clientSecret(secret)
          .build();
    }
    return builder
        .realm("master")
        .clientId("admin-cli")
        .grantType(OAuth2Constants.PASSWORD)
        .username(user)
        .password(pass)
        .build();
  }

  @Bean
  RealmResource realm(Keycloak keycloakAdmin, @Value("${app.config.keycloak.realm}") String realm) {
    return keycloakAdmin.realm(realm);
  }
}
