package com.tfg.schooledule.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.tfg.schooledule.domain.dto.AdminCursoFormDTO;
import com.tfg.schooledule.domain.entity.CursoAcademico;
import com.tfg.schooledule.infrastructure.mapper.AdminCursoMapper;
import com.tfg.schooledule.infrastructure.repository.CursoAcademicoRepository;
import com.tfg.schooledule.infrastructure.repository.GrupoRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminCursoServiceTest {

  @Mock private CursoAcademicoRepository cursoAcademicoRepository;
  @Mock private GrupoRepository grupoRepository;
  @Mock private AdminCursoMapper adminCursoMapper;

  @InjectMocks private AdminCursoService adminCursoService;

  private AdminCursoFormDTO buildForm(String nombre) {
    AdminCursoFormDTO form = new AdminCursoFormDTO();
    form.setNombre(nombre);
    form.setFechaInicio(LocalDate.of(2024, 9, 1));
    form.setFechaFin(LocalDate.of(2025, 6, 30));
    return form;
  }

  @Test
  void crear_cursoValido_persisteConActivoFalse() {
    AdminCursoFormDTO form = buildForm("2024/2025");

    when(cursoAcademicoRepository.existsByNombre("2024/2025")).thenReturn(false);

    adminCursoService.crear(form);

    verify(cursoAcademicoRepository)
        .save(
            argThat(c -> "2024/2025".equals(c.getNombre()) && Boolean.FALSE.equals(c.getActivo())));
  }

  @Test
  void crear_nombreDuplicado_lanzaIllegalArgumentException() {
    AdminCursoFormDTO form = buildForm("2024/2025");

    when(cursoAcademicoRepository.existsByNombre("2024/2025")).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> adminCursoService.crear(form));
    verify(cursoAcademicoRepository, never()).save(any());
  }

  @Test
  void activar_llamaDesactivarTodosYPoneActivoTrue() {
    CursoAcademico curso = CursoAcademico.builder().id(1).nombre("2024/2025").activo(false).build();

    when(cursoAcademicoRepository.findById(1)).thenReturn(Optional.of(curso));
    when(cursoAcademicoRepository.desactivarTodosExcepto(1)).thenReturn(0);

    adminCursoService.activar(1);

    verify(cursoAcademicoRepository).desactivarTodosExcepto(1);
    verify(cursoAcademicoRepository).save(argThat(c -> Boolean.TRUE.equals(c.getActivo())));
  }

  @Test
  void activar_cursoYaActivo_sigueActivandoCorrectamente() {
    CursoAcademico nuevo = CursoAcademico.builder().id(3).nombre("2024/2025").activo(false).build();

    when(cursoAcademicoRepository.findById(3)).thenReturn(Optional.of(nuevo));
    when(cursoAcademicoRepository.desactivarTodosExcepto(3)).thenReturn(1);

    adminCursoService.activar(3);

    verify(cursoAcademicoRepository).desactivarTodosExcepto(3);
    verify(cursoAcademicoRepository).save(argThat(c -> Boolean.TRUE.equals(c.getActivo())));
  }

  @Test
  void cerrar_poneCursoInactivo() {
    CursoAcademico curso = CursoAcademico.builder().id(1).nombre("2024/2025").activo(true).build();

    when(cursoAcademicoRepository.findById(1)).thenReturn(Optional.of(curso));

    adminCursoService.cerrar(1);

    verify(cursoAcademicoRepository).save(argThat(c -> Boolean.FALSE.equals(c.getActivo())));
  }

  @Test
  void activar_cursoNoExiste_lanzaEntityNotFoundException() {
    when(cursoAcademicoRepository.findById(999)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> adminCursoService.activar(999))
        .isInstanceOf(EntityNotFoundException.class);
  }
}
