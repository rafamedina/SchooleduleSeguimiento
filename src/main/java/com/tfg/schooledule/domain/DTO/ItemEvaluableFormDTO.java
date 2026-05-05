package com.tfg.schooledule.domain.dto;

import com.tfg.schooledule.domain.enums.TipoActividad;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Formulario para crear un nuevo ítem evaluable en una impartición")
@Data
@NoArgsConstructor
public class ItemEvaluableFormDTO {

  @Schema(description = "Nombre descriptivo del ítem", example = "Examen Tema 1")
  @NotBlank
  @Size(max = 200)
  private String nombre;

  @Schema(description = "Tipo de actividad evaluable", example = "EXAMEN")
  @NotNull
  private TipoActividad tipo;

  @Schema(description = "Fecha de realización (ISO 8601)", example = "2025-11-15")
  @NotNull
  private LocalDate fecha;

  @Schema(description = "ID del período de evaluación al que pertenece", example = "1")
  @NotNull
  @Positive
  private Integer periodoEvaluacionId;

  @Schema(description = "ID del resultado de aprendizaje al que está vinculado", example = "2")
  @NotNull
  @Positive
  private Integer resultadoAprendizajeId;
}
