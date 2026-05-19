package com.tfg.schooledule.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Calificación de un criterio de evaluación específico")
public record TeacherCriterioGradeDTO(
    @Schema(description = "ID del criterio de evaluación", example = "5")
        Integer criterioEvaluacionId,
    @Schema(description = "Código del criterio", example = "CE1.1") String codigo,
    @Schema(description = "Descripción del criterio", example = "Identifica tipos de datos")
        String descripcion,
    @Schema(
            description = "Valor de la calificación (0.00-10.00, null si no calificado)",
            example = "8.50")
        BigDecimal valor,
    @Schema(description = "Comentario opcional del profesor", example = "Buen trabajo")
        String comentario,
    @Schema(
            description = "ID de la entidad Calificacion en BD (null si no existe aún)",
            example = "101")
        Integer calificacionId,
    @Schema(description = "Peso del criterio definido por el centro", example = "20.00")
        BigDecimal peso) {}
