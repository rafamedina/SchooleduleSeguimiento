package com.tfg.schooledule.infrastructure.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfg.schooledule.domain.dto.*;
import com.tfg.schooledule.domain.entity.Centro;
import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.infrastructure.service.TeacherDashboardService;
import com.tfg.schooledule.infrastructure.service.UsuarioService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProfeController.class)
class ProfeControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockBean private UsuarioService usuarioService;
  @MockBean private TeacherDashboardService teacherService;

  private Usuario buildProfe() {
    return Usuario.builder()
        .id(2)
        .username("profe1")
        .email("juan@tfg.com")
        .nombre("Juan")
        .apellidos("Garcia")
        .build();
  }

  private Usuario buildProfeConCentro() {
    Centro centro = Centro.builder().id(1).nombre("IES Central").ubicacion("Madrid").build();
    return Usuario.builder()
        .id(2)
        .username("profe1")
        .email("juan@tfg.com")
        .nombre("Juan")
        .apellidos("Garcia")
        .centros(Set.of(centro))
        .build();
  }

  private TeacherGradeItemDTO buildItemDto(BigDecimal mediaRa) {
    return new TeacherGradeItemDTO(
        1,
        1,
        "RA1",
        "Identifica sistemas ERP",
        "Examen RA1",
        "EXAMEN",
        LocalDate.now(),
        List.of(
            new TeacherCriterioGradeDTO(1, "a", "CE-a", mediaRa, null, mediaRa != null ? 1 : null),
            new TeacherCriterioGradeDTO(2, "b", "CE-b", null, null, null)),
        mediaRa);
  }

  @Test
  @WithMockUser(username = "juan@tfg.com", roles = "PROFESOR")
  void dashboard_200_yModelContieneCentros() throws Exception {
    Usuario profe = buildProfe();
    List<TeacherCenterDTO> centros = List.of(new TeacherCenterDTO(1, "IES Central", "Madrid", 2L));

    when(usuarioService.buscarPorCorreo("juan@tfg.com")).thenReturn(Optional.of(profe));
    when(teacherService.getCentersForTeacher(profe)).thenReturn(centros);

    mockMvc
        .perform(get("/profe/dashboard"))
        .andExpect(status().isOk())
        .andExpect(view().name("profe/dashboard"))
        .andExpect(model().attributeExists("centros"))
        .andExpect(model().attribute("centros", centros));
  }

  @Test
  @WithMockUser(username = "juan@tfg.com", roles = "PROFESOR")
  void asignaturas_200_cuandoCentroEsDelProfesor() throws Exception {
    Usuario profe = buildProfe();
    List<TeacherSubjectDTO> subjects =
        List.of(new TeacherSubjectDTO(1, "M1", "Modulo1", "DAW1-A", "2025/2026", 15L));

    when(usuarioService.buscarPorCorreo("juan@tfg.com")).thenReturn(Optional.of(profe));
    when(teacherService.getSubjectsForTeacherAndCenter(2, 1)).thenReturn(subjects);

    mockMvc
        .perform(get("/profe/centro/1/asignaturas"))
        .andExpect(status().isOk())
        .andExpect(view().name("profe/asignaturas"))
        .andExpect(model().attributeExists("asignaturas"));
  }

  @Test
  @WithMockUser(username = "juan@tfg.com", roles = "PROFESOR")
  void alumnos_403_cuandoImparticionNoEsDelProfesor() throws Exception {
    Usuario profe = buildProfe();
    when(usuarioService.buscarPorCorreo("juan@tfg.com")).thenReturn(Optional.of(profe));
    when(teacherService.getRosterForImparticion(2, 99))
        .thenThrow(new AccessDeniedException("forbidden"));

    mockMvc.perform(get("/profe/imparticion/99/alumnos")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "juan@tfg.com", roles = "PROFESOR")
  void getNotas_devuelveJsonConPeriodoItemCriterioHierarchy() throws Exception {
    Usuario profe = buildProfe();
    TeacherStudentGradesDTO dto =
        new TeacherStudentGradesDTO(
            1,
            "Ana Lopez",
            "DAW1-A · Modulo1",
            List.of(
                new TeacherPeriodoGradesDTO(
                    1,
                    "P1",
                    new BigDecimal("100.00"),
                    false,
                    List.of(buildItemDto(new BigDecimal("8.50"))),
                    new BigDecimal("8.50"))),
            new BigDecimal("8.50"));

    when(usuarioService.buscarPorCorreo("juan@tfg.com")).thenReturn(Optional.of(profe));
    when(teacherService.getStudentGrades(2, 1)).thenReturn(dto);

    mockMvc
        .perform(get("/profe/api/matricula/1/notas"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.periodos[0].media").value(8.50))
        .andExpect(jsonPath("$.alumnoNombre").value("Ana Lopez"))
        .andExpect(jsonPath("$.periodos[0].items[0].criterios").isArray())
        .andExpect(jsonPath("$.periodos[0].items[0].criterios.length()").value(2))
        .andExpect(jsonPath("$.periodos[0].items[0].mediaRa").value(8.50));
  }

  @Test
  @WithMockUser(username = "juan@tfg.com", roles = "PROFESOR")
  void postNotas_400_cuandoMatriculaIdNoCoincide() throws Exception {
    Usuario profe = buildProfe();
    when(usuarioService.buscarPorCorreo("juan@tfg.com")).thenReturn(Optional.of(profe));

    GradeUpsertRequest req =
        new GradeUpsertRequest(
            99, List.of(new GradeUpsertRequest.Entry(1, new BigDecimal("7.00"), null)));

    mockMvc
        .perform(
            post("/profe/api/matricula/1/notas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(username = "juan@tfg.com", roles = "PROFESOR")
  void postNotas_persiste_yDevuelvePayloadActualizado() throws Exception {
    Usuario profe = buildProfe();
    GradeUpsertRequest req =
        new GradeUpsertRequest(
            1, List.of(new GradeUpsertRequest.Entry(1, new BigDecimal("9.00"), "Perfecto")));

    TeacherStudentGradesDTO refreshed =
        new TeacherStudentGradesDTO(
            1,
            "Ana Lopez",
            "DAW1-A · Modulo1",
            List.of(
                new TeacherPeriodoGradesDTO(
                    1,
                    "P1",
                    new BigDecimal("100.00"),
                    false,
                    List.of(buildItemDto(new BigDecimal("9.00"))),
                    new BigDecimal("9.00"))),
            new BigDecimal("9.00"));

    when(usuarioService.buscarPorCorreo("juan@tfg.com")).thenReturn(Optional.of(profe));
    when(teacherService.upsertGrades(2, "juan@tfg.com", req)).thenReturn(refreshed);

    mockMvc
        .perform(
            post("/profe/api/matricula/1/notas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.mediaGlobal").value(9.00))
        .andExpect(jsonPath("$.periodos[0].items[0].mediaRa").value(9.00))
        .andExpect(jsonPath("$.periodos[0].items[0].criterios[0].valor").value(9.00));
  }

  @Test
  @WithMockUser(username = "juan@tfg.com", roles = "PROFESOR")
  void asignaturas_200_incluyeNombreCentroEnModelo() throws Exception {
    Usuario profe = buildProfeConCentro();
    List<TeacherSubjectDTO> subjects =
        List.of(new TeacherSubjectDTO(1, "M1", "Modulo1", "DAW1-A", "2025/2026", 15L));

    when(usuarioService.buscarPorCorreo("juan@tfg.com")).thenReturn(Optional.of(profe));
    when(teacherService.getSubjectsForTeacherAndCenter(2, 1)).thenReturn(subjects);

    mockMvc
        .perform(get("/profe/centro/1/asignaturas"))
        .andExpect(status().isOk())
        .andExpect(view().name("profe/asignaturas"))
        .andExpect(model().attribute("centroNombre", "IES Central"));
  }

  @Test
  @WithMockUser(username = "juan@tfg.com", roles = "PROFESOR")
  void alumnos_200_retornaVistaConRosterYLabel() throws Exception {
    Usuario profe = buildProfe();
    List<TeacherStudentRowDTO> roster =
        List.of(new TeacherStudentRowDTO(1, 3, "Ana Lopez", "ana@t.com", false));
    List<TeacherSubjectDTO> subjects =
        List.of(new TeacherSubjectDTO(1, "M1", "Modulo1", "DAW1-A", "2025/2026", 1L));

    when(usuarioService.buscarPorCorreo("juan@tfg.com")).thenReturn(Optional.of(profe));
    when(teacherService.getRosterForImparticion(2, 1)).thenReturn(roster);
    when(teacherService.getCentroIdByImparticion(2, 1)).thenReturn(1);
    when(teacherService.getSubjectsForTeacherAndCenter(2, 1)).thenReturn(subjects);

    mockMvc
        .perform(get("/profe/imparticion/1/alumnos"))
        .andExpect(status().isOk())
        .andExpect(view().name("profe/alumnos"))
        .andExpect(model().attributeExists("alumnos"))
        .andExpect(model().attribute("imparticionLabel", "DAW1-A · Modulo1"));
  }

  @Test
  @WithMockUser(username = "juan@tfg.com", roles = "PROFESOR")
  void getNotas_403JsonCuandoServicioLanzaAccessDenied() throws Exception {
    Usuario profe = buildProfe();
    when(usuarioService.buscarPorCorreo("juan@tfg.com")).thenReturn(Optional.of(profe));
    when(teacherService.getStudentGrades(2, 5)).thenThrow(new AccessDeniedException("forbidden"));

    mockMvc
        .perform(get("/profe/api/matricula/5/notas").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value("forbidden"));
  }

  @Test
  @WithMockUser(username = "juan@tfg.com", roles = "PROFESOR")
  void postNotas_409JsonCuandoPeriodoCerrado() throws Exception {
    Usuario profe = buildProfe();
    GradeUpsertRequest req =
        new GradeUpsertRequest(
            1, List.of(new GradeUpsertRequest.Entry(1, new BigDecimal("7.50"), null)));

    when(usuarioService.buscarPorCorreo("juan@tfg.com")).thenReturn(Optional.of(profe));
    when(teacherService.upsertGrades(2, "juan@tfg.com", req))
        .thenThrow(new IllegalStateException("El periodo está cerrado"));

    mockMvc
        .perform(
            post("/profe/api/matricula/1/notas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error").value("conflict"));
  }

  @Test
  @WithMockUser(username = "juan@tfg.com", roles = "PROFESOR")
  void postNotas_400JsonCuandoValorFueraDeRango() throws Exception {
    Usuario profe = buildProfe();
    when(usuarioService.buscarPorCorreo("juan@tfg.com")).thenReturn(Optional.of(profe));

    // valor = 15 → supera @DecimalMax("10.00") → MethodArgumentNotValidException → 400 JSON
    String cuerpoInvalido =
        "{\"matriculaId\":1,\"entries\":[{\"criterioEvaluacionId\":1,\"valor\":15.00}]}";

    mockMvc
        .perform(
            post("/profe/api/matricula/1/notas")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(cuerpoInvalido)
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("validation"))
        .andExpect(jsonPath("$.details").isArray());
  }
}
