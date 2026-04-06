package com.tfg.schooledule.domain.DTO;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeDashboardDTO {
  private String periodoNombre;
  private Map<String, List<GradeDTO>> gradesByModulo;
}
