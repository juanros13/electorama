package com.electorama.msvc.electoralhistory.dto;

import java.util.List;

/**
 * El resultado de un territorio (el estado, un municipio o una sección).
 *
 * @param code clave del territorio: 0 = estatal, 1..17 = municipio, o el número de sección
 * @param nominalList LN de las casillas ordinarias (B, C, E), la base de la participación
 * @param turnout votos de casillas ordinarias / su LN; null si no hay LN
 * @param groups de mayor a menor votación
 * @param marginPp puntos porcentuales entre el primero y el segundo (sobre votación total)
 */
public record TerritoryResult(
    int code,
    String name,
    long stations,
    long specialStations,
    long nominalList,
    long totalVotes,
    Double turnout,
    long nullVotes,
    long nonRegisteredVotes,
    List<GroupVotes> groups,
    String winner,
    String runnerUp,
    Double marginPp) {}
