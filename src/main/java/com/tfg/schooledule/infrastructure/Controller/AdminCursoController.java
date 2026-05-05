package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.domain.dto.AdminCursoFormDTO;
import com.tfg.schooledule.infrastructure.service.AdminCursoService;
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

@Tag(name = "Admin - Cursos Académicos")
@Controller
@RequestMapping("/admin/cursos")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCursoController {

  private final AdminCursoService adminCursoService;

  public AdminCursoController(AdminCursoService adminCursoService) {
    this.adminCursoService = adminCursoService;
  }

  @Operation(
      summary = "Listado de cursos académicos",
      description =
          "Renderiza la tabla con todos los cursos académicos. "
              + "Estados posibles: BORRADOR, ACTIVO, CERRADO. Solo puede existir un curso ACTIVO.")
  @ApiResponse(
      responseCode = "200",
      description = "Vista HTML: admin/cursos/lista. Modelo: cursos (List<AdminCursoListDTO>)")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping
  public String lista(Model model) {
    model.addAttribute("cursos", adminCursoService.listarTodos());
    return "admin/cursos/lista";
  }

  @Operation(
      summary = "Formulario: nuevo curso académico",
      description = "Muestra el formulario vacío para crear un nuevo curso académico.")
  @ApiResponse(
      responseCode = "200",
      description = "Vista HTML: admin/cursos/formulario. Modelo: form (AdminCursoFormDTO vacío)")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping("/nuevo")
  public String nuevo(Model model) {
    model.addAttribute("form", new AdminCursoFormDTO());
    return "admin/cursos/formulario";
  }

  @Operation(
      summary = "Acción: crear curso académico",
      description =
          "Procesa el formulario AdminCursoFormDTO. "
              + "Campos: nombre (max 20), fechaInicio (ISO date), fechaFin (ISO date). "
              + "Validación: fechaFin debe ser posterior a fechaInicio. "
              + "Se crea en estado BORRADOR. Con éxito: redirect a /admin/cursos.")
  @ApiResponse(responseCode = "302", description = "Creación exitosa → redirect a /admin/cursos")
  @ApiResponse(responseCode = "200", description = "Formulario con errores de validación")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @PostMapping("/nuevo")
  public String crear(
      @Valid @ModelAttribute("form") AdminCursoFormDTO form,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      return "admin/cursos/formulario";
    }
    try {
      adminCursoService.crear(form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute("error", ex.getMessage());
      return "admin/cursos/formulario";
    }
    return "redirect:/admin/cursos";
  }

  @Operation(
      summary = "Formulario: editar curso académico",
      description = "Carga el formulario pre-relleno para editar los datos del curso.")
  @ApiResponse(
      responseCode = "200",
      description =
          "Vista HTML: admin/cursos/formulario. Modelo: form (AdminCursoFormDTO con datos actuales)")
  @ApiResponse(responseCode = "404", description = "Curso no encontrado")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping("/{id}/editar")
  public String editar(
      @Parameter(description = "ID del curso académico", required = true, example = "2")
          @PathVariable
          @Positive
          Integer id,
      Model model) {
    model.addAttribute("form", adminCursoService.obtenerParaEditar(id));
    return "admin/cursos/formulario";
  }

  @Operation(
      summary = "Acción: actualizar curso académico",
      description =
          "Persiste los cambios del curso. "
              + "No se puede editar un curso en estado CERRADO. "
              + "Con éxito: redirect a /admin/cursos.")
  @ApiResponse(
      responseCode = "302",
      description = "Actualización exitosa → redirect a /admin/cursos")
  @ApiResponse(responseCode = "200", description = "Formulario con errores de validación")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @PostMapping("/{id}/editar")
  public String actualizar(
      @Parameter(description = "ID del curso académico", required = true, example = "2")
          @PathVariable
          @Positive
          Integer id,
      @Valid @ModelAttribute("form") AdminCursoFormDTO form,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      return "admin/cursos/formulario";
    }
    try {
      adminCursoService.actualizar(id, form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute("error", ex.getMessage());
      return "admin/cursos/formulario";
    }
    return "redirect:/admin/cursos";
  }

  @Operation(
      summary = "Acción: activar curso",
      description =
          "Cambia el estado del curso a ACTIVO. "
              + "Solo puede haber un curso ACTIVO a la vez: si ya hay otro activo, lanza error. "
              + "El curso debe estar en estado BORRADOR para poder activarse.")
  @ApiResponse(responseCode = "302", description = "Activación exitosa → redirect a /admin/cursos")
  @ApiResponse(responseCode = "302", description = "Error de negocio → redirect con flash 'error'")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @PostMapping("/{id}/activar")
  public String activar(
      @Parameter(description = "ID del curso académico", required = true, example = "2")
          @PathVariable
          @Positive
          Integer id,
      RedirectAttributes redirectAttributes) {
    try {
      adminCursoService.activar(id);
    } catch (Exception ex) {
      redirectAttributes.addFlashAttribute("error", ex.getMessage());
    }
    return "redirect:/admin/cursos";
  }

  @Operation(
      summary = "Acción: cerrar curso",
      description =
          "Cambia el estado del curso de ACTIVO a CERRADO. "
              + "Un curso cerrado no puede reabrirse. "
              + "Los datos históricos (notas, matrículas) se conservan.")
  @ApiResponse(responseCode = "302", description = "Cierre exitoso → redirect a /admin/cursos")
  @ApiResponse(responseCode = "302", description = "Error de negocio → redirect con flash 'error'")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @PostMapping("/{id}/cerrar")
  public String cerrar(
      @Parameter(description = "ID del curso académico", required = true, example = "2")
          @PathVariable
          @Positive
          Integer id,
      RedirectAttributes redirectAttributes) {
    try {
      adminCursoService.cerrar(id);
    } catch (Exception ex) {
      redirectAttributes.addFlashAttribute("error", ex.getMessage());
    }
    return "redirect:/admin/cursos";
  }
}
