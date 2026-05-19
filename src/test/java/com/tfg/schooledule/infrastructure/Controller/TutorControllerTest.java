package com.tfg.schooledule.infrastructure.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tfg.schooledule.domain.dto.*;
import com.tfg.schooledule.domain.entity.*;
import com.tfg.schooledule.infrastructure.repository.CursoAcademicoRepository;
import com.tfg.schooledule.infrastructure.security.SecurityAuditLogger;
import com.tfg.schooledule.infrastructure.service.AdminCursoActivoService;
import com.tfg.schooledule.infrastructure.service.TutorService;
import com.tfg.schooledule.infrastructure.service.UsuarioService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TutorController.class)
@Import(TutorControllerTest.MethodSecurityTestConfig.class)
class TutorControllerTest {

  @EnableMethodSecurity
  static class MethodSecurityTestConfig {}

  @Autowired private MockMvc mockMvc;

  @MockBean private TutorService tutorService;
  @MockBean private UsuarioService usuarioService;
  @MockBean private SecurityAuditLogger securityAuditLogger;
  @MockBean private CursoAcademicoRepository cursoAcademicoRepository;
  @MockBean private AdminCursoActivoService adminCursoActivoService;

  private Usuario buildTutor() {
    return Usuario.builder()
        .id(1)
        .username("ana@tfg.com")
        .email("ana@tfg.com")
        .nombre("Ana")
        .apellidos("García")
        .build();
  }

