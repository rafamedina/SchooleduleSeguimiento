package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.domain.dto.UsuarioImportResultado;
import com.tfg.schooledule.domain.exception.UsuarioImportException;
import com.tfg.schooledule.infrastructure.service.UsuarioImportService;
import com.tfg.schooledule.infrastructure.service.UsuarioPlantillaExcelService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsuarioImportController {

  private static final Set<String> MIME_XLSX =
      Set.of(
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
          "application/octet-stream");

  private static final String VIEW_IMPORTAR = "admin/usuarios/importar";
  private static final String ATTR_ERROR = "error";

  private final UsuarioImportService usuarioImportService;
  private final UsuarioPlantillaExcelService plantillaService;

  public AdminUsuarioImportController(
      UsuarioImportService usuarioImportService, UsuarioPlantillaExcelService plantillaService) {
    this.usuarioImportService = usuarioImportService;
    this.plantillaService = plantillaService;
  }

  @GetMapping("/plantilla-excel")
  public void descargarPlantilla(HttpServletResponse response) throws IOException {
    byte[] bytes = plantillaService.generarPlantilla();
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("Content-Disposition", "attachment; filename=plantilla_usuarios.xlsx");
    response.getOutputStream().write(bytes);
  }

  @GetMapping("/importar")
  public String formulario() {
    return VIEW_IMPORTAR;
  }

  @PostMapping("/importar")
  public String importar(
      @RequestParam("archivo") MultipartFile archivo,
      Model model,
      RedirectAttributes redirectAttributes) {
    if (!esXlsx(archivo)) {
      model.addAttribute(ATTR_ERROR, "El archivo debe ser .xlsx y no puede estar vacío");
      return VIEW_IMPORTAR;
    }
    try {
      UsuarioImportResultado resultado = usuarioImportService.importar(archivo.getBytes());
      redirectAttributes.addFlashAttribute(
          "exito",
          resultado.usuariosCreados()
              + " usuarios creados, "
              + resultado.matriculasCreadas()
              + " matrículas generadas");
      return "redirect:/admin/usuarios";
    } catch (UsuarioImportException ex) {
      model.addAttribute("errores", ex.getErrores());
      return VIEW_IMPORTAR;
    } catch (IllegalArgumentException ex) {
      model.addAttribute(ATTR_ERROR, "El archivo no es un Excel .xlsx válido");
      return VIEW_IMPORTAR;
    } catch (IOException ex) {
      model.addAttribute(ATTR_ERROR, "No se pudo leer el archivo");
      return VIEW_IMPORTAR;
    }
  }

  private boolean esXlsx(MultipartFile archivo) {
    if (archivo == null || archivo.isEmpty()) return false;
    String nombre = archivo.getOriginalFilename();
    if (nombre == null || !nombre.toLowerCase().endsWith(".xlsx")) return false;
    String ct = archivo.getContentType();
    return ct != null && MIME_XLSX.contains(ct);
  }
}
