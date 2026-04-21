package com.tfg.schooledule.infrastructure.mapper;

import com.tfg.schooledule.domain.dto.GradeDTO;
import com.tfg.schooledule.domain.entity.Calificacion;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GradeMapper {

  @Mapping(
      target = "itemNombre",
      expression =
          "java(calificacion.getCriterioEvaluacion().getCodigo()"
              + " + \" – \" + calificacion.getCriterioEvaluacion().getDescripcion())")
  @Mapping(target = "fecha", ignore = true)
  @Mapping(target = "tipoActividad", ignore = true)
  GradeDTO toDto(Calificacion calificacion);

  List<GradeDTO> toDtoList(List<Calificacion> calificaciones);
}
