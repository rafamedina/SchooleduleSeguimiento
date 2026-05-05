package com.tfg.schooledule.infrastructure.mapper;

import com.tfg.schooledule.domain.dto.AdminGrupoFormDTO;
import com.tfg.schooledule.domain.entity.Grupo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdminGrupoMapper {

  @Mapping(target = "centroId", source = "centro.id")
  @Mapping(target = "cursoAcademicoId", source = "cursoAcademico.id")
  AdminGrupoFormDTO toFormDTO(Grupo grupo);
}
