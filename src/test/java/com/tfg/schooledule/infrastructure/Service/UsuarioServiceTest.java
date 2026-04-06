package com.tfg.schooledule.infrastructure.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tfg.schooledule.domain.DTO.AlumnoProfileDTO;
import com.tfg.schooledule.domain.DTO.GradeDashboardDTO;
import com.tfg.schooledule.domain.entity.*;
import com.tfg.schooledule.domain.enums.TipoActividad;
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

  @InjectMocks private UsuarioService usuarioService;

  @Test
  public void testGetAlumnoProfile_Success() {
    Integer usuarioId = 1;
    Usuario usuario =
        Usuario.builder()
            .id(usuarioId)
            .username("testuser")
            .nombre("Test")
            .apellidos("User")
            .email("test@example.com")
            .build();

    Centro centro = Centro.builder().nombre("Centro Test").build();
    CursoAcademico curso = CursoAcademico.builder().nombre("2025/2026").build();
    Grupo grupo = Grupo.builder().nombre("DAM2").centro(centro).cursoAcademico(curso).build();
    Imparticion imparticion = Imparticion.builder().grupo(grupo).build();
    Matricula matricula =
        Matricula.builder().alumno(usuario).imparticion(imparticion).centro(centro).build();

    when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
    when(matriculaRepository.findFirstByAlumnoIdOrderByImparticionGrupoCursoAcademicoIdDesc(
            usuarioId))
        .thenReturn(Optional.of(matricula));

    AlumnoProfileDTO profile = usuarioService.getAlumnoProfile(usuarioId);

    assertNotNull(profile);
    assertEquals("testuser", profile.getUsername());
    assertEquals("Centro Test", profile.getCentroNombre());
    assertEquals("DAM2", profile.getGrupoNombre());
    assertEquals("2025/2026", profile.getCursoAcademico());
  }

  @Test
  public void testGetAlumnoProfile_UserNotFound() {
    Integer usuarioId = 99;
    when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

    Exception exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              usuarioService.getAlumnoProfile(usuarioId);
            });

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
        assertThrows(
            RuntimeException.class,
            () -> {
              usuarioService.getAlumnoProfile(usuarioId);
            });

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
    ItemEvaluable item =
        ItemEvaluable.builder()
            .nombre("Examen 1")
            .tipo(TipoActividad.EXAMEN)
            .periodoEvaluacion(periodo)
            .build();
    Calificacion calif =
        Calificacion.builder()
            .matricula(matricula)
            .itemEvaluable(item)
            .valor(new BigDecimal("8.5"))
            .build();

    when(calificacionRepository.findByAlumnoIdAndPeriodoId(usuarioId, periodoId))
        .thenReturn(List.of(calif));

    GradeDashboardDTO dashboard = usuarioService.getStudentGrades(usuarioId, periodoId);

    assertNotNull(dashboard);
    assertEquals("1er Trimestre", dashboard.getPeriodoNombre());
    assertTrue(dashboard.getGradesByModulo().containsKey("Programacion"));
    assertEquals(1, dashboard.getGradesByModulo().get("Programacion").size());
  }

  @Test
  public void testGetStudentGrades_Empty() {
    Integer usuarioId = 1;
    Integer periodoId = 1;
    when(calificacionRepository.findByAlumnoIdAndPeriodoId(usuarioId, periodoId))
        .thenReturn(Collections.emptyList());

    GradeDashboardDTO dashboard = usuarioService.getStudentGrades(usuarioId, periodoId);

    assertNotNull(dashboard);
    assertTrue(dashboard.getGradesByModulo().isEmpty());
    assertNull(dashboard.getPeriodoNombre());
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
}
