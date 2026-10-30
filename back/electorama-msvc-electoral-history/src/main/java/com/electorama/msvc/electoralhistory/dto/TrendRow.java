package com.electorama.msvc.electoralhistory.dto;

import java.util.List;

/** Un territorio (el estado o un municipio) a lo largo de los años de un cargo. */
public record TrendRow(int code, String name, List<TrendPoint> points) {}
