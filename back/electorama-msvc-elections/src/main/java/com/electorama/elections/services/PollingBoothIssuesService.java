package com.electorama.elections.services;

import com.electorama.elections.models.dto.*;
import java.util.List;
import org.springframework.data.domain.Page;

public interface PollingBoothIssuesService {

  List<PollingBoothIssuesDto> getIssuesByPollingBooth(Long pollingBoothId);

  PollingBoothIssuesDto addIssue(PollingBoothIssuesDto issue, String authId);

  List<PollingBoothIssuesListDto> getIssuesByPollingBootList(List<Long> pollingBooths);

  List<PollingBoothIssuesCategoryDto> getCategory();

  Page<PollingBoothIssuesDescriptionDto> list(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      Long municipalityId,
      Integer sectionNum,
      Long federalDistrictId,
      Long localDistrictId,
      Long subCategoryId,
      Long typeId);

  List<TotalIncidentsDto> listTotalIncidents(
      Long municipalityId,
      Integer sectionNum,
      Long federalDistrictId,
      Long localDistrictId,
      Long subCategoryId,
      Long typeId);

  List<PollingBoothIssuesDescriptionDto> downloadIssues(
      Long municipalityId,
      Integer sectionNum,
      Long federalDistrictId,
      Long localDistrictId,
      Long subCategoryId,
      Long typeId);
}
