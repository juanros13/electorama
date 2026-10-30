package com.electorama.msvc.canvasandwalls.service;

import com.electorama.msvc.canvasandwalls.model.dto.ReportCanvasAndWallDto;
import com.electorama.msvc.canvasandwalls.model.dto.ReportCanvasAndWallMunicipalityDto;
import com.electorama.msvc.canvasandwalls.model.entity.ReportCanvasAndWallMunicipalityEntity;
import com.electorama.msvc.canvasandwalls.model.mapper.ReportCanvasAndWallsMunicipalityMapper;
import com.electorama.msvc.canvasandwalls.model.repository.ReportCanvasAndWallMunicipalityRepository;
import com.electorama.msvc.canvasandwalls.model.rest.UserResponse;
import com.electorama.msvc.canvasandwalls.service.rest.GeographyRestClient;
import com.electorama.msvc.canvasandwalls.service.rest.UsersRestClient;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportCanvasAndWallMunicipalityServiceImpl
    implements ReportCanvasAndWallMunicipalityService {

  @Autowired ReportCanvasAndWallMunicipalityRepository report;

  @Autowired GeographyRestClient geographyRestClient;

  @Autowired UsersRestClient usuariosRestClient;

  private final ReportCanvasAndWallsMunicipalityMapper reportCanvasAndWallsMunicipalityMapper =
      new ReportCanvasAndWallsMunicipalityMapper();

  @Override
  public List<ReportCanvasAndWallMunicipalityDto> findAllByMunicipality(
      ReportCanvasAndWallDto reportVO) {
    List<ReportCanvasAndWallMunicipalityDto> reportFinalResponse = null;
    List<ReportCanvasAndWallMunicipalityEntity> reporte =
        report.findAllByMunicipality(reportVO.getStartDate(), reportVO.getEndDate());
    List<UserResponse> listaResponse = usuariosRestClient.readMetaMunicipalities();

    return reporte.stream()
        .map(
            row -> {
              ReportCanvasAndWallMunicipalityDto otherRow =
                  reportCanvasAndWallsMunicipalityMapper.convertToDto(row);
              otherRow.setMunicipalityId(
                  geographyRestClient.readMunicipalityById(row.getMunicipalityId()).getNomMun());
              UserResponse next =
                  listaResponse.stream()
                      .filter(r -> row.getMunicipalityId().equals(r.getMunicipalityId()))
                      .collect(Collectors.toList())
                      .get(0);
              otherRow.setCoordinators(next.getCoordinadores());
              return otherRow;
            })
        .collect(Collectors.toList());
  }

  @Override
  public List<ReportCanvasAndWallMunicipalityDto> findByCategoryMunicipality(
      ReportCanvasAndWallDto reportVO) {
    List<ReportCanvasAndWallMunicipalityEntity> reporte =
        report.findAllByMunicipality(reportVO.getStartDate(), reportVO.getEndDate());
    List<UserResponse> listaResponse = usuariosRestClient.readMetaMunicipalities();
    return reporte.stream()
        .map(
            row -> {
              ReportCanvasAndWallMunicipalityDto otherRow =
                  reportCanvasAndWallsMunicipalityMapper.convertToDto(row);
              otherRow.setMunicipalityId(
                  geographyRestClient.readMunicipalityById(row.getMunicipalityId()).getNomMun());
              if (row.getCanvasAndWallCategoryId().equals(reportVO.getCategory())) {
                UserResponse next =
                    listaResponse.stream()
                        .filter(r -> row.getMunicipalityId().equals(r.getMunicipalityId()))
                        .collect(Collectors.toList())
                        .get(0);
                otherRow.setCoordinators(next.getCoordinadores());
                return otherRow;
              }
              return null;
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public List<ReportCanvasAndWallMunicipalityDto> findByProjectMunicipality(
      ReportCanvasAndWallDto reportVO) {
    List<ReportCanvasAndWallMunicipalityEntity> reporte =
        report.findAllByMunicipality(reportVO.getStartDate(), reportVO.getEndDate());
    List<UserResponse> listaResponse = usuariosRestClient.readMetaMunicipalities();
    return reporte.stream()
        .map(
            row -> {
              ReportCanvasAndWallMunicipalityDto otherRow =
                  reportCanvasAndWallsMunicipalityMapper.convertToDto(row);
              otherRow.setMunicipalityId(
                  geographyRestClient.readMunicipalityById(row.getMunicipalityId()).getNomMun());
              if (row.getCanvasAndWallProyectId().equals(reportVO.getProyect())) {
                UserResponse next =
                    listaResponse.stream()
                        .filter(r -> row.getMunicipalityId().equals(r.getMunicipalityId()))
                        .collect(Collectors.toList())
                        .get(0);
                otherRow.setCoordinators(next.getCoordinadores());
                return otherRow;
              }
              return null;
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public List<ReportCanvasAndWallMunicipalityDto> findByProjectAndCategoryMunicipality(
      ReportCanvasAndWallDto reportVO) {
    List<ReportCanvasAndWallMunicipalityEntity> reporte =
        report.findAllByMunicipality(reportVO.getStartDate(), reportVO.getEndDate());
    List<UserResponse> listaResponse = usuariosRestClient.readMetaMunicipalities();
    return reporte.stream()
        .map(
            row -> {
              ReportCanvasAndWallMunicipalityDto otherRow =
                  reportCanvasAndWallsMunicipalityMapper.convertToDto(row);
              otherRow.setMunicipalityId(
                  geographyRestClient.readMunicipalityById(row.getMunicipalityId()).getNomMun());
              if (row.getCanvasAndWallProyectId().equals(reportVO.getProyect())
                  && row.getCanvasAndWallCategoryId().equals(reportVO.getCategory())) {
                UserResponse next =
                    listaResponse.stream()
                        .filter(r -> row.getMunicipalityId().equals(r.getMunicipalityId()))
                        .collect(Collectors.toList())
                        .get(0);
                otherRow.setCoordinators(next.getCoordinadores());
                return otherRow;
              }
              return null;
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }
}
