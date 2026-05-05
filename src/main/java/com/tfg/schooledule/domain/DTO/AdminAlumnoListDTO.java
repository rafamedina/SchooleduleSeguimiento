package com.tfg.schooledule.domain.dto;

public record AdminAlumnoListDTO(
    Integer id, String nombre, String apellidos, String email, int numMatriculas) {}
