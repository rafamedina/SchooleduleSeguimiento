package com.tfg.schooledule.infrastructure.controller;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tfg.schooledule.infrastructure.repository.CentroRepository;
import com.tfg.schooledule.infrastructure.repository.GrupoRepository;
import com.tfg.schooledule.infrastructure.repository.ModuloRepository;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import com.tfg.schooledule.infrastructure.security.SecurityAuditLogger;
import com.tfg.schooledule.infrastructure.service.AdminImparticionService;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminImparticionController.class)
@Import(AdminImparticionControllerTest.MethodSecurityTestConfig.class)
class AdminImparticionControllerTest {

  @EnableMethodSecurity
  static class MethodSecurityTestConfig {}

  @Autowired private MockMvc mockMvc;

  @MockBean private AdminImparticionService adminImparticionService;
  @MockBean private ModuloRepository moduloRepository;
  @MockBean private GrupoRepository grupoRepository;
  @MockBean private UsuarioRepository usuarioRepository;
  @MockBean private CentroRepository centroRepository;
  @MockBean private SecurityAuditLogger securityAuditLogger;

  @Test
  void lista_sinAuth_redirige302() throws Exception {
    mockMvc
        .perform(get("/admin/imparticiones").header("Accept", "text/html"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(roles = "ALUMNO")
  void lista_conAlumno_403() throws Exception {
    mockMvc.perform(get("/admin/imparticiones")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "PROFESOR")
  void lista_conProfesor_403() throws Exception {
    mockMvc.perform(get("/admin/imparticiones")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void lista_conAdmin_200_retornaVista() throws Exception {
    when(adminImparticionService.listarTodas()).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/admin/imparticiones"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/imparticiones/lista"))
        .andExpect(model().attributeExists("imparticiones"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crearImparticion_moduloIdNull_reRenderizaFormulario() throws Exception {
    when(moduloRepository.findByActivoTrueOrderByNombreAsc()).thenReturn(Collections.emptyList());
    when(grupoRepository.findAllByOrderByCentroNombreAscNombreAsc())
        .thenReturn(Collections.emptyList());
    when(usuarioRepository.findAllProfesoresOrdenados()).thenReturn(Collections.emptyList());
    when(centroRepository.findAllByActivoTrueOrderByNombreAsc())
        .thenReturn(Collections.emptyList());

    mockMvc
        .perform(
            post("/admin/imparticiones/nuevo")
                .param("grupoId", "1")
                .param("profesorId", "2")
                .param("centroId", "1")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/imparticiones/formulario"))
        .andExpect(model().attributeHasFieldErrors("form", "moduloId"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crearImparticion_datosValidos_redirigeLista() throws Exception {
    mockMvc
        .perform(
            post("/admin/imparticiones/nuevo")
                .param("moduloId", "1")
                .param("grupoId", "1")
                .param("profesorId", "2")
                .param("centroId", "1")
                .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/imparticiones"));

    verify(adminImparticionService).crear(any());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void eliminarImparticion_conMatriculas_flashErrorYRedirecciona() throws Exception {
    doThrow(new IllegalStateException("No se puede eliminar: tiene matrículas asociadas"))
        .when(adminImparticionService)
        .eliminar(1);

    mockMvc
        .perform(post("/admin/imparticiones/1/eliminar").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/imparticiones"))
        .andExpect(flash().attributeExists("error"));
  }
}
