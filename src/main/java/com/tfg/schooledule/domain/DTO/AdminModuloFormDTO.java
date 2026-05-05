package com.tfg.schooledule.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Formulario de creación/edición de un módulo formativo (asignatura)")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminModuloFormDTO {

  @Schema(description = "ID del módulo (null en creación, presente en edición)", example = "3")
  private Integer id;

  @Schema(
      description =
          "Código único del módulo. Solo mayúsculas, dígitos, guiones y guiones bajos. Max 20 chars.",
      example = "DAW-PROG")
  @NotBlank
  @Size(max = 20)
  @Pattern(
      regexp = "^[A-Z0-9_-]+$",
      message = "El código solo puede contener mayúsculas, dígitos, guiones y guiones bajos")
  private String codigo;

  @Schema(description = "Nombre completo del módulo (max 150 caracteres)", example = "Programación")
  @NotBlank
  @Size(max = 150)
  private String nombre;
}
