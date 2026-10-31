package com.electorama.msvc.structure.controller;

import com.electorama.msvc.structure.model.dto.CampaignRequests.CampaignData;
import com.electorama.msvc.structure.model.dto.CampaignRequests.Elections;
import com.electorama.msvc.structure.model.dto.CampaignRequests.Levels;
import com.electorama.msvc.structure.model.dto.CampaignRequests.StatusChange;
import com.electorama.msvc.structure.model.dto.CampaignRequests.StructureData;
import com.electorama.msvc.structure.model.dto.CampaignSummary;
import com.electorama.msvc.structure.model.repository.TerritoryRepository;
import com.electorama.msvc.structure.model.repository.TerritoryRepository.TerritoryOption;
import com.electorama.msvc.structure.service.CampaignAdminService;
import com.electorama.msvc.structure.service.CampaignService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Las campañas: consultarlas (cualquiera con sesión) y configurarlas (ADMIN): datos generales,
 * estatus, cargos en disputa, estructuras y niveles del organigrama.
 */
@RestController
@RequestMapping("/campaigns")
@RequiredArgsConstructor
public class CampaignController {

  private final CampaignService campaignService;
  private final CampaignAdminService admin;
  private final TerritoryRepository territories;

  @GetMapping
  public List<CampaignSummary> all() {
    return campaignService.all();
  }

  @GetMapping("/active")
  public CampaignSummary active() {
    return campaignService.active();
  }

  /** Estados y municipios que pueden ser el alcance de una campaña. */
  @GetMapping("/territory-options")
  @PreAuthorize("hasRole('ADMIN')")
  public List<TerritoryOption> territoryOptions() {
    return territories.campaignScopes();
  }

  @GetMapping("/{id}")
  public CampaignSummary detail(@PathVariable Long id) {
    return campaignService.detail(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMIN')")
  public CampaignSummary create(@RequestBody CampaignData data) {
    return admin.create(data);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public CampaignSummary update(@PathVariable Long id, @RequestBody CampaignData data) {
    return admin.update(id, data);
  }

  @PatchMapping("/{id}/status")
  @PreAuthorize("hasRole('ADMIN')")
  public CampaignSummary changeStatus(@PathVariable Long id, @RequestBody StatusChange change) {
    return admin.changeStatus(id, change == null ? null : change.status());
  }

  /** La lista completa de niveles, de la cúspide hacia abajo. */
  @PutMapping("/{id}/levels")
  @PreAuthorize("hasRole('ADMIN')")
  public CampaignSummary replaceLevels(@PathVariable Long id, @RequestBody Levels body) {
    return admin.replaceLevels(id, body == null ? null : body.levels());
  }

  @PutMapping("/{id}/elections")
  @PreAuthorize("hasRole('ADMIN')")
  public CampaignSummary replaceElections(@PathVariable Long id, @RequestBody Elections body) {
    return admin.replaceElections(id, body == null ? null : body.elections());
  }

  @PostMapping("/{id}/structures")
  @PreAuthorize("hasRole('ADMIN')")
  public CampaignSummary addStructure(@PathVariable Long id, @RequestBody StructureData data) {
    return admin.addStructure(id, data);
  }

  @PutMapping("/structures/{structureId}")
  @PreAuthorize("hasRole('ADMIN')")
  public CampaignSummary updateStructure(
      @PathVariable Long structureId, @RequestBody StructureData data) {
    return admin.updateStructure(structureId, data);
  }

  @DeleteMapping("/structures/{structureId}")
  @PreAuthorize("hasRole('ADMIN')")
  public CampaignSummary deleteStructure(@PathVariable Long structureId) {
    return admin.deleteStructure(structureId);
  }
}
