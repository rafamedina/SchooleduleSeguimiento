package com.tfg.schooledule.infrastructure.service;

import com.tfg.schooledule.domain.entity.CursoAcademico;
import com.tfg.schooledule.infrastructure.repository.CursoAcademicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminCursoActivoService {

  private final CursoAcademicoRepository cursoAcademicoRepository;

  public AdminCursoActivoService(CursoAcademicoRepository cursoAcademicoRepository) {
    this.cursoAcademicoRepository = cursoAcademicoRepository;
  }

  @Transactional(readOnly = true)
  public CursoAcademico getCursoActivo() {
    return loadCursoActivo();
  }

  @Transactional(readOnly = true)
  public Integer getCursoActivoId() {
    return loadCursoActivo().getId();
  }

  private CursoAcademico loadCursoActivo() {
    return cursoAcademicoRepository
        .findByActivo(true)
        .orElseThrow(() -> new IllegalStateException("No hay curso académico activo"));
  }
}
