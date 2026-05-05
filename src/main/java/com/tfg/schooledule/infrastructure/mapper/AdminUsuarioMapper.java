package com.tfg.schooledule.infrastructure.mapper;

import com.tfg.schooledule.domain.dto.AdminUsuarioFormDTO;
import com.tfg.schooledule.domain.dto.AdminUsuarioListDTO;
import com.tfg.schooledule.domain.entity.Centro;
import com.tfg.schooledule.domain.entity.Rol;
import com.tfg.schooledule.domain.entity.Usuario;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AdminUsuarioMapper {

  @Mapping(target = "roles", source = "usuario", qualifiedByName = "rolesToStringSet")
  @Mapping(target = "centros", source = "usuario", qualifiedByName = "centrosToStringSet")
  AdminUsuarioListDTO toListDTO(Usuario usuario);

  @Mapping(target = "password", ignore = true)
  @Mapping(target = "roleIds", source = "usuario", qualifiedByName = "rolesToIdSet")
  @Mapping(target = "centroIds", source = "usuario", qualifiedByName = "centrosToIdSet")
  AdminUsuarioFormDTO toFormDTO(Usuario usuario);

  @Named("rolesToStringSet")
  default Set<String> rolesToStringSet(Usuario usuario) {
    if (usuario.getRoles() == null) return new HashSet<>();
    return usuario.getRoles().stream().map(Rol::getNombre).collect(Collectors.toSet());
  }

  @Named("centrosToStringSet")
  default Set<String> centrosToStringSet(Usuario usuario) {
    if (usuario.getCentros() == null) return new HashSet<>();
    return usuario.getCentros().stream().map(Centro::getNombre).collect(Collectors.toSet());
  }

  @Named("rolesToIdSet")
  default Set<Integer> rolesToIdSet(Usuario usuario) {
    if (usuario.getRoles() == null) return new HashSet<>();
    return usuario.getRoles().stream().map(Rol::getId).collect(Collectors.toSet());
  }

  @Named("centrosToIdSet")
  default Set<Integer> centrosToIdSet(Usuario usuario) {
    if (usuario.getCentros() == null) return new HashSet<>();
    return usuario.getCentros().stream().map(Centro::getId).collect(Collectors.toSet());
  }
}
