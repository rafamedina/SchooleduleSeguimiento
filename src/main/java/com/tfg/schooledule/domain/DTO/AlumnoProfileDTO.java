package com.tfg.schooledule.domain.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlumnoProfileDTO {
  private Integer id;
  private String username;
  private String nombre;
  private String apellidos;
  private String email;
  private String centroNombre;
  private String grupoNombre;
  private String cursoAcademico;
}
