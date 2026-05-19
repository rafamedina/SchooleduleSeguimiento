package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.domain.dto.AdminModuloImportarFormDTO;
import com.tfg.schooledule.domain.dto.AdminModuloPesosFormDTO;
import com.tfg.schooledule.domain.dto.AdminModuloResumenDTO;
import com.tfg.schooledule.domain.exception.ModuloImportException;
import com.tfg.schooledule.infrastructure.service.AdminModuloService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Tag(name = "Admin - Módulos")
@Controller
@RequestMapping("/admin/modulos")
@PreAuthorize("hasRole('ADMIN')")
@SuppressWarnings("java:S6833")
public class AdminModuloController {

  private static final Set<String> MIME_XLSX =
      Set.of(
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
          "application/octet-stream");

  private static final String VIEW_IMPORTAR = "admin/modulos/importar";
  private static final String VIEW_EDITAR_PESOS = "admin/modulos/editar-pesos";
  private static final String ATTR_ERROR = "error";
  private static final String ATTR_MODULO_ID = "moduloId";
  private static final String REDIRECT_MODULOS = "redirect:/admin/modulos";

  private final AdminModuloService adminModuloService;

  public AdminModuloController(AdminModuloService adminModuloService) {
    this.adminModuloService = adminModuloService;
  }

  @Operation(summary = "Listado de módulos")
  @ApiResponse(responseCode = "200", description = "Vista HTML: admin/modulos/lista")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping
  public String lista(@RequestParam(required = false) String nombre, Model model) {
    model.addAttribute("modulos", adminModuloService.listarFiltrado(nombre));
    model.addAttribute("nombre", nombre);
    return "admin/modulos/lista";
  }

  @Operation(summary = "Resumen de módulo (JSON)")
  @ApiResponse(responseCode = "200", description = "JSON: AdminModuloResumenDTO")
  @ApiResponse(responseCode = "404", description = "Módulo no encontrado")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping("/{id}/resumen")
  @ResponseBody
  public AdminModuloResumenDTO resumen(
      @Parameter(description = "ID del módulo formativo", required = true) @PathVariable @Positive
          Integer id) {
    return adminModuloService.getResumen(id);
  }

  @Operation(summary = "Formulario: importar módulo desde Excel")
  @ApiResponse(responseCode = "200", description = "Vista HTML: admin/modulos/importar")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping("/importar")
  public String formularioImportar(Model model) {
    model.addAttribute("form", new AdminModuloImportarFormDTO());
    return VIEW_IMPORTAR;
  }

  @Operation(summary = "Acción: importar módulo desde Excel")
  @ApiResponse(
      responseCode = "302",
      description = "Importación exitosa → redirect a /admin/modulos")
  @ApiResponse(responseCode = "200", description = "Formulario con errores")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @PostMapping("/importar")
  public String importar(
      @Valid @ModelAttribute("form") AdminModuloImportarFormDTO form,
      BindingResult bindingResult,
      @RequestParam("archivo") MultipartFile archivo,
      Model model,
      RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      return VIEW_IMPORTAR;
    }
    if (archivo.isEmpty()) {
      model.addAttribute(ATTR_ERROR, "Debes seleccionar un archivo Excel (.xlsx).");
      return VIEW_IMPORTAR;
    }
    if (!esXlsx(archivo)) {
      model.addAttribute(
          ATTR_ERROR, "El archivo debe tener extensión .xlsx y tipo de contenido Excel.");
      return VIEW_IMPORTAR;
    }
    try {
      byte[] bytes = archivo.getBytes();
      int totalCes =
          adminModuloService.importarModulo(
              form.getCodigo(), form.getNombre(), form.getCursoAcademicoId(), bytes);
      redirectAttributes.addFlashAttribute(
          "exito", totalCes + " criterios de evaluación importados correctamente.");
      return REDIRECT_MODULOS;
    } catch (ModuloImportException e) {
      model.addAttribute("errores", e.getErrores());
      return VIEW_IMPORTAR;
    } catch (IllegalStateException | EntityNotFoundException e) {
      model.addAttribute(ATTR_ERROR, e.getMessage());
      return VIEW_IMPORTAR;
    } catch (IOException e) {
      model.addAttribute(ATTR_ERROR, "No se pudo leer el archivo subido.");
      return VIEW_IMPORTAR;
    }
  }

  @Operation(summary = "Formulario: editar pesos de RAs y CEs de un módulo")
  @ApiResponse(responseCode = "200", description = "Vista HTML: admin/modulos/editar-pesos")
  @ApiResponse(responseCode = "404", description = "Módulo no encontrado")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping("/{id}/editar")
  public String editarPesos(
      @Parameter(description = "ID del módulo formativo", required = true) @PathVariable @Positive
          Integer id,
      Model model) {
    model.addAttribute(ATTR_MODULO_ID, id);
    model.addAttribute("form", adminModuloService.obtenerParaEditarPesos(id));
    return VIEW_EDITAR_PESOS;
  }

  @Operation(summary = "Acción: actualizar pesos de RAs y CEs de un módulo")
  @ApiResponse(
      responseCode = "302",
      description = "Actualización exitosa → redirect a /admin/modulos")
  @ApiResponse(responseCode = "200", description = "Formulario con errores de validación")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @PostMapping("/{id}/editar")
  public String actualizarPesos(
      @Parameter(description = "ID del módulo formativo", required = true) @PathVariable @Positive
          Integer id,
      @Valid @ModelAttribute("form") AdminModuloPesosFormDTO form,
      BindingResult bindingResult,
      Model model,
      RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      model.addAttribute(ATTR_MODULO_ID, id);
      return VIEW_EDITAR_PESOS;
    }
    try {
      adminModuloService.actualizarPesos(id, form);
      redirectAttributes.addFlashAttribute("exito", "Módulo actualizado correctamente.");
    } catch (IllegalArgumentException | EntityNotFoundException ex) {
      model.addAttribute(ATTR_ERROR, ex.getMessage());
      model.addAttribute(ATTR_MODULO_ID, id);
      return VIEW_EDITAR_PESOS;
    }
    return REDIRECT_MODULOS;
  }

  @Operation(summary = "Acción: activar/desactivar módulo")
  @ApiResponse(responseCode = "302", description = "Toggle exitoso → redirect a /admin/modulos")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @PostMapping("/{id}/toggle-activo")
  public String toggleActivo(
      @Parameter(description = "ID del módulo formativo", required = true) @PathVariable @Positive
          Integer id,
      RedirectAttributes redirectAttributes) {
    try {
      adminModuloService.toggleActivo(id);
    } catch (IllegalStateException ex) {
      redirectAttributes.addFlashAttribute(ATTR_ERROR, ex.getMessage());
    }
    return REDIRECT_MODULOS;
  }

  private boolean esXlsx(MultipartFile archivo) {
    String nombre = archivo.getOriginalFilename();
    if (nombre == null || !nombre.toLowerCase(Locale.ROOT).endsWith(".xlsx")) return false;
    String contentType = archivo.getContentType();
    return contentType != null && MIME_XLSX.contains(contentType);
  }
}
