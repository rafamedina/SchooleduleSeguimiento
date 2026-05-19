package com.tfg.schooledule.domain.dto;

import java.math.BigDecimal;

public record AdminModuloCeResumenDTO(
    Integer id,
    String codigo,
    String descripcion,
    BigDecimal peso,
    String instrumento,
    String unidadDidactica,
    Short trimestre) {}
