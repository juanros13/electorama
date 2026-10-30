package com.electorama.msvc.followers.controller;

import com.dropbox.core.v2.DbxClientV2;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Slf4j
@RestController
public class DropboxDownloadController {

  @Autowired private DbxClientV2 dbxClient;

  @GetMapping("/dropboxDownload")
  public ResponseEntity<StreamingResponseBody> dropboxDownload(@RequestParam() String path) {
    String extension = FilenameUtils.getExtension(path);
    Path pathFile = Paths.get(path);
    String fileName = pathFile.getFileName().toString();
    StreamingResponseBody streamingResponseBody =
        new StreamingResponseBody() {

          @Override
          public void writeTo(OutputStream outputStream) {
            try {

              String fileStr = path;

              dbxClient.files().download(fileStr).download(outputStream);

            } catch (Exception e) {
              log.error(e.getMessage());
            }
          }
        };

    HttpHeaders headers = new HttpHeaders();
    if (extension == "zip") {
      headers.add(HttpHeaders.CONTENT_TYPE, "application/zip");
    } else if (extension == "pdf") {
      headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");
    } else {
      headers.add(HttpHeaders.CONTENT_TYPE, "image/*");
    }

    headers.add(HttpHeaders.CONTENT_DISPOSITION, " attachment; filename=" + fileName);
    return new ResponseEntity<StreamingResponseBody>(streamingResponseBody, headers, HttpStatus.OK);
  }
}
