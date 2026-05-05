package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.infrastructure.service.AdminUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Admin - Dashboard")
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

  private final AdminUsuarioService adminUsuarioService;

  public AdminController(AdminUsuarioService adminUsuarioService) {
    this.adminUsuarioService = adminUsuarioService;
  }

  @Operation(
      summary = "Panel de administrador",
      description =
          "Vista principal del área de administración. "
              + "Carga estadísticas globales del sistema: total de centros, usuarios, alumnos, "
              + "módulos activos e imparticiones del curso activo. "
              + "Requiere rol ROLE_ADMIN.")
  @ApiResponse(
      responseCode = "200",
      description =
          "Vista HTML: admin/dashboard. Modelo: stats (DashboardStatsDTO con contadores globales)")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @ApiResponse(responseCode = "302", description = "Redirect a /login si no hay sesión activa")
  @GetMapping("/dashboard")
  public String panelAdministrador(Model model) {
    model.addAttribute("stats", adminUsuarioService.getStats());
    return "admin/dashboard";
  }
}
