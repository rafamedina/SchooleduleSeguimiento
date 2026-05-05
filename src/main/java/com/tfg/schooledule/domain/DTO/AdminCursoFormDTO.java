package com.tfg.schooledule.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@Schema(description = "Formulario de creación/edición de un curso académico")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCursoFormDTO {

  @Schema(
      description = "ID del curso académico (null en creación, presente en edición)",
      example = "2")
  private Integer id;

  @Schema(description = "Nombre del curso académico (max 20 caracteres)", example = "2024-2025")
  @NotBlank
  @Size(max = 20)
  private String nombre;

  @Schema(description = "Fecha de inicio del curso (ISO 8601: yyyy-MM-dd)", example = "2024-09-01")
  @NotNull
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate fechaInicio;

  @Schema(
      description =
          "Fecha de fin del curso (ISO 8601: yyyy-MM-dd). Debe ser posterior a fechaInicio.",
      example = "2025-06-30")
  @NotNull
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate fechaFin;

  @AssertTrue(message = "La fecha de fin debe ser posterior a la fecha de inicio")
  public boolean isFechaFinValida() {
    if (fechaInicio == null || fechaFin == null) return true;
    return fechaFin.isAfter(fechaInicio);
  }
}
