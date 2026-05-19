package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.domain.dto.AdminImparticionFormDTO;
import com.tfg.schooledule.infrastructure.repository.ModuloRepository;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import com.tfg.schooledule.infrastructure.service.CentroAdminGrupoService;
import com.tfg.schooledule.infrastructure.service.CentroAdminImparticionService;
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
@RequestMapping("/centro-admin/imparticiones")
@PreAuthorize("hasRole('ADMIN_CENTRO')")
public class CentroAdminImparticionController {

  private static final String VIEW_FORM = "centro-admin/imparticiones/formulario";
  private static final String ATTR_ERROR = "error";
  private static final String REDIRECT_IMPARTICIONES = "redirect:/centro-admin/imparticiones";

  private final CentroAdminImparticionService imparticionService;
  private final CentroAdminGrupoService grupoService;
  private final ModuloRepository moduloRepository;
  private final UsuarioRepository usuarioRepository;

  public CentroAdminImparticionController(
      CentroAdminImparticionService imparticionService,
      CentroAdminGrupoService grupoService,
      ModuloRepository moduloRepository,
      UsuarioRepository usuarioRepository) {
    this.imparticionService = imparticionService;
    this.grupoService = grupoService;
    this.moduloRepository = moduloRepository;
    this.usuarioRepository = usuarioRepository;
  }

  @GetMapping
  public String lista(Principal principal, Model model) {
    int adminId = resolveId(principal);
    model.addAttribute("imparticiones", imparticionService.listarImparticionesDeCentros(adminId));
    return "centro-admin/imparticiones/lista";
  }

  @GetMapping("/nueva")
  public String nueva(Principal principal, Model model) {
    int adminId = resolveId(principal);
    model.addAttribute("form", new AdminImparticionFormDTO());
    cargarListas(model, adminId);
    return VIEW_FORM;
  }

  @PostMapping("/nueva")
  public String crear(
      Principal principal,
      @Valid @ModelAttribute("form") AdminImparticionFormDTO form,
      BindingResult bindingResult,
      Model model) {
    int adminId = resolveId(principal);
    if (bindingResult.hasErrors()) {
      cargarListas(model, adminId);
      return VIEW_FORM;
    }
    try {
      imparticionService.crear(adminId, form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute(ATTR_ERROR, ex.getMessage());
      cargarListas(model, adminId);
      return VIEW_FORM;
    }
    return REDIRECT_IMPARTICIONES;
  }

  @GetMapping("/{id}/editar")
  public String editar(Principal principal, @PathVariable @Positive Integer id, Model model) {
    int adminId = resolveId(principal);
    model.addAttribute("form", imparticionService.obtenerParaEditar(adminId, id));
    cargarListas(model, adminId);
    return VIEW_FORM;
  }

  @PostMapping("/{id}/editar")
  public String actualizar(
      Principal principal,
      @PathVariable @Positive Integer id,
      @Valid @ModelAttribute("form") AdminImparticionFormDTO form,
      BindingResult bindingResult,
      Model model) {
    int adminId = resolveId(principal);
    if (bindingResult.hasErrors()) {
      cargarListas(model, adminId);
      return VIEW_FORM;
    }
    try {
      imparticionService.actualizar(adminId, id, form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute(ATTR_ERROR, ex.getMessage());
      cargarListas(model, adminId);
      return VIEW_FORM;
    }
    return REDIRECT_IMPARTICIONES;
  }

  @PostMapping("/{id}/eliminar")
  public String eliminar(
      Principal principal,
      @PathVariable @Positive Integer id,
      RedirectAttributes redirectAttributes) {
    int adminId = resolveId(principal);
    try {
      imparticionService.eliminar(adminId, id);
    } catch (IllegalStateException ex) {
      redirectAttributes.addFlashAttribute(ATTR_ERROR, ex.getMessage());
    }
    return REDIRECT_IMPARTICIONES;
  }

  private void cargarListas(Model model, int adminId) {
    model.addAttribute("grupos", grupoService.listarGruposDeCentros(adminId));
    model.addAttribute("modulos", moduloRepository.findAllByOrderByNombreAsc());
    model.addAttribute("profesores", usuarioRepository.findAllProfesoresOrdenados());
    model.addAttribute("centros", grupoService.getCentrosDelAdmin(adminId));
  }

  int resolveId(Principal principal) {
    return usuarioRepository
        .findUsuarioByEmail(principal.getName())
        .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"))
        .getId();
  }
}
