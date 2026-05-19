package com.tfg.schooledule.domain.dto;

public record AdminGrupoListDTO(
    Integer id,
    String nombre,
    String centroNombre,
    String cursoAcademicoNombre,
    int numImparticiones,
    String tutorNombre) {}
