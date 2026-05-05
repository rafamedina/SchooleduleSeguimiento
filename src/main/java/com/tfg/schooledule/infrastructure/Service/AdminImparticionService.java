package com.tfg.schooledule.infrastructure.service;

import com.tfg.schooledule.domain.dto.AdminImparticionFormDTO;
import com.tfg.schooledule.domain.dto.AdminImparticionListDTO;
import com.tfg.schooledule.domain.entity.Imparticion;
import com.tfg.schooledule.infrastructure.mapper.AdminImparticionMapper;
import com.tfg.schooledule.infrastructure.repository.*;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminImparticionService {

  private final ImparticionRepository imparticionRepository;
  private final ModuloRepository moduloRepository;
  private final GrupoRepository grupoRepository;
  private final UsuarioRepository usuarioRepository;
  private final CentroRepository centroRepository;
  private final MatriculaRepository matriculaRepository;
  private final PeriodoEvaluacionRepository periodoEvaluacionRepository;
  private final AdminImparticionMapper adminImparticionMapper;

  public AdminImparticionService(
      ImparticionRepository imparticionRepository,
      ModuloRepository moduloRepository,
      GrupoRepository grupoRepository,
      UsuarioRepository usuarioRepository,
      CentroRepository centroRepository,
      MatriculaRepository matriculaRepository,
      PeriodoEvaluacionRepository periodoEvaluacionRepository,
      AdminImparticionMapper adminImparticionMapper) {
    this.imparticionRepository = imparticionRepository;
    this.moduloRepository = moduloRepository;
    this.grupoRepository = grupoRepository;
    this.usuarioRepository = usuarioRepository;
    this.centroRepository = centroRepository;
    this.matriculaRepository = matriculaRepository;
    this.periodoEvaluacionRepository = periodoEvaluacionRepository;
    this.adminImparticionMapper = adminImparticionMapper;
  }

  @Transactional(readOnly = true)
  public List<AdminImparticionListDTO> listarTodas() {
    return imparticionRepository.findAllByOrderByGrupoNombreAscModuloNombreAsc().stream()
        .map(
            i ->
                new AdminImparticionListDTO(
                    i.getId(),
                    i.getModulo().getCodigo(),
                    i.getModulo().getNombre(),
                    i.getGrupo().getNombre(),
                    i.getCentro().getNombre(),
                    i.getProfesor().getApellidos() + ", " + i.getProfesor().getNombre()))
        .toList();
  }

  @Transactional(readOnly = true)
  public AdminImparticionFormDTO obtenerParaEditar(Integer id) {
    Imparticion imparticion =
        imparticionRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Impartición no encontrada: " + id));
    return adminImparticionMapper.toFormDTO(imparticion);
  }

  @Transactional
  public void crear(AdminImparticionFormDTO dto) {
    if (imparticionRepository.existsByModuloIdAndGrupoId(dto.getModuloId(), dto.getGrupoId())) {
      throw new IllegalArgumentException("Este módulo ya se imparte en ese grupo");
    }
    var modulo =
        moduloRepository
            .findById(dto.getModuloId())
            .orElseThrow(
                () -> new EntityNotFoundException("Módulo no encontrado: " + dto.getModuloId()));
    var grupo =
        grupoRepository
            .findById(dto.getGrupoId())
            .orElseThrow(
                () -> new EntityNotFoundException("Grupo no encontrado: " + dto.getGrupoId()));
    var profesor =
        usuarioRepository
            .findById(dto.getProfesorId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException("Profesor no encontrado: " + dto.getProfesorId()));
    var centro =
        centroRepository
            .findById(dto.getCentroId())
            .orElseThrow(
                () -> new EntityNotFoundException("Centro no encontrado: " + dto.getCentroId()));
    imparticionRepository.save(
        Imparticion.builder()
            .modulo(modulo)
            .grupo(grupo)
            .profesor(profesor)
            .centro(centro)
            .build());
  }

  @Transactional
  public void actualizar(Integer id, AdminImparticionFormDTO dto) {
    Imparticion imparticion =
        imparticionRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Impartición no encontrada: " + id));
    if (imparticionRepository.existsByModuloIdAndGrupoIdAndIdNot(
        dto.getModuloId(), dto.getGrupoId(), id)) {
      throw new IllegalArgumentException("Este módulo ya se imparte en ese grupo");
    }
    var modulo =
        moduloRepository
            .findById(dto.getModuloId())
            .orElseThrow(
                () -> new EntityNotFoundException("Módulo no encontrado: " + dto.getModuloId()));
    var grupo =
        grupoRepository
            .findById(dto.getGrupoId())
            .orElseThrow(
                () -> new EntityNotFoundException("Grupo no encontrado: " + dto.getGrupoId()));
    var profesor =
        usuarioRepository
            .findById(dto.getProfesorId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException("Profesor no encontrado: " + dto.getProfesorId()));
    var centro =
        centroRepository
            .findById(dto.getCentroId())
            .orElseThrow(
                () -> new EntityNotFoundException("Centro no encontrado: " + dto.getCentroId()));
    imparticion.setModulo(modulo);
    imparticion.setGrupo(grupo);
    imparticion.setProfesor(profesor);
    imparticion.setCentro(centro);
    imparticionRepository.save(imparticion);
  }

  @Transactional
  public void eliminar(Integer id) {
    if (!imparticionRepository.existsById(id)) {
      throw new EntityNotFoundException("Impartición no encontrada: " + id);
    }
    if (matriculaRepository.existsByImparticionId(id)) {
      throw new IllegalStateException("No se puede eliminar: tiene matrículas asociadas");
    }
    if (periodoEvaluacionRepository.existsByImparticionId(id)) {
      throw new IllegalStateException("No se puede eliminar: tiene periodos de evaluación");
    }
    imparticionRepository.deleteById(id);
  }
}
