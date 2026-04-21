package com.tfg.schooledule.domain.dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public record UsuarioDTO(
    Integer id,
    String username,
    String nombre,
    String apellidos,
    String email,
    boolean activo,
    LocalDateTime fechaCreacion,
    String roles)
    implements Serializable {

  @Serial private static final long serialVersionUID = 1L;
}
