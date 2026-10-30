package com.electorama.msvc.structure.controller;

import com.electorama.msvc.structure.model.dto.CampaignSummary;
import com.electorama.msvc.structure.service.CampaignService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Las campañas: la activa (2027) y las cerradas, que sólo se consultan (2024). */
@RestController
@RequestMapping("/campaigns")
@RequiredArgsConstructor
public class CampaignController {

  private final CampaignService campaignService;

  @GetMapping
  public List<CampaignSummary> all() {
    return campaignService.all();
  }

  @GetMapping("/active")
  public CampaignSummary active() {
    return campaignService.active();
  }
}
