package com.tfg.schooledule.infrastructure.service;

import com.tfg.schooledule.domain.dto.AdminCentroFormDTO;
import com.tfg.schooledule.domain.dto.AdminCentroListDTO;
import com.tfg.schooledule.domain.entity.Centro;
import com.tfg.schooledule.infrastructure.mapper.AdminCentroMapper;
import com.tfg.schooledule.infrastructure.repository.CentroRepository;
import com.tfg.schooledule.infrastructure.repository.GrupoRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminCentroService {

  private final CentroRepository centroRepository;
  private final GrupoRepository grupoRepository;
  private final AdminCentroMapper adminCentroMapper;

  public AdminCentroService(
      CentroRepository centroRepository,
      GrupoRepository grupoRepository,
      AdminCentroMapper adminCentroMapper) {
    this.centroRepository = centroRepository;
    this.grupoRepository = grupoRepository;
    this.adminCentroMapper = adminCentroMapper;
  }

  @Transactional(readOnly = true)
  public List<AdminCentroListDTO> listarTodos() {
    return centroRepository.findAllByOrderByNombreAsc().stream()
        .map(
            c ->
                new AdminCentroListDTO(
                    c.getId(),
                    c.getNombre(),
                    c.getUbicacion(),
                    c.getActivo(),
                    c.getProfesores().size(),
                    (int) grupoRepository.countByCentroId(c.getId())))
        .toList();
  }

  @Transactional(readOnly = true)
  public AdminCentroFormDTO obtenerParaEditar(Integer id) {
    Centro centro =
        centroRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Centro no encontrado: " + id));
    return adminCentroMapper.toFormDTO(centro);
  }

  @Transactional
  public void crear(AdminCentroFormDTO dto) {
    if (centroRepository.existsByNombre(dto.getNombre())) {
      throw new IllegalArgumentException(
          "Ya existe un centro con el nombre '" + dto.getNombre() + "'");
    }
    Centro centro = Centro.builder().nombre(dto.getNombre()).ubicacion(dto.getUbicacion()).build();
    centroRepository.save(centro);
  }

  @Transactional
  public void actualizar(Integer id, AdminCentroFormDTO dto) {
    Centro centro =
        centroRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Centro no encontrado: " + id));
    if (centroRepository.existsByNombreAndIdNot(dto.getNombre(), id)) {
      throw new IllegalArgumentException(
          "Ya existe un centro con el nombre '" + dto.getNombre() + "'");
    }
    adminCentroMapper.updateEntity(dto, centro);
    centroRepository.save(centro);
  }

  @Transactional
  public void toggleActivo(Integer id) {
    Centro centro =
        centroRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Centro no encontrado: " + id));
    if (Boolean.TRUE.equals(centro.getActivo()) && grupoRepository.existsByCentroId(id)) {
      throw new IllegalStateException(
          "No se puede desactivar el centro porque tiene grupos asignados");
    }
    centro.setActivo(!centro.getActivo());
    centroRepository.save(centro);
  }
}
