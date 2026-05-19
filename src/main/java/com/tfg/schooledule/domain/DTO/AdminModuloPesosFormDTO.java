package com.tfg.schooledule.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminModuloPesosFormDTO {

  @NotBlank
  @Size(max = 20)
  @Pattern(
      regexp = "^[A-Z0-9_-]+$",
      message = "El código solo puede contener mayúsculas, dígitos, guiones y guiones bajos")
  private String codigo;

  @NotBlank
  @Size(max = 150)
  private String nombre;

  private List<AdminModuloRaPesoDTO> ras = new ArrayList<>();
}
