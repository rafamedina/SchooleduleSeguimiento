package com.tfg.schooledule.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Desglose completo de calificaciones de un alumno en una impartición")
public record TeacherStudentGradesDTO(
    @Schema(description = "ID de la matrícula", example = "42") Integer matriculaId,
    @Schema(description = "Nombre completo del alumno", example = "Ana García López")
        String alumnoNombre,
    @Schema(
            description = "Etiqueta de la impartición: grupo · módulo",
            example = "1DAW · Programación")
        String imparticionLabel,
    @Schema(description = "Desglose por períodos de evaluación")
        List<TeacherPeriodoGradesDTO> periodos,
    @Schema(
            description = "Media global ponderada de todos los períodos (0.00-10.00)",
            example = "7.50")
        BigDecimal mediaGlobal) {}
