package com.electorama.msvc.structure.model.dto;

/**
 * Rotar al titular de un puesto.
 *
 * <p>La operación que el modelo viejo no tenía: no existe ningún endpoint que cambie {@code
 * promoters.cordinator_zone_auth_id} después del alta, así que rotar a un coordinador exigía tocar
 * la base a mano.
 *
 * @param personId la persona que entra
 * @param authId la cuenta de Keycloak con la que ocupará el puesto; vive en la asignación y no en
 *     la persona porque un mismo humano puede tener varias cuentas
 * @param reason por qué rota, que queda escrito en las dos asignaciones
 */
public record RotateRequest(Long personId, String authId, String reason) {}
