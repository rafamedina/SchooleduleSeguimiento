package com.tfg.schooledule.domain.dto;

import java.util.List;

public record UsuarioImportPreviewDTO(
    boolean valido, List<UsuarioImportErrorDTO> errores, int totalUsuarios) {}
