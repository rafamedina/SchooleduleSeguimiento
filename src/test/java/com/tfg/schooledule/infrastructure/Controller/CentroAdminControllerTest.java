package com.tfg.schooledule.infrastructure.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tfg.schooledule.domain.dto.*;
import com.tfg.schooledule.domain.entity.*;
import com.tfg.schooledule.infrastructure.repository.CursoAcademicoRepository;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import com.tfg.schooledule.infrastructure.security.SecurityAuditLogger;
import com.tfg.schooledule.infrastructure.service.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CentroAdminController.class)
@Import(CentroAdminControllerTest.MethodSecurityTestConfig.class)
class CentroAdminControllerTest {

  @EnableMethodSecurity
  static class MethodSecurityTestConfig {}

  @Autowired private MockMvc mockMvc;

  @MockBean private CentroAdminDashboardService dashboardService;
  @MockBean private CentroAdminContextService centroAdminContextService;
  @MockBean private UsuarioRepository usuarioRepository;
  @MockBean private UsuarioService usuarioService;
  @MockBean private SecurityAuditLogger securityAuditLogger;
  @MockBean private CursoAcademicoRepository cursoAcademicoRepository;
  @MockBean private AdminCursoActivoService adminCursoActivoService;

  // --- Dashboard ---

  @Test
  void dashboard_redirigeLLogin_sinAutenticacion() throws Exception {
    mockMvc
        .perform(get("/centro-admin/dashboard").header("Accept", "text/html"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(roles = "ALUMNO")
  void dashboard_403_conRolAlumno() throws Exception {
    mockMvc.perform(get("/centro-admin/dashboard")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void dashboard_403_conRolAdminGlobal() throws Exception {
    mockMvc.perform(get("/centro-admin/dashboard")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "admin@getafe.es", roles = "ADMIN_CENTRO")
  void dashboard_ok_conRolAdminCentro() throws Exception {
    Usuario user =
        Usuario.builder()
            .id(10)
            .email("admin@getafe.es")
            .nombre("Admin")
            .apellidos("Getafe")
            .build();
    when(usuarioRepository.findUsuarioByEmail("admin@getafe.es")).thenReturn(Optional.of(user));

    Centro c = new Centro();
    c.setId(1);
    c.setNombre("IES Getafe");
    var stats = new CentroAdminStatsDTO(1, 3, 5, 20);
    when(dashboardService.buildStats(10)).thenReturn(stats);

    mockMvc
        .perform(get("/centro-admin/dashboard"))
        .andExpect(status().isOk())
        .andExpect(view().name("centro-admin/dashboard"));
  }
}
