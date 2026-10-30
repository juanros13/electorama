package com.electorama.msvc.structure.service;

import com.electorama.msvc.structure.exception.StructureException;
import com.electorama.msvc.structure.model.dto.PagedResponse;
import com.electorama.msvc.structure.model.projection.FollowerRow;
import com.electorama.msvc.structure.model.projection.MembershipView;
import com.electorama.msvc.structure.model.repository.FollowerQueryRepository;
import com.electorama.msvc.structure.model.repository.MembershipRepository;
import com.electorama.msvc.structure.model.repository.PositionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** El convencido: consultarlo, trasladarlo de puesto y darlo de baja sin perder su historia. */
@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipService {

  private final MembershipRepository memberships;
  private final PositionRepository positions;
  private final FollowerQueryRepository followerQuery;
  private final CampaignService campaigns;

  @Transactional(readOnly = true)
  public MembershipView get(Long id) {
    return memberships.view(id).orElseThrow(() -> StructureException.notFound("la membresía", id));
  }

  @Transactional(readOnly = true)
  public List<MembershipView> byPosition(Long positionId, int page, int size) {
    positions
        .summary(positionId)
        .orElseThrow(() -> StructureException.notFound("el puesto", positionId));
    return memberships.currentByPosition(positionId, PageRequest.of(page, Math.min(size, 500)));
  }

  /**
   * Los convencidos de un coordinador de zona, con su cadena de mando.
   *
   * <p>Es lo que `msvc-followers` resolvía con un JOIN a `promoters` dentro de su propia base. El
   * total se cuenta con el mismo filtro que el contenido — el facade viejo contaba ignorando el
   * filtro por promotor, así que la paginación mentía en cuanto se filtraba.
   */
  @Transactional(readOnly = true)
  public PagedResponse<FollowerRow> byCoordinator(
      String coordinatorAuthId, String promoterAuthId, int page, int size, Long campaignId) {
    if (coordinatorAuthId == null || coordinatorAuthId.isBlank()) {
      throw StructureException.badRequest("falta el authId del coordinador de zona");
    }
    int capped = Math.min(size, 500);
    PageRequest paging = PageRequest.of(page, capped);
    boolean filtraPromotor = promoterAuthId != null && !promoterAuthId.isBlank();
    Long campaign = campaigns.resolve(campaignId);

    List<FollowerRow> content =
        filtraPromotor
            ? followerQuery.byCoordinatorAndPromoter(
                coordinatorAuthId, promoterAuthId, campaign, paging)
            : followerQuery.byCoordinator(coordinatorAuthId, campaign, paging);
    long total =
        filtraPromotor
            ? followerQuery.countByCoordinatorAndPromoter(
                coordinatorAuthId, promoterAuthId, campaign)
            : followerQuery.countByCoordinator(coordinatorAuthId, campaign);
    return PagedResponse.of(content, page, capped, total);
  }

  /**
   * Traslada al convencido a otro puesto.
   *
   * <p>El linaje NO se toca: {@code captured_by_assignment_id} es inmutable por trigger, así que el
   * traslado no puede borrar de dónde vino. Eso es lo que hoy se pierde cuando {@code DELETE
   * /followers/{id}} y {@code createByFollower} ponen {@code user_create_id = null}.
   */
  @Transactional
  public MembershipView transfer(Long id, Long positionId) {
    get(id);
    positions
        .findByIdAndValidToIsNull(positionId)
        .orElseThrow(() -> StructureException.notFound("el puesto vigente", positionId));

    if (memberships.moveToPosition(id, positionId) == 0) {
      throw StructureException.conflict("la membresía " + id + " ya no está vigente");
    }
    log.info("membresía {} trasladada al puesto {}", id, positionId);
    return get(id);
  }

  /**
   * Cierra la vigencia: el convencido desaparece de lo vigente y su historia se queda.
   *
   * <p>En el modelo viejo esto era un DELETE físico, y con él se iba el rastro.
   */
  @Transactional
  public void close(Long id) {
    get(id);
    if (memberships.close(id) == 0) {
      throw StructureException.conflict("la membresía " + id + " ya estaba dada de baja");
    }
    log.info("membresía {} dada de baja", id);
  }
}
