package com.tfg.schooledule.domain.dto;

public record AdminImparticionListDTO(
    Integer id,
    String moduloCodigo,
    String moduloNombre,
    String grupoNombre,
    String centroNombre,
    String profesorNombreCompleto) {}
