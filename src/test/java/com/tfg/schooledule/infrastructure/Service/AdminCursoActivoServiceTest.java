package com.tfg.schooledule.infrastructure.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tfg.schooledule.domain.entity.CursoAcademico;
import com.tfg.schooledule.infrastructure.repository.CursoAcademicoRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminCursoActivoServiceTest {

  @Mock private CursoAcademicoRepository cursoAcademicoRepository;

  @InjectMocks private AdminCursoActivoService adminCursoActivoService;

  @Test
  void getCursoActivo_debeRetornarCursoConActivoTrue() {
    CursoAcademico curso =
        CursoAcademico.builder()
            .id(1)
            .nombre("2025/2026")
            .fechaInicio(LocalDate.of(2025, 9, 1))
            .fechaFin(LocalDate.of(2026, 6, 30))
            .activo(true)
            .build();
    when(cursoAcademicoRepository.findByActivo(true)).thenReturn(Optional.of(curso));

    CursoAcademico result = adminCursoActivoService.getCursoActivo();

    assertThat(result.getId()).isEqualTo(1);
    assertThat(result.getNombre()).isEqualTo("2025/2026");
  }

  @Test
  void getCursoActivo_sinNingunActivo_lanzaIllegalState() {
    when(cursoAcademicoRepository.findByActivo(true)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> adminCursoActivoService.getCursoActivo())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("No hay curso académico activo");
  }

  @Test
  void getCursoActivoId_retornaIdDelCursoActivo() {
    CursoAcademico curso =
        CursoAcademico.builder()
            .id(42)
            .nombre("2025/2026")
            .fechaInicio(LocalDate.of(2025, 9, 1))
            .fechaFin(LocalDate.of(2026, 6, 30))
            .activo(true)
            .build();
    when(cursoAcademicoRepository.findByActivo(true)).thenReturn(Optional.of(curso));

    assertThat(adminCursoActivoService.getCursoActivoId()).isEqualTo(42);
  }
}
