package com.tfg.schooledule.domain.dto;

import java.math.BigDecimal;

public record ModuloImportRowDTO(
    int numeroFila,
    String raCodigo,
    String raDescripcion,
    BigDecimal raPeso,
    String ceCodigo,
    String ceDescripcion,
    BigDecimal cePeso,
    String instrumento,
    String unidadDidactica,
    Integer trimestre) {}
