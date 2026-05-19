package com.tfg.schooledule.domain.dto;

public record TutorGrupoListDTO(
    Integer grupoId,
    String grupoNombre,
    String centroNombre,
    String cursoNombre,
    long numImparticiones,
    long numAlumnos) {}
