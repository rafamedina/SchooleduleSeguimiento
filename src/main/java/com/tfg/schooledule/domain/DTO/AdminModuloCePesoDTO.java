package com.tfg.schooledule.domain.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminModuloCePesoDTO {
  private Integer id;
  private String codigo;
  private String descripcion;
  private BigDecimal peso;
}
