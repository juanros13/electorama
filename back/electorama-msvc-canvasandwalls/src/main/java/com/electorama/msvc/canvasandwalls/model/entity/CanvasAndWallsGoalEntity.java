package com.electorama.msvc.canvasandwalls.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "canvas_and_walls_goals")
public class CanvasAndWallsGoalEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull private Long municipalityId;

  @NotNull private String referenceAuthId;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "canvas_and_wall_proyect_id", referencedColumnName = "id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private CanvasAndWallsProyectEntity canvasAndWallsProyect;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "canvas_and_wall_category_id", referencedColumnName = "id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private CanvasAndWallsCategoryEntity canvasAndWallsCategory;

  @NotNull private Integer goal;
}
