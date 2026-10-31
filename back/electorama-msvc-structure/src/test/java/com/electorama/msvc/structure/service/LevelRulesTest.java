package com.electorama.msvc.structure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.electorama.msvc.structure.exception.StructureException;
import com.electorama.msvc.structure.model.dto.CampaignRequests.LevelData;
import com.electorama.msvc.structure.model.entity.CampaignLevel;
import com.electorama.msvc.structure.model.enums.Level;
import com.electorama.msvc.structure.model.enums.TerritoryType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class LevelRulesTest {

  private static LevelData level(String name, String territory, String function) {
    return new LevelData(null, null, name, null, territory, null, null, function, null, null);
  }

  private static final List<LevelData> FOUR =
      List.of(
          level("Coordinador estatal", "STATE", null),
          level("Coordinador municipal", "MUNICIPALITY", "REFERENT"),
          level("Promotor", "SECTION", "ZONE_COORDINATOR"),
          level("Movilizador", "SECTION", "PROMOTER"));

  @Test
  void cuatroNivelesConRangoCodigoPluralYRolPorDefecto() {
    var plan = LevelRules.plan(FOUR, List.of(), Map.of(), "TABASCO_2030");

    assertThat(plan)
        .extracting(LevelRules.Planned::rank)
        .containsExactly((short) 1, (short) 2, (short) 3, (short) 4);
    assertThat(plan.get(0).code()).isEqualTo("COORDINADOR_ESTATAL");
    assertThat(plan.get(2).namePlural()).isEqualTo("Promotores");
    assertThat(plan.get(1).roleName()).isEqualTo("TABASCO_2030_COORDINADOR_MUNICIPAL");
    assertThat(plan.get(0).territoryType()).isEqualTo(TerritoryType.STATE);
    assertThat(plan.get(3).function()).isEqualTo(Level.PROMOTER);
  }

  @Test
  void elSlugQuitaAcentosYEspacios() {
    assertThat(LevelRules.slug("Coordinación de Sección")).isEqualTo("COORDINACION_DE_SECCION");
    assertThat(LevelRules.slug("2030 Tabasco")).isEqualTo("N_2030_TABASCO");
  }

  @Test
  void lasFuncionesVanUnaVezYEnSuOrden() {
    var invertidas =
        List.of(level("Promotor", "SECTION", "PROMOTER"), level("Jefe", null, "REFERENT"));
    assertThatThrownBy(() -> LevelRules.plan(invertidas, List.of(), Map.of(), "X"))
        .isInstanceOf(StructureException.class)
        .hasMessageContaining("debe ir debajo");

    var repetidas = List.of(level("Uno", null, "REFERENT"), level("Dos", null, "REFERENT"));
    assertThatThrownBy(() -> LevelRules.plan(repetidas, List.of(), Map.of(), "X"))
        .isInstanceOf(StructureException.class);
  }

  @Test
  void rechazaCodigosRepetidosSinNombreYRolesInvalidos() {
    var dobles = List.of(level("Enlace", null, null), level("enlace", null, null));
    assertThatThrownBy(() -> LevelRules.plan(dobles, List.of(), Map.of(), "X"))
        .hasMessageContaining("dos niveles con el código ENLACE");

    assertThatThrownBy(
            () -> LevelRules.plan(List.of(level(" ", null, null)), List.of(), Map.of(), "X"))
        .hasMessageContaining("no tiene nombre");

    var conRol =
        new LevelData(null, null, "Enlace", null, null, null, null, null, "con espacio", null);
    assertThatThrownBy(() -> LevelRules.plan(List.of(conRol), List.of(), Map.of(), "X"))
        .hasMessageContaining("MAYÚSCULAS");

    assertThatThrownBy(() -> LevelRules.plan(List.of(), List.of(), Map.of(), "X"))
        .hasMessageContaining("al menos un nivel");
  }

  @Test
  void unNivelConPuestosSoloCambiaNombreMetaYRoles() {
    CampaignLevel promotor =
        CampaignLevel.builder()
            .id(7L)
            .rank((short) 1)
            .code("PROMOTOR")
            .nameSingular("Promotor")
            .territoryType(TerritoryType.SECTION)
            .levelFunction(Level.ZONE_COORDINATOR)
            .roleName("CORDINATOR_ZONE")
            .build();
    Map<Long, Long> used = Map.of(7L, 12L);

    var renombrado =
        new LevelData(
            7L,
            "PROMOTOR",
            "Promovente",
            "Promoventes",
            "SECTION",
            150,
            null,
            "ZONE_COORDINATOR",
            "CORDINATOR_ZONE",
            null);
    assertThat(
            LevelRules.plan(List.of(renombrado), List.of(promotor), used, "X")
                .get(0)
                .nameSingular())
        .isEqualTo("Promovente");

    var movido =
        new LevelData(
            7L,
            "PROMOTOR",
            "Promotor",
            null,
            "MUNICIPALITY",
            null,
            null,
            "ZONE_COORDINATOR",
            null,
            null);
    assertThatThrownBy(() -> LevelRules.plan(List.of(movido), List.of(promotor), used, "X"))
        .isInstanceOfSatisfying(
            StructureException.class,
            e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.CONFLICT));

    var otro = level("Nuevo", null, null);
    assertThatThrownBy(() -> LevelRules.plan(List.of(otro), List.of(promotor), used, "X"))
        .hasMessageContaining("no se puede quitar");
  }
}
