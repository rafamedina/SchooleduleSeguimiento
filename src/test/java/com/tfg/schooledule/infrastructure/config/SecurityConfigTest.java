package com.tfg.schooledule.infrastructure.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    // Assuming /admin/dashboard exists and returns 200 or something else than 403
    mockMvc.perform(get("/admin/dashboard")).andExpect(status().isOk());
  }

  @Test
  void testPublicPathsAreAccessible() throws Exception {
    mockMvc.perform(get("/login")).andExpect(status().isOk());
  }

  @Test
  void testLoginPostRequiresCsrf() throws Exception {
    // This should fail with 403 Forbidden because CSRF will be enabled
    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/login")
                .param("username", "test")
                .param("password", "test"))
        .andExpect(status().isForbidden());
  }
}
