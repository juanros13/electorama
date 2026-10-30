package com.electorama.geography.model.repository;

import com.electorama.geography.model.dto.catalog.CatalogDistrict;
import com.electorama.geography.model.dto.catalog.CatalogMunicipality;
import com.electorama.geography.model.dto.catalog.CatalogSection;
import com.electorama.geography.model.dto.catalog.CatalogSummary;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Las consultas de la pantalla de catálogos territoriales.
 *
 * <p>Van en SQL nativo y no en JPA por dos razones: los filtros son opcionales y se combinan, y
 * {@code SeccionEntity} declara {@code @NotNull} columnas que en la tabla sí son nulas (el DL de 3
 * secciones), así que un {@code save} de la entidad falla donde un {@code UPDATE} de una columna
 * no.
 *
 * <p>El distrito de una sección se lee de {@code distrito_f} / {@code distrito_l} —el número que
 * publica el INE— y el id de {@code distrito_federal_id} / {@code distrito_local_id}; G3 dejó los
 * dos federales en concordancia.
 */
@Repository
@RequiredArgsConstructor
public class CatalogQueryRepository {

  private final NamedParameterJdbcTemplate jdbc;

  private static final String SECTION_SELECT =
      "SELECT s.id, s.seccion, s.entidad_id, s.municipio_id, m.nom_mun,"
          + "       nullif(trim(s.distrito_federal_id), '')::bigint AS df_id,"
          + "       s.distrito_f::int AS df, s.distrito_local_id AS dl_id, s.distrito_l::int AS dl,"
          + "       s.tipo::int AS tipo, s.meta::int AS meta, s.status,"
          + "       s.nominal_list, s.goal_percentage,"
          + "       (s.geom IS NOT NULL) AS has_geom,"
          + "       (SELECT count(*) FROM geo_localidades_juntos l"
          + "         WHERE l.seccion_id = s.id AND l.deleted IS NULL) AS localities"
          + "  FROM geo_secciones s"
          + "  LEFT JOIN geo_municipios m ON m.id = s.municipio_id";

  private static final String SECTION_WHERE =
      " WHERE s.entidad_id = :stateId"
          + "   AND (CAST(:municipalityId AS bigint) IS NULL OR s.municipio_id = :municipalityId)"
          + "   AND (CAST(:federalDistrict AS int) IS NULL OR s.distrito_f::int = :federalDistrict)"
          + "   AND (CAST(:localDistrict AS int) IS NULL OR s.distrito_l::int = :localDistrict)"
          + "   AND (CAST(:status AS boolean) IS NULL OR s.status = :status)"
          + "   AND (CAST(:q AS text) IS NULL"
          + "        OR ltrim(s.seccion, '0') LIKE ltrim(CAST(:q AS text), '0') || '%')";

  public List<CatalogSection> sections(SectionFilter filter, int page, int size) {
    MapSqlParameterSource params =
        filter.params().addValue("limit", size).addValue("offset", (long) page * size);
    return jdbc.query(
        SECTION_SELECT
            + SECTION_WHERE
            + " ORDER BY s.seccion::int, s.id LIMIT :limit OFFSET :offset",
        params,
        (rs, i) -> section(rs));
  }

  public long countSections(SectionFilter filter) {
    Long total =
        jdbc.queryForObject(
            "SELECT count(*) FROM geo_secciones s" + SECTION_WHERE, filter.params(), Long.class);
    return total == null ? 0 : total;
  }

  /**
   * Los totales de las tarjetas, con los mismos filtros que la lista.
   *
   * <p>Los distritos se cuentan como número: hay secciones con {@code distrito_f} = '2' junto a las
   * de '02', y como texto salían dos distritos.
   */
  public CatalogSummary summarizeSections(SectionFilter filter) {
    return jdbc.queryForObject(
        "SELECT count(*) AS sections,"
            + "       count(DISTINCT s.municipio_id) AS municipalities,"
            + "       count(DISTINCT s.distrito_f::int) AS federal_districts,"
            + "       count(DISTINCT s.distrito_l::int) AS local_districts,"
            + "       coalesce(sum(s.meta), 0) AS meta,"
            + "       coalesce(sum(s.nominal_list), 0) AS nominal_list,"
            + "       count(*) FILTER (WHERE s.nominal_list IS NULL) AS without_nominal_list"
            + "  FROM geo_secciones s"
            + SECTION_WHERE,
        filter.params(),
        (rs, i) ->
            new CatalogSummary(
                rs.getLong("sections"),
                rs.getLong("municipalities"),
                rs.getLong("federal_districts"),
                rs.getLong("local_districts"),
                rs.getLong("meta"),
                rs.getLong("nominal_list"),
                rs.getLong("without_nominal_list")));
  }

