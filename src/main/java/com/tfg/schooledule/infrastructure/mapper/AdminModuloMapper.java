package com.tfg.schooledule.infrastructure.mapper;

import com.tfg.schooledule.domain.dto.AdminModuloFormDTO;
import com.tfg.schooledule.domain.entity.Modulo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AdminModuloMapper {

  AdminModuloFormDTO toFormDTO(Modulo modulo);

  @Mapping(target = "activo", ignore = true)
  void updateEntity(AdminModuloFormDTO dto, @MappingTarget Modulo target);
}
