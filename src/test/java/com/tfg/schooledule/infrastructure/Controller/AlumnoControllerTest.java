package com.tfg.schooledule.infrastructure.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tfg.schooledule.domain.dto.AlumnoProfileDTO;
import com.tfg.schooledule.domain.dto.GradeDashboardDTO;
import com.tfg.schooledule.domain.entity.Centro;
import com.tfg.schooledule.domain.entity.Grupo;
import com.tfg.schooledule.domain.entity.Imparticion;
import com.tfg.schooledule.domain.entity.Matricula;
import com.tfg.schooledule.domain.entity.Modulo;
import com.tfg.schooledule.domain.entity.PeriodoEvaluacion;
import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.infrastructure.service.UsuarioService;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AlumnoController.class)
@Import(AlumnoControllerTest.MethodSecurityTestConfig.class)
public class AlumnoControllerTest {

  @EnableMethodSecurity
  static class MethodSecurityTestConfig {}

  @Autowired private MockMvc mockMvc;

  @MockBean private UsuarioService usuarioService;

  private Usuario buildAlumno() {
    return Usuario.builder()
        .id(1)
        .username("ana")
        .email("ana@tfg.com")
        .nombre("Ana")
        .apellidos("López")
        .build();
  }

  private Matricula buildMatricula(Integer imparticionId, String moduloNombre) {
    Centro centro = Centro.builder().id(1).nombre("IES Central").build();
    Grupo grupo = Grupo.builder().id(1).nombre("DAW1-A").build();
    Modulo modulo = Modulo.builder().id(1).nombre(moduloNombre).build();
    Imparticion imparticion =
        Imparticion.builder().id(imparticionId).modulo(modulo).grupo(grupo).centro(centro).build();
    return Matricula.builder().id(imparticionId).imparticion(imparticion).centro(centro).build();
  }

  @Test
  @WithMockUser(username = "ana@tfg.com", roles = "ALUMNO")
  public void dashboard_modelContieneAsignaturas() throws Exception {
    Usuario u = buildAlumno();
    List<Matricula> matriculas =
        List.of(buildMatricula(10, "Desarrollo Web"), buildMatricula(11, "Bases de Datos"));

    when(usuarioService.buscarPorCorreo("ana@tfg.com")).thenReturn(Optional.of(u));
    when(usuarioService.getAsignaturasAlumno(1)).thenReturn(matriculas);

    mockMvc
        .perform(get("/alumno/dashboard"))
        .andExpect(status().isOk())
        .andExpect(view().name("alumno/dashboard"))
        .andExpect(model().attributeExists("asignaturas"))
        .andExpect(model().attribute("asignaturas", matriculas));
  }

  @Test
  @WithMockUser(username = "ana@tfg.com", roles = "ALUMNO")
  public void dashboard_modelContieneAlumnoNombre() throws Exception {
    Usuario u = buildAlumno();
    when(usuarioService.buscarPorCorreo("ana@tfg.com")).thenReturn(Optional.of(u));
    when(usuarioService.getAsignaturasAlumno(1)).thenReturn(List.of());

    mockMvc
        .perform(get("/alumno/dashboard"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("alumnoNombre", "Ana López"));
  }

  @Test
  @WithMockUser(username = "ana@tfg.com", roles = "ALUMNO")
  public void notas_sinParams_eligePrimerPeriodoAutomaticamente() throws Exception {
    Usuario u = buildAlumno();
    PeriodoEvaluacion p1 = PeriodoEvaluacion.builder().id(7).nombre("1er Trimestre").build();
    PeriodoEvaluacion p2 = PeriodoEvaluacion.builder().id(8).nombre("2º Trimestre").build();
    GradeDashboardDTO dashboard = new GradeDashboardDTO("1er Trimestre", new HashMap<>());

    when(usuarioService.buscarPorCorreo("ana@tfg.com")).thenReturn(Optional.of(u));
    when(usuarioService.getAsignaturasAlumno(1)).thenReturn(List.of());
    when(usuarioService.getStudentPeriods(1)).thenReturn(List.of(p1, p2));
    when(usuarioService.getStudentGrades(1, 7)).thenReturn(dashboard);

    mockMvc
        .perform(get("/alumno/notas"))
        .andExpect(status().isOk())
        .andExpect(view().name("alumno/dashboard_notas"))
        .andExpect(model().attribute("selectedPeriodoId", 7))
        .andExpect(model().attribute("dashboard", dashboard));
  }

  @Test
  @WithMockUser(username = "ana@tfg.com", roles = "ALUMNO")
  public void notas_conImparticionId_colocaSelectedImparticionIdEnModel() throws Exception {
    Usuario u = buildAlumno();
    when(usuarioService.buscarPorCorreo("ana@tfg.com")).thenReturn(Optional.of(u));
    when(usuarioService.getAsignaturasAlumno(1)).thenReturn(List.of(buildMatricula(42, "Prog")));
    when(usuarioService.getStudentPeriods(1)).thenReturn(List.of());

    mockMvc
        .perform(get("/alumno/notas").param("imparticionId", "42"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("selectedImparticionId", 42))
        .andExpect(model().attributeExists("asignaturas"));
  }

  @Test
  @WithMockUser(username = "ana@tfg.com", roles = "ALUMNO")
  public void perfil_200_yModelContienePerfil() throws Exception {
    Usuario u = buildAlumno();
    AlumnoProfileDTO profile =
        new AlumnoProfileDTO(
            1, "ana", "Ana", "López", "ana@tfg.com", "IES Central", "DAW1-A", "2025/2026");

    when(usuarioService.buscarPorCorreo("ana@tfg.com")).thenReturn(Optional.of(u));
    when(usuarioService.getAlumnoProfile(1)).thenReturn(profile);

    mockMvc
        .perform(get("/alumno/perfil"))
        .andExpect(status().isOk())
        .andExpect(view().name("alumno/perfil"))
        .andExpect(model().attribute("profile", profile));
  }

  @Test
  @WithMockUser(username = "juan@tfg.com", roles = "PROFESOR")
  public void rolProfesor_accedeDashboardAlumno_retorna403() throws Exception {
    mockMvc.perform(get("/alumno/dashboard")).andExpect(status().isForbidden());
  }
}
