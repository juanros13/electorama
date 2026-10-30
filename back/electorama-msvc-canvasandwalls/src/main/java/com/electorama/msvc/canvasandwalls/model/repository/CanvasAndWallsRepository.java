package com.electorama.msvc.canvasandwalls.model.repository;

import com.electorama.msvc.canvasandwalls.model.entity.CanvasAndWallsEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CanvasAndWallsRepository extends JpaRepository<CanvasAndWallsEntity, Long> {

  Page<CanvasAndWallsEntity> findByMunicipalitynameOrSeccionNumberOrLocalityNameOrId(
      String municipality, String seccion, String locality, String id, Pageable pageable);

  Page<CanvasAndWallsEntity> findByReferenceAuthIdOrCoordinateZoneId(
      String referenceAuthId, String coorZonaAuthId, Pageable pageable);

  Page<CanvasAndWallsEntity>
      findByReferenceAuthIdAndMunicipalitynameOrSeccionNumberOrLocalityNameOrIdOrCoordinateZoneIdAndMunicipalitynameOrSeccionNumberOrLocalityNameOrId(
          String referenceAuthId,
          String municipality,
          String seccion,
          String locality,
          String id,
          String coorZonaAuthId,
          String municipality1,
          String seccion1,
          String locality1,
          String id1,
          Pageable pageable);

  @Query(
      "SELECT u FROM CanvasAndWallsEntity u WHERE u.referenceAuthId = ?1 and ( u.municipalityId = ?2 OR u.seccionNumber = ?3 OR u.localityName = ?4 OR u.id = ?5   )")
  Page<CanvasAndWallsEntity> findByReferenceAuthIdAndMunicipalitynameOrSeccionNumberOrLocalityName(
      String referenceAuthId,
      String municipality,
      String seccion,
      String locality,
      String id,
      Pageable pageable);

  @Query(
      "SELECT u FROM CanvasAndWallsEntity u WHERE u.coordinateZoneId = ?1 and ( u.municipalityId = ?2 OR u.seccionNumber = ?3 OR u.localityName = ?4 OR u.id = ?5   )")
  Page<CanvasAndWallsEntity>
      findByCoordinateZoneIdAndMunicipalitynameOrSeccionNumberOrLocalityNameOrId(
          String coorZonaAuthId,
          String municipality1,
          String seccion1,
          String locality1,
          String id1,
          Pageable pageable);

  Optional<CanvasAndWallsEntity> findFirstByUuid(String uuid);
}
