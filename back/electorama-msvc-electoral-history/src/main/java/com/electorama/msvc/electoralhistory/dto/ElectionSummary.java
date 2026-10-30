package com.electorama.msvc.electoralhistory.dto;

import java.time.LocalDate;

/** Una elección cargada, con la fuente oficial de donde salió. */
public record ElectionSummary(
    int id,
    int year,
    String office,
    String scope,
    LocalDate electionDate,
    String publisher,
    String sourceUrl,
    String sourceFile,
    String cutLabel) {}
