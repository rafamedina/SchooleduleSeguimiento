package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.domain.dto.AdminModuloFormDTO;
import com.tfg.schooledule.infrastructure.service.AdminModuloService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Tag(name = "Admin - Módulos")
@Controller
@RequestMapping("/admin/modulos")
@PreAuthorize("hasRole('ADMIN')")
public class AdminModuloController {

  private final AdminModuloService adminModuloService;

  public AdminModuloController(AdminModuloService adminModuloService) {
    this.adminModuloService = adminModuloService;
  }

  @Operation(
      summary = "Listado de módulos",
      description =
          "Renderiza la tabla con todos los módulos formativos (asignaturas) del sistema. "
              + "Muestra código, nombre y estado activo/inactivo.")
  @ApiResponse(
      responseCode = "200",
      description = "Vista HTML: admin/modulos/lista. Modelo: modulos (List<AdminModuloListDTO>)")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping
  public String lista(Model model) {
    model.addAttribute("modulos", adminModuloService.listarTodos());
    return "admin/modulos/lista";
  }

  @Operation(
      summary = "Formulario: nuevo módulo",
      description = "Muestra el formulario vacío para crear un nuevo módulo formativo.")
  @ApiResponse(
      responseCode = "200",
      description = "Vista HTML: admin/modulos/formulario. Modelo: form (AdminModuloFormDTO vacío)")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping("/nuevo")
  public String nuevo(Model model) {
    model.addAttribute("form", new AdminModuloFormDTO());
    return "admin/modulos/formulario";
  }

  @Operation(
      summary = "Acción: crear módulo",
      description =
          "Procesa el formulario AdminModuloFormDTO. "
              + "Campos: codigo (max 20, solo MAYÚSCULAS/dígitos/guiones), nombre (max 150). "
              + "El código debe ser único. Con éxito: redirect a /admin/modulos.")
  @ApiResponse(responseCode = "302", description = "Creación exitosa → redirect a /admin/modulos")
  @ApiResponse(responseCode = "200", description = "Formulario con errores de validación")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @PostMapping("/nuevo")
  public String crear(
      @Valid @ModelAttribute("form") AdminModuloFormDTO form,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      return "admin/modulos/formulario";
    }
    try {
      adminModuloService.crear(form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute("error", ex.getMessage());
      return "admin/modulos/formulario";
    }
    return "redirect:/admin/modulos";
  }

  @Operation(
      summary = "Formulario: editar módulo",
      description = "Carga el formulario pre-relleno con los datos del módulo para su edición.")
  @ApiResponse(
      responseCode = "200",
      description =
          "Vista HTML: admin/modulos/formulario. Modelo: form (AdminModuloFormDTO con datos actuales)")
  @ApiResponse(responseCode = "404", description = "Módulo no encontrado")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping("/{id}/editar")
  public String editar(
      @Parameter(description = "ID del módulo formativo", required = true, example = "3")
          @PathVariable
          @Positive
          Integer id,
      Model model) {
    model.addAttribute("form", adminModuloService.obtenerParaEditar(id));
    return "admin/modulos/formulario";
  }

  @Operation(
      summary = "Acción: actualizar módulo",
      description =
          "Persiste los cambios del formulario de edición del módulo. "
              + "Con éxito: redirect a /admin/modulos.")
  @ApiResponse(
      responseCode = "302",
      description = "Actualización exitosa → redirect a /admin/modulos")
  @ApiResponse(responseCode = "200", description = "Formulario con errores de validación")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @PostMapping("/{id}/editar")
  public String actualizar(
      @Parameter(description = "ID del módulo formativo", required = true, example = "3")
          @PathVariable
          @Positive
          Integer id,
      @Valid @ModelAttribute("form") AdminModuloFormDTO form,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      return "admin/modulos/formulario";
    }
    try {
      adminModuloService.actualizar(id, form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute("error", ex.getMessage());
      return "admin/modulos/formulario";
    }
    return "redirect:/admin/modulos";
  }

  @Operation(
      summary = "Acción: activar/desactivar módulo",
      description =
          "Alterna el estado activo del módulo. "
              + "Un módulo inactivo no aparece disponible al crear nuevas imparticiones.")
  @ApiResponse(responseCode = "302", description = "Toggle exitoso → redirect a /admin/modulos")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @PostMapping("/{id}/toggle-activo")
  public String toggleActivo(
      @Parameter(description = "ID del módulo formativo", required = true, example = "3")
          @PathVariable
          @Positive
          Integer id,
      RedirectAttributes redirectAttributes) {
    try {
      adminModuloService.toggleActivo(id);
    } catch (IllegalStateException ex) {
      redirectAttributes.addFlashAttribute("error", ex.getMessage());
    }
    return "redirect:/admin/modulos";
  }
}
