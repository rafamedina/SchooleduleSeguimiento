package com.tfg.schooledule.domain.dto;

public record AdminModuloListDTO(
    Integer id,
    String codigo,
    String nombre,
    Boolean activo,
    int numImparticiones,
    int numResultadosAprendizaje) {}
