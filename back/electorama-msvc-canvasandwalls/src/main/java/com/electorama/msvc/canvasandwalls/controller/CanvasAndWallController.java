package com.electorama.msvc.canvasandwalls.controller;

import com.dropbox.core.v2.DbxClientV2;
import com.electorama.msvc.canvasandwalls.model.dto.*;
import com.electorama.msvc.canvasandwalls.model.dto.ReportPDFDto;
import com.electorama.msvc.canvasandwalls.service.CanvasAndWallService;
import com.electorama.msvc.canvasandwalls.service.DropboxService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping(value = "/canvasAndWalls")
public class CanvasAndWallController {

  @Autowired CanvasAndWallService canvasAndWallService;

  @Autowired private DropboxService dropboxService;

  @Autowired private DbxClientV2 dbxClient;

  @GetMapping
  public Map<String, Object> list(
      @RequestParam(defaultValue = "0") Integer pageNo,
      @RequestParam(defaultValue = "30") Integer pageSize,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "desc") String orderBy,
      @RequestParam(defaultValue = "") String search,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    Map<String, Object> response = new HashMap<>();
    String typeRol = "";

    if (servletRequest.isUserInRole("ADMIN")) {
      typeRol = "ADMIN";
    } else if (servletRequest.isUserInRole("CORDINATOR_MUNICIPAL")) {
      typeRol = "CORDINATOR_MUNICIPAL";
    } else if (servletRequest.isUserInRole("CORDINATOR_ZONE")) {
      typeRol = "CORDINATOR_ZONE";
    } else if (servletRequest.isUserInRole("CORDINATOR_ZONE_ALT")) {
      typeRol = "CORDINATOR_ZONE_ALT";
    }

