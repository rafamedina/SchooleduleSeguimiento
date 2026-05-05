package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.infrastructure.service.AdminAuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Admin - Auditoría")
@Controller
@RequestMapping("/admin/auditoria")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuditoriaController {

  private final AdminAuditoriaService adminAuditoriaService;

  public AdminAuditoriaController(AdminAuditoriaService adminAuditoriaService) {
    this.adminAuditoriaService = adminAuditoriaService;
  }

  @Operation(
      summary = "Registro de auditoría de calificaciones",
      description =
          "Vista con el historial completo de cambios de calificaciones. "
              + "Permite filtrar por email del alumno, nombre del módulo y rango de fechas. "
              + "Todos los filtros son opcionales; sin filtros devuelve todos los registros. "
              + "Los registros son generados automáticamente por triggers PL/pgSQL en la base de datos "
              + "cada vez que se inserta o modifica una calificación.")
  @ApiResponse(
      responseCode = "200",
      description =
          "Vista HTML: admin/auditoria/lista. "
              + "Modelo: registros (List<AdminAuditoriaListDTO>), "
              + "alumnoEmail, moduloNombre, fechaDesde, fechaHasta (valores de filtro actuales)")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping
  public String lista(
      @Parameter(description = "Filtro por email del alumno (búsqueda parcial)", example = "ana@")
          @RequestParam(required = false)
          String alumnoEmail,
      @Parameter(description = "Filtro por nombre del módulo (búsqueda parcial)", example = "DAW")
          @RequestParam(required = false)
          String moduloNombre,
      @Parameter(
              description = "Fecha de inicio del rango (ISO 8601: yyyy-MM-dd)",
              example = "2025-09-01")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate fechaDesde,
      @Parameter(
              description = "Fecha de fin del rango (ISO 8601: yyyy-MM-dd)",
              example = "2026-06-30")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate fechaHasta,
      Model model) {
    model.addAttribute(
        "registros",
        adminAuditoriaService.buscar(alumnoEmail, moduloNombre, fechaDesde, fechaHasta));
    model.addAttribute("alumnoEmail", alumnoEmail);
    model.addAttribute("moduloNombre", moduloNombre);
    model.addAttribute("fechaDesde", fechaDesde);
    model.addAttribute("fechaHasta", fechaHasta);
    return "admin/auditoria/lista";
  }
}
