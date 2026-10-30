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
 * <p>Sigue entrando al realm {@code master} con usuario y contraseña de administrador. Lo correcto
 * es una cuenta de servicio (client credentials) con los roles de {@code realm-management}; eso se
 * configura en Keycloak y aquí sólo cambiaría el {@code grantType}.
 */
@Configuration
public class KeycloakAdminConfig {

  @Bean(destroyMethod = "close")
  Keycloak keycloakAdmin(
      @Value("${app.config.keycloak.server-url}") String serverUrl,
      @Value("${app.config.keycloak.clientId}") String clientId,
      @Value("${app.config.keycloak.user}") String user,
      @Value("${app.config.keycloak.pass}") String pass) {
    return KeycloakBuilder.builder()
        .serverUrl(serverUrl)
        .realm("master")
        .grantType(OAuth2Constants.PASSWORD)
        .clientId(clientId)
        .username(user)
        .password(pass)
        .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build())
        .build();
  }

  @Bean
  RealmResource realm(Keycloak keycloakAdmin, @Value("${app.config.keycloak.realm}") String realm) {
    return keycloakAdmin.realm(realm);
  }
}
