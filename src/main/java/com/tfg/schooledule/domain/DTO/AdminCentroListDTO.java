package com.tfg.schooledule.domain.dto;

public record AdminCentroListDTO(
    Integer id,
    String nombre,
    String ubicacion,
    Boolean activo,
    int numProfesores,
    int numGrupos) {}
