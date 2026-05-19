package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import com.tfg.schooledule.infrastructure.service.CentroAdminContextService;
import com.tfg.schooledule.infrastructure.service.CentroAdminDashboardService;
import jakarta.persistence.EntityNotFoundException;
import java.security.Principal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/centro-admin")
@PreAuthorize("hasRole('ADMIN_CENTRO')")
public class CentroAdminController {

  private final CentroAdminDashboardService dashboardService;
  private final CentroAdminContextService contextService;
  private final UsuarioRepository usuarioRepository;

  public CentroAdminController(
      CentroAdminDashboardService dashboardService,
      CentroAdminContextService contextService,
      UsuarioRepository usuarioRepository) {
    this.dashboardService = dashboardService;
    this.contextService = contextService;
    this.usuarioRepository = usuarioRepository;
  }

  @GetMapping({"/dashboard", ""})
  public String dashboard(Principal principal, Model model) {
    Integer adminId = resolveAdminId(principal);
    model.addAttribute("stats", dashboardService.buildStats(adminId));
    model.addAttribute("centros", contextService.getCentrosDelAdmin(adminId));
    return "centro-admin/dashboard";
  }

  Integer resolveAdminId(Principal principal) {
    return usuarioRepository
        .findUsuarioByEmail(principal.getName())
        .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"))
        .getId();
  }
}
