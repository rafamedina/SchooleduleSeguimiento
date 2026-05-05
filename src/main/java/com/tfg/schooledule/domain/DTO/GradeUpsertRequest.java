package com.tfg.schooledule.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Petición de actualización/creación de calificaciones para una matrícula")
public record GradeUpsertRequest(
    @Schema(description = "ID de la matrícula. Debe coincidir con el path variable", example = "42")
        @NotNull
        Integer matriculaId,
    @Schema(description = "Lista de calificaciones a crear o actualizar (mínimo 1)")
        @NotEmpty
        @Valid
        List<Entry> entries) {

  @Schema(description = "Una calificación individual: criterio + valor + comentario opcional")
  public record Entry(
      @Schema(description = "ID del ítem evaluable al que pertenece este criterio", example = "8")
          @NotNull
          @Positive
          Integer itemEvaluableId,
      @Schema(description = "ID del criterio de evaluación a calificar", example = "5") @NotNull
          Integer criterioEvaluacionId,
      @Schema(description = "Valor de la calificación entre 0.00 y 10.00", example = "7.50")
          @DecimalMin("0.00")
          @DecimalMax("10.00")
          BigDecimal valor,
      @Schema(description = "Comentario opcional del profesor (max 1000 caracteres)")
          @Size(max = 1000)
          String comentario) {}
}
