package com.tfg.schooledule.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TeacherGradeItemDTO(
    Integer itemEvaluableId,
    Integer resultadoAprendizajeId,
    String raCodigo,
    String raDescripcion,
    String itemNombre,
    String tipoActividad,
    LocalDate fecha,
    List<TeacherCriterioGradeDTO> criterios,
    BigDecimal mediaRa) {}
