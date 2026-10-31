package com.electorama.msvc.followers.service.rest;

import com.electorama.msvc.followers.config.CustomFeignClientConfiguration;
import com.electorama.msvc.followers.model.rest.StructureCountResponse;
import com.electorama.msvc.followers.model.rest.StructureFollowerResponse;
import com.electorama.msvc.followers.model.rest.StructurePageResponse;
import java.util.List;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * El modelo de estructura: puestos, nombramientos y convencidos.
 *
 * <p>Este cliente es el que rompe el ciclo con la base propia: la jerarquia dejo de resolverse con
 * un JOIN local a `promoters` y se le pregunta a su dueno. Ver la fase 4 de la migracion.
 */
@FeignClient(
    name = "${app.config.structure-url}",
    configuration = CustomFeignClientConfiguration.class,
    url = "${app.config.structure-url}")
public interface StructureRestClient {

  @GetMapping("/memberships/by-coordinator/{authId}")
  StructurePageResponse<StructureFollowerResponse> findByCoordinator(
      @PathVariable("authId") String authId,
      @RequestParam(value = "promoterAuthId", required = false) String promoterAuthId,
      @RequestParam("page") int page,
      @RequestParam("size") int size);
}
