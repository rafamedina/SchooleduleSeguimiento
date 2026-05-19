package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.domain.dto.AdminGrupoFormDTO;
import com.tfg.schooledule.infrastructure.repository.CursoAcademicoRepository;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import com.tfg.schooledule.infrastructure.service.CentroAdminGrupoService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.security.Principal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/centro-admin/grupos")
@PreAuthorize("hasRole('ADMIN_CENTRO')")
public class CentroAdminGrupoController {

  private static final String VIEW_FORM = "centro-admin/grupos/formulario";
  private static final String ATTR_ERROR = "error";
  private static final String REDIRECT_GRUPOS = "redirect:/centro-admin/grupos";

  private final CentroAdminGrupoService grupoService;
  private final CursoAcademicoRepository cursoAcademicoRepository;
  private final UsuarioRepository usuarioRepository;

  public CentroAdminGrupoController(
      CentroAdminGrupoService grupoService,
      CursoAcademicoRepository cursoAcademicoRepository,
      UsuarioRepository usuarioRepository) {
    this.grupoService = grupoService;
    this.cursoAcademicoRepository = cursoAcademicoRepository;
    this.usuarioRepository = usuarioRepository;
  }

  @GetMapping
  public String lista(Principal principal, Model model) {
    int adminId = resolveId(principal);
    model.addAttribute("grupos", grupoService.listarGruposDeCentros(adminId));
    return "centro-admin/grupos/lista";
  }

  @GetMapping("/nuevo")
  public String nuevo(Principal principal, Model model) {
    int adminId = resolveId(principal);
    model.addAttribute("form", new AdminGrupoFormDTO());
    cargarListas(model, adminId);
    return VIEW_FORM;
  }

  @PostMapping("/nuevo")
  public String crear(
      Principal principal,
      @Valid @ModelAttribute("form") AdminGrupoFormDTO form,
      BindingResult bindingResult,
      Model model) {
    int adminId = resolveId(principal);
    if (bindingResult.hasErrors()) {
      cargarListas(model, adminId);
      return VIEW_FORM;
    }
    try {
      grupoService.crear(adminId, form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute(ATTR_ERROR, ex.getMessage());
      cargarListas(model, adminId);
      return VIEW_FORM;
    }
    return REDIRECT_GRUPOS;
  }

  @GetMapping("/{id}/editar")
  public String editar(Principal principal, @PathVariable @Positive Integer id, Model model) {
    int adminId = resolveId(principal);
    model.addAttribute("form", grupoService.obtenerParaEditar(adminId, id));
    cargarListas(model, adminId);
    return VIEW_FORM;
  }

  @PostMapping("/{id}/editar")
  public String actualizar(
      Principal principal,
      @PathVariable @Positive Integer id,
      @Valid @ModelAttribute("form") AdminGrupoFormDTO form,
      BindingResult bindingResult,
      Model model) {
    int adminId = resolveId(principal);
    if (bindingResult.hasErrors()) {
      cargarListas(model, adminId);
      return VIEW_FORM;
    }
    try {
      grupoService.actualizar(adminId, id, form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute(ATTR_ERROR, ex.getMessage());
      cargarListas(model, adminId);
      return VIEW_FORM;
    }
    return REDIRECT_GRUPOS;
  }

  @PostMapping("/{id}/eliminar")
  public String eliminar(
      Principal principal,
      @PathVariable @Positive Integer id,
      RedirectAttributes redirectAttributes) {
    int adminId = resolveId(principal);
    try {
      grupoService.eliminar(adminId, id);
    } catch (IllegalStateException ex) {
      redirectAttributes.addFlashAttribute(ATTR_ERROR, ex.getMessage());
    }
    return REDIRECT_GRUPOS;
  }

  private void cargarListas(Model model, int adminId) {
    model.addAttribute("centros", grupoService.getCentrosDelAdmin(adminId));
    model.addAttribute("cursos", cursoAcademicoRepository.findAllByOrderByNombreAsc());
    model.addAttribute("profesores", usuarioRepository.findAllProfesoresOrdenados());
  }

  int resolveId(Principal principal) {
    return usuarioRepository
        .findUsuarioByEmail(principal.getName())
        .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"))
        .getId();
  }
}
