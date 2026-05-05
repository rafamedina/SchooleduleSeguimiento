package com.tfg.schooledule.infrastructure.mapper;

import com.tfg.schooledule.domain.dto.AdminImparticionFormDTO;
import com.tfg.schooledule.domain.entity.Imparticion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdminImparticionMapper {

  @Mapping(target = "moduloId", source = "modulo.id")
  @Mapping(target = "grupoId", source = "grupo.id")
  @Mapping(target = "profesorId", source = "profesor.id")
  @Mapping(target = "centroId", source = "centro.id")
  AdminImparticionFormDTO toFormDTO(Imparticion imparticion);
}
