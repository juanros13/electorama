package com.electorama.msvc.electoralhistory;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Contra la base LOCAL ya cargada (E1..E5 de Presidencia 2024), así que sólo corre con
 * ELECTORAL_HISTORY_IT=true. Las cifras esperadas son las oficiales del INE para Tabasco.
 *
 * <pre>
 * ELECTORAL_HISTORY_IT=true mvn -pl electorama-msvc-electoral-history verify -Dtest=ElectionControllerIT
 * </pre>
 */
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "ELECTORAL_HISTORY_IT", matches = "true")
class ElectionControllerIT {

  @Autowired MockMvc mvc;

  private static final String PRES_2024 = "/v1/elections/2024/PRESIDENT/territories";

  @Test
  void sinTokenEs401() throws Exception {
    mvc.perform(get(PRES_2024)).andExpect(status().isUnauthorized());
  }

  @Test
  void sinRolAdminEs403() throws Exception {
    mvc.perform(get(PRES_2024).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PROMOTER"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void presidencia2024CuadraConElIne() throws Exception {
    mvc.perform(get(PRES_2024).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.election.cutLabel").value("08/06/2024 20:30 (UTC-6)"))
        .andExpect(jsonPath("$.state.totalVotes").value(1114001))
        .andExpect(jsonPath("$.state.nullVotes").value(25696))
        .andExpect(jsonPath("$.state.winner").value("SHH"))
        .andExpect(jsonPath("$.state.groups[0].votes").value(897143))
        .andExpect(jsonPath("$.territories.length()").value(17));
  }

  @Test
  void lasSeccionesDeCentroSumanElMunicipio() throws Exception {
    var admin = jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    String municipalities =
        mvc.perform(get(PRES_2024).with(admin)).andReturn().getResponse().getContentAsString();
    String sections =
        mvc.perform(get(PRES_2024 + "/4/sections").with(admin))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    var json = new com.fasterxml.jackson.databind.ObjectMapper();
    long centro = 0;
    for (var m : json.readTree(municipalities).get("territories")) {
      if (m.get("code").asInt() == 4) {
        centro = m.get("totalVotes").asLong();
      }
    }
    var tree = json.readTree(sections);
    long sumSections = 0;
    for (var s : tree.get("sections")) {
      sumSections += s.get("totalVotes").asLong();
    }
    org.junit.jupiter.api.Assertions.assertTrue(centro > 0);
    org.junit.jupiter.api.Assertions.assertEquals(centro, sumSections);
    org.junit.jupiter.api.Assertions.assertEquals(
        centro, tree.get("territory").get("totalVotes").asLong());
    org.junit.jupiter.api.Assertions.assertEquals(
        "CENTRO", tree.get("territory").get("name").asText());
  }

  @Test
  void laTendenciaDeAyuntamientosTraeLosTresAnios() throws Exception {
    mvc.perform(
            get("/v1/trend")
                .param("office", "MAYOR")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.elections.length()").value(3))
        .andExpect(jsonPath("$.state.points[0].year").value(2018))
        .andExpect(jsonPath("$.state.points[2].year").value(2024))
        .andExpect(jsonPath("$.state.points[2].winner.code").exists())
        .andExpect(jsonPath("$.territories.length()").value(17))
        .andExpect(jsonPath("$.territories[0].points.length()").value(3));
  }

  @Test
  void elTotalEstatalNoCambiaConElNivel() throws Exception {
    var admin = jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    for (String level : new String[] {"MUNICIPALITY", "LOCAL_DISTRICT", "FEDERAL_DISTRICT"}) {
      mvc.perform(get(PRES_2024).param("level", level).with(admin))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.state.totalVotes").value(1114001))
          .andExpect(jsonPath("$.state.groups[0].votes").value(897143));
    }
    mvc.perform(get(PRES_2024).param("level", "LOCAL_DISTRICT").with(admin))
        .andExpect(jsonPath("$.territories.length()").value(21));
    mvc.perform(get(PRES_2024).param("level", "FEDERAL_DISTRICT").with(admin))
        .andExpect(jsonPath("$.territories.length()").value(6))
        .andExpect(
            jsonPath("$.territories[0].name")
                .value(org.hamcrest.Matchers.startsWith("DISTRITO FEDERAL 01")));
  }

  @Test
  void diputacionFederalPorDistritoTraeLosCandidatos() throws Exception {
    mvc.perform(
            get("/v1/elections/2024/FEDERAL_DEPUTY/territories")
                .param("level", "FEDERAL_DISTRICT")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.candidatesAvailable").value(true))
        .andExpect(jsonPath("$.territories.length()").value(6))
        .andExpect(jsonPath("$.territories[0].groups[0].candidate").isNotEmpty());
  }

  @Test
  void analisis2024SenalaLasAlcaldiasEnRiesgo() throws Exception {
    mvc.perform(
            get("/v1/analysis/2024")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.brand.office").value("GOVERNOR"))
        .andExpect(jsonPath("$.previousYear").value(2021))
        .andExpect(jsonPath("$.municipalities.length()").value(17))
        .andExpect(jsonPath("$.localDistricts.length()").value(21))
        .andExpect(jsonPath("$.kpis.mayoraltiesLost").value(3))
        .andExpect(jsonPath("$.kpis.localDistrictsLost").value(0))
        // Paraíso (14): la perdió MORENA ante MC, con los aliados divididos
        .andExpect(
            jsonPath("$.municipalities[?(@.code == 14)].status")
                .value(org.hamcrest.Matchers.contains("LOST")))
        .andExpect(
            jsonPath("$.municipalities[?(@.code == 14)].winner.code")
                .value(org.hamcrest.Matchers.contains("MC")))
        .andExpect(
            jsonPath("$.municipalities[?(@.code == 14)].alliesSplit")
                .value(org.hamcrest.Matchers.contains(true)))
        // Jalpa de Méndez (10): ganada por 0.1 pp
        .andExpect(
            jsonPath("$.municipalities[?(@.code == 10)].status")
                .value(org.hamcrest.Matchers.contains("CLOSE")))
        .andExpect(jsonPath("$.sections[0].status").value("LOST"))
        .andExpect(jsonPath("$.findings").isNotEmpty());
  }

  @Test
  void todasLasSeccionesDeAyuntamiento2024() throws Exception {
    mvc.perform(
            get("/v1/elections/2024/MAYOR/sections")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1181))
        .andExpect(jsonPath("$[0].blocVotes.MORENA_ALLIES").isNumber());
  }

  @Test
  void agrupacionDesconocidaEs400() throws Exception {
    mvc.perform(
            get(PRES_2024)
                .param("groupBy", "nada")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isBadRequest());
  }
}
