package com.tfg.schooledule.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GradeDTO(
    String itemNombre,
    BigDecimal valor,
    String comentario,
    LocalDate fecha,
    String tipoActividad) {}
