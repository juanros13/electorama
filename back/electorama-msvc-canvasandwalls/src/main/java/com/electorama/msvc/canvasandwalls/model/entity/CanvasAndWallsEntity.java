package com.electorama.msvc.canvasandwalls.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(name = "canvas_and_walls")
public class CanvasAndWallsEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull private Long municipalityId;

  private String uuid;

  private String deviceId;

  private String municipalityname;

  @NotNull private Long seccionId;

  private String seccionNumber;

  private Long localityId;

  private String localityName;

  private String pathEvidencePhoto;

  @NotBlank private String userCreatedId;

  private String coordinateZoneId;

  private String referenceAuthId;

  private String latitude;

  private String longitude;

  @NotNull private Date dateOfPlacement;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "canvas_and_wall_proyect_id", referencedColumnName = "id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private CanvasAndWallsProyectEntity canvasAndWallsProyect;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "canvas_and_wall_category_id", referencedColumnName = "id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private CanvasAndWallsCategoryEntity canvasAndWallsCategory;

  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "created")
  private Date created;

  @UpdateTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "updated")
  private Date updated;

  private Date deleted;
}
