package com.tfg.schooledule.infrastructure.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

  @Mock private UsuarioRepository usuarioRepository;
  @Mock private MatriculaRepository matriculaRepository;
  @Mock private CalificacionRepository calificacionRepository;
  @Mock private PeriodoEvaluacionRepository periodoRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private AlumnoProfileMapper alumnoProfileMapper;
  @Mock private GradeDashboardMapper gradeDashboardMapper;

  @InjectMocks private UsuarioService usuarioService;

  @Test
  public void testGetAlumnoProfile_Success() {
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
  public void testGetAlumnoProfile_UserNotFound() {
    Integer usuarioId = 99;
    when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

    Exception exception =
        assertThrows(RuntimeException.class, () -> usuarioService.getAlumnoProfile(usuarioId));

    assertEquals("Usuario no encontrado", exception.getMessage());
  }

  @Test
  public void testGetAlumnoProfile_MatriculaNotFound() {
    Integer usuarioId = 1;
    Usuario usuario = Usuario.builder().id(usuarioId).build();
    when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
    when(matriculaRepository.findFirstByAlumnoIdOrderByImparticionGrupoCursoAcademicoIdDesc(
            usuarioId))
        .thenReturn(Optional.empty());

    Exception exception =
        assertThrows(RuntimeException.class, () -> usuarioService.getAlumnoProfile(usuarioId));

    assertEquals("Matricula no encontrada", exception.getMessage());
  }

  @Test
  public void testGetStudentGrades_Success() {
    Integer usuarioId = 1;
    Integer periodoId = 1;

    PeriodoEvaluacion periodo =
        PeriodoEvaluacion.builder().id(periodoId).nombre("1er Trimestre").build();
    Modulo modulo = Modulo.builder().nombre("Programacion").build();
    Imparticion imparticion = Imparticion.builder().modulo(modulo).build();
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
  public void testGetStudentGrades_Empty() {
    Integer usuarioId = 1;
    Integer periodoId = 1;
    GradeDashboardDTO expected = new GradeDashboardDTO(null, new HashMap<>());

    when(calificacionRepository.findByAlumnoIdAndPeriodoId(usuarioId, periodoId))
        .thenReturn(Collections.emptyList());
    when(gradeDashboardMapper.toDto(Collections.emptyList(), null)).thenReturn(expected);

    GradeDashboardDTO dashboard = usuarioService.getStudentGrades(usuarioId, periodoId);

    assertNotNull(dashboard);
    assertTrue(dashboard.gradesByModulo().isEmpty());
    assertNull(dashboard.periodoNombre());
  }

  @Test
  public void testComprobarPassword_Success() {
    String email = "test@tfg.com";
    String pass = "1234";
    Usuario user = Usuario.builder().email(email).passwordHash("hashed").build();

    when(usuarioRepository.findUsuarioByEmail(email)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(pass, "hashed")).thenReturn(true);

    assertTrue(usuarioService.comprobarPassword(email, pass));
  }

  @Test
  public void testComprobarPassword_UserNotFound() {
    String email = "missing@tfg.com";
    when(usuarioRepository.findUsuarioByEmail(email)).thenReturn(Optional.empty());

    assertFalse(usuarioService.comprobarPassword(email, "any"));
  }

  @Test
  public void testComprobarPassword_WrongPassword() {
    String email = "test@tfg.com";
    Usuario user = Usuario.builder().email(email).passwordHash("hashed").build();

    when(usuarioRepository.findUsuarioByEmail(email)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

    assertFalse(usuarioService.comprobarPassword(email, "wrong"));
  }

  @Test
  public void testBuscarPorCorreo() {
    String email = "test@tfg.com";
    Usuario user = Usuario.builder().email(email).build();
    when(usuarioRepository.findUsuarioByEmail(email)).thenReturn(Optional.of(user));

    Optional<Usuario> result = usuarioService.buscarPorCorreo(email);
    assertTrue(result.isPresent());
    assertEquals(email, result.get().getEmail());
  }

  @Test
  public void testBuscarPorNombreUsuario() {
    String username = "admin";
    Usuario user = Usuario.builder().username(username).build();
    when(usuarioRepository.findByUsername(username)).thenReturn(Optional.of(user));

    Optional<Usuario> result = usuarioService.buscarPorNombreUsuario(username);
    assertTrue(result.isPresent());
    assertEquals(username, result.get().getUsername());
  }

  @Test
  public void testGetStudentPeriods() {
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
  public void testGetAsignaturasAlumno_devuelveListaDelRepositorio() {
    Integer usuarioId = 1;
    List<Matricula> matriculas = List.of(new Matricula(), new Matricula());
    when(matriculaRepository.findActivasByAlumnoId(usuarioId)).thenReturn(matriculas);

    List<Matricula> result = usuarioService.getAsignaturasAlumno(usuarioId);

    assertEquals(2, result.size());
    verify(matriculaRepository).findActivasByAlumnoId(usuarioId);
  }

  @Test
  public void testGetAsignaturasAlumno_listaVaciaCuandoSinMatriculas() {
    Integer usuarioId = 1;
    when(matriculaRepository.findActivasByAlumnoId(usuarioId)).thenReturn(Collections.emptyList());

    List<Matricula> result = usuarioService.getAsignaturasAlumno(usuarioId);

    assertTrue(result.isEmpty());
    verify(matriculaRepository).findActivasByAlumnoId(usuarioId);
  }
}
