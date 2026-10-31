package com.electorama.msvc.structure.model.repository;

import com.electorama.msvc.structure.model.dto.BuildRequests.SectionSeat;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Las escrituras para armar la estructura, en SQL directo.
 *
 * <p>Va con JDBC y no con JPA a propósito: los puestos se crean de a cientos al generar vacantes, y
 * las reglas (jerarquía, territorio contenido, campaña cerrada) las pone la base con triggers. Por
 * JDBC su mensaje llega tal cual al cliente; por JPA llegaba envuelto como error genérico.
 *
 * <p>El {@code code} lleva el id del propio puesto ({@code CZ|S-0098|p123}) para ser único sin
 * depender de la cuenta del titular, que en 2027 se nombra después de crear el puesto.
 */
@Repository
@RequiredArgsConstructor
public class BuildRepository {

  private final NamedParameterJdbcTemplate jdbc;

  /** Una estructura con su campaña (estado y territorio) y su tipo (MAIN/ALTERNATE). */
  public record StructureRow(
      Long id, String type, String campaignStatus, Long campaignId, Long campaignTerritoryId) {}

  /** Un territorio vigente. */
  public record TerritoryRow(
      Long id, String type, String code, Integer sectionGeoId, Integer municipalityGeoId) {}

  /** Un puesto vigente con lo necesario para colgarle hijos. */
  public record PositionRow(
      Long id,
      Long structureId,
      String level,
      Long territoryId,
      String territoryType,
      String territoryCode,
      Integer sectionGeoId,
      Integer municipalityGeoId,
      Integer goal,
      Long levelId) {}

  public Optional<StructureRow> structure(Long structureId) {
    return jdbc
        .query(
            "SELECT s.id, s.type, c.status, c.id, c.territory_id"
                + "  FROM structure s JOIN campaign c ON c.id = s.campaign_id"
                + " WHERE s.id = :id",
            new MapSqlParameterSource("id", structureId),
            (rs, i) ->
                new StructureRow(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getLong(4),
                    (Long) rs.getObject(5)))
        .stream()
        .findFirst();
  }

  public Optional<TerritoryRow> sectionTerritory(Integer sectionGeoId) {
    return territory("t.type = 'SECTION' AND t.section_geo_id = :geo", sectionGeoId);
  }

  public Optional<TerritoryRow> municipalityTerritory(Integer municipalityGeoId) {
    return territory("t.type = 'MUNICIPALITY' AND t.municipality_geo_id = :geo", municipalityGeoId);
  }

  /** Un territorio vigente por su id en {@code territory}. */
  public Optional<TerritoryRow> territoryById(Long territoryId) {
    return territory("t.id = :geo", territoryId);
  }

  private Optional<TerritoryRow> territory(String where, Object geoId) {
    return jdbc
        .query(
            "SELECT t.id, t.type, t.code, t.section_geo_id,"
                + "       coalesce(t.municipality_geo_id, p.municipality_geo_id)"
                + "  FROM territory t LEFT JOIN territory p ON p.id = t.parent_territory_id"
                + " WHERE t.valid_to IS NULL AND "
                + where
                + " ORDER BY t.id LIMIT 1",
            new MapSqlParameterSource("geo", geoId),
            (rs, i) ->
                new TerritoryRow(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    (Integer) rs.getObject(4),
                    (Integer) rs.getObject(5)))
        .stream()
        .findFirst();
  }

  public Optional<PositionRow> position(Long positionId) {
    return jdbc
        .query(
            "SELECT p.id, p.structure_id, p.level, t.id, t.type, t.code, t.section_geo_id,"
                + "       coalesce(t.municipality_geo_id, tp.municipality_geo_id), p.goal, p.level_id"
                + "  FROM position p"
                + "  JOIN territory t ON t.id = p.territory_id"
                + "  LEFT JOIN territory tp ON tp.id = t.parent_territory_id"
                + " WHERE p.id = :id AND p.valid_to IS NULL",
            new MapSqlParameterSource("id", positionId),
            (rs, i) ->
                new PositionRow(
                    rs.getLong(1),
                    rs.getLong(2),
                    rs.getString(3),
                    rs.getLong(4),
                    rs.getString(5),
                    rs.getString(6),
                    (Integer) rs.getObject(7),
                    (Integer) rs.getObject(8),
                    (Integer) rs.getObject(9),
                    rs.getLong(10)))
        .stream()
        .findFirst();
  }

