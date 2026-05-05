package com.tfg.schooledule.infrastructure.mapper;

import com.tfg.schooledule.domain.dto.AdminCentroFormDTO;
import com.tfg.schooledule.domain.entity.Centro;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AdminCentroMapper {

  AdminCentroFormDTO toFormDTO(Centro centro);

  @Mapping(target = "activo", ignore = true)
  @Mapping(target = "profesores", ignore = true)
  @Mapping(target = "configuracion", ignore = true)
  void updateEntity(AdminCentroFormDTO dto, @MappingTarget Centro target);
}
