package com.tfg.schooledule.domain.dto;

import com.tfg.schooledule.domain.enums.EstadoMatricula;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(
    description = "Formulario de creación/edición de una matrícula de alumno en una impartición")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminMatriculaFormDTO {

  @Schema(
      description = "ID de la matrícula (null en creación, presente en edición)",
      example = "25")
  private Integer id;

  @Schema(description = "ID de la impartición en la que se matricula al alumno", example = "12")
  @NotNull
  @Positive
  private Integer imparticionId;

  @Schema(
      description = "Estado de la matrícula. Valores: ACTIVO, BAJA, PENDIENTE",
      example = "ACTIVO")
  @NotNull
  private EstadoMatricula estado;

  @Schema(description = "Indica si el alumno está repitiendo esta impartición", example = "false")
  @NotNull
  private Boolean esRepetidor;
}
