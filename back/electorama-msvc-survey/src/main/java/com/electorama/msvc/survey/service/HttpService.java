package com.electorama.msvc.survey.service;

import com.electorama.msvc.survey.model.dto.CsvDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HttpService {

  @Autowired private RestTemplate restTemplate;

  public void sendRequest(CsvDto user, String url) {
    restTemplate.postForEntity(url, user, String.class);
  }
}
