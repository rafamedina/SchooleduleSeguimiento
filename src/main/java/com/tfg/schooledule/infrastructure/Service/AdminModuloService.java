package com.tfg.schooledule.infrastructure.service;

import com.tfg.schooledule.domain.dto.AdminModuloFormDTO;
import com.tfg.schooledule.domain.dto.AdminModuloListDTO;
import com.tfg.schooledule.domain.entity.Modulo;
import com.tfg.schooledule.infrastructure.mapper.AdminModuloMapper;
import com.tfg.schooledule.infrastructure.repository.ImparticionRepository;
import com.tfg.schooledule.infrastructure.repository.ModuloRepository;
import com.tfg.schooledule.infrastructure.repository.ResultadoAprendizajeRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminModuloService {

  private final ModuloRepository moduloRepository;
  private final ImparticionRepository imparticionRepository;
  private final ResultadoAprendizajeRepository raRepository;
  private final AdminModuloMapper adminModuloMapper;

  public AdminModuloService(
      ModuloRepository moduloRepository,
      ImparticionRepository imparticionRepository,
      ResultadoAprendizajeRepository raRepository,
      AdminModuloMapper adminModuloMapper) {
    this.moduloRepository = moduloRepository;
    this.imparticionRepository = imparticionRepository;
    this.raRepository = raRepository;
    this.adminModuloMapper = adminModuloMapper;
  }

  @Transactional(readOnly = true)
  public List<AdminModuloListDTO> listarTodos() {
    return moduloRepository.findAllByOrderByNombreAsc().stream()
        .map(
            m ->
                new AdminModuloListDTO(
                    m.getId(),
                    m.getCodigo(),
                    m.getNombre(),
                    m.getActivo(),
                    imparticionRepository.countByModuloId(m.getId()),
                    raRepository.countByModuloId(m.getId())))
        .toList();
  }

  @Transactional(readOnly = true)
  public AdminModuloFormDTO obtenerParaEditar(Integer id) {
    Modulo modulo =
        moduloRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Módulo no encontrado: " + id));
    return adminModuloMapper.toFormDTO(modulo);
  }

  @Transactional
  public void crear(AdminModuloFormDTO dto) {
    if (moduloRepository.existsByCodigo(dto.getCodigo())) {
      throw new IllegalArgumentException(
          "Ya existe un módulo con el código '" + dto.getCodigo() + "'");
    }
    Modulo modulo = Modulo.builder().codigo(dto.getCodigo()).nombre(dto.getNombre()).build();
    moduloRepository.save(modulo);
  }

  @Transactional
  public void actualizar(Integer id, AdminModuloFormDTO dto) {
    Modulo modulo =
        moduloRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Módulo no encontrado: " + id));
    if (moduloRepository.existsByCodigoAndIdNot(dto.getCodigo(), id)) {
      throw new IllegalArgumentException(
          "Ya existe un módulo con el código '" + dto.getCodigo() + "'");
    }
    adminModuloMapper.updateEntity(dto, modulo);
    moduloRepository.save(modulo);
  }

  @Transactional
  public void toggleActivo(Integer id) {
    Modulo modulo =
        moduloRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Módulo no encontrado: " + id));
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
