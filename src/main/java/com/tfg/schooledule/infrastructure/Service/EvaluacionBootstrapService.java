package com.tfg.schooledule.infrastructure.service;

import com.tfg.schooledule.domain.entity.Imparticion;
import com.tfg.schooledule.domain.entity.ItemEvaluable;
import com.tfg.schooledule.domain.entity.Modulo;
import com.tfg.schooledule.domain.entity.PeriodoEvaluacion;
import com.tfg.schooledule.domain.entity.ResultadoAprendizaje;
import com.tfg.schooledule.domain.enums.TipoActividad;
import com.tfg.schooledule.infrastructure.repository.ImparticionRepository;
import com.tfg.schooledule.infrastructure.repository.ItemEvaluableRepository;
import com.tfg.schooledule.infrastructure.repository.PeriodoEvaluacionRepository;
import com.tfg.schooledule.infrastructure.repository.ResultadoAprendizajeRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EvaluacionBootstrapService {

  private final ImparticionRepository imparticionRepository;
  private final PeriodoEvaluacionRepository periodoRepository;
  private final ItemEvaluableRepository itemRepository;
  private final ResultadoAprendizajeRepository raRepository;

  public EvaluacionBootstrapService(
      ImparticionRepository imparticionRepository,
      PeriodoEvaluacionRepository periodoRepository,
      ItemEvaluableRepository itemRepository,
      ResultadoAprendizajeRepository raRepository) {
    this.imparticionRepository = imparticionRepository;
    this.periodoRepository = periodoRepository;
    this.itemRepository = itemRepository;
    this.raRepository = raRepository;
  }

  public void bootstrapParaImparticion(Imparticion imparticion) {
    Integer cursoId = imparticion.getGrupo().getCursoAcademico().getId();
    List<ResultadoAprendizaje> ras =
        raRepository.findByModuloIdAndCursoAcademicoIdOrderByCodigoAsc(
            imparticion.getModulo().getId(), cursoId);
    if (ras.isEmpty()) return;
    crearPeriodoEItems(imparticion, ras);
  }

  public void bootstrapParaRas(
      Modulo modulo, List<ResultadoAprendizaje> ras, Integer cursoAcademicoId) {
    if (ras.isEmpty()) return;
    List<Imparticion> imparticiones =
        imparticionRepository.findByFiltro(null, null, modulo.getId(), null, cursoAcademicoId);
    for (Imparticion imp : imparticiones) {
      crearPeriodoEItems(imp, ras);
    }
  }

  private void crearPeriodoEItems(Imparticion imp, List<ResultadoAprendizaje> ras) {
    if (periodoRepository.existsByImparticionId(imp.getId())) return;
    PeriodoEvaluacion periodo =
        periodoRepository.save(
            PeriodoEvaluacion.builder()
                .imparticion(imp)
                .nombre("Evaluación")
                .peso(new BigDecimal("100"))
                .build());
    for (ResultadoAprendizaje ra : ras) {
      itemRepository.save(
          ItemEvaluable.builder()
              .imparticion(imp)
              .periodoEvaluacion(periodo)
              .nombre(ra.getCodigo())
              .tipo(TipoActividad.EXAMEN)
              .resultadoAprendizaje(ra)
              .build());
    }
  }
}
