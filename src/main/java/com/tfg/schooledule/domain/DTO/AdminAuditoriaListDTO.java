package com.tfg.schooledule.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminAuditoriaListDTO(
    Integer id,
    String alumnoEmail,
    String moduloNombre,
    Integer centroId,
    String centroNombre,
    BigDecimal valorAnterior,
    BigDecimal valorNuevo,
    String usuarioResponsable,
    LocalDateTime fechaCambio,
    String motivo) {}
