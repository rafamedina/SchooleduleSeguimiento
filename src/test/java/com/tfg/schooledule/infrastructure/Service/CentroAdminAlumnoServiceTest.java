package com.tfg.schooledule.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.tfg.schooledule.domain.dto.AdminAlumnoListDTO;
import com.tfg.schooledule.domain.dto.AdminMatriculaFormDTO;
import com.tfg.schooledule.domain.dto.AdminMatriculaListDTO;
import com.tfg.schooledule.domain.entity.*;
import com.tfg.schooledule.domain.enums.EstadoMatricula;
import com.tfg.schooledule.infrastructure.repository.*;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class CentroAdminAlumnoServiceTest {

  @Mock private CentroAdminContextService context;
  @Mock private AdminAlumnoService adminAlumnoService;
  @Mock private MatriculaRepository matriculaRepository;
  @Mock private ImparticionRepository imparticionRepository;

  @InjectMocks private CentroAdminAlumnoService service;

  private static final int ADMIN_ID = 10;
  private static final int CENTRO_A = 1;

  private Centro centroA;
  private Usuario alumno;
  private Imparticion imparticion;
  private Matricula matricula;

  @BeforeEach
  void setUp() {
    centroA = new Centro();
    centroA.setId(CENTRO_A);

    Rol rolAlumno = new Rol();
    rolAlumno.setNombre("ROLE_ALUMNO");

    alumno = new Usuario();
    alumno.setId(20);
    alumno.setNombre("Ana");
    alumno.setApellidos("García");
    alumno.setEmail("ana@ies.es");
    alumno.setRoles(Set.of(rolAlumno));

    Grupo grupo = new Grupo();
    grupo.setId(5);
    grupo.setCentro(centroA);

    Modulo modulo = new Modulo();
    modulo.setNombre("SGE");

    imparticion = new Imparticion();
    imparticion.setId(7);
    imparticion.setCentro(centroA);
    imparticion.setGrupo(grupo);
    imparticion.setModulo(modulo);

    matricula = new Matricula();
    matricula.setId(3);
    matricula.setAlumno(alumno);
    matricula.setImparticion(imparticion);
    matricula.setEstado(EstadoMatricula.ACTIVA);
    matricula.setEsRepetidor(false);
  }

  @Test
  void listarAlumnos_retornaAlumnosConMatriculasEnCentros() {
    when(context.getCentroIdsDelAdmin(ADMIN_ID)).thenReturn(Set.of(CENTRO_A));
    when(matriculaRepository.findByCentroIds(anyCollection())).thenReturn(List.of(matricula));

    List<AdminAlumnoListDTO> result = service.listarAlumnosDeCentros(ADMIN_ID);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).nombre()).isEqualTo("Ana");
  }

  @Test
  void listarAlumnos_sinDuplicadosSiAlumnoTieneVariasMatriculas() {
    Matricula m2 = new Matricula();
    m2.setId(4);
    m2.setAlumno(alumno);
    m2.setImparticion(imparticion);
    m2.setEstado(EstadoMatricula.ACTIVA);
    m2.setEsRepetidor(false);

    when(context.getCentroIdsDelAdmin(ADMIN_ID)).thenReturn(Set.of(CENTRO_A));
    when(matriculaRepository.findByCentroIds(anyCollection())).thenReturn(List.of(matricula, m2));

    List<AdminAlumnoListDTO> result = service.listarAlumnosDeCentros(ADMIN_ID);

    assertThat(result).hasSize(1);
  }

  @Test
  void crearMatricula_ok_siImparticionPertenece() {
    when(context.getCentroIdsDelAdmin(ADMIN_ID)).thenReturn(Set.of(CENTRO_A));
    when(imparticionRepository.existsByIdAndCentroIdIn(eq(7), anyCollection())).thenReturn(true);
    AdminMatriculaFormDTO form = new AdminMatriculaFormDTO();
    form.setImparticionId(7);
    doNothing().when(adminAlumnoService).crearMatricula(20, form);

    service.crearMatricula(ADMIN_ID, 20, form);

    verify(adminAlumnoService).crearMatricula(20, form);
  }

  @Test
  void crearMatricula_lanza_siImparticionEsDeOtroCentro() {
    when(context.getCentroIdsDelAdmin(ADMIN_ID)).thenReturn(Set.of(CENTRO_A));
    when(imparticionRepository.existsByIdAndCentroIdIn(eq(99), anyCollection())).thenReturn(false);
    AdminMatriculaFormDTO form = new AdminMatriculaFormDTO();
    form.setImparticionId(99);

    assertThatThrownBy(() -> service.crearMatricula(ADMIN_ID, 20, form))
        .isInstanceOf(AccessDeniedException.class);
    verify(adminAlumnoService, never()).crearMatricula(anyInt(), any());
  }

  @Test
  void eliminarMatricula_ok_siPertenece() {
    doNothing().when(context).validateMatriculaBelongsToCentroAdmin(ADMIN_ID, 3);
    doNothing().when(adminAlumnoService).eliminarMatricula(3);

    service.eliminarMatricula(ADMIN_ID, 3);

    verify(adminAlumnoService).eliminarMatricula(3);
  }

  @Test
  void eliminarMatricula_lanza_siEsDeOtroCentro() {
    doThrow(new AccessDeniedException("no"))
        .when(context)
        .validateMatriculaBelongsToCentroAdmin(ADMIN_ID, 99);

    assertThatThrownBy(() -> service.eliminarMatricula(ADMIN_ID, 99))
        .isInstanceOf(AccessDeniedException.class);
    verify(adminAlumnoService, never()).eliminarMatricula(anyInt());
  }

  @Test
  void listarMatriculas_ok_siAlumnoPerteneceAlCentro() {
    doNothing().when(context).validateUsuarioGestionablePorCentroAdmin(ADMIN_ID, 20);
    var dto =
        new AdminMatriculaListDTO(3, "SGE", "1DAW-A", "IES Getafe", EstadoMatricula.ACTIVA, false);
    when(adminAlumnoService.listarMatriculas(20)).thenReturn(List.of(dto));

    List<AdminMatriculaListDTO> result = service.listarMatriculas(ADMIN_ID, 20);

    assertThat(result).hasSize(1);
  }
}
