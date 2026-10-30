package com.electorama.msvc.followers.service;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import com.dropbox.core.v2.sharing.SharedLinkErrorException;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;
import com.electorama.msvc.followers.model.utils.FileException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.StringTokenizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class DropBoxServiceImpl implements DropboxService {

  @Autowired private DbxClientV2 dbxClient;

  @Override
  public String uploadFile(
      MultipartFile file, String fileName, String path, List<String> contentTypes) {
    try {
      // String fileName = file.getOriginalFilename();
      String fileContenType = file.getContentType();
      if (contentTypes.contains(fileContenType)) {
        InputStream inputStream = file.getInputStream();
        return uploadFile(inputStream, fileName, path);
      } else {
        throw new MultipartException(
            file.getOriginalFilename() + " No es un archivo permitido, solo permite archivos pdf");
      }

    } catch (IOException e) {
      throw new FileException("Error de IO: " + e.getMessage());
    }
  }

  @Override
  public String uploadFile(InputStream is, String fileName, String path) {
    StringTokenizer st = new StringTokenizer(path, "/");

    try {

      log.info("Iniciando upload");
      FileMetadata metadata =
          dbxClient
              .files()
              .uploadBuilder(path + fileName)
              .withAutorename(true)
              .withMode(WriteMode.OVERWRITE)
              .uploadAndFinish(is);

      // String url = dbxClient.sharing().createSharedLinkWithSettings(metadata.getId()).getUrl();
      // cors configuration
      String newUrl = "http://sembrandovida.gob";

      return metadata.getPathDisplay();
    } catch (IOException e) {
      throw new FileException("Error de IO: " + e.getMessage());
    } catch (DbxException e) {
      throw new FileException("Error de Dbx: " + e.getCause());
    }
  }

  @Override
  public URI getUriFromFile(String fileName) {
    try {
      ListFolderResult result = dbxClient.files().listFolder("");

      for (Metadata metadata : result.getEntries()) {
        if (metadata.getName().equalsIgnoreCase(fileName)) {

          String url = null;
          List<SharedLinkMetadata> list =
              dbxClient
                  .sharing()
                  .listSharedLinksBuilder()
                  .withPath(((FileMetadata) metadata).getId())
                  .start()
                  .getLinks();
          for (SharedLinkMetadata shared : list) {
            url = shared.getUrl();
          }
          if (url == null) {
            url =
                dbxClient
                    .sharing()
                    .createSharedLinkWithSettings(((FileMetadata) metadata).getId())
                    .getUrl();
          }
          // cors configuration
          String newUrl = "https://dl.dropboxusercontent.com" + url.substring(23);
          return new URI(newUrl);
        }
      }
      return null;
    } catch (ListFolderErrorException e) {
      throw new FileException("Error de ListFolderError: " + e.getMessage());
    } catch (DbxException e) {
      throw new FileException("Error de Dbx: " + e.getMessage());
    } catch (URISyntaxException e) {
      throw new FileException("Error de URISyntax: " + e.getMessage());
    }
  }

  @Override
  public void deleteFile(String url) {
    try {
      // changing to standard dropbox url
      url = "https://www.dropbox.com/home/Aplicaciones/nucleopro" + url;

      SharedLinkMetadata metadata = dbxClient.sharing().getSharedLinkMetadata(url);

      if (!metadata.getName().equalsIgnoreCase("blank-avatar.jpg")
          && !metadata.getName().equalsIgnoreCase("cat.jpg")
          && !metadata.getName().equalsIgnoreCase("prod.jpg")) {

        dbxClient.files().deleteV2(metadata.getId());
        log.info("Archivo Borrado");
      } else {
        log.info("Error en borrado");
      }
    } catch (SharedLinkErrorException e) {
      throw new FileException("Error de SharedLinkError: " + e.getMessage());
    } catch (DbxException e) {
      throw new FileException("Error de Dbx: " + e.getMessage());
    }
  }
}