    if (servletRequest.isUserInRole("ADMIN")) {
      log.info("ADMINS");
      response.put(
          "canvasAndWall", canvasAndWallService.listAll(pageNo, pageSize, sortBy, orderBy, search));
    } else {
      log.info("OTROS");
      log.info(authIdHeader);
      response.put(
          "canvasAndWall",
          canvasAndWallService.listByRole(
              pageNo, pageSize, sortBy, orderBy, search, authIdHeader, typeRol));
    }
    // response.put(
    //    "canvasAndWalls", canvasAndWallService.list(pageNo, pageSize, sortBy, orderBy, search));
    return response;
  }

  @PostMapping
  public ResponseEntity create(
      @RequestParam("municipalityId") Long municipalityId,
      @RequestParam("seccionId") Long seccionId,
      @RequestParam("localityId") Long localityId,
      @RequestParam("coordinateZoneId") String coordinateZoneId,
      @RequestParam("referenceAuthId") String referenceAuthId,
      @RequestParam("dateOfPlacement") Date dateOfPlacement,
      @RequestParam("canvasAndWallsProyectId") Long canvasAndWallsProyectId,
      @RequestParam("canvasAndWallsCategoryId") Long canvasAndWallsCategoryId,
      @RequestParam("latitude") String latitude,
      @RequestParam("longitude") String longitude,
      @RequestParam(name = "file", required = true) MultipartFile file,
      @RequestHeader("X-Auth-Id") String authIdHeader) {

    CanvasAndWallsDto request = new CanvasAndWallsDto();
    request.setMunicipalityId(municipalityId);
    request.setSeccionId(seccionId);
    request.setLocalityId(localityId);
    request.setUserCreatedId(authIdHeader);
    request.setCoordinateZoneId(coordinateZoneId);
    request.setReferenceAuthId(referenceAuthId);
    request.setDateOfPlacement(dateOfPlacement);
    request.setLatitude(latitude);
    request.setLongitude(longitude);
    CanvasAndWallProyectDto proyectDto = new CanvasAndWallProyectDto();
    proyectDto.setId(canvasAndWallsProyectId);
    request.setCanvasAndWallsProyect(proyectDto);
    CanvasAndWallCategoryDto categoryDto = new CanvasAndWallCategoryDto();
    categoryDto.setId(canvasAndWallsCategoryId);
    request.setCanvasAndWallsCategory(categoryDto);

    log.info("Creating follower with {}", request.toString());
    CanvasAndWallsDto canvasAndWallsDto = canvasAndWallService.save(request, file, authIdHeader);
    return ResponseEntity.ok(canvasAndWallsDto);
  }

  @PutMapping
  public ResponseEntity createMobile(
      @RequestParam("municipalityId") Long municipalityId,
      @RequestParam("uuid") String uuid,
      @RequestParam("deviceId") String deviceId,
      @RequestParam("seccionId") Long seccionId,
      @RequestParam("localityId") Long localityId,
      @RequestParam("coordinateZoneId") String coordinateZoneId,
      @RequestParam("referenceAuthId") String referenceAuthId,
      @RequestParam("dateOfPlacement") Date dateOfPlacement,
      @RequestParam("canvasAndWallsProyectId") Long canvasAndWallsProyectId,
      @RequestParam("canvasAndWallsCategoryId") Long canvasAndWallsCategoryId,
      @RequestParam("latitude") String latitude,
      @RequestParam("longitude") String longitude,
      @RequestParam(name = "file", required = true) MultipartFile file,
      @RequestHeader("X-Auth-Id") String authIdHeader) {

    CanvasAndWallsDto request = new CanvasAndWallsDto();
    request.setMunicipalityId(municipalityId);
    request.setUuid(uuid);
    request.setDeviceId(deviceId);
    request.setSeccionId(seccionId);
    request.setLocalityId(localityId);
    request.setUserCreatedId(authIdHeader);
    request.setCoordinateZoneId(coordinateZoneId);
    request.setReferenceAuthId(referenceAuthId);
    request.setDateOfPlacement(dateOfPlacement);
    request.setLatitude(latitude);
    request.setLongitude(longitude);
    CanvasAndWallProyectDto proyectDto = new CanvasAndWallProyectDto();
    proyectDto.setId(canvasAndWallsProyectId);
    request.setCanvasAndWallsProyect(proyectDto);
    CanvasAndWallCategoryDto categoryDto = new CanvasAndWallCategoryDto();
    categoryDto.setId(canvasAndWallsCategoryId);
    request.setCanvasAndWallsCategory(categoryDto);

    log.info("Creating follower with {}", request.toString());
    CanvasAndWallsDto canvasAndWallsDto = canvasAndWallService.save(request, file, authIdHeader);
    return ResponseEntity.ok(canvasAndWallsDto);
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> editCanvasAndWalls(
      @RequestParam("municipalityId") Long municipalityId,
      @RequestParam("seccionId") Long seccionId,
      @RequestParam("localityId") Long localityId,
      @RequestParam("userCreatedId") String userCreatedId,
      @RequestParam("coordinateZoneId") String coordinateZoneId,
      @RequestParam("referenceAuthId") String referenceAuthId,
      @RequestParam("dateOfPlacement") Date dateOfPlacement,
      @RequestParam("latitude") String latitude,
      @RequestParam("longitude") String longitude,
      @RequestParam("canvasAndWallsProyectId") Long canvasAndWallsProyectId,
      @RequestParam("canvasAndWallsCategoryId") Long canvasAndWallsCategoryId,
      @RequestParam(name = "file", required = true) MultipartFile file,
      @PathVariable Long id,
      @RequestHeader("X-Auth-Id") String authIdHeader) {

    Optional<CanvasAndWallsDto> o = canvasAndWallService.byId(id, authIdHeader);
    if (o.isPresent()) {
      CanvasAndWallsDto canvasAndWallsDto = o.get();

      canvasAndWallsDto.setUserCreatedId(userCreatedId);
      canvasAndWallsDto.setCoordinateZoneId(coordinateZoneId);
      canvasAndWallsDto.setReferenceAuthId(referenceAuthId);
      canvasAndWallsDto.setMunicipalityId(municipalityId);
      canvasAndWallsDto.setSeccionId(seccionId);
      canvasAndWallsDto.setLocalityId(localityId);
      canvasAndWallsDto.setDateOfPlacement(dateOfPlacement);
      canvasAndWallsDto.setLatitude(latitude);
      canvasAndWallsDto.setLongitude(longitude);
      canvasAndWallsDto.getCanvasAndWallsCategory().setId(canvasAndWallsCategoryId);
      canvasAndWallsDto.getCanvasAndWallsProyect().setId(canvasAndWallsProyectId);

      return ResponseEntity.status(HttpStatus.CREATED)
          .body(canvasAndWallService.save(canvasAndWallsDto, file, authIdHeader));
    }
    return ResponseEntity.notFound().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteCanvasAndWalls(
      @PathVariable Long id, @RequestHeader("X-Auth-Id") String authIdHeader) {
    Optional<CanvasAndWallsDto> o = canvasAndWallService.byId(id, authIdHeader);
    if (o.isPresent()) {
      CanvasAndWallsDto canvasAndWallsDto = o.get();
      long miliseconds = System.currentTimeMillis();
      Date date = new Date(miliseconds);

      canvasAndWallsDto.setDeleted(date);
      return ResponseEntity.status(HttpStatus.OK)
          .body(canvasAndWallService.saveDelete(canvasAndWallsDto, authIdHeader));
    }
    return ResponseEntity.notFound().build();
  }

  private ResponseEntity<Map<String, String>> validar(BindingResult result) {
    Map<String, String> errores = new HashMap<>();
    result
        .getFieldErrors()
        .forEach(
            err -> {
              errores.put(
                  err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
            });
    return ResponseEntity.badRequest().body(errores);
  }

  @GetMapping("/proyects")
  public ResponseEntity<?> getProyects(@RequestHeader("X-Auth-Id") String authIdHeader) {
    // List<CanvasAndWallProyectDto> visitedPeopleDto = canvasAndWallService.getProyects();
    Map<String, Object> response = new HashMap<>();

    try {
      response.put("proyects", canvasAndWallService.getProyects());
      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/categories")
  public ResponseEntity<?> getCategories(@RequestHeader("X-Auth-Id") String authIdHeader) {
    // List<CanvasAndWallProyectDto> visitedPeopleDto = canvasAndWallService.getProyects();
    Map<String, Object> response = new HashMap<>();

    try {
      response.put("categories", canvasAndWallService.getCategories());
      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/descargaPDFS")
  public String descargarPDFS() {

    String rutaArchivoPDF = "/Users/juanros13/Documents/Informe_Lonas_Bardas.pdf";

    List<ReportPDFDto> Dto = canvasAndWallService.armaPDFS();
    Dto.stream()
        .map(
            entityMap -> {
              try {
                log.info(entityMap.getPathEvidencePhoto());
                String extension = FilenameUtils.getExtension(entityMap.getPathEvidencePhoto());
                Path pathFile = Paths.get(entityMap.getPathEvidencePhoto());
                String fileName = pathFile.getFileName().toString();

                // Construir la ruta local donde se guardará el archivo
                // String localFilePath = "/users/juanos13/documents/" + fileName;
                String localFilePath =
                    "/Users/juanros13/Documents/SOMOSLAMAYORIA/BARDAS_LONAS/" + fileName;

                // Descargar el archivo desde Dropbox y guardarlo localmente
                try (OutputStream outputStream = new FileOutputStream(localFilePath)) {
                  dbxClient
                      .files()
                      .download(entityMap.getPathEvidencePhoto())
                      .download(outputStream);
                }

                log.info("Archivo descargado y guardado localmente: " + localFilePath);

              } catch (Exception e) {
                log.error(e.getMessage());
              }
              return entityMap;
            })
        .collect(Collectors.toList());

    return rutaArchivoPDF;
  }

  @GetMapping("/armaPDFS")
  public String armaPDFS() {

    String rutaArchivoPDF = "/Users/juanros13/Documents/Informe_Lonas_Bardas.pdf";

    List<ReportPDFDto> Dto = canvasAndWallService.armaPDFS();

    try {

      // Crear el documento PDF
      Document document = new Document(PageSize.A4);

      PdfWriter.getInstance(document, new FileOutputStream(rutaArchivoPDF));
      document.open();

      Font font = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD); // Crear fuente negrita
      Font font2 = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL); // Crear fuente negrita
      float maxWidth = PageSize.A4.getWidth() - 36;

      Dto.stream()
          .map(
              entityMap -> {
                try {
                  Paragraph paragraph = new Paragraph();
                  Paragraph paragraph2 = new Paragraph();
                  paragraph2.setLeading(1);

                  paragraph2.setAlignment(Element.ALIGN_CENTER);
                  paragraph2.add(new Chunk("INFORME DE LONAS Y BARDAS ", font));
                  paragraph2.add(
                      new Chunk(String.valueOf(entityMap.getProyecto().toUpperCase()), font));
                  document.add(paragraph2);

                  paragraph.add(new Chunk("ID: ", font));
                  paragraph.add(new Chunk(String.valueOf(entityMap.getId()), font2));
                  paragraph.add(new Phrase(" \n\n"));
                  paragraph.add(new Chunk("NOMBRE DEL REFERENTE: ", font)); // Agregar la fuente al
                  paragraph.add(new Chunk(String.valueOf(entityMap.getReferente())));
                  paragraph.add(new Phrase("\n\n"));

                  paragraph.add(
                      new Chunk("NOMBRE DEL RESPONSABLE: ", font)); // Agregar la fuente al
                  paragraph.add(new Chunk(String.valueOf(entityMap.getCoordinadorZona())));
                  paragraph.add(new Phrase("\n\n"));

                  paragraph.add(new Chunk("ENTIDAD: ", font)); // Agregar la fuente al párrafo
                  paragraph.add(new Chunk(String.valueOf(entityMap.getEntidad())));
                  paragraph.add(new Phrase("\n\n"));

                  paragraph.add(new Chunk("MUNICIPIO: ", font)); // Agregar la fuente al párrafo
                  paragraph.add(new Chunk(String.valueOf(entityMap.getMunicipio())));
                  paragraph.add(new Phrase("\n\n"));

                  paragraph.add(new Chunk("LOCALIDAD: ", font)); // Agregar la fuente al párrafo
                  paragraph.add(new Chunk(String.valueOf(entityMap.getLocalidad())));
                  paragraph.add(new Phrase("\n\n"));

                  paragraph.add(
                      new Chunk("FECHA DE REGISTRO: ", font)); // Agregar la fuente al párrafo
                  paragraph.add(new Chunk(String.valueOf(entityMap.getFechaRegistro())));
                  paragraph.add(
                      new Chunk("            SECCIÓN: ", font)); // Agregar la fuente al párrafo
                  paragraph.add(new Chunk(String.valueOf(entityMap.getSeccion())));
                  paragraph.add(new Phrase("\n\n"));

                  paragraph.add(new Chunk("CATEGORÍA: ", font)); // Agregar la fuente al párrafo
                  paragraph.add(new Chunk(String.valueOf(entityMap.getCategoria())));
                  paragraph.add(
                      new Chunk("          PROYECTO: ", font)); // Agregar la fuente al párrafo
                  paragraph.add(new Chunk(String.valueOf(entityMap.getProyecto())));
                  paragraph.add(new Phrase("\n\n"));

                  paragraph.add(new Chunk("LATITUD: ", font)); // Agregar la fuente al párrafo
                  paragraph.add(new Chunk(String.valueOf(entityMap.getLatitud())));
                  paragraph.add(
                      new Chunk("              LONGITUD: ", font)); // Agregar la fuente al
                  paragraph.add(new Chunk(String.valueOf(entityMap.getLongitud())));
                  paragraph.add(new Phrase("\n\n\n\n\n\n"));
                  document.add(paragraph);

                  String rutaImagen =
                      "/Users/juanros13/Documents" + entityMap.getPathEvidencePhoto();
                  log.info(rutaImagen);
                  BufferedImage bufferedImage = null;
                  try {
                    bufferedImage = ImageIO.read(new File(rutaImagen));
                  } catch (Exception e) {
                    e.printStackTrace();
                  }

                  if (bufferedImage != null) {

                    bufferedImage = compressImage(bufferedImage, maxWidth);

                    Image foto = Image.getInstance(bufferedImage, null);

                    int orientation = 0;

                    if (foto.getOriginalType() != foto.ORIGINAL_NONE) {
                      orientation = foto.getOriginalType();
                    }

                    foto.scaleToFit(400, 300);

                    // Aplicar la transformación para mantener la orientación original
                    switch (orientation) {
                      case Image.ORIGINAL_BMP:
                      case Image.ORIGINAL_JPEG:
                      case Image.ORIGINAL_PNG:
                      case Image.ORIGINAL_GIF:
                      case Image.ORIGINAL_TIFF:
                      case Image.ORIGINAL_WMF:
                      case Image.ORIGINAL_PS:
                        // Ajustar la rotación según la orientación de la imagen original
                        // foto.setRotationDegrees(270);
                        foto.setAlignment(Element.ALIGN_CENTER);
                        break;
                      case Image.ORIGINAL_JPEG2000:
                      case Image.ORIGINAL_JBIG2:
                        // Ajustar la rotación y la escala para mantener la orientación
                        foto.setRotationDegrees(0);
                        foto.scaleAbsolute(foto.getWidth(), foto.getHeight());
                        foto.setAlignment(Element.ALIGN_CENTER);
                        break;
                      default:
                        // No se pudo determinar la orientación, dejar la imagen sin cambios
                    }

                    // Agregar la imagen al documento
                    document.add(foto);
                  }
                  document.newPage();

                } catch (DocumentException e) {
                  e.printStackTrace();
                } catch (MalformedURLException e) {
                  e.printStackTrace();
                } catch (IOException e) {
                  e.printStackTrace();
                }
                return entityMap;
              })
          .collect(Collectors.toList());

      document.close();

      return "PDF generado y guardado en: " + rutaArchivoPDF;
    } catch (IOException | DocumentException e) {
      // Manejar las excepciones aquí
      e.printStackTrace(); // Puedes usar un logger para registrar el error en lugar de
      // imprimirlo en la consola
      return "Error al generar y guardar el PDF.";
    }
  }

  private static BufferedImage compressImage(BufferedImage image, float maxWidth) {
    int width = image.getWidth();
    int height = image.getHeight();

    if (width <= maxWidth) {
      return image; // No es necesario comprimir la imagen
    }

    float aspectRatio = (float) height / (float) width;
    int newWidth = (int) maxWidth;
    int newHeight = (int) (maxWidth * aspectRatio);

    BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
    resizedImage
        .createGraphics()
        .drawImage(
            image.getScaledInstance(newWidth, newHeight, BufferedImage.SCALE_SMOOTH), 0, 0, null);

    return resizedImage;
  }
}
