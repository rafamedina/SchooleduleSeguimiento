package com.tfg.schooledule.domain.dto;

public record AlumnoProfileDTO(
    Integer id,
    String username,
    String nombre,
    String apellidos,
    String email,
    String centroNombre,
    String grupoNombre,
    String cursoAcademico) {}
