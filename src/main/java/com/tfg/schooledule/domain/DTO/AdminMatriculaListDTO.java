package com.tfg.schooledule.domain.dto;

import com.tfg.schooledule.domain.enums.EstadoMatricula;

public record AdminMatriculaListDTO(
    Integer id,
    String moduloNombre,
    String grupoNombre,
    String centroNombre,
    EstadoMatricula estado,
    Boolean esRepetidor) {}
