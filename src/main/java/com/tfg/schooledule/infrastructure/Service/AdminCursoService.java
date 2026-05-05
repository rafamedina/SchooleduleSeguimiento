package com.tfg.schooledule.infrastructure.service;

import com.tfg.schooledule.domain.dto.AdminCursoFormDTO;
import com.tfg.schooledule.domain.dto.AdminCursoListDTO;
import com.tfg.schooledule.domain.entity.CursoAcademico;
import com.tfg.schooledule.infrastructure.mapper.AdminCursoMapper;
import com.tfg.schooledule.infrastructure.repository.CursoAcademicoRepository;
import com.tfg.schooledule.infrastructure.repository.GrupoRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminCursoService {

  private final CursoAcademicoRepository cursoAcademicoRepository;
  private final GrupoRepository grupoRepository;
  private final AdminCursoMapper adminCursoMapper;

  public AdminCursoService(
      CursoAcademicoRepository cursoAcademicoRepository,
      GrupoRepository grupoRepository,
      AdminCursoMapper adminCursoMapper) {
    this.cursoAcademicoRepository = cursoAcademicoRepository;
    this.grupoRepository = grupoRepository;
    this.adminCursoMapper = adminCursoMapper;
  }

  @Transactional(readOnly = true)
  public List<AdminCursoListDTO> listarTodos() {
    return cursoAcademicoRepository.findAllByOrderByFechaInicioDesc().stream()
        .map(
            c ->
                new AdminCursoListDTO(
                    c.getId(),
                    c.getNombre(),
                    c.getFechaInicio(),
                    c.getFechaFin(),
                    c.getActivo(),
                    (int) grupoRepository.countByCursoAcademicoId(c.getId())))
        .toList();
  }

  @Transactional(readOnly = true)
  public AdminCursoFormDTO obtenerParaEditar(Integer id) {
    CursoAcademico curso =
        cursoAcademicoRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Curso académico no encontrado: " + id));
    return adminCursoMapper.toFormDTO(curso);
  }

  @Transactional
  public void crear(AdminCursoFormDTO dto) {
    if (cursoAcademicoRepository.existsByNombre(dto.getNombre())) {
      throw new IllegalArgumentException(
          "Ya existe un curso académico con el nombre '" + dto.getNombre() + "'");
    }
    cursoAcademicoRepository.save(
        CursoAcademico.builder()
            .nombre(dto.getNombre())
            .fechaInicio(dto.getFechaInicio())
            .fechaFin(dto.getFechaFin())
            .activo(false)
            .build());
  }

  @Transactional
  public void actualizar(Integer id, AdminCursoFormDTO dto) {
    CursoAcademico curso =
        cursoAcademicoRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Curso académico no encontrado: " + id));
    if (cursoAcademicoRepository.existsByNombreAndIdNot(dto.getNombre(), id)) {
      throw new IllegalArgumentException(
          "Ya existe un curso académico con el nombre '" + dto.getNombre() + "'");
    }
    adminCursoMapper.updateEntity(dto, curso);
    cursoAcademicoRepository.save(curso);
  }

  @Transactional
  public void activar(Integer id) {
    CursoAcademico curso =
        cursoAcademicoRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Curso académico no encontrado: " + id));
    cursoAcademicoRepository.desactivarTodosExcepto(id);
    curso.setActivo(true);
    cursoAcademicoRepository.save(curso);
  }

  @Transactional
  public void cerrar(Integer id) {
    CursoAcademico curso =
        cursoAcademicoRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Curso académico no encontrado: " + id));
    curso.setActivo(false);
    cursoAcademicoRepository.save(curso);
  }
}