  /** ¿Ya hay un puesto vigente de ese nivel sobre ese territorio en esa estructura? */
  public boolean hasPositionOn(Long structureId, Long territoryId, Long levelId) {
    Integer n =
        jdbc.queryForObject(
            "SELECT count(*) FROM position WHERE structure_id = :s AND territory_id = :t"
                + " AND level_id = :l AND valid_to IS NULL",
            new MapSqlParameterSource("s", structureId)
                .addValue("t", territoryId)
                .addValue("l", levelId),
            Integer.class);
    return n != null && n > 0;
  }

  /** Hijos vigentes de un puesto de cierto nivel, opcionalmente sólo en un territorio. */
  public int countChildren(Long parentId, Long levelId, Long territoryId) {
    MapSqlParameterSource params =
        new MapSqlParameterSource("parent", parentId).addValue("level", levelId);
    String where = "";
    if (territoryId != null) {
      where = " AND territory_id = :t";
      params.addValue("t", territoryId);
    }
    Integer n =
        jdbc.queryForObject(
            "SELECT count(*) FROM position WHERE parent_position_id = :parent AND level_id = :level"
                + " AND valid_to IS NULL"
                + where,
            params,
            Integer.class);
    return n == null ? 0 : n;
  }

  /**
   * Inserta el puesto y devuelve su id. El código se arma con el id que da la secuencia en la misma
   * sentencia: {@code <prefijo>|<código del territorio>|p<id>}. La función ({@code level}) la copia
   * el trigger desde el nivel.
   */
  public long insertPosition(
      Long structureId,
      Long levelId,
      Long parentId,
      TerritoryRow territory,
      Integer goal,
      Integer childLimit,
      String prefix) {
    Long id =
        jdbc.queryForObject(
            "WITH n AS (SELECT nextval(pg_get_serial_sequence('position', 'id')) AS id)"
                + " INSERT INTO position (id, structure_id, level_id, parent_position_id, territory_id,"
                + "                       code, goal, child_limit)"
                + " SELECT n.id, :structure, :level, :parent, :territory,"
                + "        :prefix || '|' || :tcode || '|p' || n.id, :goal, :childLimit"
                + "   FROM n"
                + " RETURNING id",
            new MapSqlParameterSource("structure", structureId)
                .addValue("level", levelId)
                .addValue("parent", parentId)
                .addValue("territory", territory.id())
                .addValue("tcode", territory.code())
                .addValue("goal", goal)
                .addValue("childLimit", childLimit)
                .addValue("prefix", prefix),
            Long.class);
    return id == null ? 0 : id;
  }

  /**
   * Los otros puestos de promotor (coordinador de zona) VACANTES de la misma sección, estructura y
   * referente que el puesto: los que se ocupan con «toda la sección».
   */
  public List<SectionSeat> vacantSectionSiblings(Long positionId) {
    return jdbc.query(
        "SELECT s.id, s.section_key, s.goal"
            + "  FROM position p"
            + "  JOIN position s ON s.structure_id = p.structure_id"
            + "                 AND s.territory_id = p.territory_id"
            + "                 AND s.level = p.level"
            + "                 AND s.parent_position_id IS NOT DISTINCT FROM p.parent_position_id"
            + "                 AND s.valid_to IS NULL AND s.id <> p.id"
            + " WHERE p.id = :id AND p.valid_to IS NULL AND p.level = 'ZONE_COORDINATOR'"
            + "   AND NOT EXISTS (SELECT 1 FROM assignment a"
            + "                    WHERE a.position_id = s.id AND a.role = 'HOLDER'"
            + "                      AND upper(a.validity) IS NULL)"
            + " ORDER BY length(s.section_key), s.section_key, s.id",
        new MapSqlParameterSource("id", positionId),
        (rs, i) -> new SectionSeat(rs.getLong(1), rs.getString(2), (Integer) rs.getObject(3)));
  }
}
