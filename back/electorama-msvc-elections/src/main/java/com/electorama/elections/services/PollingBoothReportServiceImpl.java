package com.electorama.elections.services;

import com.electorama.elections.facade.ReportPollingBoothResultFacade;
import com.electorama.elections.facade.ReportPollingBoothStatusFacade;
import com.electorama.elections.models.dto.ReportPollingBoothResultsByPollingBoothIdsDto;
import com.electorama.elections.models.dto.ReportPollingBoothStatusOpenByPollingBoothIdsDto;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PollingBoothReportServiceImpl implements PollingBoothReportService {

  @Autowired ReportPollingBoothStatusFacade reportPollingBoothStatusFacade;

  @Autowired ReportPollingBoothResultFacade reportPollingBoothResultFacade;

  @Override
  public List<ReportPollingBoothStatusOpenByPollingBoothIdsDto>
      getPollingBoothStatusOpenByPollingBoothIds(List<Long> pollingBoothIds) {
    return reportPollingBoothStatusFacade.getReportStatusOpen(pollingBoothIds);
  }

  @Override
  public List<ReportPollingBoothResultsByPollingBoothIdsDto>
      getPollingBoothResultsByPollingBoothIds(List<Long> pollingBoothIds) {
    return reportPollingBoothResultFacade.getReportResults(pollingBoothIds);
  }
}
