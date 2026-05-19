package com.tfg.schooledule.domain.dto;

import java.util.List;

public record AdminModuloListDTO(
    Integer id,
    String codigo,
    String nombre,
    Boolean activo,
    int numImparticiones,
    int numResultadosAprendizaje,
    List<String> cursosConRas) {}
