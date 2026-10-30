package com.electorama.msvc.structure.model.repository;

import com.electorama.msvc.structure.model.entity.Membership;
import com.electorama.msvc.structure.model.projection.DailyActivity;
import com.electorama.msvc.structure.model.projection.MembershipView;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {

  String VIEW_SELECT =
      "SELECT m.id AS id, m.legacy_id AS legacyId, m.position_id AS positionId,"
          + "       m.structure_id AS structureId, m.person_id AS personId,"
          + "       trim(concat_ws(' ', p.names, p.last_name, p.sur_name)) AS personName,"
          + "       p.curp AS curp, p.ine AS ine, p.cell_phone_number AS cellPhoneNumber,"
          + "       t.section_geo_id AS sectionGeoId, m.status AS status, m.source AS source,"
          + "       m.vote_flag AS voteFlag, lower(m.validity) AS validFrom,"
          + "       upper(m.validity) AS validTo, m.captured_by_assignment_id AS capturedByAssignmentId,"
          + "       m.captured_by_auth_id AS capturedByAuthId,"
          // en membership_detail son texto (vienen de la app vieja): sólo si parecen número
          + "       CASE WHEN d.latitude ~ '^-?[0-9]+([.][0-9]+)?$'"
          + "            THEN CAST(d.latitude AS double precision) END AS latitude,"
          + "       CASE WHEN d.longitude ~ '^-?[0-9]+([.][0-9]+)?$'"
          + "            THEN CAST(d.longitude AS double precision) END AS longitude"
          + " FROM membership m"
          + " JOIN person p ON p.id = m.person_id"
          + " LEFT JOIN territory t ON t.id = m.territory_id"
          + " LEFT JOIN membership_detail d ON d.membership_id = m.id";

  @Query(value = VIEW_SELECT + " WHERE m.id = :id", nativeQuery = true)
  Optional<MembershipView> view(@Param("id") Long id);

  @Query(
      value =
          VIEW_SELECT
              + " WHERE m.position_id = :positionId AND upper(m.validity) IS NULL"
              + " ORDER BY p.last_name, p.names, m.id",
      nativeQuery = true)
  List<MembershipView> currentByPosition(@Param("positionId") Long positionId, Pageable pageable);

  /**
   * Los convencidos de todo el subárbol de un puesto.
   *
   * <p>El recorrido es recursivo sobre el organigrama, no sobre el territorio: es la pregunta
   * «cuántos lleva este coordinador contando a sus promotores», que en el modelo viejo exigía un
   * JOIN a {@code promoters} por cada nivel.
   */
  @Query(
      value =
          "WITH RECURSIVE tree AS ("
              + "   SELECT id FROM position WHERE id = :positionId"
              + "   UNION ALL"
              + "   SELECT c.id FROM position c JOIN tree t ON c.parent_position_id = t.id"
              + "    WHERE c.valid_to IS NULL)"
              + " SELECT count(*) FROM membership m"
              + " JOIN tree t ON t.id = m.position_id"
              + " WHERE upper(m.validity) IS NULL",
      nativeQuery = true)
  long countInSubtree(@Param("positionId") Long positionId);

  /**
   * La actividad por día del subárbol: los convencidos vigentes, por el día en que se capturaron
   * (hora de México), con los días sin captura en cero. Cuenta lo mismo que {@link
   * #countInSubtree}, así que la suma de la gráfica cuadra con el avance.
   *
   * <p>Sin rango, va del primer al último día con captura; sin capturas, los últimos 30 días. Las
   * fechas viajan como texto ISO: un {@code null} tipado como fecha confunde al driver.
   */
  @Query(
      value =
          "WITH RECURSIVE tree AS ("
              + "   SELECT id FROM position WHERE id = :positionId"
              + "   UNION ALL"
              + "   SELECT c.id FROM position c JOIN tree t ON c.parent_position_id = t.id"
              + "    WHERE c.valid_to IS NULL),"
              + " days AS ("
              + "   SELECT CAST(lower(m.validity) AT TIME ZONE 'America/Mexico_City' AS date) AS day,"
              + "          count(*) AS n"
              + "     FROM membership m JOIN tree t ON t.id = m.position_id"
              + "    WHERE upper(m.validity) IS NULL"
              + "    GROUP BY 1),"
              + " span AS ("
              + "   SELECT coalesce(CAST(:fromDay AS date), min(day), CURRENT_DATE - 29) AS f,"
              + "          coalesce(CAST(:toDay AS date), max(day), CURRENT_DATE) AS t"
              + "     FROM days)"
              + " SELECT to_char(g, 'YYYY-MM-DD') AS day, coalesce(d.n, 0) AS captured"
              + "   FROM span, generate_series(span.f, span.t, interval '1 day') g"
              + "   LEFT JOIN days d ON d.day = CAST(g AS date)"
              + "  ORDER BY g",
      nativeQuery = true)
  List<DailyActivity> dailyInSubtree(
      @Param("positionId") Long positionId,
      @Param("fromDay") String fromDay,
      @Param("toDay") String toDay);

  /**
   * Mueve al convencido de puesto sin tocar su linaje.
   *
   * <p>El trigger {@code membership_protect_lineage} impide reescribir {@code
   * captured_by_assignment_id}, así que el traslado no puede falsear de dónde vino. Esto es lo que
   * en el modelo viejo obligaba a mover o huerfanar convencidos al mover al promotor.
   */
  @Modifying
  @Query(
      value =
          "UPDATE membership SET position_id = :positionId"
              + " WHERE id = :id AND upper(validity) IS NULL",
      nativeQuery = true)
  int moveToPosition(@Param("id") Long id, @Param("positionId") Long positionId);

  @Modifying
  @Query(
      value =
          "UPDATE membership"
              + "   SET validity = tstzrange(lower(validity),"
              + "         GREATEST(clock_timestamp(), lower(validity) + interval '1 microsecond'))"
              + " WHERE id = :id AND upper(validity) IS NULL",
      nativeQuery = true)
  int close(@Param("id") Long id);
}
