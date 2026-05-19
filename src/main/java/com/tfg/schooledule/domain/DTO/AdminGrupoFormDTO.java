package com.tfg.schooledule.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Formulario de creación/edición de un grupo de alumnos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminGrupoFormDTO {

  @Schema(description = "ID del grupo (null en creación, presente en edición)", example = "5")
  private Integer id;

  @Schema(description = "Nombre del grupo (max 50 caracteres)", example = "1DAW-A")
  @NotBlank
  @Size(max = 50)
  private String nombre;

  @Schema(description = "ID del centro educativo al que pertenece el grupo", example = "1")
  @NotNull
  @Positive
  private Integer centroId;

  @Schema(description = "ID del curso académico al que pertenece el grupo", example = "2")
  @NotNull
  @Positive
  private Integer cursoAcademicoId;

  @Schema(description = "ID del usuario tutor del grupo (opcional)", example = "3")
  @Positive
  private Integer tutorId;
}
