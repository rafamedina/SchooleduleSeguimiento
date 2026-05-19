package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.infrastructure.repository.CursoAcademicoRepository;
import com.tfg.schooledule.infrastructure.service.AdminCursoActivoService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(
    assignableTypes = {
      AdminGrupoController.class,
      AdminImparticionController.class,
      AdminAlumnoController.class,
      AdminUsuarioController.class,
      AdminModuloController.class,
      AdminCentroController.class,
      AdminCursoController.class,
      AdminAuditoriaController.class,
      AdminController.class,
      AdminModuloImportController.class
    })
public class AdminGlobalModelAdvice {

  private final AdminCursoActivoService cursoActivoService;
  private final CursoAcademicoRepository cursoAcademicoRepository;

  public AdminGlobalModelAdvice(
      AdminCursoActivoService cursoActivoService,
      CursoAcademicoRepository cursoAcademicoRepository) {
    this.cursoActivoService = cursoActivoService;
    this.cursoAcademicoRepository = cursoAcademicoRepository;
  }

  @ModelAttribute
  public void addCursoActivo(Model model) {
    try {
      model.addAttribute("cursoActivoId", cursoActivoService.getCursoActivoId());
    } catch (IllegalStateException e) {
      model.addAttribute("cursoActivoId", null);
    }
    model.addAttribute(
        "cursosDisponibles", cursoAcademicoRepository.findAllByOrderByFechaInicioDesc());
  }
}
