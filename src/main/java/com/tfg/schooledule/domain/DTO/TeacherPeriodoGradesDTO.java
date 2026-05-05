package com.tfg.schooledule.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Calificaciones de un período de evaluación (ej: 1er Trimestre)")
public record TeacherPeriodoGradesDTO(
    @Schema(description = "ID del período de evaluación", example = "1") Integer periodoId,
    @Schema(description = "Nombre del período", example = "1er Trimestre") String periodoNombre,
    @Schema(description = "Peso del período en la nota final (0.00-1.00)", example = "0.33")
        BigDecimal peso,
    @Schema(description = "Indica si el período está cerrado para edición") boolean cerrado,
    @Schema(description = "Ítems evaluables del período con sus calificaciones")
        List<TeacherGradeItemDTO> items,
    @Schema(description = "Media calculada del período (0.00-10.00)", example = "7.00")
        BigDecimal media) {}
