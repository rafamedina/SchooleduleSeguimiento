package com.tfg.schooledule.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Ítem evaluable con sus calificaciones por criterio de evaluación")
public record TeacherGradeItemDTO(
    @Schema(description = "ID del ítem evaluable", example = "8") Integer itemEvaluableId,
    @Schema(description = "ID del resultado de aprendizaje al que pertenece", example = "2")
        Integer resultadoAprendizajeId,
    @Schema(description = "Código del RA", example = "RA1") String raCodigo,
    @Schema(description = "Descripción del RA", example = "Conoce los fundamentos de programación")
        String raDescripcion,
    @Schema(description = "Nombre del ítem evaluable", example = "Examen Tema 1") String itemNombre,
    @Schema(description = "Tipo de actividad", example = "EXAMEN") String tipoActividad,
    @Schema(description = "Fecha de realización", example = "2025-11-15") LocalDate fecha,
    @Schema(description = "Criterios de evaluación con sus calificaciones individuales")
        List<TeacherCriterioGradeDTO> criterios,
    @Schema(
            description = "Media calculada de los criterios del ítem (0.00-10.00)",
            example = "8.00")
        BigDecimal mediaRa,
    @Schema(description = "Peso del RA en el periodo, definido por el centro", example = "25.00")
        BigDecimal raPeso) {}
