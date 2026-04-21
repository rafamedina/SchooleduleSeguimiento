package com.tfg.schooledule.infrastructure.mapper;

import com.tfg.schooledule.domain.dto.UsuarioDTO;
import com.tfg.schooledule.domain.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

  @Mapping(target = "fechaCreacion", source = "fechaRegistro")
  @Mapping(target = "roles", source = "usuario", qualifiedByName = "rolesToString")
  UsuarioDTO toDto(Usuario usuario);

  @Named("rolesToString")
  default String rolesToString(Usuario usuario) {
    if (usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
      return "";
    }
    return usuario.getRoles().stream()
        .map(r -> r.getNombre())
        .sorted()
        .reduce((a, b) -> a + ", " + b)
        .orElse("");
  }
}
