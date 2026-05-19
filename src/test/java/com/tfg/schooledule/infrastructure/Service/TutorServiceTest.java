package com.tfg.schooledule.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.tfg.schooledule.domain.dto.TutorGrupoListDTO;
import com.tfg.schooledule.domain.dto.TutorImparticionDTO;
import com.tfg.schooledule.domain.entity.*;
import com.tfg.schooledule.domain.enums.EstadoMatricula;
import com.tfg.schooledule.infrastructure.repository.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class TutorServiceTest {

  @Mock private GrupoRepository grupoRepository;
  @Mock private ImparticionRepository imparticionRepository;
  @Mock private MatriculaRepository matriculaRepository;
  @Mock private TeacherDashboardService teacherDashboardService;

  @InjectMocks private TutorService tutorService;

  private Usuario tutor;
  private Usuario otroProfesor;
  private Centro centro;
  private CursoAcademico curso;
  private Grupo grupo1;
  private Grupo grupo2;
  private Modulo modulo;
  private Imparticion imparticion;

  @BeforeEach
  void setUp() {
    tutor = Usuario.builder().id(1).nombre("Ana").apellidos("García").email("ana@tfg.com").build();
    otroProfesor =
        Usuario.builder().id(2).nombre("Luis").apellidos("Pérez").email("luis@tfg.com").build();
    centro = Centro.builder().id(10).nombre("IES Test").build();
    curso = CursoAcademico.builder().id(5).nombre("2024/2025").build();
    modulo = Modulo.builder().id(3).codigo("MOD01").nombre("Programación").build();

    grupo1 =
        Grupo.builder()
            .id(100)
            .nombre("1DAW-A")
            .centro(centro)
            .cursoAcademico(curso)
            .tutor(tutor)
            .build();
    grupo2 =
        Grupo.builder()
            .id(101)
            .nombre("1DAW-B")
            .centro(centro)
            .cursoAcademico(curso)
            .tutor(tutor)
            .build();

    imparticion =
        Imparticion.builder()
            .id(50)
            .modulo(modulo)
            .grupo(grupo1)
            .profesor(tutor)
            .centro(centro)
            .build();
  }

  // 4.1.1
  @Test
  void getGruposDeTutor_returnsOnlyTutorGroups() {
    when(grupoRepository.findByTutorId(1)).thenReturn(List.of(grupo1, grupo2));
    when(imparticionRepository.findByGrupoId(100)).thenReturn(List.of(imparticion));
    when(imparticionRepository.findByGrupoId(101)).thenReturn(List.of());
    when(matriculaRepository.findByImparticionIdAndEstado(50, EstadoMatricula.ACTIVA))
        .thenReturn(List.of());

    List<TutorGrupoListDTO> result = tutorService.getGruposDeTutor(1);

    assertThat(result).hasSize(2);
    assertThat(result)
        .extracting(TutorGrupoListDTO::grupoNombre)
        .containsExactlyInAnyOrder("1DAW-A", "1DAW-B");
  }

  // 4.1.2
  @Test
  void getGruposDeTutor_returnsEmpty_whenNoGroups() {
    when(grupoRepository.findByTutorId(99)).thenReturn(List.of());

    List<TutorGrupoListDTO> result = tutorService.getGruposDeTutor(99);

    assertThat(result).isEmpty();
  }

  // 4.1.3
  @Test
  void validateTutorOwnership_throws_whenNotTutor() {
    when(grupoRepository.existsByIdAndTutorId(100, 99)).thenReturn(false);

    assertThatThrownBy(() -> tutorService.validateTutorOwnership(99, 100))
        .isInstanceOf(AccessDeniedException.class);
  }

  // 4.1.4
  @Test
  void validateTutorOwnership_passes_whenTutor() {
    when(grupoRepository.existsByIdAndTutorId(100, 1)).thenReturn(true);

    tutorService.validateTutorOwnership(1, 100);
    // no exception
  }

  // 4.1.5
  @Test
  void getImparticionesByGrupo_flagsEditableCorrectly() {
    Imparticion imp1 =
        Imparticion.builder()
            .id(50)
            .modulo(modulo)
            .grupo(grupo1)
            .profesor(tutor)
            .centro(centro)
            .build();
    Imparticion imp2 =
        Imparticion.builder()
            .id(51)
            .modulo(modulo)
            .grupo(grupo1)
            .profesor(otroProfesor)
            .centro(centro)
            .build();

    when(grupoRepository.existsByIdAndTutorId(100, 1)).thenReturn(true);
    when(imparticionRepository.findByGrupoId(100)).thenReturn(List.of(imp1, imp2));
    when(matriculaRepository.findByImparticionIdAndEstado(50, EstadoMatricula.ACTIVA))
        .thenReturn(List.of());
    when(matriculaRepository.findByImparticionIdAndEstado(51, EstadoMatricula.ACTIVA))
        .thenReturn(List.of());

    List<TutorImparticionDTO> result = tutorService.getImparticionesByGrupo(1, 100);

    assertThat(result).hasSize(2);
    assertThat(
            result.stream()
                .filter(i -> i.imparticionId().equals(50))
                .findFirst()
                .get()
                .puedeEditarNotas())
        .isTrue();
    assertThat(
            result.stream()
                .filter(i -> i.imparticionId().equals(51))
                .findFirst()
                .get()
                .puedeEditarNotas())
        .isFalse();
  }

  // 4.1.6
  @Test
  void getImparticionesByGrupo_throwsAccessDenied_whenNotTutor() {
    when(grupoRepository.existsByIdAndTutorId(100, 99)).thenReturn(false);

    assertThatThrownBy(() -> tutorService.getImparticionesByGrupo(99, 100))
        .isInstanceOf(AccessDeniedException.class);
  }

  // 4.1.7
  @Test
  void getStudentGradesAsTutor_succeeds_whenTutor() {
    Matricula matricula =
        Matricula.builder()
            .id(200)
            .alumno(Usuario.builder().id(5).nombre("Carlos").apellidos("López").build())
            .imparticion(imparticion)
            .centro(centro)
            .estado(EstadoMatricula.ACTIVA)
            .build();

    when(matriculaRepository.findById(200)).thenReturn(Optional.of(matricula));
    when(grupoRepository.existsByIdAndTutorId(100, 1)).thenReturn(true);
    when(teacherDashboardService.buildGradesDTO(matricula)).thenReturn(null);

    tutorService.getStudentGradesAsTutor(1, 200);

    verify(teacherDashboardService).buildGradesDTO(matricula);
  }

  // 4.1.8
  @Test
  void getStudentGradesAsTutor_throws_whenNotTutor() {
    Matricula matricula =
        Matricula.builder()
            .id(200)
            .alumno(Usuario.builder().id(5).nombre("Carlos").apellidos("López").build())
            .imparticion(imparticion)
            .centro(centro)
            .estado(EstadoMatricula.ACTIVA)
            .build();

    when(matriculaRepository.findById(200)).thenReturn(Optional.of(matricula));
    when(grupoRepository.existsByIdAndTutorId(100, 99)).thenReturn(false);

    assertThatThrownBy(() -> tutorService.getStudentGradesAsTutor(99, 200))
        .isInstanceOf(AccessDeniedException.class);
  }
}
