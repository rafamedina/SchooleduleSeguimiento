package com.tfg.schooledule.infrastructure.Controller;

import com.tfg.schooledule.domain.DTO.AlumnoProfileDTO;
import com.tfg.schooledule.domain.DTO.GradeDashboardDTO;
import com.tfg.schooledule.domain.entity.PeriodoEvaluacion;
import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.infrastructure.Service.UsuarioService;
import java.security.Principal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/alumno")
public class AlumnoController {

  @Autowired private UsuarioService usuarioService;

  @GetMapping("/dashboard")
  public String panelAlumno() {
    return "alumno/menuAlumno";
  }

  @GetMapping("/perfil")
  public String perfilAlumno(Principal principal, Model model) {
    String username = principal.getName();
    Usuario usuario =
        usuarioService
            .buscarPorNombreUsuario(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    AlumnoProfileDTO profile = usuarioService.getAlumnoProfile(usuario.getId());
    model.addAttribute("profile", profile);
    return "alumno/perfil";
  }

  @GetMapping("/notas")
  public String dashboardNotas(
      @RequestParam(required = false) Integer periodoId, Principal principal, Model model) {
    String username = principal.getName();
    Usuario usuario =
        usuarioService
            .buscarPorNombreUsuario(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    List<PeriodoEvaluacion> periodos = usuarioService.getStudentPeriods(usuario.getId());
    model.addAttribute("periodos", periodos);

    if (periodoId == null && !periodos.isEmpty()) {
      periodoId = periodos.get(0).getId();
    }

    if (periodoId != null) {
      GradeDashboardDTO dashboard = usuarioService.getStudentGrades(usuario.getId(), periodoId);
      model.addAttribute("dashboard", dashboard);
      model.addAttribute("selectedPeriodoId", periodoId);
    }

    return "alumno/dashboard_notas";
  }
}
