package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.domain.dto.TeacherStudentGradesDTO;
import com.tfg.schooledule.domain.dto.TutorImparticionDTO;
import com.tfg.schooledule.domain.entity.Grupo;
import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.infrastructure.service.TutorService;
import com.tfg.schooledule.infrastructure.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Tutor")
@Controller
@RequestMapping("/tutor")
@PreAuthorize("hasRole('PROFESOR')")
@SuppressWarnings("java:S6833")
public class TutorController {

  private final TutorService tutorService;
  private final UsuarioService usuarioService;

  public TutorController(TutorService tutorService, UsuarioService usuarioService) {
    this.tutorService = tutorService;
    this.usuarioService = usuarioService;
  }

  @Operation(summary = "Grupos del tutor — lista de grupos de los que el profesor es tutor")
  @GetMapping("/grupos")
  public String grupos(Principal principal, Model model) {
    Usuario tutor = resolveTutor(principal);
    model.addAttribute("grupos", tutorService.getGruposDeTutor(tutor.getId()));
    model.addAttribute("teacherName", tutor.getNombre() + " " + tutor.getApellidos());
    return "tutor/grupos";
  }

  @Operation(summary = "Imparticiones de un grupo — vista desde el rol tutor")
  @GetMapping("/grupo/{grupoId}/imparticiones")
  public String imparticiones(
      @PathVariable @Positive Integer grupoId, Principal principal, Model model) {
    Usuario tutor = resolveTutor(principal);
    tutorService.validateTutorOwnership(tutor.getId(), grupoId);
    List<TutorImparticionDTO> imparticiones =
        tutorService.getImparticionesByGrupo(tutor.getId(), grupoId);
    Grupo grupo = tutorService.getGrupoOrFail(grupoId);
    model.addAttribute("imparticiones", imparticiones);
    model.addAttribute("grupoNombre", grupo.getNombre());
    model.addAttribute("grupoId", grupoId);
    return "tutor/imparticiones";
  }

  @Operation(
      summary = "Alumnos de una impartición — RO si no es también el profesor, redirect si lo es")
  @GetMapping("/grupo/{grupoId}/alumnos/{imparticionId}")
  public String alumnos(
      @PathVariable @Positive Integer grupoId,
      @PathVariable @Positive Integer imparticionId,
      Principal principal,
      Model model) {
    Usuario tutor = resolveTutor(principal);
    tutorService.validateTutorOwnership(tutor.getId(), grupoId);

    List<TutorImparticionDTO> imparticiones =
        tutorService.getImparticionesByGrupo(tutor.getId(), grupoId);
    TutorImparticionDTO imp =
        imparticiones.stream()
            .filter(i -> i.imparticionId().equals(imparticionId))
            .findFirst()
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Impartición no pertenece al grupo"));

    if (imp.puedeEditarNotas()) {
      return "redirect:/profe/imparticion/" + imparticionId + "/alumnos";
    }

    Grupo grupo = tutorService.getGrupoOrFail(grupoId);
    model.addAttribute(
        "alumnos", tutorService.buildRosterAsTutor(tutor.getId(), grupoId, imparticionId));
    model.addAttribute("imparticionLabel", grupo.getNombre() + " · " + imp.moduloNombre());
    model.addAttribute("grupoId", grupoId);
    model.addAttribute("imparticionId", imparticionId);
    return "tutor/alumnos";
  }

  @Operation(summary = "API — Notas de una matrícula (solo lectura, contexto tutor)")
  @GetMapping("/api/matricula/{matriculaId}/notas")
  @ResponseBody
  public TeacherStudentGradesDTO getNotas(
      @PathVariable @Positive Integer matriculaId, Principal principal) {
    Usuario tutor = resolveTutor(principal);
    return tutorService.getStudentGradesAsTutor(tutor.getId(), matriculaId);
  }

  private Usuario resolveTutor(Principal principal) {
    return usuarioService
        .buscarPorCorreo(principal.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
  }
}
