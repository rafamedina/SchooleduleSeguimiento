package com.tfg.schooledule.infrastructure.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tfg.schooledule.domain.dto.DashboardStatsDTO;
import com.tfg.schooledule.infrastructure.security.SecurityAuditLogger;
import com.tfg.schooledule.infrastructure.service.AdminUsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminController.class)
@Import(AdminControllerTest.MethodSecurityTestConfig.class)
class AdminControllerTest {

  @EnableMethodSecurity
  static class MethodSecurityTestConfig {}

  @Autowired private MockMvc mockMvc;

  @MockBean private AdminUsuarioService adminUsuarioService;
  @MockBean private SecurityAuditLogger securityAuditLogger;

  @Test
  void dashboard_sinAuth_redirige302() throws Exception {
    mockMvc
        .perform(get("/admin/dashboard").header("Accept", "text/html"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(roles = "ALUMNO")
  void dashboard_conAlumno_403() throws Exception {
    mockMvc.perform(get("/admin/dashboard")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "PROFESOR")
  void dashboard_conProfesor_403() throws Exception {
    mockMvc.perform(get("/admin/dashboard")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void dashboard_conAdmin_200_contieneStats() throws Exception {
    DashboardStatsDTO stats = new DashboardStatsDTO(10, 8, 2, 1, 5, 3, 2, "2024/2025", 15, 4);
    when(adminUsuarioService.getStats()).thenReturn(stats);

    mockMvc
        .perform(get("/admin/dashboard"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/dashboard"))
        .andExpect(model().attribute("stats", stats));
  }
}
