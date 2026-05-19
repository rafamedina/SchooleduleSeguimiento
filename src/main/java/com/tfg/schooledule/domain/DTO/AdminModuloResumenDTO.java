package com.tfg.schooledule.domain.dto;

import java.util.List;

public record AdminModuloResumenDTO(
    Integer id,
    String codigo,
    String nombre,
    Boolean activo,
    int numImparticiones,
    int numCursosConRas,
    int numRasTotal,
    int numCesTotal,
    List<AdminModuloCursoResumenDTO> cursos) {}
