package com.tfg.schooledule.infrastructure.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.tfg.schooledule.domain.dto.AlumnoProfileDTO;
import com.tfg.schooledule.domain.entity.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class AlumnoProfileMapperTest {

  private final AlumnoProfileMapper mapper = Mappers.getMapper(AlumnoProfileMapper.class);

  @Test
  void toDto_composesFromUsuarioAndMatricula() {
    Usuario usuario =
        Usuario.builder()
            .id(1)
            .username("alumno1")
            .nombre("Ana")
            .apellidos("García")
            .email("ana@tfg.com")
            .build();

    Centro centro = Centro.builder().nombre("IES Ejemplo").build();
    CursoAcademico curso = CursoAcademico.builder().nombre("2025/2026").build();
    Grupo grupo = Grupo.builder().nombre("DAM2A").centro(centro).cursoAcademico(curso).build();
    Imparticion imparticion = Imparticion.builder().grupo(grupo).build();
    Matricula matricula =
        Matricula.builder().alumno(usuario).imparticion(imparticion).centro(centro).build();

    AlumnoProfileDTO dto = mapper.toDto(usuario, matricula);

    assertEquals(1, dto.id());
    assertEquals("alumno1", dto.username());
    assertEquals("Ana", dto.nombre());
    assertEquals("García", dto.apellidos());
    assertEquals("ana@tfg.com", dto.email());
    assertEquals("IES Ejemplo", dto.centroNombre());
    assertEquals("DAM2A", dto.grupoNombre());
    assertEquals("2025/2026", dto.cursoAcademico());
  }
}
