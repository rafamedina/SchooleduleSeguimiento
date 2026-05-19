package com.tfg.schooledule.domain.dto;

import java.util.List;

public record AdminModuloCursoResumenDTO(
    Integer cursoAcademicoId,
    String cursoNombre,
    int numRas,
    int numCes,
    List<AdminModuloRaResumenDTO> ras) {}
