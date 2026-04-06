package com.tfg.schooledule.infrastructure.Controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tfg.schooledule.domain.DTO.AlumnoProfileDTO;
import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.infrastructure.Service.UsuarioService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AlumnoController.class)
public class AlumnoControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private UsuarioService usuarioService;

  @Test
  @WithMockUser(username = "testalumno", roles = "ALUMNO")
  public void testPerfilAlumno_Success() throws Exception {
    Usuario usuario = Usuario.builder().id(1).username("testalumno").build();
    AlumnoProfileDTO profile =
        AlumnoProfileDTO.builder()
            .nombre("Test")
            .apellidos("Alumno")
            .email("test@alumno.com")
            .centroNombre("Centro Test")
            .build();

    when(usuarioService.buscarPorNombreUsuario("testalumno")).thenReturn(Optional.of(usuario));
    when(usuarioService.getAlumnoProfile(1)).thenReturn(profile);

    mockMvc
        .perform(get("/alumno/perfil"))
        .andExpect(status().isOk())
        .andExpect(view().name("alumno/perfil"))
        .andExpect(model().attributeExists("profile"))
        .andExpect(model().attribute("profile", profile));
  }

  @Test
  @WithMockUser(username = "testalumno", roles = "ALUMNO")
  public void testDashboardNotas_Success() throws Exception {
    Usuario usuario = Usuario.builder().id(1).username("testalumno").build();
    com.tfg.schooledule.domain.DTO.GradeDashboardDTO dashboard =
        com.tfg.schooledule.domain.DTO.GradeDashboardDTO.builder()
            .periodoNombre("T1")
            .gradesByModulo(new java.util.HashMap<>())
            .build();

    when(usuarioService.buscarPorNombreUsuario("testalumno")).thenReturn(Optional.of(usuario));
    when(usuarioService.getStudentPeriods(1)).thenReturn(java.util.List.of());
    when(usuarioService.getStudentGrades(1, 1)).thenReturn(dashboard);

    mockMvc
        .perform(get("/alumno/notas").param("periodoId", "1"))
        .andExpect(status().isOk())
        .andExpect(view().name("alumno/dashboard_notas"))
        .andExpect(model().attributeExists("dashboard"))
        .andExpect(model().attribute("dashboard", dashboard));
  }

  @Test
  @WithMockUser(username = "testalumno", roles = "ALUMNO")
  public void testDashboardAlumno_Success() throws Exception {
    mockMvc
        .perform(get("/alumno/dashboard"))
        .andExpect(status().isOk())
        .andExpect(view().name("alumno/menuAlumno"))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"sidebar\"")));
  }
}
