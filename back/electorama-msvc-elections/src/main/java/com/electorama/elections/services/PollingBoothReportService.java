package com.electorama.elections.services;

import com.electorama.elections.models.dto.*;
import java.util.List;

public interface PollingBoothReportService {

  List<ReportPollingBoothStatusOpenByPollingBoothIdsDto> getPollingBoothStatusOpenByPollingBoothIds(
      List<Long> pollingBoothIds);

  List<ReportPollingBoothResultsByPollingBoothIdsDto> getPollingBoothResultsByPollingBoothIds(
      List<Long> pollingBoothIds);
}
