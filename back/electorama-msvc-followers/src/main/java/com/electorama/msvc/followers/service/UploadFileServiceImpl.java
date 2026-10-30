package com.electorama.msvc.followers.service;

import java.time.LocalDate;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadFileServiceImpl implements UploadFileService {

  @Autowired private DropboxService dropboxService;

  @Override
  public String uploadFile(MultipartFile file) {

    String ext;
    Random random = new Random();
    int randomWithNextInt = random.nextInt() & Integer.MAX_VALUE;
    LocalDate currentDate = LocalDate.now();

    ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));

    String path = "/SOMOSLAMAYORIA/BARDAS_LONAS/";

    String fileName = "EVIDENCIA_" + currentDate + "_" + randomWithNextInt + "." + ext;

    List<String> contentTypes =
        Arrays.asList("image/jpg", "image/jpeg", "image/png", "application/pdf");

    return dropboxService.uploadFile(file, fileName, path, contentTypes);
  }
}
