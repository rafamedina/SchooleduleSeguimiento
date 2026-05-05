package com.tfg.schooledule.infrastructure.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void testAdminPathIsProtected() throws Exception {
    mockMvc.perform(get("/admin/dashboard")).andExpect(status().is3xxRedirection());
  }

  @Test
  @WithMockUser(roles = "ALUMNO")
  void testAdminPathIsForbiddenForAlumno() throws Exception {
    mockMvc.perform(get("/admin/dashboard")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void testAdminPathIsAccessibleForAdmin() throws Exception {
    mockMvc.perform(get("/admin/dashboard")).andExpect(status().isOk());
  }

  @Test
  void testPublicPathsAreAccessible() throws Exception {
    mockMvc.perform(get("/login")).andExpect(status().isOk());
  }

  @Test
  void testLoginPostRequiresCsrf() throws Exception {
    mockMvc
        .perform(post("/login").param("username", "test").param("password", "test"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ALUMNO")
  void responseContiensCspConFrameAncestors() throws Exception {
    mockMvc
        .perform(get("/alumno/dashboard"))
        .andExpect(
            header()
                .string(
                    "Content-Security-Policy",
                    org.hamcrest.Matchers.containsString("frame-ancestors 'none'")));
  }

  @Test
  @WithMockUser(roles = "ALUMNO")
  void responseContieneReferrerPolicy() throws Exception {
    mockMvc
        .perform(get("/alumno/dashboard"))
        .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"));
  }

  @Test
  @WithMockUser(roles = "ALUMNO")
  void responseContieneXContentTypeOptions() throws Exception {
    mockMvc
        .perform(get("/alumno/dashboard"))
        .andExpect(header().string("X-Content-Type-Options", "nosniff"));
  }

  @Test
  void snapAdminDenyAll_sinAuth_retorna3xxORechazo() throws Exception {
    mockMvc.perform(get("/snap-admin/")).andExpect(status().is3xxRedirection());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void snapAdminDenyAll_conAdmin_retorna403() throws Exception {
    mockMvc.perform(get("/snap-admin/")).andExpect(status().isForbidden());
  }
}
