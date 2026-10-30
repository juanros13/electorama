package com.electorama.msvc.survey.service;

import com.dropbox.core.v2.files.*;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface DropboxService {

  String uploadFile(MultipartFile file, String fileName, String path, List<String> contentTypes);

  String uploadFile(InputStream is, String fileName, String path);

  URI getUriFromFile(String fileName);

  void deleteFile(String url);
}
