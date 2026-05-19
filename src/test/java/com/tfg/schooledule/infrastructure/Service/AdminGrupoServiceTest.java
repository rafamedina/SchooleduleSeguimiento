package com.tfg.schooledule.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.tfg.schooledule.domain.dto.AdminGrupoFormDTO;
import com.tfg.schooledule.domain.entity.Centro;
import com.tfg.schooledule.domain.entity.CursoAcademico;
import com.tfg.schooledule.domain.entity.Grupo;
import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.infrastructure.mapper.AdminGrupoMapper;
import com.tfg.schooledule.infrastructure.repository.CentroRepository;
import com.tfg.schooledule.infrastructure.repository.CursoAcademicoRepository;
import com.tfg.schooledule.infrastructure.repository.GrupoRepository;
import com.tfg.schooledule.infrastructure.repository.ImparticionRepository;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminGrupoServiceTest {

  @Mock private GrupoRepository grupoRepository;
  @Mock private ImparticionRepository imparticionRepository;
  @Mock private CentroRepository centroRepository;
  @Mock private CursoAcademicoRepository cursoAcademicoRepository;
  @Mock private UsuarioRepository usuarioRepository;
  @Mock private AdminGrupoMapper adminGrupoMapper;

  @InjectMocks private AdminGrupoService adminGrupoService;

  private AdminGrupoFormDTO buildForm(String nombre, int centroId, int cursoId) {
    AdminGrupoFormDTO form = new AdminGrupoFormDTO();
    form.setNombre(nombre);
    form.setCentroId(centroId);
    form.setCursoAcademicoId(cursoId);
    return form;
  }

  @Test
  void crear_grupoValido_persisteEntidad() {
    AdminGrupoFormDTO form = buildForm("1DAW-A", 1, 1);
    Centro centro = Centro.builder().id(1).nombre("Centro Test").build();
    CursoAcademico curso = CursoAcademico.builder().id(1).nombre("2024/2025").build();

    when(centroRepository.findById(1)).thenReturn(Optional.of(centro));
    when(cursoAcademicoRepository.findById(1)).thenReturn(Optional.of(curso));
    when(grupoRepository.existsByNombreAndCentroIdAndCursoAcademicoId("1DAW-A", 1, 1))
        .thenReturn(false);

    adminGrupoService.crear(form);

    verify(grupoRepository).save(argThat(g -> "1DAW-A".equals(g.getNombre())));
  }

  @Test
  void crear_nombreDuplicado_lanzaIllegalArgumentException() {
    AdminGrupoFormDTO form = buildForm("1DAW-A", 1, 1);
    Centro centro = Centro.builder().id(1).nombre("Centro Test").build();
    CursoAcademico curso = CursoAcademico.builder().id(1).nombre("2024/2025").build();

    when(centroRepository.findById(1)).thenReturn(Optional.of(centro));
    when(cursoAcademicoRepository.findById(1)).thenReturn(Optional.of(curso));
    when(grupoRepository.existsByNombreAndCentroIdAndCursoAcademicoId("1DAW-A", 1, 1))
        .thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> adminGrupoService.crear(form));
    verify(grupoRepository, never()).save(any());
  }

  @Test
  void eliminar_grupoConImparticiones_lanzaIllegalStateException() {
    when(grupoRepository.existsById(1)).thenReturn(true);
    when(imparticionRepository.existsByGrupoId(1)).thenReturn(true);

    assertThatThrownBy(() -> adminGrupoService.eliminar(1))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("imparticiones");
    verify(grupoRepository, never()).deleteById(any());
  }

  @Test
  void eliminar_grupoSinImparticiones_eliminaCorrectamente() {
    when(grupoRepository.existsById(2)).thenReturn(true);
    when(imparticionRepository.existsByGrupoId(2)).thenReturn(false);

    adminGrupoService.eliminar(2);

    verify(grupoRepository).deleteById(2);
  }

  @Test
  void eliminar_grupoInexistente_lanzaEntityNotFoundException() {
    when(grupoRepository.existsById(999)).thenReturn(false);

    assertThatThrownBy(() -> adminGrupoService.eliminar(999))
        .isInstanceOf(EntityNotFoundException.class);
    verify(grupoRepository, never()).deleteById(any());
  }

  @Test
  void actualizar_nombreDuplicadoEnOtroGrupo_lanzaIllegalArgumentException() {
    AdminGrupoFormDTO form = buildForm("1DAW-A", 1, 1);
    Grupo grupo = Grupo.builder().id(1).nombre("1DAW-B").build();
    Centro centro = Centro.builder().id(1).nombre("Centro Test").build();
    CursoAcademico curso = CursoAcademico.builder().id(1).nombre("2024/2025").build();

    when(grupoRepository.findById(1)).thenReturn(Optional.of(grupo));
    when(centroRepository.findById(1)).thenReturn(Optional.of(centro));
    when(cursoAcademicoRepository.findById(1)).thenReturn(Optional.of(curso));
    when(grupoRepository.existsByNombreAndCentroIdAndCursoAcademicoIdAndIdNot("1DAW-A", 1, 1, 1))
        .thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> adminGrupoService.actualizar(1, form));
    verify(grupoRepository, never()).save(any());
  }

  // 7.6.1
  @Test
  void crear_conTutor_asignaTutor() {
    AdminGrupoFormDTO form = buildForm("1DAW-A", 1, 1);
    form.setTutorId(10);
    Centro centro = Centro.builder().id(1).nombre("Centro Test").build();
    CursoAcademico curso = CursoAcademico.builder().id(1).nombre("2024/2025").build();
    Usuario tutor = Usuario.builder().id(10).nombre("Ana").apellidos("García").build();

    when(centroRepository.findById(1)).thenReturn(Optional.of(centro));
    when(cursoAcademicoRepository.findById(1)).thenReturn(Optional.of(curso));
    when(grupoRepository.existsByNombreAndCentroIdAndCursoAcademicoId("1DAW-A", 1, 1))
        .thenReturn(false);
    when(usuarioRepository.findById(10)).thenReturn(Optional.of(tutor));

    adminGrupoService.crear(form);

    verify(grupoRepository).save(argThat(g -> g.getTutor() != null && g.getTutor().getId() == 10));
  }

  // 7.6.2
  @Test
  void crear_sinTutor_tutorEsNull() {
    AdminGrupoFormDTO form = buildForm("1DAW-A", 1, 1);
    Centro centro = Centro.builder().id(1).nombre("Centro Test").build();
    CursoAcademico curso = CursoAcademico.builder().id(1).nombre("2024/2025").build();

    when(centroRepository.findById(1)).thenReturn(Optional.of(centro));
    when(cursoAcademicoRepository.findById(1)).thenReturn(Optional.of(curso));
    when(grupoRepository.existsByNombreAndCentroIdAndCursoAcademicoId("1DAW-A", 1, 1))
        .thenReturn(false);

    adminGrupoService.crear(form);

    verify(grupoRepository).save(argThat(g -> g.getTutor() == null));
  }

  // 7.6.3
  @Test
  void actualizar_cambioTutor_actualizaTutor() {
    AdminGrupoFormDTO form = buildForm("1DAW-A", 1, 1);
    form.setTutorId(20);
    Grupo grupo = Grupo.builder().id(1).nombre("1DAW-A").build();
    Centro centro = Centro.builder().id(1).nombre("Centro Test").build();
    CursoAcademico curso = CursoAcademico.builder().id(1).nombre("2024/2025").build();
    Usuario nuevoTutor = Usuario.builder().id(20).nombre("Luis").apellidos("Pérez").build();

    when(grupoRepository.findById(1)).thenReturn(Optional.of(grupo));
    when(centroRepository.findById(1)).thenReturn(Optional.of(centro));
    when(cursoAcademicoRepository.findById(1)).thenReturn(Optional.of(curso));
    when(grupoRepository.existsByNombreAndCentroIdAndCursoAcademicoIdAndIdNot("1DAW-A", 1, 1, 1))
        .thenReturn(false);
    when(usuarioRepository.findById(20)).thenReturn(Optional.of(nuevoTutor));

    adminGrupoService.actualizar(1, form);

    verify(grupoRepository).save(argThat(g -> g.getTutor() != null && g.getTutor().getId() == 20));
  }

  // 7.6.4
  @Test
  void actualizar_quitarTutor_poneTutorNull() {
    AdminGrupoFormDTO form = buildForm("1DAW-A", 1, 1);
    Usuario tutorActual = Usuario.builder().id(10).nombre("Ana").apellidos("García").build();
    Grupo grupo = Grupo.builder().id(1).nombre("1DAW-A").tutor(tutorActual).build();
    Centro centro = Centro.builder().id(1).nombre("Centro Test").build();
    CursoAcademico curso = CursoAcademico.builder().id(1).nombre("2024/2025").build();

    when(grupoRepository.findById(1)).thenReturn(Optional.of(grupo));
    when(centroRepository.findById(1)).thenReturn(Optional.of(centro));
    when(cursoAcademicoRepository.findById(1)).thenReturn(Optional.of(curso));
    when(grupoRepository.existsByNombreAndCentroIdAndCursoAcademicoIdAndIdNot("1DAW-A", 1, 1, 1))
        .thenReturn(false);

    adminGrupoService.actualizar(1, form);

    verify(grupoRepository).save(argThat(g -> g.getTutor() == null));
  }
}
