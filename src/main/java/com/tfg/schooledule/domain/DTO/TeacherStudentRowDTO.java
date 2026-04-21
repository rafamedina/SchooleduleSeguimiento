package com.tfg.schooledule.domain.dto;

public record TeacherStudentRowDTO(
    Integer matriculaId,
    Integer alumnoId,
    String nombreCompleto,
    String email,
    Boolean esRepetidor) {}
