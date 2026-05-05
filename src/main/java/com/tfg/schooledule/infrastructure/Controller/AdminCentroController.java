package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.domain.dto.AdminCentroFormDTO;
import com.tfg.schooledule.infrastructure.service.AdminCentroService;
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

@Tag(name = "Admin - Centros")
@Controller
@RequestMapping("/admin/centros")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCentroController {

  private final AdminCentroService adminCentroService;

  public AdminCentroController(AdminCentroService adminCentroService) {
    this.adminCentroService = adminCentroService;
  }

  @Operation(
      summary = "Listado de centros",
      description =
          "Renderiza la tabla con todos los centros educativos del sistema. "
              + "Muestra nombre, ubicación y estado activo/inactivo.")
  @ApiResponse(
      responseCode = "200",
      description = "Vista HTML: admin/centros/lista. Modelo: centros (List<AdminCentroListDTO>)")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping
  public String lista(Model model) {
    model.addAttribute("centros", adminCentroService.listarTodos());
    return "admin/centros/lista";
  }

  @Operation(
      summary = "Formulario: nuevo centro",
      description = "Muestra el formulario vacío para crear un nuevo centro educativo.")
  @ApiResponse(
      responseCode = "200",
      description = "Vista HTML: admin/centros/formulario. Modelo: form (AdminCentroFormDTO vacío)")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping("/nuevo")
  public String nuevo(Model model) {
    model.addAttribute("form", new AdminCentroFormDTO());
    return "admin/centros/formulario";
  }

  @Operation(
      summary = "Acción: crear centro",
      description =
          "Procesa el formulario AdminCentroFormDTO. "
              + "Campos requeridos: nombre (max 100). Opcionales: ubicacion (max 200). "
              + "Si hay errores de validación: redibuja el formulario. "
              + "Si el nombre ya existe: redibuja con mensaje de error. "
              + "Si tiene éxito: redirect a /admin/centros.")
  @ApiResponse(responseCode = "302", description = "Creación exitosa → redirect a /admin/centros")
  @ApiResponse(
      responseCode = "200",
      description = "Formulario con errores de validación o nombre duplicado")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @PostMapping("/nuevo")
  public String crear(
      @Valid @ModelAttribute("form") AdminCentroFormDTO form,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      return "admin/centros/formulario";
    }
    try {
      adminCentroService.crear(form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute("error", ex.getMessage());
      return "admin/centros/formulario";
    }
    return "redirect:/admin/centros";
  }

  @Operation(
      summary = "Formulario: editar centro",
      description =
          "Carga el formulario pre-relleno con los datos del centro indicado para su edición.")
  @ApiResponse(
      responseCode = "200",
      description =
          "Vista HTML: admin/centros/formulario. Modelo: form (AdminCentroFormDTO con datos actuales)")
  @ApiResponse(responseCode = "404", description = "Centro no encontrado")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping("/{id}/editar")
  public String editar(
      @Parameter(description = "ID del centro educativo", required = true, example = "1")
          @PathVariable
          @Positive
          Integer id,
      Model model) {
    model.addAttribute("form", adminCentroService.obtenerParaEditar(id));
    return "admin/centros/formulario";
  }

  @Operation(
      summary = "Acción: actualizar centro",
      description =
          "Persiste los cambios del formulario de edición. "
              + "Mismas validaciones que en creación. "
              + "Con éxito: redirect a /admin/centros.")
  @ApiResponse(
      responseCode = "302",
      description = "Actualización exitosa → redirect a /admin/centros")
  @ApiResponse(responseCode = "200", description = "Formulario con errores de validación")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @PostMapping("/{id}/editar")
  public String actualizar(
      @Parameter(description = "ID del centro educativo", required = true, example = "1")
          @PathVariable
          @Positive
          Integer id,
      @Valid @ModelAttribute("form") AdminCentroFormDTO form,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      return "admin/centros/formulario";
    }
    try {
      adminCentroService.actualizar(id, form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute("error", ex.getMessage());
      return "admin/centros/formulario";
    }
    return "redirect:/admin/centros";
  }

  @Operation(
      summary = "Acción: activar/desactivar centro",
      description =
          "Alterna el estado activo del centro. "
              + "Un centro inactivo no puede recibir nuevas imparticiones ni matrículas. "
              + "Lanza error si el centro tiene restricciones de negocio que impidan el cambio.")
  @ApiResponse(responseCode = "302", description = "Toggle exitoso → redirect a /admin/centros")
  @ApiResponse(
      responseCode = "302",
      description = "Error de negocio → redirect a /admin/centros con flash 'error'")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @PostMapping("/{id}/toggle-activo")
  public String toggleActivo(
      @Parameter(description = "ID del centro educativo", required = true, example = "1")
          @PathVariable
          @Positive
          Integer id,
      RedirectAttributes redirectAttributes) {
    try {
      adminCentroService.toggleActivo(id);
    } catch (IllegalStateException ex) {
      redirectAttributes.addFlashAttribute("error", ex.getMessage());
    }
    return "redirect:/admin/centros";
  }
}
