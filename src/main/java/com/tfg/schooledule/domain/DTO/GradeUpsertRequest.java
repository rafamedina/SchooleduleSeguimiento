package com.tfg.schooledule.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record GradeUpsertRequest(
    @NotNull Integer matriculaId, @NotEmpty @Valid List<Entry> entries) {

  public record Entry(
      @NotNull Integer criterioEvaluacionId,
      @DecimalMin("0.00") @DecimalMax("10.00") BigDecimal valor,
      @Size(max = 1000) String comentario) {}
}
