package com.electorama.msvc.followers.controller;

import com.electorama.msvc.followers.service.CatalogsFollowersService;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class CatalogsFollowersController {

  @Autowired private CatalogsFollowersService catalogsFollowersService;

  @GetMapping("/catFamilyRole")
  public Map<String, Object> catFamilyRole() {

    Map<String, Object> body = new HashMap<>();
    body.put("catFamilyRole", catalogsFollowersService.findAllFamilyRole());
    return body;
  }

  @GetMapping("/catMaritalStatus")
  public Map<String, Object> catMaritalStatus() {

    Map<String, Object> body = new HashMap<>();
    body.put("catMaritalStatus", catalogsFollowersService.findAllMaritalStatus());
    return body;
  }

  @GetMapping("/catSex")
  public Map<String, Object> catSex() {

    Map<String, Object> body = new HashMap<>();
    body.put("catSex", catalogsFollowersService.findAllSex());
    return body;
  }
}