  public Optional<CatalogSection> section(Long id) {
    return jdbc
        .query(
            SECTION_SELECT + " WHERE s.id = :id",
            new MapSqlParameterSource("id", id),
            (rs, i) -> section(rs))
        .stream()
        .findFirst();
  }

  // Las tablas de geography no tienen PK, así que Postgres no deja agrupar por id y leer las demás
  // columnas: las secciones se agregan aparte y se unen al catálogo.

  public List<CatalogMunicipality> municipalities(Integer stateId) {
    return jdbc.query(
        "SELECT m.id, m.cve_mun, m.nom_mun, m.entidad_id,"
            + "       coalesce(a.sections, 0) AS sections, coalesce(a.meta, 0) AS meta,"
            + "       coalesce(a.nominal_list, 0) AS nominal_list, m.morena_vote_2024,"
            + "       (m.geom IS NOT NULL) AS has_geom"
            + "  FROM geo_municipios m"
            + "  LEFT JOIN (SELECT s.municipio_id, count(*) AS sections, sum(s.meta) AS meta,"
            + "                    sum(s.nominal_list) AS nominal_list"
            + "               FROM geo_secciones s"
            + "              WHERE s.status AND s.entidad_id = :stateId"
            + "              GROUP BY s.municipio_id) a ON a.municipio_id = m.id"
            + " WHERE m.entidad_id = :stateId"
            + " ORDER BY m.cve_mun",
        new MapSqlParameterSource("stateId", stateId),
        (rs, i) ->
            new CatalogMunicipality(
                rs.getLong("id"),
                rs.getString("cve_mun"),
                rs.getString("nom_mun"),
                rs.getInt("entidad_id"),
                rs.getLong("sections"),
                rs.getLong("meta"),
                rs.getBoolean("has_geom"),
                rs.getLong("nominal_list"),
                rs.getObject("morena_vote_2024", Integer.class)));
  }

  public List<CatalogDistrict> federalDistricts(Integer stateId) {
    return jdbc.query(
        "SELECT d.id, d.distrito_federal AS number, d.entidad_id, d.status,"
            + "       coalesce(a.sections, 0) AS sections, coalesce(a.meta, 0) AS meta,"
            + "       a.municipalities, (d.geom IS NOT NULL) AS has_geom"
            + "  FROM geo_distritos_federales d"
            + "  LEFT JOIN ("
            + sectionAggregate("s.distrito_federal_id")
            + ") a ON a.district_id = d.id::text"
            + " WHERE d.entidad_id = :stateId"
            + " ORDER BY d.status DESC, d.distrito_federal",
        new MapSqlParameterSource("stateId", stateId),
        (rs, i) -> district(rs));
  }

  public List<CatalogDistrict> localDistricts(Integer stateId) {
    return jdbc.query(
        "SELECT d.id, d.distrito_local AS number, d.entidad_id, true AS status,"
            + "       coalesce(a.sections, 0) AS sections, coalesce(a.meta, 0) AS meta,"
            + "       a.municipalities, (d.geom IS NOT NULL) AS has_geom"
            + "  FROM geo_distritos_locales d"
            + "  LEFT JOIN ("
            + sectionAggregate("s.distrito_local_id")
            + ") a ON a.district_id = d.id"
            + " WHERE d.entidad_id = :stateId"
            + " ORDER BY d.distrito_local",
        new MapSqlParameterSource("stateId", stateId),
        (rs, i) -> district(rs));
  }

  /** Secciones vigentes del estado agrupadas por la columna de distrito que se le pase. */
  private static String sectionAggregate(String districtColumn) {
    return "SELECT "
        + districtColumn
        + " AS district_id, count(*) AS sections, sum(s.meta) AS meta,"
        + "        string_agg(DISTINCT m.nom_mun, ', ' ORDER BY m.nom_mun) AS municipalities"
        + "   FROM geo_secciones s"
        + "   LEFT JOIN geo_municipios m ON m.id = s.municipio_id"
        + "  WHERE s.status AND s.entidad_id = :stateId"
        + "  GROUP BY "
        + districtColumn;
  }

  /**
   * Cambia sólo lo que viene; devuelve las filas tocadas (0 si la sección no existe).
   *
   * <p>Con LN, la meta no se escribe: se recalcula como {@code round(LN × % / 100)} con los valores
   * que queden (G5). Sin LN, la meta que venga se guarda tal cual.
   */
  public int updateSection(SectionChange change) {
    return jdbc.update(
        "UPDATE geo_secciones"
            + "   SET nominal_list    = coalesce(CAST(:nominalList AS int), nominal_list),"
            + "       goal_percentage = coalesce(CAST(:goalPercentage AS numeric), goal_percentage),"
            + "       status          = coalesce(CAST(:status AS boolean), status),"
            + "       meta = CASE"
            + "         WHEN coalesce(CAST(:nominalList AS int), nominal_list) IS NOT NULL"
            + "         THEN round(coalesce(CAST(:nominalList AS int), nominal_list)"
            + "                    * coalesce(CAST(:goalPercentage AS numeric), goal_percentage) / 100)"
            + "         ELSE coalesce(CAST(:meta AS bigint), meta)"
            + "       END"
            + " WHERE id = :id",
        new MapSqlParameterSource("id", change.id())
            .addValue("meta", change.meta())
            .addValue("nominalList", change.nominalList())
            .addValue("goalPercentage", change.goalPercentage())
            .addValue("status", change.status()));
  }

