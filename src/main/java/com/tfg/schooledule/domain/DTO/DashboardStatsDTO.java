package com.tfg.schooledule.domain.dto;

public record DashboardStatsDTO(
    long totalUsuarios,
    long activos,
    long inactivos,
    long totalAdmins,
    long totalAlumnos,
    long totalProfesores,
    long totalCentros,
    String cursoActivoNombre,
    long totalMatriculasActivas,
    long totalImparticiones) {}
