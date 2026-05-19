package com.tfg.schooledule.domain.dto;

import java.math.BigDecimal;
import java.util.List;

public record AdminModuloRaResumenDTO(
    Integer id,
    String codigo,
    String descripcion,
    BigDecimal pesoSugerido,
    List<AdminModuloCeResumenDTO> ces) {}
