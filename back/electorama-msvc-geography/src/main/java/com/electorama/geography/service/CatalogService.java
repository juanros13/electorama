package com.electorama.geography.service;

import com.electorama.geography.model.dto.catalog.CatalogDistrict;
import com.electorama.geography.model.dto.catalog.CatalogMunicipality;
import com.electorama.geography.model.dto.catalog.CatalogPage;
import com.electorama.geography.model.dto.catalog.CatalogSection;
import com.electorama.geography.model.dto.catalog.CatalogSummary;
import com.electorama.geography.model.repository.CatalogQueryRepository;
import com.electorama.geography.model.repository.CatalogQueryRepository.SectionChange;
import com.electorama.geography.model.repository.CatalogQueryRepository.SectionFilter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * El catálogo territorial para administrarlo: listar con agregados y editar lo poco editable.
 *
 * <p>La meta de la sección es el techo de la sección. La meta de cada coordinador vive en su puesto
 * de msvc-structure; la pantalla compara las dos, pero ninguna se calcula de la otra.
 */
@Service
@RequiredArgsConstructor
public class CatalogService {

  private static final int MAX_PAGE_SIZE = 500;

  private final CatalogQueryRepository catalog;

  @Transactional(readOnly = true)
  public CatalogPage<CatalogSection> sections(SectionFilter filter, int page, int size) {
    int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
    int safePage = Math.max(page, 0);
    return new CatalogPage<>(
        catalog.sections(filter, safePage, safeSize),
        safePage,
        safeSize,
        catalog.countSections(filter));
  }

  @Transactional(readOnly = true)
  public CatalogSummary summarizeSections(SectionFilter filter) {
    return catalog.summarizeSections(filter);
  }

  @Transactional(readOnly = true)
  public CatalogSection section(Long id) {
    return catalog.section(id).orElseThrow(() -> notFound("la sección", id));
  }

  /** El polígono de la sección en GeoJSON; 404 si no tiene geometría. */
  @Transactional(readOnly = true)
  public String sectionGeoJson(Long id) {
    return catalog.sectionGeoJson(id).orElseThrow(() -> notFound("la geometría de la sección", id));
  }

  /** null si la sección no tiene geometría (no se puede decir que el punto esté fuera). */
  @Transactional(readOnly = true)
  public Boolean sectionCovers(Long id, double lat, double lng, double toleranceMeters) {
    return catalog.sectionCovers(id, lat, lng, toleranceMeters).orElse(null);
  }

  @Transactional(readOnly = true)
  public List<CatalogMunicipality> municipalities(Integer stateId) {
    return catalog.municipalities(stateId);
  }

  @Transactional(readOnly = true)
  public List<CatalogDistrict> federalDistricts(Integer stateId) {
    return catalog.federalDistricts(stateId);
  }

  @Transactional(readOnly = true)
  public List<CatalogDistrict> localDistricts(Integer stateId) {
    return catalog.localDistricts(stateId);
  }

  /**
   * Con LN, la meta sale del porcentaje: una meta directa se rechaza en vez de ignorarla en
   * silencio, para que quien la mande sepa que tiene que cambiar el porcentaje.
   */
  @Transactional
  public CatalogSection updateSection(SectionChange change) {
    CatalogSection current = section(change.id());
    boolean hasNominalList = change.nominalList() != null || current.nominalList() != null;
    if (change.meta() != null && hasNominalList) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "La meta de la sección "
              + current.section()
              + " se calcula con el % de su LN: cambia el porcentaje");
    }
    if (catalog.updateSection(change) == 0) {
      throw notFound("la sección", change.id());
    }
    return section(change.id());
  }

  /** El catálogo de municipios está en caché (ver MunicipalityServiceImpl): se vacía entero. */
  @Transactional
  @Caching(
      evict = {
        @CacheEvict(value = "municipalityById", allEntries = true),
        @CacheEvict(value = "municipalitiesByEntity", allEntries = true),
        @CacheEvict(value = "allMunicipalities", allEntries = true)
      })
  public void renameMunicipality(Long id, String name) {
    if (catalog.renameMunicipality(id, name.trim().toUpperCase()) == 0) {
      throw notFound("el municipio", id);
    }
  }

  private static ResponseStatusException notFound(String what, Long id) {
    return new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe " + what + " " + id);
  }
}
