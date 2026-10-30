package com.electorama.msvc.users.service;

import com.electorama.msvc.users.exception.ApiException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Le pregunta a msvc-structure si una cuenta ocupa un puesto en la rama de quien llama. La rama
 * vive en el organigrama, no aquí, así que msvc-users no puede contestarlo solo.
 *
 * <p>Viaja con el token de quien llama: msvc-structure calcula la rama desde ese token, así que no
 * se le puede pedir la de otro.
 */
@Slf4j
@Component
public class StructureBranchClient {

  private final RestTemplate http;
  private final String baseUrl;

  public StructureBranchClient(
      RestTemplateBuilder builder,
      @Value("${app.config.structure-url:${MSVC_STRUCTURE:http://localhost:9014}}")
          String baseUrl) {
    this.http = builder.build();
    this.baseUrl = baseUrl;
  }

  public boolean holderInBranch(String targetAuthId, String bearerToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(bearerToken);
    try {
      ResponseEntity<Map> response =
          http.exchange(
              baseUrl + "/positions/holders/{authId}/in-branch",
              HttpMethod.GET,
              new HttpEntity<>(headers),
              Map.class,
              targetAuthId);
      Map<?, ?> body = response.getBody();
      return body != null && Boolean.TRUE.equals(body.get("inBranch"));
    } catch (RestClientException e) {
      log.warn("msvc-structure no contestó si {} es de la rama: {}", targetAuthId, e.getMessage());
      throw ApiException.structure("No se pudo comprobar la rama en msvc-structure");
    }
  }
}
