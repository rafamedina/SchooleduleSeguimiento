package com.tfg.schooledule.infrastructure.mapper;

import com.tfg.schooledule.domain.dto.GradeDTO;
import com.tfg.schooledule.domain.dto.GradeDashboardDTO;
import com.tfg.schooledule.domain.entity.Calificacion;
import com.tfg.schooledule.infrastructure.repository.AuditoriaNotaRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class GradeDashboardMapper {

  protected GradeMapper gradeMapper;
  protected AuditoriaNotaRepository auditoriaRepo;

  @Autowired
  public void setGradeMapper(GradeMapper gradeMapper) {
    this.gradeMapper = gradeMapper;
  }

  @Autowired
  public void setAuditoriaRepo(AuditoriaNotaRepository auditoriaRepo) {
    this.auditoriaRepo = auditoriaRepo;
  }

  public GradeDashboardDTO toDto(List<Calificacion> califs, String periodoNombre) {
    if (califs == null || califs.isEmpty()) {
      return new GradeDashboardDTO(periodoNombre, new HashMap<>());
    }
    Map<String, List<GradeDTO>> gradesByModulo =
        califs.stream()
            .collect(
                Collectors.groupingBy(
                    c -> c.getMatricula().getImparticion().getModulo().getNombre(),
                    Collectors.mapping(
                        c -> {
                          GradeDTO base = gradeMapper.toDto(c);
                          boolean modificada = auditoriaRepo.countByCalificacionId(c.getId()) > 1;
                          return new GradeDTO(
                              base.itemNombre(),
                              base.valor(),
                              base.comentario(),
                              base.fecha(),
                              base.tipoActividad(),
                              modificada);
                        },
                        Collectors.toList())));
    return new GradeDashboardDTO(periodoNombre, gradesByModulo);
  }
}
