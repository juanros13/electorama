package com.electorama.msvc.electoralhistory.dto;

import java.util.List;

/**
 * Un territorio en una elección, para compararla con las otras del mismo cargo.
 *
 * @param groups en la agrupación pedida (por defecto, bloque)
 * @param winner la candidatura que ganó ahí, SIEMPRE por candidatura: el resultado real, aunque
 *     {@code groups} venga por bloque
 * @param marginPp puntos entre la candidatura ganadora y la segunda
 */
public record TrendPoint(
    int year,
    long totalVotes,
    Double turnout,
    List<GroupVotes> groups,
    GroupVotes winner,
    Double marginPp) {}
