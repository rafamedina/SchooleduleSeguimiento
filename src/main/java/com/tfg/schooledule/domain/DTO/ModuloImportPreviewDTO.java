package com.tfg.schooledule.domain.dto;

import java.util.List;

public record ModuloImportPreviewDTO(
    boolean valido,
    List<ModuloImportErrorDTO> errores,
    List<String> advertencias,
    int totalRas,
    int totalCes) {}
