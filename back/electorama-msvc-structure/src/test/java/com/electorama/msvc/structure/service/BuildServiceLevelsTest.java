package com.electorama.msvc.structure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.electorama.msvc.structure.model.dto.BuildRequests.CreatePosition;
import com.electorama.msvc.structure.model.entity.CampaignLevel;
import com.electorama.msvc.structure.model.enums.Level;
import com.electorama.msvc.structure.model.enums.StructureType;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Cómo resuelve BuildService los niveles de una campaña de cuatro, sin base. */
class BuildServiceLevelsTest {

  private static CampaignLevel level(long id, int rank, String code, Level function) {
    return CampaignLevel.builder()
        .id(id)
        .rank((short) rank)
        .code(code)
        .nameSingular(code)
        .levelFunction(function)
        .roleName("R_" + code)
        .build();
  }

  private static final CampaignLevel ESTATAL = level(1, 1, "ESTATAL", null);
  private static final CampaignLevel MUNICIPAL = level(2, 2, "MUNICIPAL", Level.REFERENT);
  private static final CampaignLevel PROMOTOR = level(3, 3, "PROMOTOR", Level.ZONE_COORDINATOR);
  private static final CampaignLevel COLONIA = level(4, 4, "MOVILIZADOR_DE_COLONIA", null);
  private static final List<CampaignLevel> ALL = List.of(ESTATAL, MUNICIPAL, PROMOTOR, COLONIA);

  private static CreatePosition request(Long levelId, Level level) {
    return new CreatePosition(1L, level, null, null, null, null, null, levelId, null);
  }

  @Test
  void elNivelDeArribaYElDeAbajo() {
    assertThat(BuildService.above(ALL, ESTATAL)).isNull();
    assertThat(BuildService.above(ALL, PROMOTOR)).isSameAs(MUNICIPAL);
    assertThat(BuildService.below(ALL, MUNICIPAL)).isSameAs(PROMOTOR);
    assertThat(BuildService.below(ALL, COLONIA)).isNull();
  }

  @Test
  void elNivelPedidoPorIdOPorSuFuncion() {
    assertThat(BuildService.requestedLevel(ALL, request(3L, null))).isSameAs(PROMOTOR);
    assertThat(BuildService.requestedLevel(ALL, request(null, Level.REFERENT))).isSameAs(MUNICIPAL);
    assertThatThrownBy(() -> BuildService.requestedLevel(ALL, request(99L, null)))
        .hasMessageContaining("no es de esta campaña");
    assertThatThrownBy(() -> BuildService.requestedLevel(ALL, request(null, Level.PROMOTER)))
        .hasMessageContaining("no tiene un nivel PROMOTER");
    assertThatThrownBy(() -> BuildService.requestedLevel(ALL, request(null, null)))
        .hasMessageContaining("falta el nivel");
  }

  @Test
  void lasFuncionesConservanSuPrefijoYLosNivelesNuevosUsanSuCodigo() {
    assertThat(BuildService.prefix(MUNICIPAL)).isEqualTo("REF");
    assertThat(BuildService.prefix(PROMOTOR)).isEqualTo("CZ");
    assertThat(BuildService.prefix(ESTATAL)).isEqualTo("ESTATAL");
    assertThat(BuildService.prefix(COLONIA)).isEqualTo("MOVILIZADOR_");
  }

  @Test
  void elRolDelNivelUsaElAlternoSoloEnLaAlterna() {
    CampaignLevel conAlterno =
        CampaignLevel.builder()
            .roleName("CORDINATOR_ZONE")
            .alternateRoleName("CORDINATOR_ZONE_ALT")
            .build();
    assertThat(conAlterno.roleFor(StructureType.MAIN)).isEqualTo("CORDINATOR_ZONE");
    assertThat(conAlterno.roleFor(StructureType.ALTERNATE)).isEqualTo("CORDINATOR_ZONE_ALT");
    assertThat(ESTATAL.roleFor(StructureType.ALTERNATE)).isEqualTo("R_ESTATAL");
  }
}
