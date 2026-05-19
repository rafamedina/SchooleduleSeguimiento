package com.tfg.schooledule.domain.dto;

public record UsuarioImportRowDTO(
    int numeroFila,
    String username,
    String nombre,
    String apellidos,
    String email,
    String password,
    String centroNombre,
    String cursoAcademicoNombre,
    String grupoNombre,
    String esRepetidorRaw) {}
