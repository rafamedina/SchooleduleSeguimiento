package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import com.tfg.schooledule.infrastructure.service.AdminAuditoriaService;
import com.tfg.schooledule.infrastructure.service.AuditoriaExcelExportService;
import com.tfg.schooledule.infrastructure.service.CentroAdminContextService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/centro-admin/auditoria")
@PreAuthorize("hasRole('ADMIN_CENTRO')")
public class CentroAdminAuditoriaController {

  private static final String EXCEL_CONTENT_TYPE =
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

  private final AdminAuditoriaService adminAuditoriaService;
  private final AuditoriaExcelExportService excelExportService;
  private final CentroAdminContextService contextService;
  private final UsuarioRepository usuarioRepository;

  public CentroAdminAuditoriaController(
      AdminAuditoriaService adminAuditoriaService,
      AuditoriaExcelExportService excelExportService,
      CentroAdminContextService contextService,
      UsuarioRepository usuarioRepository) {
    this.adminAuditoriaService = adminAuditoriaService;
    this.excelExportService = excelExportService;
    this.contextService = contextService;
    this.usuarioRepository = usuarioRepository;
  }

  @GetMapping
  public String lista(
      @RequestParam(required = false) String alumnoEmail,
      @RequestParam(required = false) String moduloNombre,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate fechaDesde,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate fechaHasta,
      Principal principal,
      Model model) {
    var centroIds = contextService.getCentroIdsDelAdmin(resolveAdminId(principal));
    model.addAttribute(
        "registros",
        adminAuditoriaService.buscar(alumnoEmail, moduloNombre, fechaDesde, fechaHasta, centroIds));
    model.addAttribute("alumnoEmail", alumnoEmail);
    model.addAttribute("moduloNombre", moduloNombre);
    model.addAttribute("fechaDesde", fechaDesde);
    model.addAttribute("fechaHasta", fechaHasta);
    return "centro-admin/auditoria/lista";
  }

  @GetMapping("/exportar")
  public void exportarExcel(
      @RequestParam(required = false) String alumnoEmail,
      @RequestParam(required = false) String moduloNombre,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate fechaDesde,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate fechaHasta,
      Principal principal,
      HttpServletResponse response)
      throws IOException {
    var centroIds = contextService.getCentroIdsDelAdmin(resolveAdminId(principal));
    var registros =
        adminAuditoriaService.buscar(alumnoEmail, moduloNombre, fechaDesde, fechaHasta, centroIds);
    String filename =
        "auditoria_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx";
    response.setContentType(EXCEL_CONTENT_TYPE);
    response.setHeader("Content-Disposition", "attachment; filename=" + filename);
    response.getOutputStream().write(excelExportService.exportar(registros));
  }

  Integer resolveAdminId(Principal principal) {
    return usuarioRepository
        .findUsuarioByEmail(principal.getName())
        .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"))
        .getId();
  }
}
