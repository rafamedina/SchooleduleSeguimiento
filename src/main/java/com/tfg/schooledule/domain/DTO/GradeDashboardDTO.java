package com.tfg.schooledule.domain.dto;

import java.util.List;
import java.util.Map;

public record GradeDashboardDTO(String periodoNombre, Map<String, List<GradeDTO>> gradesByModulo) {}
