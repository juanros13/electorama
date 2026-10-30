package com.electorama.googlecalendar.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class GoogleApiController {
  private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

  private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);

  @Value("${google.oauth.callback.uri}")
  private String CALLBACK_URI;

  @Value("${google.secret.key.path}")
  private Resource gdSecretsKeys;

  private static final String TOKENS_DIRECTORY_PATH = "tokens";
  private GoogleAuthorizationCodeFlow flow;

  @PostConstruct
  public void init() throws Exception {
    // Load client secrets.
    GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(
            JSON_FACTORY, new InputStreamReader(gdSecretsKeys.getInputStream()));

    flow =
        new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
            .build();
  }

  @GetMapping(value = "/googlesignin")
  public void doGoogleSignIn(HttpServletResponse response) throws Exception {
    GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();
    String redirectUrl = url.setRedirectUri(CALLBACK_URI).setAccessType("offline").build();
    response.sendRedirect(redirectUrl);
  }
}
