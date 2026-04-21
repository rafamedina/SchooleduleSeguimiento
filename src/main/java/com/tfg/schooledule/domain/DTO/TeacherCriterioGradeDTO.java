package com.tfg.schooledule.domain.dto;

import java.math.BigDecimal;

public record TeacherCriterioGradeDTO(
    Integer criterioEvaluacionId,
    String codigo,
    String descripcion,
    BigDecimal valor,
    String comentario,
    Integer calificacionId) {}
