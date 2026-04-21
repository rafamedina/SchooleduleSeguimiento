package com.tfg.schooledule.domain.dto;

import java.math.BigDecimal;
import java.util.List;

public record TeacherStudentGradesDTO(
    Integer matriculaId,
    String alumnoNombre,
    String imparticionLabel,
    List<TeacherPeriodoGradesDTO> periodos,
    BigDecimal mediaGlobal) {}
