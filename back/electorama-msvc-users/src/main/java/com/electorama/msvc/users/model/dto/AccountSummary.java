package com.electorama.msvc.users.model.dto;

import java.util.List;

/**
 * Lo que el perfil del organigrama muestra de una cuenta.
 *
 * @param created alta de la cuenta en Keycloak (epoch en milisegundos)
 * @param roles sólo los roles de estructura y ADMIN; los internos del realm no interesan aquí
 */
public record AccountSummary(
    String authId,
    String username,
    String email,
    String firstName,
    String lastName,
    boolean enabled,
    Long created,
    List<String> roles) {}
