package com.electorama.msvc.citizenissues.controller;

import com.dropbox.core.*;
import com.electorama.msvc.citizenissues.services.UploadFileService;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/dropbox")
public class UploadFileController {

  @Autowired UploadFileService uploadFile;

  @PostMapping("/upload")
  public ResponseEntity uploadFile(
      @RequestParam("file") MultipartFile file,
      @RequestParam("IssueDetailId") Long issueDetailId,
      @RequestParam("IssueSolutionDocumentsCategory") Integer issueSolutionDocumentsCategory,
      @RequestParam("uuid") String uuid)
      throws IOException, DbxException {
    uploadFile.uploadFile(file, issueDetailId, issueSolutionDocumentsCategory, uuid);
    // return ResponseEntity.ok("Archivo subido exitosamente a Dropbox");
    String mensaje = "Archivo subido exitosamente a Dropbox";
    return ResponseEntity.status(HttpStatus.OK).body("{\"mensaje\": \"" + mensaje + "\"}");
  }
}
