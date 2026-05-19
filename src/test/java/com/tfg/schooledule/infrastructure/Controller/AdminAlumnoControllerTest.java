package com.tfg.schooledule.infrastructure.controller;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tfg.schooledule.domain.dto.AdminMatriculaFormDTO;
import com.tfg.schooledule.domain.entity.Rol;
import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.domain.enums.EstadoMatricula;
import com.tfg.schooledule.infrastructure.repository.CentroRepository;
import com.tfg.schooledule.infrastructure.repository.CursoAcademicoRepository;
import com.tfg.schooledule.infrastructure.repository.GrupoRepository;
import com.tfg.schooledule.infrastructure.repository.ImparticionRepository;
import com.tfg.schooledule.infrastructure.security.SecurityAuditLogger;
import com.tfg.schooledule.infrastructure.service.AdminAlumnoService;
import com.tfg.schooledule.infrastructure.service.AdminCursoActivoService;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminAlumnoController.class)
@Import(AdminAlumnoControllerTest.MethodSecurityTestConfig.class)
class AdminAlumnoControllerTest {

  @EnableMethodSecurity
  static class MethodSecurityTestConfig {}

  @Autowired private MockMvc mockMvc;

  @MockBean private AdminAlumnoService adminAlumnoService;
  @MockBean private ImparticionRepository imparticionRepository;
  @MockBean private CentroRepository centroRepository;
  @MockBean private GrupoRepository grupoRepository;
  @MockBean private CursoAcademicoRepository cursoAcademicoRepository;
  @MockBean private AdminCursoActivoService adminCursoActivoService;
  @MockBean private SecurityAuditLogger securityAuditLogger;

  @Test
  void lista_sinAuth_redirige302() throws Exception {
    mockMvc
        .perform(get("/admin/alumnos").header("Accept", "text/html"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(roles = "ALUMNO")
  void lista_conAlumno_403() throws Exception {
    mockMvc.perform(get("/admin/alumnos")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "PROFESOR")
  void lista_conProfesor_403() throws Exception {
    mockMvc.perform(get("/admin/alumnos")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void lista_conAdmin_200_retornaVista() throws Exception {
    when(adminAlumnoService.listarFiltrado(any())).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/admin/alumnos"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/alumnos/lista"))
        .andExpect(model().attributeExists("alumnos"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void matriculas_conAdmin_200_retornaVista() throws Exception {
    Usuario alumno =
        Usuario.builder()
            .id(1)
            .nombre("Juan")
            .apellidos("García")
            .email("j@t.com")
            .roles(Set.of(Rol.builder().id(1).nombre("ALUMNO").build()))
            .build();
    when(adminAlumnoService.obtenerAlumno(1)).thenReturn(alumno);
    when(adminAlumnoService.listarMatriculas(1)).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/admin/alumnos/1/matriculas"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/alumnos/matriculas"))
        .andExpect(model().attributeExists("alumno", "matriculas"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void nuevaMatricula_conAdmin_200_retornaFormulario() throws Exception {
    Usuario alumno =
        Usuario.builder()
            .id(1)
            .nombre("Juan")
            .apellidos("García")
            .email("j@t.com")
            .roles(Set.of(Rol.builder().id(1).nombre("ALUMNO").build()))
            .build();
    when(adminAlumnoService.obtenerAlumno(1)).thenReturn(alumno);
    when(imparticionRepository.findAllByOrderByGrupoNombreAscModuloNombreAsc())
        .thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/admin/alumnos/1/matriculas/nueva"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/alumnos/matricula-formulario"))
        .andExpect(model().attributeExists("alumno", "form"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crearMatricula_datosValidos_redirige() throws Exception {
    mockMvc
        .perform(
            post("/admin/alumnos/1/matriculas/nueva")
                .param("imparticionId", "1")
                .param("estado", "ACTIVA")
                .param("esRepetidor", "false")
                .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/alumnos/1/matriculas"));

    verify(adminAlumnoService).crearMatricula(eq(1), any());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void editarMatricula_conAdmin_200_retornaFormulario() throws Exception {
    Usuario alumno =
        Usuario.builder()
            .id(1)
            .nombre("Juan")
            .apellidos("García")
            .email("j@t.com")
            .roles(Set.of(Rol.builder().id(1).nombre("ALUMNO").build()))
            .build();
    when(adminAlumnoService.obtenerAlumno(1)).thenReturn(alumno);
    when(adminAlumnoService.obtenerMatriculaParaEditar(1))
        .thenReturn(new AdminMatriculaFormDTO(1, 1, EstadoMatricula.ACTIVA, false));
    when(imparticionRepository.findAllByOrderByGrupoNombreAscModuloNombreAsc())
        .thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/admin/alumnos/1/matriculas/1/editar"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/alumnos/matricula-formulario"))
        .andExpect(model().attributeExists("alumno", "form"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void actualizarMatricula_datosValidos_redirige() throws Exception {
    mockMvc
        .perform(
            post("/admin/alumnos/1/matriculas/1/editar")
                .param("imparticionId", "1")
                .param("estado", "ACTIVA")
                .param("esRepetidor", "false")
                .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/alumnos/1/matriculas"));

    verify(adminAlumnoService).actualizarMatricula(eq(1), any());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void eliminarMatricula_exito_redirige() throws Exception {
    mockMvc
        .perform(post("/admin/alumnos/1/matriculas/1/eliminar").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/alumnos/1/matriculas"));

    verify(adminAlumnoService).eliminarMatricula(1);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crearMatricula_imparticionNula_reRenderizaFormulario() throws Exception {
    Usuario alumno =
        Usuario.builder()
            .id(1)
            .nombre("Juan")
            .apellidos("García")
            .email("j@t.com")
            .roles(Set.of(Rol.builder().id(1).nombre("ALUMNO").build()))
            .build();
    when(adminAlumnoService.obtenerAlumno(1)).thenReturn(alumno);
    when(imparticionRepository.findAllByOrderByGrupoNombreAscModuloNombreAsc())
        .thenReturn(Collections.emptyList());

    mockMvc
        .perform(
            post("/admin/alumnos/1/matriculas/nueva")
                .param("estado", "ACTIVA")
                .param("esRepetidor", "false")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/alumnos/matricula-formulario"))
        .andExpect(model().attributeHasFieldErrors("form", "imparticionId"));
  }
}
