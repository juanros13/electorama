package com.electorama.msvc.canvasandwalls.model.dto;

import jakarta.persistence.*;
import java.util.Date;
import lombok.Data;

@Data
public class CanvasAndWallsDto {
  private Long id;

  private Long municipalityId;

  private String uuid;

  private String deviceId;

  private Long seccionId;

  private Long localityId;

  private String municipalityname;

  private String seccionNumber;

  private String localityName;

  private String pathEvidencePhoto;

  private String userCreatedId;

  private String coordinateZoneId;

  private String coordinateZone;

  private String referenceAuthId;

  private String reference;

  private Date dateOfPlacement;

  private CanvasAndWallProyectDto canvasAndWallsProyect;

  private CanvasAndWallCategoryDto canvasAndWallsCategory;

  private Date updated;

  private Date created;

  private Date deleted;

  private String latitude;

  private String longitude;
}
