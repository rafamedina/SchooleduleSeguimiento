package com.tfg.schooledule.infrastructure.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfg.schooledule.domain.dto.GradeUpsertRequest;
import com.tfg.schooledule.domain.dto.TeacherCenterDTO;
import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.infrastructure.service.TeacherDashboardService;
import com.tfg.schooledule.infrastructure.service.UsuarioService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integración completa: SecurityConfig + GlobalApiExceptionHandler + ProfeController. El servicio
 * se mockea para evitar la incompatibilidad de NAMED_ENUM con H2.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProfeControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  // Mocks para aislar el acceso a la BD
  @MockBean private TeacherDashboardService teacherService;
  @MockBean private UsuarioService usuarioService;

  private Usuario profe1;

  @BeforeEach
  void setUp() {
    profe1 =
        Usuario.builder()
            .id(2)
            .username("profe1")
            .email("juan@tfg.com")
            .nombre("Juan")
            .apellidos("Garcia")
            .build();

    when(usuarioService.buscarPorCorreo("juan@tfg.com")).thenReturn(Optional.of(profe1));
    when(teacherService.getCentersForTeacher(profe1))
        .thenReturn(List.of(new TeacherCenterDTO(1, "IES Central", "Madrid", 2L)));
  }

  // ── Seguridad por rol ─────────────────────────────────────────────────────

  @Test
  @WithMockUser(roles = "ALUMNO")
  void rolAlumno_accedeAProfe_retorna403() throws Exception {
    // Un usuario con ROLE_ALUMNO no puede entrar en /profe/**
    mockMvc.perform(get("/profe/dashboard")).andExpect(status().isForbidden());
  }

  @Test
  void sinAutenticar_accedeAProfe_redireccionALogin() throws Exception {
    // Sin autenticación Spring Security redirige al login
    mockMvc.perform(get("/profe/dashboard")).andExpect(status().is3xxRedirection());
  }

  @Test
  @WithMockUser(username = "juan@tfg.com", roles = "PROFESOR")
  void rolProfesor_accedeDashboard_retorna200() throws Exception {
    mockMvc
        .perform(get("/profe/dashboard"))
        .andExpect(status().isOk())
        .andExpect(view().name("profe/dashboard"));
  }

  // ── GlobalApiExceptionHandler: AccessDeniedException → 403 JSON ──────────

  @Test
  @WithMockUser(username = "juan@tfg.com", roles = "PROFESOR")
  void getNotas_propietarioDistinto_retorna403Json() throws Exception {
    // El servicio lanza AccessDeniedException si la matrícula no pertenece al profesor
    when(teacherService.getStudentGrades(2, 99))
        .thenThrow(new AccessDeniedException("no autorizado"));

    mockMvc
        .perform(get("/profe/api/matricula/99/notas").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("forbidden"));
  }

  // ── GlobalApiExceptionHandler: IllegalStateException → 409 JSON ──────────

  @Test
  @WithMockUser(username = "juan@tfg.com", roles = "PROFESOR")
  void postNotas_periodoCerrado_retorna409Json() throws Exception {
    GradeUpsertRequest req =
        new GradeUpsertRequest(
            1, List.of(new GradeUpsertRequest.Entry(1, new BigDecimal("8.00"), null)));

    // El servicio detecta el periodo cerrado antes de persistir
    when(teacherService.upsertGrades(2, "juan@tfg.com", req))
        .thenThrow(new IllegalStateException("El periodo está cerrado"));

    mockMvc
        .perform(
            post("/profe/api/matricula/1/notas")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isConflict())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("conflict"))
        .andExpect(jsonPath("$.message").value("El periodo está cerrado"));
  }

  // ── Validación de matriculaId path vs body ────────────────────────────────

  @Test
  @WithMockUser(username = "juan@tfg.com", roles = "PROFESOR")
  void postNotas_matriculaIdNoCoincide_retorna400() throws Exception {
    // body.matriculaId = 99, path = 1 → mismatch → 400
    GradeUpsertRequest req =
        new GradeUpsertRequest(
            99, List.of(new GradeUpsertRequest.Entry(1, new BigDecimal("7.00"), null)));

    mockMvc
        .perform(
            post("/profe/api/matricula/1/notas")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isBadRequest());
  }
}
