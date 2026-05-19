package com.tfg.schooledule.domain.dto;

public record ModuloImportErrorDTO(
    int fila, // 0 = error global
    String campo,
    String mensaje) {}
