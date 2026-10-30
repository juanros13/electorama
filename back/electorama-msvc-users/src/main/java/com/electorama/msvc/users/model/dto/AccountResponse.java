package com.electorama.msvc.users.model.dto;

/** La cuenta creada: su id de Keycloak (el authId del nombramiento) y su usuario. */
public record AccountResponse(String authId, String username) {}
