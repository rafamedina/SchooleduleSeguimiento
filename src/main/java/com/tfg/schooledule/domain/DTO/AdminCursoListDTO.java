package com.tfg.schooledule.domain.dto;

import java.time.LocalDate;

public record AdminCursoListDTO(
    Integer id,
    String nombre,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    Boolean activo,
    int numGrupos) {}