  // 5.1.1 — redirige a login sin auth
  @Test
  void grupos_redirigeLLogin_sinAutenticacion() throws Exception {
    mockMvc
        .perform(get("/tutor/grupos").header("Accept", "text/html"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  // 5.1.2 — 403 rol incorrecto
  @Test
  @WithMockUser(username = "alumno@tfg.com", roles = "ALUMNO")
  void grupos_403_conRolAlumno() throws Exception {
    mockMvc.perform(get("/tutor/grupos")).andExpect(status().isForbidden());
  }

  // 5.1.3 — 200 con rol PROFESOR
  @Test
  @WithMockUser(username = "ana@tfg.com", roles = "PROFESOR")
  void grupos_200_conRolProfesor() throws Exception {
    Usuario tutor = buildTutor();
    when(usuarioService.buscarPorCorreo("ana@tfg.com")).thenReturn(Optional.of(tutor));
    when(tutorService.getGruposDeTutor(1))
        .thenReturn(
            List.of(new TutorGrupoListDTO(100, "1DAW-A", "IES Test", "2024/2025", 2L, 30L)));

    mockMvc
        .perform(get("/tutor/grupos"))
        .andExpect(status().isOk())
        .andExpect(view().name("tutor/grupos"))
        .andExpect(model().attributeExists("grupos"));
  }

  // 5.1.4 — 403 cuando no es tutor del grupo
  @Test
  @WithMockUser(username = "ana@tfg.com", roles = "PROFESOR")
  void imparticiones_403_cuandoNoEsTutorDelGrupo() throws Exception {
    Usuario tutor = buildTutor();
    when(usuarioService.buscarPorCorreo("ana@tfg.com")).thenReturn(Optional.of(tutor));
    doThrow(new AccessDeniedException("forbidden"))
        .when(tutorService)
        .validateTutorOwnership(1, 999);

    mockMvc.perform(get("/tutor/grupo/999/imparticiones")).andExpect(status().isForbidden());
  }

  // 5.1.5 — 200 cuando el grupo pertenece al tutor
  @Test
  @WithMockUser(username = "ana@tfg.com", roles = "PROFESOR")
  void imparticiones_200_cuandoEsTutorDelGrupo() throws Exception {
    Usuario tutor = buildTutor();
    Centro centro = Centro.builder().id(10).nombre("IES Test").build();
    CursoAcademico curso = CursoAcademico.builder().id(5).nombre("2024/2025").build();
    Grupo grupo =
        Grupo.builder().id(100).nombre("1DAW-A").centro(centro).cursoAcademico(curso).build();

    when(usuarioService.buscarPorCorreo("ana@tfg.com")).thenReturn(Optional.of(tutor));
    doNothing().when(tutorService).validateTutorOwnership(1, 100);
    when(tutorService.getImparticionesByGrupo(1, 100))
        .thenReturn(List.of(new TutorImparticionDTO(50, "Programación", "Luis Pérez", 15L, false)));
    when(tutorService.getGrupoOrFail(100)).thenReturn(grupo);

    mockMvc
        .perform(get("/tutor/grupo/100/imparticiones"))
        .andExpect(status().isOk())
        .andExpect(view().name("tutor/imparticiones"))
        .andExpect(model().attributeExists("imparticiones"));
  }

  // 5.1.6 — 403 para alumnos de grupo ajeno
  @Test
  @WithMockUser(username = "ana@tfg.com", roles = "PROFESOR")
  void alumnos_403_cuandoNoEsTutor() throws Exception {
    Usuario tutor = buildTutor();
    when(usuarioService.buscarPorCorreo("ana@tfg.com")).thenReturn(Optional.of(tutor));
    doThrow(new AccessDeniedException("forbidden"))
        .when(tutorService)
        .validateTutorOwnership(1, 999);

    mockMvc.perform(get("/tutor/grupo/999/alumnos/50")).andExpect(status().isForbidden());
  }

  // 5.1.7 — redirect a /profe cuando puedeEditarNotas=true
  @Test
  @WithMockUser(username = "ana@tfg.com", roles = "PROFESOR")
  void alumnos_redirectToProfe_cuandoPuedeEditar() throws Exception {
    Usuario tutor = buildTutor();
    when(usuarioService.buscarPorCorreo("ana@tfg.com")).thenReturn(Optional.of(tutor));
    doNothing().when(tutorService).validateTutorOwnership(1, 100);
    when(tutorService.getImparticionesByGrupo(1, 100))
        .thenReturn(List.of(new TutorImparticionDTO(50, "Programación", "Ana García", 15L, true)));

    mockMvc
        .perform(get("/tutor/grupo/100/alumnos/50"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/profe/imparticion/50/alumnos"));
  }

  // 5.1.7b — 200 vista RO cuando no puede editar
  @Test
  @WithMockUser(username = "ana@tfg.com", roles = "PROFESOR")
  void alumnos_200_vistaRO_cuandoNoEsProfesor() throws Exception {
    Usuario tutor = buildTutor();
    Centro centro = Centro.builder().id(10).nombre("IES Test").build();
    CursoAcademico curso = CursoAcademico.builder().id(5).nombre("2024/2025").build();
    Grupo grupo =
        Grupo.builder().id(100).nombre("1DAW-A").centro(centro).cursoAcademico(curso).build();

    when(usuarioService.buscarPorCorreo("ana@tfg.com")).thenReturn(Optional.of(tutor));
    doNothing().when(tutorService).validateTutorOwnership(1, 100);
    when(tutorService.getImparticionesByGrupo(1, 100))
        .thenReturn(List.of(new TutorImparticionDTO(50, "Programación", "Luis Pérez", 15L, false)));
    when(tutorService.getGrupoOrFail(100)).thenReturn(grupo);
    when(tutorService.buildRosterAsTutor(1, 100, 50)).thenReturn(List.of());

    mockMvc
        .perform(get("/tutor/grupo/100/alumnos/50"))
        .andExpect(status().isOk())
        .andExpect(view().name("tutor/alumnos"))
        .andExpect(model().attributeExists("alumnos"));
  }

  // 5.1.8 — 403 notas de matrícula de grupo ajeno
  @Test
  @WithMockUser(username = "ana@tfg.com", roles = "PROFESOR")
  void notasApi_403_cuandoMatriculaEsDeGrupoAjeno() throws Exception {
    Usuario tutor = buildTutor();
    when(usuarioService.buscarPorCorreo("ana@tfg.com")).thenReturn(Optional.of(tutor));
    when(tutorService.getStudentGradesAsTutor(1, 200))
        .thenThrow(new AccessDeniedException("forbidden"));

    mockMvc.perform(get("/tutor/api/matricula/200/notas")).andExpect(status().isForbidden());
  }

  // 5.1.9 — 200 JSON para matrícula de grupo propio
  @Test
  @WithMockUser(username = "ana@tfg.com", roles = "PROFESOR")
  void notasApi_200_cuandoEsTutor() throws Exception {
    Usuario tutor = buildTutor();
    TeacherStudentGradesDTO dto =
        new TeacherStudentGradesDTO(
            200,
            "Carlos López",
            "1DAW-A · Programación",
            List.of(
                new TeacherPeriodoGradesDTO(
                    1,
                    "Primer trimestre",
                    new BigDecimal("40.00"),
                    false,
                    List.of(
                        new TeacherGradeItemDTO(
                            1,
                            1,
                            "RA1",
                            "Descripción RA1",
                            "Examen",
                            "EXAMEN",
                            LocalDate.now(),
                            List.of(),
                            null,
                            null)),
                    null)),
            null);

    when(usuarioService.buscarPorCorreo("ana@tfg.com")).thenReturn(Optional.of(tutor));
    when(tutorService.getStudentGradesAsTutor(1, 200)).thenReturn(dto);

    mockMvc
        .perform(get("/tutor/api/matricula/200/notas"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json"));
  }

  // 5.1.10 — no existe POST de notas en /tutor/**
  @Test
  @WithMockUser(username = "ana@tfg.com", roles = "PROFESOR")
  void noExistePostDeNotasEnContextoTutor() throws Exception {
    mockMvc.perform(post("/tutor/api/matricula/200/notas")).andExpect(status().is4xxClientError());
  }
}
