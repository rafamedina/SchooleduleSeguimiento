package com.tfg.schooledule.domain.enums;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum InstrumentoEvaluacion {
  PRUEBA_OBJETIVA,
  ACTIVIDAD_EVALUABLE,
  TRABAJO_PROYECTO,
  EXPOSICION_ORAL,
  OBSERVACION_ACTITUD;

  public static Optional<InstrumentoEvaluacion> fromTexto(String texto) {
    if (texto == null || texto.isBlank()) return Optional.empty();
    String normalizado = texto.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    return Arrays.stream(values()).filter(v -> v.name().equals(normalizado)).findFirst();
  }
}
