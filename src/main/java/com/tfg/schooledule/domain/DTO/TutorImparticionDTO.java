package com.tfg.schooledule.domain.dto;

public record TutorImparticionDTO(
    Integer imparticionId,
    String moduloNombre,
    String profesorNombre,
    long numAlumnos,
    boolean puedeEditarNotas) {}
