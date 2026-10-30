package com.electorama.msvc.visits.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadFileService {

  String uploadFile(MultipartFile file);
}
