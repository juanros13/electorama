package com.electorama.msvc.electoralhistory.dto;

/**
 * Los votos de una candidatura, partido o bloque en un territorio.
 *
 * @param pct sobre la votación total emitida (incluye nulos y no registrados)
 * @param validPct sobre la votación válida (sin nulos ni no registrados)
 * @param candidate la persona que encabezó la candidatura en ese territorio; sólo cuando se agrupa
 *     por candidatura y el territorio es el de la contienda (o la contienda es estatal). null si la
 *     fuente no trae el nombre (el IEPCT sólo lo publica para independientes)
 */
public record GroupVotes(
    String code,
    String label,
    String color,
    long votes,
    double pct,
    double validPct,
    String candidate) {

  public GroupVotes withCandidate(String name) {
    return new GroupVotes(code, label, color, votes, pct, validPct, name);
  }
}
