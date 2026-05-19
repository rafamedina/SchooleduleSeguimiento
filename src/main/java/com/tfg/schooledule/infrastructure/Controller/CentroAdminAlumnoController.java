package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.domain.dto.AdminMatriculaFormDTO;
import com.tfg.schooledule.domain.enums.EstadoMatricula;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import com.tfg.schooledule.infrastructure.service.CentroAdminAlumnoService;
import com.tfg.schooledule.infrastructure.service.CentroAdminImparticionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.security.Principal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/centro-admin/alumnos")
@PreAuthorize("hasRole('ADMIN_CENTRO')")
@SuppressWarnings("java:S1075")
public class CentroAdminAlumnoController {

  private static final String ATTR_ALUMNO = "alumno";
  private static final String VIEW_MATRICULA_FORM = "centro-admin/alumnos/matricula-formulario";
  private static final String PATH_MATRICULAS = "/matriculas";
  private static final String REDIRECT_ALUMNOS = "redirect:/centro-admin/alumnos/";

  private final CentroAdminAlumnoService alumnoService;
  private final CentroAdminImparticionService imparticionService;
  private final UsuarioRepository usuarioRepository;

  public CentroAdminAlumnoController(
      CentroAdminAlumnoService alumnoService,
      CentroAdminImparticionService imparticionService,
      UsuarioRepository usuarioRepository) {
    this.alumnoService = alumnoService;
    this.imparticionService = imparticionService;
    this.usuarioRepository = usuarioRepository;
  }

  @GetMapping
  public String lista(Principal principal, Model model) {
    int adminId = resolveId(principal);
    model.addAttribute("alumnos", alumnoService.listarAlumnosDeCentros(adminId));
    return "centro-admin/alumnos/lista";
  }

  @GetMapping("/{alumnoId}/matriculas")
  public String matriculas(
      Principal principal, @PathVariable @Positive Integer alumnoId, Model model) {
    int adminId = resolveId(principal);
    model.addAttribute(ATTR_ALUMNO, alumnoService.obtenerAlumno(adminId, alumnoId));
    model.addAttribute("matriculas", alumnoService.listarMatriculas(adminId, alumnoId));
    return "centro-admin/alumnos/matriculas";
  }

  @GetMapping("/{alumnoId}/matriculas/nueva")
  public String nuevaMatricula(
      Principal principal, @PathVariable @Positive Integer alumnoId, Model model) {
    int adminId = resolveId(principal);
    model.addAttribute(ATTR_ALUMNO, alumnoService.obtenerAlumno(adminId, alumnoId));
    model.addAttribute("form", new AdminMatriculaFormDTO());
    cargarImparticionesYEstados(model, adminId);
    return VIEW_MATRICULA_FORM;
  }

  @PostMapping("/{alumnoId}/matriculas/nueva")
  public String crearMatricula(
      Principal principal,
      @PathVariable @Positive Integer alumnoId,
      @Valid @ModelAttribute("form") AdminMatriculaFormDTO form,
      BindingResult bindingResult,
      Model model) {
    int adminId = resolveId(principal);
    if (bindingResult.hasErrors()) {
      model.addAttribute(ATTR_ALUMNO, alumnoService.obtenerAlumno(adminId, alumnoId));
      cargarImparticionesYEstados(model, adminId);
      return VIEW_MATRICULA_FORM;
    }
    try {
      alumnoService.crearMatricula(adminId, alumnoId, form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute(ATTR_ALUMNO, alumnoService.obtenerAlumno(adminId, alumnoId));
      model.addAttribute("error", ex.getMessage());
      cargarImparticionesYEstados(model, adminId);
      return VIEW_MATRICULA_FORM;
    }
    return REDIRECT_ALUMNOS + alumnoId + PATH_MATRICULAS;
  }

  @GetMapping("/{alumnoId}/matriculas/{id}/editar")
  public String editarMatricula(
      Principal principal,
      @PathVariable @Positive Integer alumnoId,
      @PathVariable @Positive Integer id,
      Model model) {
    int adminId = resolveId(principal);
    model.addAttribute(ATTR_ALUMNO, alumnoService.obtenerAlumno(adminId, alumnoId));
    model.addAttribute("form", alumnoService.obtenerMatriculaParaEditar(adminId, id));
    cargarImparticionesYEstados(model, adminId);
    return VIEW_MATRICULA_FORM;
  }

  @PostMapping("/{alumnoId}/matriculas/{id}/editar")
  public String actualizarMatricula(
      Principal principal,
      @PathVariable @Positive Integer alumnoId,
      @PathVariable @Positive Integer id,
      @Valid @ModelAttribute("form") AdminMatriculaFormDTO form,
      BindingResult bindingResult,
      Model model) {
    int adminId = resolveId(principal);
    if (bindingResult.hasErrors()) {
      model.addAttribute(ATTR_ALUMNO, alumnoService.obtenerAlumno(adminId, alumnoId));
      cargarImparticionesYEstados(model, adminId);
      return VIEW_MATRICULA_FORM;
    }
    alumnoService.actualizarMatricula(adminId, id, form);
    return REDIRECT_ALUMNOS + alumnoId + PATH_MATRICULAS;
  }

  @PostMapping("/{alumnoId}/matriculas/{id}/eliminar")
  public String eliminarMatricula(
      Principal principal,
      @PathVariable @Positive Integer alumnoId,
      @PathVariable @Positive Integer id,
      RedirectAttributes redirectAttributes) {
    int adminId = resolveId(principal);
    try {
      alumnoService.eliminarMatricula(adminId, id);
    } catch (IllegalStateException ex) {
      redirectAttributes.addFlashAttribute("error", ex.getMessage());
    }
    return REDIRECT_ALUMNOS + alumnoId + PATH_MATRICULAS;
  }

  private void cargarImparticionesYEstados(Model model, int adminId) {
    model.addAttribute("imparticiones", imparticionService.listarImparticionesDeCentros(adminId));
    model.addAttribute("estadosMatricula", EstadoMatricula.values());
  }

  int resolveId(Principal principal) {
    return usuarioRepository
        .findUsuarioByEmail(principal.getName())
        .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"))
        .getId();
  }
}
