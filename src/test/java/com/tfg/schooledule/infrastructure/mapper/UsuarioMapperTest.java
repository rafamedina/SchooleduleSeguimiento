package com.tfg.schooledule.infrastructure.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.tfg.schooledule.domain.dto.UsuarioDTO;
import com.tfg.schooledule.domain.entity.Rol;
import com.tfg.schooledule.domain.entity.Usuario;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class UsuarioMapperTest {

  private final UsuarioMapper mapper = Mappers.getMapper(UsuarioMapper.class);

  @Test
  void toDto_mapsAllPlainFields() {
    LocalDateTime now = LocalDateTime.now();
    Rol rol = Rol.builder().id(1).nombre("ROLE_ADMIN").build();
    Usuario usuario =
        Usuario.builder()
            .id(1)
            .username("admin")
            .nombre("Rafael")
            .apellidos("Medina")
            .email("rafael@tfg.com")
            .activo(true)
            .fechaRegistro(now)
            .roles(Set.of(rol))
            .build();

    UsuarioDTO dto = mapper.toDto(usuario);

    assertEquals(1, dto.id());
    assertEquals("admin", dto.username());
    assertEquals("Rafael", dto.nombre());
    assertEquals("Medina", dto.apellidos());
    assertEquals("rafael@tfg.com", dto.email());
    assertTrue(dto.activo());
    assertEquals(now, dto.fechaCreacion());
    assertTrue(dto.roles().contains("ROLE_ADMIN"));
  }

  @Test
  void toDto_emptyRolesProducesEmptyString() {
    Usuario usuario = Usuario.builder().id(2).username("alumno").email("a@b.com").build();
    UsuarioDTO dto = mapper.toDto(usuario);
    assertEquals("", dto.roles());
  }
}
