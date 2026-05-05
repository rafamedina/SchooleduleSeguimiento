package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.domain.dto.AdminMatriculaFormDTO;
import com.tfg.schooledule.domain.enums.EstadoMatricula;
import com.tfg.schooledule.infrastructure.repository.ImparticionRepository;
import com.tfg.schooledule.infrastructure.service.AdminAlumnoService;
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

@Tag(name = "Admin - Alumnos")
@Controller
@RequestMapping("/admin/alumnos")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAlumnoController {

  private final AdminAlumnoService adminAlumnoService;
  private final ImparticionRepository imparticionRepository;

  public AdminAlumnoController(
      AdminAlumnoService adminAlumnoService, ImparticionRepository imparticionRepository) {
    this.adminAlumnoService = adminAlumnoService;
    this.imparticionRepository = imparticionRepository;
  }

  @Operation(
      summary = "Listado de alumnos",
      description =
          "Renderiza la tabla con todos los usuarios que tienen rol ROLE_ALUMNO. "
              + "Desde aquí se accede a la gestión de matrículas de cada alumno.")
  @ApiResponse(
      responseCode = "200",
      description = "Vista HTML: admin/alumnos/lista. Modelo: alumnos (List<AdminAlumnoListDTO>)")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping
  public String lista(Model model) {
    model.addAttribute("alumnos", adminAlumnoService.listarAlumnos());
    return "admin/alumnos/lista";
  }

  @Operation(
      summary = "Matrículas de un alumno",
      description =
          "Muestra todas las matrículas del alumno indicado: "
              + "impartición, estado (ACTIVO, BAJA, PENDIENTE) y si es repetidor.")
  @ApiResponse(
      responseCode = "200",
      description =
          "Vista HTML: admin/alumnos/matriculas. "
              + "Modelo: alumno (AdminAlumnoListDTO), matriculas (List<AdminMatriculaListDTO>)")
  @ApiResponse(responseCode = "404", description = "Alumno no encontrado")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping("/{alumnoId}/matriculas")
  public String matriculas(
      @Parameter(description = "ID del alumno", required = true, example = "10")
          @PathVariable
          @Positive
          Integer alumnoId,
      Model model) {
    model.addAttribute("alumno", adminAlumnoService.obtenerAlumno(alumnoId));
    model.addAttribute("matriculas", adminAlumnoService.listarMatriculas(alumnoId));
    return "admin/alumnos/matriculas";
  }

  @Operation(
      summary = "Formulario: nueva matrícula",
      description =
          "Formulario para matricular al alumno en una impartición. "
              + "Carga el selector de imparticiones y los valores del enum EstadoMatricula.")
  @ApiResponse(
      responseCode = "200",
      description =
          "Vista HTML: admin/alumnos/matricula-formulario. "
              + "Modelo: alumno, form (AdminMatriculaFormDTO vacío), imparticiones, estadosMatricula")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping("/{alumnoId}/matriculas/nueva")
  public String nuevaMatricula(
      @Parameter(description = "ID del alumno", required = true, example = "10")
          @PathVariable
          @Positive
          Integer alumnoId,
      Model model) {
    model.addAttribute("alumno", adminAlumnoService.obtenerAlumno(alumnoId));
    model.addAttribute("form", new AdminMatriculaFormDTO());
    cargarImparticionesYEstados(model);
    return "admin/alumnos/matricula-formulario";
  }

  @Operation(
      summary = "Acción: crear matrícula",
      description =
          "Procesa el formulario AdminMatriculaFormDTO. "
              + "Campos: imparticionId (FK obligatorio), estado (EstadoMatricula), esRepetidor (boolean). "
              + "Un alumno no puede estar matriculado dos veces en la misma impartición. "
              + "Con éxito: redirect a /admin/alumnos/{alumnoId}/matriculas.")
  @ApiResponse(
      responseCode = "302",
      description = "Creación exitosa → redirect a /admin/alumnos/{alumnoId}/matriculas")
  @ApiResponse(responseCode = "200", description = "Formulario con errores de validación")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @PostMapping("/{alumnoId}/matriculas/nueva")
  public String crearMatricula(
      @Parameter(description = "ID del alumno", required = true, example = "10")
          @PathVariable
          @Positive
          Integer alumnoId,
      @Valid @ModelAttribute("form") AdminMatriculaFormDTO form,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("alumno", adminAlumnoService.obtenerAlumno(alumnoId));
      cargarImparticionesYEstados(model);
      return "admin/alumnos/matricula-formulario";
    }
    try {
      adminAlumnoService.crearMatricula(alumnoId, form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute("alumno", adminAlumnoService.obtenerAlumno(alumnoId));
      model.addAttribute("error", ex.getMessage());
      cargarImparticionesYEstados(model);
      return "admin/alumnos/matricula-formulario";
    }
    return "redirect:/admin/alumnos/" + alumnoId + "/matriculas";
  }

  @Operation(
      summary = "Formulario: editar matrícula",
      description =
          "Carga el formulario pre-relleno con los datos de la matrícula para su edición. "
              + "Permite cambiar la impartición, el estado y el flag de repetidor.")
  @ApiResponse(
      responseCode = "200",
      description =
          "Vista HTML: admin/alumnos/matricula-formulario. "
              + "Modelo: alumno, form (AdminMatriculaFormDTO con datos actuales), imparticiones, estadosMatricula")
  @ApiResponse(responseCode = "404", description = "Matrícula o alumno no encontrado")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping("/{alumnoId}/matriculas/{id}/editar")
  public String editarMatricula(
      @Parameter(description = "ID del alumno", required = true, example = "10")
          @PathVariable
          @Positive
          Integer alumnoId,
      @Parameter(description = "ID de la matrícula", required = true, example = "25")
          @PathVariable
          @Positive
          Integer id,
      Model model) {
    model.addAttribute("alumno", adminAlumnoService.obtenerAlumno(alumnoId));
    model.addAttribute("form", adminAlumnoService.obtenerMatriculaParaEditar(id));
    cargarImparticionesYEstados(model);
    return "admin/alumnos/matricula-formulario";
  }

  @Operation(
      summary = "Acción: actualizar matrícula",
      description =
          "Persiste los cambios de la matrícula. "
              + "Con éxito: redirect a /admin/alumnos/{alumnoId}/matriculas.")
  @ApiResponse(
      responseCode = "302",
      description = "Actualización exitosa → redirect a /admin/alumnos/{alumnoId}/matriculas")
  @ApiResponse(responseCode = "200", description = "Formulario con errores de validación")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @PostMapping("/{alumnoId}/matriculas/{id}/editar")
  public String actualizarMatricula(
      @Parameter(description = "ID del alumno", required = true, example = "10")
          @PathVariable
          @Positive
          Integer alumnoId,
      @Parameter(description = "ID de la matrícula", required = true, example = "25")
          @PathVariable
          @Positive
          Integer id,
      @Valid @ModelAttribute("form") AdminMatriculaFormDTO form,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("alumno", adminAlumnoService.obtenerAlumno(alumnoId));
      cargarImparticionesYEstados(model);
      return "admin/alumnos/matricula-formulario";
    }
    adminAlumnoService.actualizarMatricula(id, form);
    return "redirect:/admin/alumnos/" + alumnoId + "/matriculas";
  }

  @Operation(
      summary = "Acción: eliminar matrícula",
      description =
          "Elimina la matrícula si no tiene calificaciones registradas. "
              + "Si hay calificaciones, redirige con mensaje de error flash.")
  @ApiResponse(
      responseCode = "302",
      description = "Eliminación exitosa → redirect a /admin/alumnos/{alumnoId}/matriculas")
  @ApiResponse(
      responseCode = "302",
      description = "Error de integridad → redirect con flash 'error'")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @PostMapping("/{alumnoId}/matriculas/{id}/eliminar")
  public String eliminarMatricula(
      @Parameter(description = "ID del alumno", required = true, example = "10")
          @PathVariable
          @Positive
          Integer alumnoId,
      @Parameter(description = "ID de la matrícula", required = true, example = "25")
          @PathVariable
          @Positive
          Integer id,
      RedirectAttributes redirectAttributes) {
    try {
      adminAlumnoService.eliminarMatricula(id);
    } catch (IllegalStateException ex) {
      redirectAttributes.addFlashAttribute("error", ex.getMessage());
    }
    return "redirect:/admin/alumnos/" + alumnoId + "/matriculas";
  }

  private void cargarImparticionesYEstados(Model model) {
    model.addAttribute(
        "imparticiones", imparticionRepository.findAllByOrderByGrupoNombreAscModuloNombreAsc());
    model.addAttribute("estadosMatricula", EstadoMatricula.values());
  }
}
