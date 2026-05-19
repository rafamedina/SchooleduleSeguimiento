package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.domain.dto.AdminUsuarioFormDTO;
import com.tfg.schooledule.infrastructure.repository.RolRepository;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import com.tfg.schooledule.infrastructure.service.CentroAdminGrupoService;
import com.tfg.schooledule.infrastructure.service.CentroAdminUsuarioService;
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
@RequestMapping("/centro-admin/usuarios")
@PreAuthorize("hasRole('ADMIN_CENTRO')")
public class CentroAdminUsuarioController {

  private static final String VIEW_FORM = "centro-admin/usuarios/formulario";
  private static final String ATTR_ERROR = "error";
  private static final String REDIRECT_USUARIOS = "redirect:/centro-admin/usuarios";

  private final CentroAdminUsuarioService usuarioService;
  private final CentroAdminGrupoService grupoService;
  private final RolRepository rolRepository;
  private final UsuarioRepository usuarioRepository;

  public CentroAdminUsuarioController(
      CentroAdminUsuarioService usuarioService,
      CentroAdminGrupoService grupoService,
      RolRepository rolRepository,
      UsuarioRepository usuarioRepository) {
    this.usuarioService = usuarioService;
    this.grupoService = grupoService;
    this.rolRepository = rolRepository;
    this.usuarioRepository = usuarioRepository;
  }

  @GetMapping
  public String lista(Principal principal, Model model) {
    int adminId = resolveId(principal);
    model.addAttribute("usuarios", usuarioService.listarUsuariosDeCentros(adminId));
    return "centro-admin/usuarios/lista";
  }

  @GetMapping("/nuevo")
  public String nuevo(Principal principal, Model model) {
    int adminId = resolveId(principal);
    model.addAttribute("form", new AdminUsuarioFormDTO());
    cargarListas(model, adminId);
    return VIEW_FORM;
  }

  @PostMapping("/nuevo")
  public String crear(
      Principal principal,
      @Valid @ModelAttribute("form") AdminUsuarioFormDTO form,
      BindingResult bindingResult,
      Model model) {
    int adminId = resolveId(principal);
    if (bindingResult.hasErrors()) {
      cargarListas(model, adminId);
      return VIEW_FORM;
    }
    try {
      usuarioService.crear(adminId, form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute(ATTR_ERROR, ex.getMessage());
      cargarListas(model, adminId);
      return VIEW_FORM;
    }
    return REDIRECT_USUARIOS;
  }

  @GetMapping("/{id}/editar")
  public String editar(Principal principal, @PathVariable @Positive Integer id, Model model) {
    int adminId = resolveId(principal);
    model.addAttribute("form", usuarioService.obtenerParaEditar(adminId, id));
    cargarListas(model, adminId);
    return VIEW_FORM;
  }

  @PostMapping("/{id}/editar")
  public String actualizar(
      Principal principal,
      @PathVariable @Positive Integer id,
      @Valid @ModelAttribute("form") AdminUsuarioFormDTO form,
      BindingResult bindingResult,
      Model model) {
    int adminId = resolveId(principal);
    if (bindingResult.hasErrors()) {
      cargarListas(model, adminId);
      return VIEW_FORM;
    }
    try {
      usuarioService.actualizar(adminId, id, form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute(ATTR_ERROR, ex.getMessage());
      cargarListas(model, adminId);
      return VIEW_FORM;
    }
    return REDIRECT_USUARIOS;
  }

  @PostMapping("/{id}/toggle-activo")
  public String toggleActivo(
      Principal principal,
      @PathVariable @Positive Integer id,
      RedirectAttributes redirectAttributes) {
    int adminId = resolveId(principal);
    try {
      usuarioService.eliminar(adminId, id);
    } catch (IllegalStateException ex) {
      redirectAttributes.addFlashAttribute(ATTR_ERROR, ex.getMessage());
    }
    return REDIRECT_USUARIOS;
  }

  private void cargarListas(Model model, int adminId) {
    var rolesPermitidos =
        rolRepository.findAll().stream()
            .filter(
                r -> r.getNombre().equals("ROLE_PROFESOR") || r.getNombre().equals("ROLE_ALUMNO"))
            .toList();
    model.addAttribute("roles", rolesPermitidos);
    model.addAttribute("centros", grupoService.getCentrosDelAdmin(adminId));
  }

  int resolveId(Principal principal) {
    return usuarioRepository
        .findUsuarioByEmail(principal.getName())
        .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"))
        .getId();
  }
}
