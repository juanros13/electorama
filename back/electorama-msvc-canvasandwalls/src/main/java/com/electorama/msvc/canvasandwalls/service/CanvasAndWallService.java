package com.electorama.msvc.canvasandwalls.service;

import com.electorama.msvc.canvasandwalls.model.dto.*;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface CanvasAndWallService {

  Page<CanvasAndWallsDto> listAll(
      Integer pageNo, Integer pageSize, String sortBy, String orderBy, String search);

  Page<CanvasAndWallsDto> listByRole(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String authIdHeader,
      String roleType);

  CanvasAndWallsDto save(CanvasAndWallsDto followers, MultipartFile file, String authId);

  Optional<CanvasAndWallsDto> byId(Long id, String authId);

  CanvasAndWallsDto saveDelete(CanvasAndWallsDto followers, String authId);

  List<CanvasAndWallProyectDto> getProyects();

  List<CanvasAndWallCategoryDto> getCategories();

  List<ReportPDFDto> armaPDFS();
}
