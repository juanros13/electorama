package com.electorama.msvc.citizenissues.services;

import com.electorama.msvc.citizenissues.model.entity.IssueDetailEntity;
import com.electorama.msvc.citizenissues.model.repository.IssueDetailRepository;
import com.electorama.msvc.citizenissues.model.repository.IssueSolutionDocumentsCategoryRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadFileServiceImpl implements UploadFileService {

  @Autowired private DropboxService dropboxService;

  @Autowired private IssueDetailRepository issueDetailRepository;

  @Autowired
  private IssueSolutionDocumentsCategoryRepository issuesSolutionDocumentsCategoryRepository;

  @Value("${app.dropbox.path}")
  private String pathDropbox;

  @Override
  public String uploadFile(
      MultipartFile file, Long issueDetailId, Integer issueSolutionDocumentsCategory, String uuid) {

    String ext;
    Random random = new Random();
    IssueDetailEntity issueDetail = issueDetailRepository.findById(issueDetailId).get();

    int randomWithNextInt = random.nextInt() & Integer.MAX_VALUE;
    LocalDate currentDate = LocalDate.now();
    ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
    String fileName = "ISSUE_" + currentDate + "_" + randomWithNextInt + ext;
    issueDetail.setIssueSolutionDocumentsCategory(
        issuesSolutionDocumentsCategoryRepository
            .findById(Long.valueOf(issueSolutionDocumentsCategory))
            .get());
    issueDetail.setPathFile(pathDropbox + fileName);
    issueDetail.setUuid(uuid);
    List<String> contentTypes =
        Arrays.asList("image/jpg", "image/jpeg", "image/png", "application/pdf");
    dropboxService.uploadFile(file, fileName, pathDropbox, contentTypes);
    issueDetailRepository.save(issueDetail);

    return pathDropbox + fileName;
  }
}
