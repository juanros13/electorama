package com.electorama.msvc.canvasandwalls.controller;

import com.dropbox.core.*;
import com.dropbox.core.DbxException;
import com.electorama.msvc.canvasandwalls.service.UploadFileService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/dropbox")
public class UploadFileController {

  @Value("${app.dropbox.app-key}")
  private String dropboxAppKey;

  @Value("${app.dropbox.app-secret}")
  private String dropboxAppSecret;

  @Autowired UploadFileService uploadFile;

  @PostMapping("/upload")
  public String uploadFile(@RequestParam("file") MultipartFile file)
      throws IOException, DbxException {
    uploadFile.uploadFile(file);
    return "Archivo subido exitosamente a Dropbox";
  }

  @PostMapping("/getTokenRefresh")
  public String getTokenRefresh(@RequestParam("file") MultipartFile file)
      throws IOException, DbxException {
    // uploadFile.uploadFile(file);

    DbxRequestConfig config = DbxRequestConfig.newBuilder("myexamplefolder").build();

    DbxAppInfo appInfo = new DbxAppInfo(dropboxAppKey, dropboxAppSecret);

    DbxAuthFinish authFinish = scopeAuthorize(appInfo, config);

    System.out.println("Authorization complete.");
    System.out.println("- User ID: " + authFinish.getUserId());
    System.out.println("- Account ID: " + authFinish.getAccountId());
    System.out.println("- Access Token: " + authFinish.getAccessToken());
    System.out.println("- Expires At: " + authFinish.getExpiresAt());
    System.out.println("- Refresh Token: " + authFinish.getRefreshToken());
    System.out.println("- Scope: " + authFinish.getScope());
    return "Archivo subido exitosamente a Dropbox";
  }

  private static DbxAuthFinish scopeAuthorize(DbxAppInfo appInfo, DbxRequestConfig requestConfig)
      throws IOException, DbxException {
    DbxWebAuth webAuth = new DbxWebAuth(requestConfig, appInfo);

    DbxWebAuth.Request webAuthRequest =
        DbxWebAuth.newRequestBuilder()
            .withNoRedirect()
            .withTokenAccessType(TokenAccessType.OFFLINE)
            // Define here the scopes that you need in your application - and the app created in
            // Dropbox has
            .withScope(Arrays.asList("files.content.read", "files.content.write"))
            .withIncludeGrantedScopes(IncludeGrantedScopes.USER)
            .build();

    String authorizeUrl = webAuth.authorize(webAuthRequest);
    System.out.println("1. Go to " + authorizeUrl);
    System.out.println("2. Click \"Allow\" (you might have to log in first).");
    System.out.println("3. Copy the authorization code.");
    System.out.print("Enter the authorization code here: ");

    String code = new BufferedReader(new InputStreamReader(System.in)).readLine();
    if (code == null) {
      System.exit(1);
    }
    code = code.trim();

    DbxAuthFinish authFinish = webAuth.finishFromCode(code);

    System.out.println("Successfully requested scope " + authFinish.getScope());
    return authFinish;
  }
}
