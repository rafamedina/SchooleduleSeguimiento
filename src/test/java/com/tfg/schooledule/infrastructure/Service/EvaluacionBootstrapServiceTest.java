package com.tfg.schooledule.infrastructure.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tfg.schooledule.domain.entity.*;
import com.tfg.schooledule.infrastructure.repository.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EvaluacionBootstrapServiceTest {

  @Mock private ImparticionRepository imparticionRepository;
  @Mock private PeriodoEvaluacionRepository periodoRepository;
  @Mock private ItemEvaluableRepository itemRepository;
  @Mock private ResultadoAprendizajeRepository raRepository;

  @InjectMocks private EvaluacionBootstrapService bootstrapService;

  private Imparticion imparticionConCurso(int impId, int moduloId, int cursoId) {
    CursoAcademico curso = CursoAcademico.builder().id(cursoId).nombre("2025-2026").build();
    Grupo grupo = Grupo.builder().id(1).nombre("1DAW-A").cursoAcademico(curso).build();
    Modulo modulo = Modulo.builder().id(moduloId).codigo("DAW01").nombre("Prog").build();
    return Imparticion.builder().id(impId).modulo(modulo).grupo(grupo).build();
  }

  private ResultadoAprendizaje ra(int id, String codigo) {
    ResultadoAprendizaje ra = new ResultadoAprendizaje();
    ra.setId(id);
    ra.setCodigo(codigo);
    return ra;
  }

  // ── bootstrapParaImparticion ──────────────────────────────────────────────

  @Test
  void bootstrapParaImparticion_sinRas_noCreaNada() {
    Imparticion imp = imparticionConCurso(99, 10, 5);
    when(raRepository.findByModuloIdAndCursoAcademicoIdOrderByCodigoAsc(10, 5))
        .thenReturn(List.of());

    bootstrapService.bootstrapParaImparticion(imp);

    verify(periodoRepository, never()).save(any());
    verify(itemRepository, never()).save(any());
  }

  @Test
  void bootstrapParaImparticion_conRasSinPeriodo_creaUnPeriodoYItems() {
    Imparticion imp = imparticionConCurso(99, 10, 5);
    when(raRepository.findByModuloIdAndCursoAcademicoIdOrderByCodigoAsc(10, 5))
        .thenReturn(List.of(ra(1, "RA1"), ra(2, "RA2")));
    when(periodoRepository.existsByImparticionId(99)).thenReturn(false);
    when(periodoRepository.save(any()))
        .thenReturn(
            PeriodoEvaluacion.builder().id(1).imparticion(imp).nombre("Evaluación").build());

    bootstrapService.bootstrapParaImparticion(imp);

    verify(periodoRepository, times(1)).save(any());
    verify(itemRepository, times(2)).save(any());
  }

  @Test
  void bootstrapParaImparticion_periodoYaExiste_noCreaNada() {
    Imparticion imp = imparticionConCurso(99, 10, 5);
    when(raRepository.findByModuloIdAndCursoAcademicoIdOrderByCodigoAsc(10, 5))
        .thenReturn(List.of(ra(1, "RA1")));
    when(periodoRepository.existsByImparticionId(99)).thenReturn(true);

    bootstrapService.bootstrapParaImparticion(imp);

    verify(periodoRepository, never()).save(any());
    verify(itemRepository, never()).save(any());
  }

  // ── bootstrapParaRas ─────────────────────────────────────────────────────

  @Test
  void bootstrapParaRas_rasVacias_noConsultaImparticiones() {
    Modulo modulo = Modulo.builder().id(10).build();

    bootstrapService.bootstrapParaRas(modulo, List.of(), 5);

    verify(imparticionRepository, never()).findByFiltro(any(), any(), any(), any(), any());
    verify(periodoRepository, never()).save(any());
  }

  @Test
  void bootstrapParaRas_sinImparticiones_noCreaNada() {
    Modulo modulo = Modulo.builder().id(10).build();
    when(imparticionRepository.findByFiltro(null, null, 10, null, 5)).thenReturn(List.of());

    bootstrapService.bootstrapParaRas(modulo, List.of(ra(1, "RA1")), 5);

    verify(periodoRepository, never()).save(any());
    verify(itemRepository, never()).save(any());
  }

  @Test
  void bootstrapParaRas_conImparticionSinPeriodo_creaItemsPorRA() {
    Modulo modulo = Modulo.builder().id(10).build();
    Imparticion imp = imparticionConCurso(99, 10, 5);
    when(imparticionRepository.findByFiltro(null, null, 10, null, 5)).thenReturn(List.of(imp));
    when(periodoRepository.existsByImparticionId(99)).thenReturn(false);
    when(periodoRepository.save(any()))
        .thenReturn(
            PeriodoEvaluacion.builder().id(1).imparticion(imp).nombre("Evaluación").build());

    bootstrapService.bootstrapParaRas(modulo, List.of(ra(1, "RA1"), ra(2, "RA2")), 5);

    verify(periodoRepository, times(1)).save(any());
    verify(itemRepository, times(2)).save(any());
  }

  @Test
  void bootstrapParaRas_periodoYaExiste_omiteImparticion() {
    Modulo modulo = Modulo.builder().id(10).build();
    Imparticion imp = imparticionConCurso(99, 10, 5);
    when(imparticionRepository.findByFiltro(null, null, 10, null, 5)).thenReturn(List.of(imp));
    when(periodoRepository.existsByImparticionId(99)).thenReturn(true);

    bootstrapService.bootstrapParaRas(modulo, List.of(ra(1, "RA1")), 5);

    verify(periodoRepository, never()).save(any());
    verify(itemRepository, never()).save(any());
  }
}
