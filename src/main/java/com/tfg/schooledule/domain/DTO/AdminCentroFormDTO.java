package com.tfg.schooledule.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Formulario de creación/edición de un centro educativo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCentroFormDTO {

  @Schema(description = "ID del centro (null en creación, presente en edición)", example = "1")
  private Integer id;

  @Schema(
      description = "Nombre del centro educativo (requerido, max 100 caracteres)",
      example = "IES Tecnológico")
  @NotBlank
  @Size(max = 100)
  private String nombre;

  @Schema(
      description = "Dirección o ubicación del centro (opcional, max 200 caracteres)",
      example = "Calle Mayor 10, Madrid")
  @Size(max = 200)
  private String ubicacion;
}
