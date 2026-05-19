package com.tfg.schooledule.infrastructure.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.tfg.schooledule.domain.dto.AlumnoProfileDTO;
import com.tfg.schooledule.domain.dto.GradeDashboardDTO;
import com.tfg.schooledule.domain.entity.*;
import com.tfg.schooledule.infrastructure.mapper.AlumnoProfileMapper;
import com.tfg.schooledule.infrastructure.mapper.GradeDashboardMapper;
import com.tfg.schooledule.infrastructure.repository.CalificacionRepository;
import com.tfg.schooledule.infrastructure.repository.MatriculaRepository;
import com.tfg.schooledule.infrastructure.repository.PeriodoEvaluacionRepository;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

  @Mock private UsuarioRepository usuarioRepository;
  @Mock private MatriculaRepository matriculaRepository;
  @Mock private CalificacionRepository calificacionRepository;
  @Mock private TeacherDashboardService teacherDashboardService;
  @Mock private PeriodoEvaluacionRepository periodoRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private AlumnoProfileMapper alumnoProfileMapper;
  @Mock private GradeDashboardMapper gradeDashboardMapper;

  @InjectMocks private UsuarioService usuarioService;

  @Test
  void testGetAlumnoProfile_Success() {
    Integer usuarioId = 1;
    Usuario usuario = Usuario.builder().id(usuarioId).username("testuser").build();
    Centro centro = Centro.builder().nombre("Centro Test").build();
    CursoAcademico curso = CursoAcademico.builder().nombre("2025/2026").build();
    Grupo grupo = Grupo.builder().nombre("DAM2").centro(centro).cursoAcademico(curso).build();
    Imparticion imparticion = Imparticion.builder().grupo(grupo).build();
    Matricula matricula =
        Matricula.builder().alumno(usuario).imparticion(imparticion).centro(centro).build();

    AlumnoProfileDTO expected =
        new AlumnoProfileDTO(
            usuarioId,
            "testuser",
            "Test",
            "User",
            "test@example.com",
            "Centro Test",
            "DAM2",
            "2025/2026");

    when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
    when(matriculaRepository.findFirstByAlumnoIdOrderByImparticionGrupoCursoAcademicoIdDesc(
            usuarioId))
        .thenReturn(Optional.of(matricula));
    when(alumnoProfileMapper.toDto(usuario, matricula)).thenReturn(expected);

    AlumnoProfileDTO profile = usuarioService.getAlumnoProfile(usuarioId);

    assertNotNull(profile);
    assertEquals("testuser", profile.username());
    assertEquals("Centro Test", profile.centroNombre());
    assertEquals("DAM2", profile.grupoNombre());
    assertEquals("2025/2026", profile.cursoAcademico());
  }

  @Test
  void testGetAlumnoProfile_UserNotFound() {
    Integer usuarioId = 99;
    when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

    Exception exception =
        assertThrows(RuntimeException.class, () -> usuarioService.getAlumnoProfile(usuarioId));

    assertEquals("Usuario no encontrado: " + usuarioId, exception.getMessage());
  }

  @Test
  void testGetAlumnoProfile_MatriculaNotFound() {
    Integer usuarioId = 1;
    Usuario usuario = Usuario.builder().id(usuarioId).build();
    when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
    when(matriculaRepository.findFirstByAlumnoIdOrderByImparticionGrupoCursoAcademicoIdDesc(
            usuarioId))
        .thenReturn(Optional.empty());

    Exception exception =
        assertThrows(RuntimeException.class, () -> usuarioService.getAlumnoProfile(usuarioId));

    assertEquals("Matrícula no encontrada para alumno: " + usuarioId, exception.getMessage());
  }

  @Test
  void testGetStudentGrades_Success() {
    Integer usuarioId = 1;
    Integer periodoId = 1;
    Integer imparticionId = 10;

    PeriodoEvaluacion periodo =
        PeriodoEvaluacion.builder().id(periodoId).nombre("1er Trimestre").build();
    Modulo modulo = Modulo.builder().nombre("Programacion").build();
    Imparticion imparticion = Imparticion.builder().id(imparticionId).modulo(modulo).build();
    Matricula matricula = Matricula.builder().imparticion(imparticion).build();
    CriterioEvaluacion ce =
        CriterioEvaluacion.builder().id(1).codigo("a").descripcion("Examen 1").build();
    Calificacion calif =
        Calificacion.builder()
            .matricula(matricula)
            .criterioEvaluacion(ce)
            .valor(new BigDecimal("8.5"))
            .build();

    GradeDashboardDTO expected =
        new GradeDashboardDTO("1er Trimestre", Map.of("Programacion", List.of()));

    when(matriculaRepository.findByAlumnoId(usuarioId)).thenReturn(List.of(matricula));
    when(periodoRepository.findByImparticionId(imparticionId)).thenReturn(List.of(periodo));
    when(calificacionRepository.findByAlumnoIdAndPeriodoId(usuarioId, periodoId))
        .thenReturn(List.of(calif));
    when(periodoRepository.findById(periodoId)).thenReturn(Optional.of(periodo));
    when(gradeDashboardMapper.toDto(any(), eq("1er Trimestre"))).thenReturn(expected);

    GradeDashboardDTO dashboard = usuarioService.getStudentGrades(usuarioId, periodoId);

    assertNotNull(dashboard);
    assertEquals("1er Trimestre", dashboard.periodoNombre());
    assertTrue(dashboard.gradesByModulo().containsKey("Programacion"));
  }

  @Test
  void testGetStudentGrades_Empty() {
    Integer usuarioId = 1;
    Integer periodoId = 1;
    Integer imparticionId = 10;

    PeriodoEvaluacion periodo = PeriodoEvaluacion.builder().id(periodoId).nombre("P1").build();
    Imparticion imparticion = Imparticion.builder().id(imparticionId).build();
    Matricula matricula = Matricula.builder().imparticion(imparticion).build();
    GradeDashboardDTO expected = new GradeDashboardDTO(null, new HashMap<>());

    when(matriculaRepository.findByAlumnoId(usuarioId)).thenReturn(List.of(matricula));
    when(periodoRepository.findByImparticionId(imparticionId)).thenReturn(List.of(periodo));
    when(calificacionRepository.findByAlumnoIdAndPeriodoId(usuarioId, periodoId))
        .thenReturn(Collections.emptyList());
    when(gradeDashboardMapper.toDto(Collections.emptyList(), null)).thenReturn(expected);

    GradeDashboardDTO dashboard = usuarioService.getStudentGrades(usuarioId, periodoId);

    assertNotNull(dashboard);
    assertTrue(dashboard.gradesByModulo().isEmpty());
    assertNull(dashboard.periodoNombre());
  }

  @Test
  void testGetStudentGrades_IDOR_periodoNoPertenecealAlumno_lanzaAccessDeniedException() {
    Integer usuarioId = 1;
    Integer periodoId = 99;
    Integer imparticionId = 10;

    PeriodoEvaluacion otherPeriodo = PeriodoEvaluacion.builder().id(5).build();
    Imparticion imparticion = Imparticion.builder().id(imparticionId).build();
    Matricula matricula = Matricula.builder().imparticion(imparticion).build();

    when(matriculaRepository.findByAlumnoId(usuarioId)).thenReturn(List.of(matricula));
    when(periodoRepository.findByImparticionId(imparticionId)).thenReturn(List.of(otherPeriodo));

    assertThrows(
        AccessDeniedException.class, () -> usuarioService.getStudentGrades(usuarioId, periodoId));
  }

  @Test
  void testGetStudentGrades_IDOR_sinMatriculas_lanzaAccessDeniedException() {
    Integer usuarioId = 1;
    Integer periodoId = 1;

    when(matriculaRepository.findByAlumnoId(usuarioId)).thenReturn(Collections.emptyList());

    assertThrows(
        AccessDeniedException.class, () -> usuarioService.getStudentGrades(usuarioId, periodoId));
  }

  @Test
  void testComprobarPassword_Success() {
    String email = "test@tfg.com";
    String pass = "1234";
    Usuario user = Usuario.builder().email(email).passwordHash("hashed").build();

    when(usuarioRepository.findUsuarioByEmail(email)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(pass, "hashed")).thenReturn(true);

    assertTrue(usuarioService.comprobarPassword(email, pass));
  }

  @Test
  void testComprobarPassword_UserNotFound() {
    String email = "missing@tfg.com";
    when(usuarioRepository.findUsuarioByEmail(email)).thenReturn(Optional.empty());

    assertFalse(usuarioService.comprobarPassword(email, "any"));
  }

  @Test
  void testComprobarPassword_WrongPassword() {
    String email = "test@tfg.com";
    Usuario user = Usuario.builder().email(email).passwordHash("hashed").build();

    when(usuarioRepository.findUsuarioByEmail(email)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

    assertFalse(usuarioService.comprobarPassword(email, "wrong"));
  }

  @Test
  void testBuscarPorCorreo() {
    String email = "test@tfg.com";
    Usuario user = Usuario.builder().email(email).build();
    when(usuarioRepository.findUsuarioByEmail(email)).thenReturn(Optional.of(user));

    Optional<Usuario> result = usuarioService.buscarPorCorreo(email);
    assertTrue(result.isPresent());
    assertEquals(email, result.get().getEmail());
  }

  @Test
  void testBuscarPorNombreUsuario() {
    String username = "admin";
    Usuario user = Usuario.builder().username(username).build();
    when(usuarioRepository.findByUsername(username)).thenReturn(Optional.of(user));

    Optional<Usuario> result = usuarioService.buscarPorNombreUsuario(username);
    assertTrue(result.isPresent());
    assertEquals(username, result.get().getUsername());
  }

  @Test
  void testGetStudentPeriods() {
    Integer usuarioId = 1;
    Integer imparticionId = 10;
    Imparticion imp = Imparticion.builder().id(imparticionId).build();
    Matricula m = Matricula.builder().imparticion(imp).build();
    PeriodoEvaluacion p = PeriodoEvaluacion.builder().id(1).nombre("P1").build();

    when(matriculaRepository.findByAlumnoId(usuarioId)).thenReturn(List.of(m));
    when(periodoRepository.findByImparticionId(imparticionId)).thenReturn(List.of(p));

    List<PeriodoEvaluacion> periods = usuarioService.getStudentPeriods(usuarioId);

    assertNotNull(periods);
    assertEquals(1, periods.size());
    assertEquals("P1", periods.get(0).getNombre());
  }

  @Test
  void testGetAsignaturasAlumno_devuelveListaDelRepositorio() {
    Integer usuarioId = 1;
    List<Matricula> matriculas = List.of(new Matricula(), new Matricula());
    when(matriculaRepository.findActivasByAlumnoId(usuarioId)).thenReturn(matriculas);

    List<Matricula> result = usuarioService.getAsignaturasAlumno(usuarioId);

    assertEquals(2, result.size());
    verify(matriculaRepository).findActivasByAlumnoId(usuarioId);
  }

  @Test
  void testGetAsignaturasAlumno_listaVaciaCuandoSinMatriculas() {
    Integer usuarioId = 1;
    when(matriculaRepository.findActivasByAlumnoId(usuarioId)).thenReturn(Collections.emptyList());

    List<Matricula> result = usuarioService.getAsignaturasAlumno(usuarioId);

    assertTrue(result.isEmpty());
    verify(matriculaRepository).findActivasByAlumnoId(usuarioId);
  }

  @Test
  void testGetStudentGrades_delegaEnGradeDashboardMapper() {
    Integer usuarioId = 1;
    Integer periodoId = 1;
    Integer imparticionId = 10;

    PeriodoEvaluacion periodo =
        PeriodoEvaluacion.builder().id(periodoId).nombre("1er Trimestre").build();
    Modulo modulo = Modulo.builder().nombre("Programacion").build();
    Imparticion imparticion = Imparticion.builder().id(imparticionId).modulo(modulo).build();
    Matricula matricula = Matricula.builder().imparticion(imparticion).build();

    GradeDashboardDTO expected = new GradeDashboardDTO("1er Trimestre", Map.of());

    when(matriculaRepository.findByAlumnoId(usuarioId)).thenReturn(List.of(matricula));
    when(periodoRepository.findByImparticionId(imparticionId)).thenReturn(List.of(periodo));
    when(calificacionRepository.findByAlumnoIdAndPeriodoId(usuarioId, periodoId))
        .thenReturn(Collections.emptyList());
    when(periodoRepository.findById(periodoId)).thenReturn(Optional.of(periodo));
    when(gradeDashboardMapper.toDto(any(), eq("1er Trimestre"))).thenReturn(expected);

    GradeDashboardDTO result = usuarioService.getStudentGrades(usuarioId, periodoId);

    assertNotNull(result);
    verify(gradeDashboardMapper).toDto(any(), eq("1er Trimestre"));
  }

  @Test
  void testGetStudentGrades_retornaDTOConFueModificadaPropagada() {
    // GradeDashboardMapper.toDto() is responsible for setting fueModificada.
    // This test verifies that UsuarioService correctly propagates the DTO returned by the mapper,
    // including any fueModificada=true values.
    Integer usuarioId = 1;
    Integer periodoId = 1;
    Integer imparticionId = 10;

    PeriodoEvaluacion periodo = PeriodoEvaluacion.builder().id(periodoId).nombre("P1").build();
    Imparticion imparticion = Imparticion.builder().id(imparticionId).build();
    Matricula matricula = Matricula.builder().imparticion(imparticion).build();

    com.tfg.schooledule.domain.dto.GradeDTO gradeConRecuperacion =
        new com.tfg.schooledule.domain.dto.GradeDTO(
            "a – CE1", new BigDecimal("8.00"), null, null, "EXAMEN", true);
    GradeDashboardDTO expected =
        new GradeDashboardDTO("P1", Map.of("Programacion", List.of(gradeConRecuperacion)));

    when(matriculaRepository.findByAlumnoId(usuarioId)).thenReturn(List.of(matricula));
    when(periodoRepository.findByImparticionId(imparticionId)).thenReturn(List.of(periodo));
    when(calificacionRepository.findByAlumnoIdAndPeriodoId(usuarioId, periodoId))
        .thenReturn(Collections.emptyList());
    when(periodoRepository.findById(periodoId)).thenReturn(Optional.of(periodo));
    when(gradeDashboardMapper.toDto(any(), eq("P1"))).thenReturn(expected);

    GradeDashboardDTO result = usuarioService.getStudentGrades(usuarioId, periodoId);

    assertNotNull(result);
    assertTrue(result.gradesByModulo().get("Programacion").get(0).fueModificada());
  }
}
