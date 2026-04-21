package com.tfg.schooledule.domain.dto;

public record TeacherSubjectDTO(
    Integer imparticionId,
    String moduloCodigo,
    String moduloNombre,
    String grupoNombre,
    String cursoAcademicoNombre,
    long alumnosCount) {}
