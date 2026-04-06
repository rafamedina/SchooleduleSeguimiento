package com.tfg.schooledule.domain.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeDTO {
  private String itemNombre;
  private BigDecimal valor;
  private String comentario;
  private LocalDate fecha;
  private String tipoActividad;
}
