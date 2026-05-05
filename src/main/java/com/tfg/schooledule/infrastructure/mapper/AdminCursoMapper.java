package com.tfg.schooledule.infrastructure.mapper;

import com.tfg.schooledule.domain.dto.AdminCursoFormDTO;
import com.tfg.schooledule.domain.entity.CursoAcademico;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AdminCursoMapper {

  AdminCursoFormDTO toFormDTO(CursoAcademico cursoAcademico);

  @Mapping(target = "activo", ignore = true)
  void updateEntity(AdminCursoFormDTO dto, @MappingTarget CursoAcademico target);
}
