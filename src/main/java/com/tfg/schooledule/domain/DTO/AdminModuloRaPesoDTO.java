package com.tfg.schooledule.domain.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminModuloRaPesoDTO {
  private Integer id;
  private String codigo;
  private String descripcion;
  private String cursoNombre;
  private BigDecimal pesoSugerido;
  private List<AdminModuloCePesoDTO> ces = new ArrayList<>();
}
