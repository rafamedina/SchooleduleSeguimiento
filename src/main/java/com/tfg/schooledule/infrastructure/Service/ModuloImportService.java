package com.tfg.schooledule.infrastructure.service;

import com.tfg.schooledule.domain.dto.ModuloImportPreviewDTO;
import com.tfg.schooledule.domain.dto.ModuloImportRowDTO;
import com.tfg.schooledule.domain.entity.CriterioEvaluacion;
import com.tfg.schooledule.domain.entity.CursoAcademico;
import com.tfg.schooledule.domain.entity.Modulo;
import com.tfg.schooledule.domain.entity.ResultadoAprendizaje;
import com.tfg.schooledule.domain.enums.InstrumentoEvaluacion;
import com.tfg.schooledule.domain.exception.ModuloImportException;
import com.tfg.schooledule.infrastructure.repository.CriterioEvaluacionRepository;
import com.tfg.schooledule.infrastructure.repository.CursoAcademicoRepository;
import com.tfg.schooledule.infrastructure.repository.ModuloRepository;
import com.tfg.schooledule.infrastructure.repository.ResultadoAprendizajeRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ModuloImportService {

  private final ModuloExcelParserService parser;
  private final ModuloImportValidatorService validatorService;
  private final ModuloRepository moduloRepository;
  private final CursoAcademicoRepository cursoAcademicoRepository;
  private final ResultadoAprendizajeRepository raRepository;
  private final CriterioEvaluacionRepository ceRepository;

  public ModuloImportService(
      ModuloExcelParserService parser,
      ModuloImportValidatorService validatorService,
      ModuloRepository moduloRepository,
      CursoAcademicoRepository cursoAcademicoRepository,
      ResultadoAprendizajeRepository raRepository,
      CriterioEvaluacionRepository ceRepository) {
    this.parser = parser;
    this.validatorService = validatorService;
    this.moduloRepository = moduloRepository;
    this.cursoAcademicoRepository = cursoAcademicoRepository;
    this.raRepository = raRepository;
    this.ceRepository = ceRepository;
  }

  @Transactional(readOnly = true)
  public ModuloImportPreviewDTO validarArchivo(
      byte[] bytes, Integer moduloId, Integer cursoAcademicoId) {
    List<ModuloImportRowDTO> filas = parser.parsear(bytes);
    return validatorService.validar(filas);
  }

  public int importar(byte[] bytes, Integer moduloId, Integer cursoAcademicoId) {
    List<ModuloImportRowDTO> filas = parser.parsear(bytes);
    ModuloImportPreviewDTO preview = validatorService.validar(filas);

    if (!preview.valido()) {
      throw new ModuloImportException(preview.errores());
    }

    Modulo modulo =
        moduloRepository
            .findById(moduloId)
            .orElseThrow(
                () -> new EntityNotFoundException("Módulo no encontrado con id: " + moduloId));

    CursoAcademico curso =
        cursoAcademicoRepository
            .findById(cursoAcademicoId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Curso académico no encontrado con id: " + cursoAcademicoId));

    verificarNoHayRasPrevios(moduloId, cursoAcademicoId);

    Map<String, List<ModuloImportRowDTO>> filasPorRa = new LinkedHashMap<>();
    for (ModuloImportRowDTO fila : filas) {
      filasPorRa.computeIfAbsent(fila.raCodigo(), k -> new java.util.ArrayList<>()).add(fila);
    }

    int totalCes = 0;
    for (Map.Entry<String, List<ModuloImportRowDTO>> entry : filasPorRa.entrySet()) {
      ModuloImportRowDTO primeraFila = entry.getValue().get(0);
      ResultadoAprendizaje ra =
          raRepository.save(
              ResultadoAprendizaje.builder()
                  .modulo(modulo)
                  .cursoAcademico(curso)
                  .codigo(primeraFila.raCodigo().trim())
                  .descripcion(primeraFila.raDescripcion().trim())
                  .pesoSugerido(primeraFila.raPeso())
                  .build());

      for (ModuloImportRowDTO fila : entry.getValue()) {
        InstrumentoEvaluacion instrumento =
            InstrumentoEvaluacion.fromTexto(fila.instrumento()).orElse(null);
        Short trimestre = fila.trimestre() != null ? fila.trimestre().shortValue() : null;

        ceRepository.save(
            CriterioEvaluacion.builder()
                .resultadoAprendizaje(ra)
                .codigo(fila.ceCodigo().trim())
                .descripcion(fila.ceDescripcion().trim())
                .peso(fila.cePeso())
                .instrumento(instrumento)
                .unidadDidactica(fila.unidadDidactica())
                .trimestre(trimestre)
                .build());
        totalCes++;
      }
    }

    return totalCes;
  }

  private void verificarNoHayRasPrevios(Integer moduloId, Integer cursoAcademicoId) {
    if (raRepository.existsByModuloIdAndCursoAcademicoId(moduloId, cursoAcademicoId)) {
      throw new IllegalStateException(
          "Ya existen RAs para este módulo y curso. Elimínalos antes de reimportar.");
    }
  }
}
