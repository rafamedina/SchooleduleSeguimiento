package com.tfg.schooledule.infrastructure.mapper;

import com.tfg.schooledule.domain.dto.GradeDTO;
import com.tfg.schooledule.domain.dto.GradeDashboardDTO;
import com.tfg.schooledule.domain.entity.Calificacion;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class GradeDashboardMapper {

  @Autowired protected GradeMapper gradeMapper;

  public GradeDashboardDTO toDto(List<Calificacion> califs, String periodoNombre) {
    if (califs == null || califs.isEmpty()) {
      return new GradeDashboardDTO(periodoNombre, new HashMap<>());
    }
    Map<String, List<GradeDTO>> gradesByModulo =
        califs.stream()
            .collect(
                Collectors.groupingBy(
                    c -> c.getMatricula().getImparticion().getModulo().getNombre(),
                    Collectors.mapping(gradeMapper::toDto, Collectors.toList())));
    return new GradeDashboardDTO(periodoNombre, gradesByModulo);
  }
}
