package com.electorama.msvc.citizenissues.services;

import com.electorama.msvc.citizenissues.model.dto.DatesRequestDto;
import com.electorama.msvc.citizenissues.model.dto.DownloadIssuesDto;
import java.util.List;

public interface DownloadIssuesService {

  List<DownloadIssuesDto> downloadIssuesByTime(DatesRequestDto datesRequestDto);
}
