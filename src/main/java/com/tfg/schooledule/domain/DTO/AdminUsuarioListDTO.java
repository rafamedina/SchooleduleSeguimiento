package com.tfg.schooledule.domain.dto;

import java.util.Set;

public record AdminUsuarioListDTO(
    Integer id,
    String username,
    String nombre,
    String apellidos,
    String email,
    boolean activo,
    Set<String> roles,
    Set<String> centros) {}
