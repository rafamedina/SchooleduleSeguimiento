package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.infrastructure.service.ModuloPlantillaExcelService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/modulos")
@PreAuthorize("hasRole('ADMIN')")
public class AdminModuloImportController {

  private final ModuloPlantillaExcelService plantillaService;

  public AdminModuloImportController(ModuloPlantillaExcelService plantillaService) {
    this.plantillaService = plantillaService;
  }

  @GetMapping("/plantilla-excel")
  public void descargarPlantilla(HttpServletResponse response) throws IOException {
    byte[] bytes = plantillaService.generarPlantilla();
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("Content-Disposition", "attachment; filename=plantilla_ras.xlsx");
    response.setContentLength(bytes.length);
    response.getOutputStream().write(bytes);
  }
}