  /** Lo que se cambia de una sección; un campo en null no se toca. */
  public record SectionChange(
      Long id, Integer meta, Integer nominalList, BigDecimal goalPercentage, Boolean status) {}

  /** Renombra el municipio y el nombre que las secciones llevan copiado. */
  public int renameMunicipality(Long id, String name) {
    MapSqlParameterSource params = new MapSqlParameterSource("id", id).addValue("name", name);
    int updated = jdbc.update("UPDATE geo_municipios SET nom_mun = :name WHERE id = :id", params);
    if (updated > 0) {
      jdbc.update("UPDATE geo_secciones SET nom_mun = :name WHERE municipio_id = :id", params);
    }
    return updated;
  }

  private static CatalogSection section(ResultSet rs) throws SQLException {
    return new CatalogSection(
        rs.getLong("id"),
        rs.getString("seccion"),
        rs.getInt("entidad_id"),
        nullableLong(rs, "municipio_id"),
        rs.getString("nom_mun"),
        nullableLong(rs, "df_id"),
        rs.getObject("df", Integer.class),
        nullableLong(rs, "dl_id"),
        rs.getObject("dl", Integer.class),
        rs.getObject("tipo", Integer.class),
        rs.getObject("meta", Integer.class),
        rs.getObject("status", Boolean.class),
        rs.getBoolean("has_geom"),
        rs.getLong("localities"),
        rs.getObject("nominal_list", Integer.class),
        rs.getBigDecimal("goal_percentage"));
  }

  /**
   * {@code getObject(col, Long.class)} del driver de Postgres no convierte desde {@code int4}, y
   * {@code municipio_id} / {@code distrito_local_id} son {@code integer}. {@code getLong} sí
   * convierte, pero devuelve 0 en vez de null.
   */
  private static Long nullableLong(ResultSet rs, String column) throws SQLException {
    long value = rs.getLong(column);
    return rs.wasNull() ? null : value;
  }

  private static CatalogDistrict district(ResultSet rs) throws SQLException {
    return new CatalogDistrict(
        rs.getLong("id"),
        rs.getInt("number"),
        rs.getInt("entidad_id"),
        rs.getBoolean("status"),
        rs.getLong("sections"),
        rs.getLong("meta"),
        rs.getString("municipalities"),
        rs.getBoolean("has_geom"));
  }

  /** Los filtros del listado de secciones; los null no filtran. */
  public record SectionFilter(
      Integer stateId,
      Long municipalityId,
      Integer federalDistrict,
      Integer localDistrict,
      Boolean status,
      String q) {

    MapSqlParameterSource params() {
      return new MapSqlParameterSource("stateId", stateId)
          .addValue("municipalityId", municipalityId)
          .addValue("federalDistrict", federalDistrict)
          .addValue("localDistrict", localDistrict)
          .addValue("status", status)
          .addValue("q", q == null || q.isBlank() ? null : q.trim());
    }
  }

  // ─────────────────────────── geometría de una sección

  /** El polígono de la sección en GeoJSON (WGS84); vacío si no tiene geometría. */
  public Optional<String> sectionGeoJson(Long id) {
    return jdbc
        .queryForList(
            "SELECT ST_AsGeoJSON(geom, 6) FROM geo_secciones WHERE id = :id AND geom IS NOT NULL",
            Map.of("id", id),
            String.class)
        .stream()
        .findFirst();
  }

  /**
   * Si el punto cae dentro de la sección o a no más de {@code toleranceMeters} de su borde (el GPS
   * de un teléfono se equivoca decenas de metros). Vacío si la sección no tiene geometría: entonces
   * no se puede decir que esté fuera.
   */
  public Optional<Boolean> sectionCovers(Long id, double lat, double lng, double toleranceMeters) {
    return jdbc
        .queryForList(
            "SELECT ST_DWithin(CAST(geom AS geography),"
                + "                CAST(ST_SetSRID(ST_Point(:lng, :lat), 4326) AS geography), :tol)"
                + "  FROM geo_secciones WHERE id = :id AND geom IS NOT NULL",
            Map.of("id", id, "lat", lat, "lng", lng, "tol", toleranceMeters),
            Boolean.class)
        .stream()
        .findFirst();
  }
}
