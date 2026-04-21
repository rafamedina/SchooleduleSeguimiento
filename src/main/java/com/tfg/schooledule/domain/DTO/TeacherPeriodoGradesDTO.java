package com.tfg.schooledule.domain.dto;

import java.math.BigDecimal;
import java.util.List;

public record TeacherPeriodoGradesDTO(
    Integer periodoId,
    String periodoNombre,
    BigDecimal peso,
    boolean cerrado,
    List<TeacherGradeItemDTO> items,
    BigDecimal media) {}
