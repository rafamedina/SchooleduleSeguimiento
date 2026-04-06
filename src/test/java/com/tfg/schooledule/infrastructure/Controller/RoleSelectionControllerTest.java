package com.tfg.schooledule.infrastructure.Controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RoleSelectionControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  @WithMockUser(roles = "USER")
  void testRoleSelectionPageIsAccessible() throws Exception {
    mockMvc
        .perform(get("/seleccionar-rol"))
        .andExpect(status().isOk())
        .andExpect(view().name("seleccionar-rol"));
  }
}
