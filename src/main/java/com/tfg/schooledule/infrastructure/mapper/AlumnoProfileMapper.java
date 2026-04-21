package com.tfg.schooledule.infrastructure.mapper;

import com.tfg.schooledule.domain.dto.AlumnoProfileDTO;
import com.tfg.schooledule.domain.entity.Matricula;
import com.tfg.schooledule.domain.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AlumnoProfileMapper {

  @Mapping(target = "id", source = "usuario.id")
  @Mapping(target = "username", source = "usuario.username")
  @Mapping(target = "nombre", source = "usuario.nombre")
  @Mapping(target = "apellidos", source = "usuario.apellidos")
  @Mapping(target = "email", source = "usuario.email")
  @Mapping(target = "centroNombre", source = "matricula.centro.nombre")
  @Mapping(target = "grupoNombre", source = "matricula.imparticion.grupo.nombre")
  @Mapping(target = "cursoAcademico", source = "matricula.imparticion.grupo.cursoAcademico.nombre")
  AlumnoProfileDTO toDto(Usuario usuario, Matricula matricula);
}
