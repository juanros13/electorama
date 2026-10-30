package com.electorama.msvc.citizenissues.services;

import org.springframework.web.multipart.MultipartFile;

public interface UploadFileService {

  String uploadFile(
      MultipartFile file, Long issueDetailId, Integer issueSolutionDocumentsCategory, String uuid);
}
