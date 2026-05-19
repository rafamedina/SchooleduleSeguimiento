package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.domain.dto.TeacherStudentGradesDTO;
import com.tfg.schooledule.domain.entity.PeriodoEvaluacion;
import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.infrastructure.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Alumno")
@Controller
@RequestMapping("/alumno")
@PreAuthorize("hasRole('ALUMNO')")
@SuppressWarnings("java:S6833")
public class AlumnoController {

  private final UsuarioService usuarioService;

  public AlumnoController(UsuarioService usuarioService) {
    this.usuarioService = usuarioService;
  }

  @Operation(
      summary = "Dashboard del alumno",
      description =
          "Vista principal del alumno. Muestra las asignaturas (imparticiones) en las que está "
              + "matriculado en el curso académico activo del centro al que pertenece. "
              + "Requiere rol ROLE_ALUMNO.")
  @ApiResponse(
      responseCode = "200",
      description =
          "Vista HTML: alumno/dashboard. Modelo: alumnoNombre (String), asignaturas (List<TeacherSubjectDTO>)")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ALUMNO")
  @GetMapping("/dashboard")
  public String dashboard(Principal principal, Model model) {
    Usuario u = resolver(principal);
    model.addAttribute("alumnoNombre", u.getNombre() + " " + u.getApellidos());
    model.addAttribute("asignaturas", usuarioService.getAsignaturasAlumno(u.getId()));
    return "alumno/dashboard";
  }

  @Operation(
      summary = "Perfil del alumno",
      description =
          "Vista con los datos personales del alumno autenticado: nombre, apellidos, email, "
              + "centro y listado de matrículas con su estado.")
  @ApiResponse(
      responseCode = "200",
      description = "Vista HTML: alumno/perfil. Modelo: profile (AlumnoProfileDTO)")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ALUMNO")
  @GetMapping("/perfil")
  public String perfil(Principal principal, Model model) {
    Usuario u = resolver(principal);
    model.addAttribute("profile", usuarioService.getAlumnoProfile(u.getId()));
    return "alumno/perfil";
  }

  @Operation(
      summary = "Vista web de notas",
      description =
          "Vista de notas renderizada en el servidor. Permite al alumno seleccionar una asignatura "
              + "(imparticionId) y un período (periodoId) para ver su cuadro de calificaciones. "
              + "Si no se especifica periodoId, carga el primer período disponible.")
  @ApiResponse(
      responseCode = "200",
      description =
          "Vista HTML: alumno/dashboard_notas. "
              + "Modelo: asignaturas, periodos, dashboard (GradeDashboardDTO), "
              + "selectedImparticionId, selectedPeriodoId")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ALUMNO")
  @GetMapping("/notas")
  public String notas(
      @Parameter(description = "ID de la impartición seleccionada (opcional)")
          @RequestParam(required = false)
          Integer imparticionId,
      @Parameter(description = "ID del período de evaluación seleccionado (opcional)")
          @RequestParam(required = false)
          Integer periodoId,
      Principal principal,
      Model model) {
    Usuario u = resolver(principal);

    model.addAttribute("asignaturas", usuarioService.getAsignaturasAlumno(u.getId()));
    model.addAttribute("selectedImparticionId", imparticionId);

    List<PeriodoEvaluacion> periodos = usuarioService.getStudentPeriods(u.getId());
    model.addAttribute("periodos", periodos);

    if (periodoId == null && !periodos.isEmpty()) {
      periodoId = periodos.get(0).getId();
    }
    if (periodoId != null) {
      model.addAttribute("dashboard", usuarioService.getStudentGrades(u.getId(), periodoId));
      model.addAttribute("selectedPeriodoId", periodoId);
    }
    return "alumno/dashboard_notas";
  }

  @Operation(
      summary = "API — Notas detalladas de una matrícula (alumno)",
      description =
          "Devuelve el desglose completo de calificaciones de la matrícula indicada en formato JSON. "
              + "El alumno solo puede consultar sus propias matrículas: "
              + "si matriculaId no pertenece al alumno autenticado, se lanza 403.")
  @ApiResponse(
      responseCode = "200",
      description = "Calificaciones completas de la matrícula",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = TeacherStudentGradesDTO.class)))
  @ApiResponse(
      responseCode = "403",
      description = "La matrícula no pertenece al alumno autenticado")
  @ApiResponse(responseCode = "401", description = "No autenticado")
  @GetMapping("/api/matricula/{matriculaId}/notas")
  @ResponseBody
  public TeacherStudentGradesDTO getNotasApi(
      @Parameter(description = "ID de la matrícula del alumno", required = true, example = "42")
          @PathVariable
          Integer matriculaId,
      Principal principal) {
    Usuario u = resolver(principal);
    return usuarioService.getAlumnoMatriculaGrades(u.getId(), matriculaId);
  }

  private Usuario resolver(Principal principal) {
    return usuarioService
        .buscarPorCorreo(principal.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida"));
  }
}
