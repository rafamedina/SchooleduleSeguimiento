package com.tfg.schooledule.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(
    description =
        "Formulario de creación/edición de una impartición "
            + "(asociación módulo + grupo + profesor + centro)")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminImparticionFormDTO {

  @Schema(
      description = "ID de la impartición (null en creación, presente en edición)",
      example = "7")
  private Integer id;

  @Schema(description = "ID del módulo formativo a impartir", example = "3")
  @NotNull
  @Positive
  private Integer moduloId;

  @Schema(description = "ID del grupo de alumnos", example = "5")
  @NotNull
  @Positive
  private Integer grupoId;

  @Schema(description = "ID del usuario con rol PROFESOR que impartirá el módulo", example = "4")
  @NotNull
  @Positive
  private Integer profesorId;

  @Schema(
      description =
          "ID del centro educativo donde se imparte. El grupo debe pertenecer a este centro.",
      example = "1")
  @NotNull
  @Positive
  private Integer centroId;
}
