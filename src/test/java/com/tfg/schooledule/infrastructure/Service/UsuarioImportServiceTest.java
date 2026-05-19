package com.tfg.schooledule.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tfg.schooledule.domain.dto.UsuarioImportErrorDTO;
import com.tfg.schooledule.domain.dto.UsuarioImportPreviewDTO;
import com.tfg.schooledule.domain.dto.UsuarioImportResultado;
import com.tfg.schooledule.domain.dto.UsuarioImportRowDTO;
import com.tfg.schooledule.domain.entity.Centro;
import com.tfg.schooledule.domain.entity.CursoAcademico;
import com.tfg.schooledule.domain.entity.Grupo;
import com.tfg.schooledule.domain.entity.Imparticion;
import com.tfg.schooledule.domain.entity.Modulo;
import com.tfg.schooledule.domain.entity.Rol;
import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.domain.exception.UsuarioImportException;
import com.tfg.schooledule.infrastructure.repository.CentroRepository;
import com.tfg.schooledule.infrastructure.repository.CursoAcademicoRepository;
import com.tfg.schooledule.infrastructure.repository.GrupoRepository;
import com.tfg.schooledule.infrastructure.repository.ImparticionRepository;
import com.tfg.schooledule.infrastructure.repository.MatriculaRepository;
import com.tfg.schooledule.infrastructure.repository.RolRepository;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioImportServiceTest {

  @Mock private UsuarioExcelParserService parser;
  @Mock private UsuarioImportValidatorService validator;
  @Mock private CentroRepository centroRepository;
  @Mock private CursoAcademicoRepository cursoAcademicoRepository;
  @Mock private GrupoRepository grupoRepository;
  @Mock private UsuarioRepository usuarioRepository;
  @Mock private RolRepository rolRepository;
  @Mock private ImparticionRepository imparticionRepository;
  @Mock private MatriculaRepository matriculaRepository;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private UsuarioImportService service;

  private Centro centro;
  private CursoAcademico curso;
  private Grupo grupo;
  private Rol rolAlumno;
  private Imparticion imparticion;
  private Usuario usuarioGuardado;

  @BeforeEach
  void setUp() {
    centro = Centro.builder().id(1).nombre("IES La Madraza").build();
    curso = CursoAcademico.builder().id(1).nombre("2025/2026").build();
    grupo = Grupo.builder().id(1).nombre("DAW 1A").centro(centro).cursoAcademico(curso).build();
    rolAlumno = Rol.builder().id(2).nombre("ROLE_ALUMNO").build();
    Modulo modulo = Modulo.builder().id(1).nombre("DAW").build();
    imparticion = Imparticion.builder().id(1).grupo(grupo).modulo(modulo).centro(centro).build();
    usuarioGuardado = Usuario.builder().id(10).username("alumno01").build();
  }

  private UsuarioImportRowDTO filaValida(int num, String username) {
    return new UsuarioImportRowDTO(
        num,
        username,
        "Juan",
        "García",
        null,
        "Abc12345",
        "IES La Madraza",
        "2025/2026",
        "DAW 1A",
        null);
  }

  private void stubEntidadesOk() {
    when(centroRepository.findByNombreIgnoreCase("IES La Madraza")).thenReturn(Optional.of(centro));
    when(cursoAcademicoRepository.findByNombreIgnoreCase("2025/2026"))
        .thenReturn(Optional.of(curso));
    when(grupoRepository.findByNombreIgnoreCaseAndCentroIdAndCursoAcademicoId("DAW 1A", 1, 1))
        .thenReturn(Optional.of(grupo));
    when(usuarioRepository.existsByUsername(anyString())).thenReturn(false);
    when(rolRepository.findByNombre("ROLE_ALUMNO")).thenReturn(rolAlumno);
    when(passwordEncoder.encode(anyString())).thenReturn("$2a$hash");
    when(usuarioRepository.save(any())).thenReturn(usuarioGuardado);
  }

  // ── Creación correcta ─────────────────────────────────────────────────────

  @Test
  void importar_archivoValido_creaUsuariosYMatriculas() {
    List<UsuarioImportRowDTO> filas = List.of(filaValida(1, "alumno01"), filaValida(2, "alumno02"));
    when(parser.parsear(any())).thenReturn(filas);
    when(validator.validar(filas)).thenReturn(new UsuarioImportPreviewDTO(true, List.of(), 2));
    stubEntidadesOk();
    when(imparticionRepository.findByGrupoId(1)).thenReturn(List.of(imparticion));

    service.importar(new byte[] {1});

    verify(usuarioRepository, times(2)).save(any());
    verify(matriculaRepository, times(2)).save(any());
  }

  @Test
  void importar_archivoValido_devuelveConteosCorrrectos() {
    List<UsuarioImportRowDTO> filas = List.of(filaValida(1, "alumno01"), filaValida(2, "alumno02"));
    when(parser.parsear(any())).thenReturn(filas);
    when(validator.validar(filas)).thenReturn(new UsuarioImportPreviewDTO(true, List.of(), 2));
    stubEntidadesOk();
    when(imparticionRepository.findByGrupoId(1)).thenReturn(List.of(imparticion, imparticion));

    UsuarioImportResultado resultado = service.importar(new byte[] {1});

    assertThat(resultado.usuariosCreados()).isEqualTo(2);
    assertThat(resultado.matriculasCreadas()).isEqualTo(4);
  }

  // ── Fallos de validación estructural ─────────────────────────────────────

  @Test
  void importar_validacionEstructuralFalla_lanzaUsuarioImportException() {
    List<UsuarioImportRowDTO> filas = List.of(filaValida(1, "alumno01"));
    when(parser.parsear(any())).thenReturn(filas);
    List<UsuarioImportErrorDTO> errores =
        List.of(new UsuarioImportErrorDTO(1, "username", "es obligatorio"));
    when(validator.validar(filas)).thenReturn(new UsuarioImportPreviewDTO(false, errores, 1));

    assertThatThrownBy(() -> service.importar(new byte[] {1}))
        .isInstanceOf(UsuarioImportException.class);
    verify(usuarioRepository, never()).save(any());
  }

  // ── Fallos de resolución BD (pre-flight) ─────────────────────────────────

  @Test
  void importar_centroNoEncontrado_lanzaUsuarioImportException() {
    List<UsuarioImportRowDTO> filas = List.of(filaValida(1, "alumno01"));
    when(parser.parsear(any())).thenReturn(filas);
    when(validator.validar(filas)).thenReturn(new UsuarioImportPreviewDTO(true, List.of(), 1));
    when(centroRepository.findByNombreIgnoreCase(anyString())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.importar(new byte[] {1}))
        .isInstanceOf(UsuarioImportException.class)
        .satisfies(
            ex -> {
              List<UsuarioImportErrorDTO> err = ((UsuarioImportException) ex).getErrores();
              assertThat(err).anyMatch(e -> e.campo().equals("centro_nombre"));
            });
    verify(usuarioRepository, never()).save(any());
  }

  @Test
  void importar_cursoNoEncontrado_lanzaUsuarioImportException() {
    List<UsuarioImportRowDTO> filas = List.of(filaValida(1, "alumno01"));
    when(parser.parsear(any())).thenReturn(filas);
    when(validator.validar(filas)).thenReturn(new UsuarioImportPreviewDTO(true, List.of(), 1));
    when(centroRepository.findByNombreIgnoreCase(anyString())).thenReturn(Optional.of(centro));
    when(cursoAcademicoRepository.findByNombreIgnoreCase(anyString())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.importar(new byte[] {1}))
        .isInstanceOf(UsuarioImportException.class)
        .satisfies(
            ex -> {
              List<UsuarioImportErrorDTO> err = ((UsuarioImportException) ex).getErrores();
              assertThat(err).anyMatch(e -> e.campo().equals("curso_academico_nombre"));
            });
    verify(usuarioRepository, never()).save(any());
  }

  @Test
  void importar_grupoNoEncontrado_lanzaUsuarioImportException() {
    List<UsuarioImportRowDTO> filas = List.of(filaValida(1, "alumno01"));
    when(parser.parsear(any())).thenReturn(filas);
    when(validator.validar(filas)).thenReturn(new UsuarioImportPreviewDTO(true, List.of(), 1));
    when(centroRepository.findByNombreIgnoreCase(anyString())).thenReturn(Optional.of(centro));
    when(cursoAcademicoRepository.findByNombreIgnoreCase(anyString()))
        .thenReturn(Optional.of(curso));
    when(grupoRepository.findByNombreIgnoreCaseAndCentroIdAndCursoAcademicoId(
            anyString(), anyInt(), anyInt()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.importar(new byte[] {1}))
        .isInstanceOf(UsuarioImportException.class)
        .satisfies(
            ex -> {
              List<UsuarioImportErrorDTO> err = ((UsuarioImportException) ex).getErrores();
              assertThat(err).anyMatch(e -> e.campo().equals("grupo_nombre"));
            });
    verify(usuarioRepository, never()).save(any());
  }

  @Test
  void importar_usernameYaExiste_lanzaUsuarioImportException() {
    List<UsuarioImportRowDTO> filas = List.of(filaValida(1, "alumno01"));
    when(parser.parsear(any())).thenReturn(filas);
    when(validator.validar(filas)).thenReturn(new UsuarioImportPreviewDTO(true, List.of(), 1));
    when(centroRepository.findByNombreIgnoreCase(anyString())).thenReturn(Optional.of(centro));
    when(cursoAcademicoRepository.findByNombreIgnoreCase(anyString()))
        .thenReturn(Optional.of(curso));
    when(grupoRepository.findByNombreIgnoreCaseAndCentroIdAndCursoAcademicoId(
            anyString(), anyInt(), anyInt()))
        .thenReturn(Optional.of(grupo));
    when(usuarioRepository.existsByUsername("alumno01")).thenReturn(true);

    assertThatThrownBy(() -> service.importar(new byte[] {1}))
        .isInstanceOf(UsuarioImportException.class)
        .satisfies(
            ex -> {
              List<UsuarioImportErrorDTO> err = ((UsuarioImportException) ex).getErrores();
              assertThat(err).anyMatch(e -> e.campo().equals("username"));
            });
    verify(usuarioRepository, never()).save(any());
  }

  @Test
  void importar_emailYaExiste_lanzaUsuarioImportException() {
    UsuarioImportRowDTO fila =
        new UsuarioImportRowDTO(
            1,
            "alumno01",
            "Juan",
            "García",
            "j@ies.es",
            "Abc12345",
            "IES La Madraza",
            "2025/2026",
            "DAW 1A",
            null);
    when(parser.parsear(any())).thenReturn(List.of(fila));
    when(validator.validar(any())).thenReturn(new UsuarioImportPreviewDTO(true, List.of(), 1));
    when(centroRepository.findByNombreIgnoreCase(anyString())).thenReturn(Optional.of(centro));
    when(cursoAcademicoRepository.findByNombreIgnoreCase(anyString()))
        .thenReturn(Optional.of(curso));
    when(grupoRepository.findByNombreIgnoreCaseAndCentroIdAndCursoAcademicoId(
            anyString(), anyInt(), anyInt()))
        .thenReturn(Optional.of(grupo));
    when(usuarioRepository.existsByUsername(anyString())).thenReturn(false);
    when(usuarioRepository.existsByEmail("j@ies.es")).thenReturn(true);

    assertThatThrownBy(() -> service.importar(new byte[] {1}))
        .isInstanceOf(UsuarioImportException.class)
        .satisfies(
            ex -> {
              List<UsuarioImportErrorDTO> err = ((UsuarioImportException) ex).getErrores();
              assertThat(err).anyMatch(e -> e.campo().equals("email"));
            });
    verify(usuarioRepository, never()).save(any());
  }

  @Test
  void importar_emailNulo_noBuscaEnBD() {
    List<UsuarioImportRowDTO> filas = List.of(filaValida(1, "alumno01"));
    when(parser.parsear(any())).thenReturn(filas);
    when(validator.validar(filas)).thenReturn(new UsuarioImportPreviewDTO(true, List.of(), 1));
    stubEntidadesOk();

    service.importar(new byte[] {1});

    verify(usuarioRepository, never()).existsByEmail(anyString());
  }

  @Test
  void importar_multifila_preFlight_recogeErroresDeTodas() {
    List<UsuarioImportRowDTO> filas = List.of(filaValida(1, "user1"), filaValida(2, "user2"));
    when(parser.parsear(any())).thenReturn(filas);
    when(validator.validar(filas)).thenReturn(new UsuarioImportPreviewDTO(true, List.of(), 2));
    when(centroRepository.findByNombreIgnoreCase(anyString())).thenReturn(Optional.of(centro));
    when(cursoAcademicoRepository.findByNombreIgnoreCase(anyString()))
        .thenReturn(Optional.of(curso));
    when(grupoRepository.findByNombreIgnoreCaseAndCentroIdAndCursoAcademicoId(
            anyString(), anyInt(), anyInt()))
        .thenReturn(Optional.of(grupo));
    when(usuarioRepository.existsByUsername(anyString())).thenReturn(true);

    assertThatThrownBy(() -> service.importar(new byte[] {1}))
        .isInstanceOf(UsuarioImportException.class)
        .satisfies(
            ex -> {
              List<UsuarioImportErrorDTO> err = ((UsuarioImportException) ex).getErrores();
              assertThat(err).hasSize(2);
            });
    verify(usuarioRepository, never()).save(any());
  }

  // ── Password y seguridad ──────────────────────────────────────────────────

  @Test
  void importar_passwordEncriptadoConBCrypt() {
    List<UsuarioImportRowDTO> filas = List.of(filaValida(1, "alumno01"));
    when(parser.parsear(any())).thenReturn(filas);
    when(validator.validar(filas)).thenReturn(new UsuarioImportPreviewDTO(true, List.of(), 1));
    stubEntidadesOk();
    when(passwordEncoder.encode("Abc12345")).thenReturn("$2a$bcrypt_hash");

    service.importar(new byte[] {1});

    verify(passwordEncoder).encode("Abc12345");
    verify(usuarioRepository).save(argThat(u -> "$2a$bcrypt_hash".equals(u.getPasswordHash())));
  }

  @Test
  void importar_mustChangePasswordTrue() {
    List<UsuarioImportRowDTO> filas = List.of(filaValida(1, "alumno01"));
    when(parser.parsear(any())).thenReturn(filas);
    when(validator.validar(filas)).thenReturn(new UsuarioImportPreviewDTO(true, List.of(), 1));
    stubEntidadesOk();

    service.importar(new byte[] {1});

    verify(usuarioRepository).save(argThat(u -> Boolean.TRUE.equals(u.getMustChangePassword())));
  }

  @Test
  void importar_rolAlumnoAsignado() {
    List<UsuarioImportRowDTO> filas = List.of(filaValida(1, "alumno01"));
    when(parser.parsear(any())).thenReturn(filas);
    when(validator.validar(filas)).thenReturn(new UsuarioImportPreviewDTO(true, List.of(), 1));
    stubEntidadesOk();

    service.importar(new byte[] {1});

    verify(rolRepository).findByNombre("ROLE_ALUMNO");
    verify(usuarioRepository).save(argThat(u -> u.getRoles().contains(rolAlumno)));
  }

  // ── Matrículas ────────────────────────────────────────────────────────────

  @Test
  void importar_grupoSinImparticiones_creaUsuarioSinMatriculas() {
    List<UsuarioImportRowDTO> filas = List.of(filaValida(1, "alumno01"));
    when(parser.parsear(any())).thenReturn(filas);
    when(validator.validar(filas)).thenReturn(new UsuarioImportPreviewDTO(true, List.of(), 1));
    stubEntidadesOk();
    when(imparticionRepository.findByGrupoId(1)).thenReturn(List.of());

    UsuarioImportResultado resultado = service.importar(new byte[] {1});

    assertThat(resultado.usuariosCreados()).isEqualTo(1);
    assertThat(resultado.matriculasCreadas()).isZero();
    verify(matriculaRepository, never()).save(any());
  }

  @Test
  void importar_matriculaYaExiste_noLaCreaDeNuevo() {
    List<UsuarioImportRowDTO> filas = List.of(filaValida(1, "alumno01"));
    when(parser.parsear(any())).thenReturn(filas);
    when(validator.validar(filas)).thenReturn(new UsuarioImportPreviewDTO(true, List.of(), 1));
    stubEntidadesOk();
    when(imparticionRepository.findByGrupoId(1)).thenReturn(List.of(imparticion));
    when(matriculaRepository.existsByAlumnoIdAndImparticionId(10, 1)).thenReturn(true);

    UsuarioImportResultado resultado = service.importar(new byte[] {1});

    verify(matriculaRepository, never()).save(any());
    assertThat(resultado.matriculasCreadas()).isZero();
  }

  // ── esRepetidor ───────────────────────────────────────────────────────────

  @Test
  void importar_esRepetidor_si_persisteTrue() {
    UsuarioImportRowDTO fila =
        new UsuarioImportRowDTO(
            1,
            "alumno01",
            "Juan",
            "García",
            null,
            "Abc12345",
            "IES La Madraza",
            "2025/2026",
            "DAW 1A",
            "si");
    when(parser.parsear(any())).thenReturn(List.of(fila));
    when(validator.validar(any())).thenReturn(new UsuarioImportPreviewDTO(true, List.of(), 1));
    stubEntidadesOk();
    when(imparticionRepository.findByGrupoId(1)).thenReturn(List.of(imparticion));

    service.importar(new byte[] {1});

    verify(matriculaRepository).save(argThat(m -> Boolean.TRUE.equals(m.getEsRepetidor())));
  }

  @Test
  void importar_esRepetidor_nulo_persisteFalse() {
    List<UsuarioImportRowDTO> filas = List.of(filaValida(1, "alumno01"));
    when(parser.parsear(any())).thenReturn(filas);
    when(validator.validar(filas)).thenReturn(new UsuarioImportPreviewDTO(true, List.of(), 1));
    stubEntidadesOk();
    when(imparticionRepository.findByGrupoId(1)).thenReturn(List.of(imparticion));

    service.importar(new byte[] {1});

    verify(matriculaRepository).save(argThat(m -> Boolean.FALSE.equals(m.getEsRepetidor())));
  }

  // ── Parser exception propagación ─────────────────────────────────────────

  @Test
  void importar_archivoBinarioInvalido_lanzaIllegalArgumentException() {
    when(parser.parsear(any())).thenThrow(new IllegalArgumentException("No es xlsx"));

    assertThatThrownBy(() -> service.importar(new byte[] {0}))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
