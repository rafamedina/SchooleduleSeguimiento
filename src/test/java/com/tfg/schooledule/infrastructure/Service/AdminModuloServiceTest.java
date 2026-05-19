package com.tfg.schooledule.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tfg.schooledule.domain.entity.Modulo;
import com.tfg.schooledule.infrastructure.repository.CriterioEvaluacionRepository;
import com.tfg.schooledule.infrastructure.repository.ImparticionRepository;
import com.tfg.schooledule.infrastructure.repository.ModuloRepository;
import com.tfg.schooledule.infrastructure.repository.ResultadoAprendizajeRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminModuloServiceTest {

  @Mock private ModuloRepository moduloRepository;
  @Mock private ImparticionRepository imparticionRepository;
  @Mock private ResultadoAprendizajeRepository raRepository;
  @Mock private CriterioEvaluacionRepository ceRepository;
  @Mock private ModuloImportService moduloImportService;

  @InjectMocks private AdminModuloService adminModuloService;

  // ── importarModulo ────────────────────────────────────────────────────────

  @Test
  void importarModulo_codigoNuevo_creaModuloYLlamaAImportService() {
    when(moduloRepository.findByCodigo("DAW01")).thenReturn(Optional.empty());
    Modulo guardado = Modulo.builder().id(10).codigo("DAW01").nombre("Desarrollo Web").build();
    when(moduloRepository.save(any())).thenReturn(guardado);
    when(moduloImportService.importar(any(), any(), any())).thenReturn(5);

    int result = adminModuloService.importarModulo("DAW01", "Desarrollo Web", 1, new byte[] {1});

    assertThat(result).isEqualTo(5);
    verify(moduloRepository).save(argThat(m -> "DAW01".equals(m.getCodigo())));
    verify(moduloImportService).importar(any(), any(), any());
  }

  @Test
  void importarModulo_codigoExistente_reutilizaModuloSinCrear() {
    Modulo existente = Modulo.builder().id(7).codigo("DAW01").nombre("Existente").build();
    when(moduloRepository.findByCodigo("DAW01")).thenReturn(Optional.of(existente));
    when(moduloImportService.importar(any(), any(), any())).thenReturn(3);

    adminModuloService.importarModulo("DAW01", "Nuevo Nombre Ignorado", 2, new byte[] {1});

    verify(moduloRepository, never()).save(any());
    verify(moduloImportService).importar(any(), any(), any());
  }

  // ── toggleActivo ──────────────────────────────────────────────────────────

  @Test
  void toggleActivo_conImparticiones_lanzaIllegalStateException() {
    Modulo modulo = Modulo.builder().id(1).codigo("DAW01").nombre("Test").activo(true).build();
    when(moduloRepository.findById(1)).thenReturn(Optional.of(modulo));
    when(imparticionRepository.existsByModuloId(1)).thenReturn(true);

    assertThatThrownBy(() -> adminModuloService.toggleActivo(1))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("imparticiones");
    verify(moduloRepository, never()).save(any());
  }

  @Test
  void toggleActivo_conResultadosAprendizaje_lanzaIllegalStateException() {
    Modulo modulo = Modulo.builder().id(2).codigo("DAW02").nombre("Test").activo(true).build();
    when(moduloRepository.findById(2)).thenReturn(Optional.of(modulo));
    when(imparticionRepository.existsByModuloId(2)).thenReturn(false);
    when(raRepository.existsByModuloId(2)).thenReturn(true);

    assertThatThrownBy(() -> adminModuloService.toggleActivo(2))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("resultados de aprendizaje");
    verify(moduloRepository, never()).save(any());
  }

  @Test
  void toggleActivo_sinDependencias_invierteActivo() {
    Modulo modulo = Modulo.builder().id(3).codigo("DAW03").nombre("Test").activo(true).build();
    when(moduloRepository.findById(3)).thenReturn(Optional.of(modulo));
    when(imparticionRepository.existsByModuloId(3)).thenReturn(false);
    when(raRepository.existsByModuloId(3)).thenReturn(false);

    adminModuloService.toggleActivo(3);

    verify(moduloRepository).save(argThat(m -> Boolean.FALSE.equals(m.getActivo())));
  }

  @Test
  void toggleActivo_moduloInactivo_activaDirectamente() {
    Modulo modulo = Modulo.builder().id(4).codigo("DAW04").nombre("Test").activo(false).build();
    when(moduloRepository.findById(4)).thenReturn(Optional.of(modulo));

    adminModuloService.toggleActivo(4);

    verify(moduloRepository).save(argThat(m -> Boolean.TRUE.equals(m.getActivo())));
    verify(imparticionRepository, never()).existsByModuloId(any());
  }
}
