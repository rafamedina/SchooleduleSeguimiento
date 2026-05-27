package com.tfg.schooledule.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Formulario para asignar un alumno a todas las imparticiones de un grupo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminAsignarGrupoFormDTO {

  @Schema(description = "ID del grupo al que se asigna el alumno", example = "3")
  @NotNull(message = "Debes seleccionar un grupo")
  @Positive
  private Integer grupoId;

  @Schema(description = "Indica si el alumno es repetidor en este grupo", example = "false")
  private boolean esRepetidor;
}
