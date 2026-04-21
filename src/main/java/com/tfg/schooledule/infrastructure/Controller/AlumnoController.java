package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.domain.dto.TeacherStudentGradesDTO;
import com.tfg.schooledule.domain.entity.PeriodoEvaluacion;
import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.infrastructure.service.UsuarioService;
import java.security.Principal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/alumno")
@PreAuthorize("hasRole('ALUMNO')")
public class AlumnoController {

  @Autowired private UsuarioService usuarioService;

  @GetMapping("/dashboard")
  public String dashboard(Principal principal, Model model) {
    Usuario u = resolver(principal);
    model.addAttribute("alumnoNombre", u.getNombre() + " " + u.getApellidos());
    model.addAttribute("asignaturas", usuarioService.getAsignaturasAlumno(u.getId()));
    return "alumno/dashboard";
  }

  @GetMapping("/perfil")
  public String perfil(Principal principal, Model model) {
    Usuario u = resolver(principal);
    model.addAttribute("profile", usuarioService.getAlumnoProfile(u.getId()));
    return "alumno/perfil";
  }

  @GetMapping("/notas")
  public String notas(
      @RequestParam(required = false) Integer imparticionId,
      @RequestParam(required = false) Integer periodoId,
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

  @GetMapping("/api/matricula/{matriculaId}/notas")
  @ResponseBody
  public TeacherStudentGradesDTO getNotasApi(
      @PathVariable Integer matriculaId, Principal principal) {
    Usuario u = resolver(principal);
    return usuarioService.getAlumnoMatriculaGrades(u.getId(), matriculaId);
  }

  private Usuario resolver(Principal principal) {
    return usuarioService
        .buscarPorCorreo(principal.getName())
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
  }
}
