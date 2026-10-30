package com.electorama.elections.services;

import org.springframework.web.multipart.MultipartFile;

public interface UploadFileService {

  String uploadFile(MultipartFile file);
}
