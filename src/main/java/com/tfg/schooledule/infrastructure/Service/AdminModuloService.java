package com.tfg.schooledule.infrastructure.service;

import com.tfg.schooledule.domain.dto.AdminModuloCePesoDTO;
import com.tfg.schooledule.domain.dto.AdminModuloCeResumenDTO;
import com.tfg.schooledule.domain.dto.AdminModuloCursoResumenDTO;
import com.tfg.schooledule.domain.dto.AdminModuloListDTO;
import com.tfg.schooledule.domain.dto.AdminModuloPesosFormDTO;
import com.tfg.schooledule.domain.dto.AdminModuloRaPesoDTO;
import com.tfg.schooledule.domain.dto.AdminModuloRaResumenDTO;
import com.tfg.schooledule.domain.dto.AdminModuloResumenDTO;
import com.tfg.schooledule.domain.entity.CriterioEvaluacion;
import com.tfg.schooledule.domain.entity.CursoAcademico;
import com.tfg.schooledule.domain.entity.Modulo;
import com.tfg.schooledule.domain.entity.ResultadoAprendizaje;
import com.tfg.schooledule.infrastructure.repository.CriterioEvaluacionRepository;
import com.tfg.schooledule.infrastructure.repository.ImparticionRepository;
import com.tfg.schooledule.infrastructure.repository.ModuloRepository;
import com.tfg.schooledule.infrastructure.repository.ResultadoAprendizajeRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminModuloService {

  private static final String ERR_MODULO = "Módulo no encontrado: ";

  private final ModuloRepository moduloRepository;
  private final ImparticionRepository imparticionRepository;
  private final ResultadoAprendizajeRepository raRepository;
  private final CriterioEvaluacionRepository ceRepository;
  private final ModuloImportService moduloImportService;

  public AdminModuloService(
      ModuloRepository moduloRepository,
      ImparticionRepository imparticionRepository,
      ResultadoAprendizajeRepository raRepository,
      CriterioEvaluacionRepository ceRepository,
      ModuloImportService moduloImportService) {
    this.moduloRepository = moduloRepository;
    this.imparticionRepository = imparticionRepository;
    this.raRepository = raRepository;
    this.ceRepository = ceRepository;
    this.moduloImportService = moduloImportService;
  }

  private AdminModuloListDTO toListDTO(Modulo m) {
    List<String> cursosConRas = raRepository.findNombresCursosConRasByModuloId(m.getId());
    return new AdminModuloListDTO(
        m.getId(),
        m.getCodigo(),
        m.getNombre(),
        m.getActivo(),
        imparticionRepository.countByModuloId(m.getId()),
        raRepository.countByModuloId(m.getId()),
        cursosConRas);
  }

  @Transactional(readOnly = true)
  public List<AdminModuloListDTO> listarFiltrado(String nombre) {
    List<Modulo> modulos =
        (nombre == null || nombre.isBlank())
            ? moduloRepository.findAllByOrderByNombreAsc()
            : moduloRepository.findByNombreContaining(nombre);
    return modulos.stream().map(this::toListDTO).toList();
  }

  @Transactional
  public int importarModulo(String codigo, String nombre, Integer cursoAcademicoId, byte[] bytes) {
    Modulo modulo =
        moduloRepository
            .findByCodigo(codigo)
            .orElseGet(
                () ->
                    moduloRepository.save(Modulo.builder().codigo(codigo).nombre(nombre).build()));
    return moduloImportService.importar(bytes, modulo.getId(), cursoAcademicoId);
  }

  @Transactional(readOnly = true)
  public AdminModuloPesosFormDTO obtenerParaEditarPesos(Integer moduloId) {
    Modulo modulo =
        moduloRepository
            .findById(moduloId)
            .orElseThrow(() -> new EntityNotFoundException(ERR_MODULO + moduloId));

    List<ResultadoAprendizaje> ras =
        raRepository.findByModuloIdOrderByCursoAcademicoIdAscCodigoAsc(moduloId);

    List<Integer> raIds = ras.stream().map(ResultadoAprendizaje::getId).toList();

    Map<Integer, List<CriterioEvaluacion>> cesByRaId =
        raIds.isEmpty()
            ? Map.of()
            : ceRepository
                .findByResultadoAprendizajeIdInOrderByResultadoAprendizajeIdAscCodigoAsc(raIds)
                .stream()
                .collect(
                    Collectors.groupingBy(
                        ce -> ce.getResultadoAprendizaje().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()));

    List<AdminModuloRaPesoDTO> raDtos =
        ras.stream()
            .map(
                ra -> {
                  List<AdminModuloCePesoDTO> ceDtos =
                      cesByRaId.getOrDefault(ra.getId(), List.of()).stream()
                          .map(
                              ce ->
                                  new AdminModuloCePesoDTO(
                                      ce.getId(),
                                      ce.getCodigo(),
                                      ce.getDescripcion(),
                                      ce.getPeso()))
                          .collect(Collectors.toCollection(ArrayList::new));
                  String cursoNombre =
                      ra.getCursoAcademico() != null ? ra.getCursoAcademico().getNombre() : null;
                  return new AdminModuloRaPesoDTO(
                      ra.getId(),
                      ra.getCodigo(),
                      ra.getDescripcion(),
                      cursoNombre,
                      ra.getPesoSugerido(),
                      ceDtos);
                })
            .collect(Collectors.toCollection(ArrayList::new));

    return new AdminModuloPesosFormDTO(modulo.getCodigo(), modulo.getNombre(), raDtos);
  }

  @Transactional
  public void actualizarPesos(Integer moduloId, AdminModuloPesosFormDTO form) {
    Modulo modulo =
        moduloRepository
            .findById(moduloId)
            .orElseThrow(() -> new EntityNotFoundException(ERR_MODULO + moduloId));

    if (moduloRepository.existsByCodigoAndIdNot(form.getCodigo(), moduloId)) {
      throw new IllegalArgumentException(
          "Ya existe un módulo con el código '" + form.getCodigo() + "'");
    }
    modulo.setCodigo(form.getCodigo());
    modulo.setNombre(form.getNombre());
    moduloRepository.save(modulo);

    if (form.getRas() == null) return;

    for (AdminModuloRaPesoDTO raDto : form.getRas()) {
      actualizarPesoRa(moduloId, raDto);
    }
  }

  private void actualizarPesoRa(Integer moduloId, AdminModuloRaPesoDTO raDto) {
    if (raDto.getId() == null) return;
    ResultadoAprendizaje ra =
        raRepository
            .findById(raDto.getId())
            .orElseThrow(() -> new EntityNotFoundException("RA no encontrado: " + raDto.getId()));
    if (!ra.getModulo().getId().equals(moduloId)) {
      throw new IllegalArgumentException(
          "El RA " + raDto.getId() + " no pertenece al módulo " + moduloId);
    }
    ra.setPesoSugerido(raDto.getPesoSugerido());
    raRepository.save(ra);

    if (raDto.getCes() == null) return;

    for (AdminModuloCePesoDTO ceDto : raDto.getCes()) {
      actualizarPesoCe(ra, ceDto);
    }
  }

  private void actualizarPesoCe(ResultadoAprendizaje ra, AdminModuloCePesoDTO ceDto) {
    if (ceDto.getId() == null) return;
    CriterioEvaluacion ce =
        ceRepository
            .findById(ceDto.getId())
            .orElseThrow(() -> new EntityNotFoundException("CE no encontrado: " + ceDto.getId()));
    if (!ce.getResultadoAprendizaje().getId().equals(ra.getId())) {
      throw new IllegalArgumentException(
          "El CE " + ceDto.getId() + " no pertenece al RA " + ra.getId());
    }
    ce.setPeso(ceDto.getPeso());
    ceRepository.save(ce);
  }

  @Transactional(readOnly = true)
  public AdminModuloResumenDTO getResumen(Integer moduloId) {
    Modulo modulo =
        moduloRepository
            .findById(moduloId)
            .orElseThrow(() -> new EntityNotFoundException(ERR_MODULO + moduloId));

    int numImparticiones = imparticionRepository.countByModuloId(moduloId);

    List<ResultadoAprendizaje> ras =
        raRepository.findByModuloIdOrderByCursoAcademicoIdAscCodigoAsc(moduloId);

    if (ras.isEmpty()) {
      return new AdminModuloResumenDTO(
          modulo.getId(),
          modulo.getCodigo(),
          modulo.getNombre(),
          modulo.getActivo(),
          numImparticiones,
          0,
          0,
          0,
          List.of());
    }

    List<Integer> raIds = ras.stream().map(ResultadoAprendizaje::getId).toList();

    Map<Integer, List<CriterioEvaluacion>> cesByRaId =
        ceRepository
            .findByResultadoAprendizajeIdInOrderByResultadoAprendizajeIdAscCodigoAsc(raIds)
            .stream()
            .collect(
                Collectors.groupingBy(
                    ce -> ce.getResultadoAprendizaje().getId(),
                    LinkedHashMap::new,
                    Collectors.toList()));

    Map<CursoAcademico, List<ResultadoAprendizaje>> rasByCurso = new LinkedHashMap<>();
    for (ResultadoAprendizaje ra : ras) {
      rasByCurso.computeIfAbsent(ra.getCursoAcademico(), k -> new ArrayList<>()).add(ra);
    }

    List<AdminModuloCursoResumenDTO> cursos = new ArrayList<>();
    for (Map.Entry<CursoAcademico, List<ResultadoAprendizaje>> entry : rasByCurso.entrySet()) {
      List<AdminModuloRaResumenDTO> raDtos =
          entry.getValue().stream()
              .map(
                  ra -> {
                    List<AdminModuloCeResumenDTO> ceDtos =
                        cesByRaId.getOrDefault(ra.getId(), List.of()).stream()
                            .map(
                                ce ->
                                    new AdminModuloCeResumenDTO(
                                        ce.getId(),
                                        ce.getCodigo(),
                                        ce.getDescripcion(),
                                        ce.getPeso(),
                                        ce.getInstrumento() != null
                                            ? ce.getInstrumento().name()
                                            : null,
                                        ce.getUnidadDidactica(),
                                        ce.getTrimestre()))
                            .toList();
                    return new AdminModuloRaResumenDTO(
                        ra.getId(),
                        ra.getCodigo(),
                        ra.getDescripcion(),
                        ra.getPesoSugerido(),
                        ceDtos);
                  })
              .toList();

      int numCesCurso = raDtos.stream().mapToInt(r -> r.ces().size()).sum();
      cursos.add(
          new AdminModuloCursoResumenDTO(
              entry.getKey().getId(),
              entry.getKey().getNombre(),
              raDtos.size(),
              numCesCurso,
              raDtos));
    }

    int numRasTotal = ras.size();
    int numCesTotal = cursos.stream().mapToInt(AdminModuloCursoResumenDTO::numCes).sum();

    return new AdminModuloResumenDTO(
        modulo.getId(),
        modulo.getCodigo(),
        modulo.getNombre(),
        modulo.getActivo(),
        numImparticiones,
        cursos.size(),
        numRasTotal,
        numCesTotal,
        cursos);
  }

  @Transactional
  public void toggleActivo(Integer id) {
    Modulo modulo =
        moduloRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ERR_MODULO + id));
    if (Boolean.TRUE.equals(modulo.getActivo())) {
      if (imparticionRepository.existsByModuloId(id)) {
        throw new IllegalStateException(
            "No se puede desactivar el módulo porque tiene imparticiones activas");
      }
      if (raRepository.existsByModuloId(id)) {
        throw new IllegalStateException(
            "No se puede desactivar el módulo porque tiene resultados de aprendizaje asociados");
      }
    }
    modulo.setActivo(!modulo.getActivo());
    moduloRepository.save(modulo);
  }
}
