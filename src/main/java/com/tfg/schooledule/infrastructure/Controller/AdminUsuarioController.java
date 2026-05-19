package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.domain.dto.AdminUsuarioFormDTO;
import com.tfg.schooledule.infrastructure.repository.CentroRepository;
import com.tfg.schooledule.infrastructure.repository.RolRepository;
import com.tfg.schooledule.infrastructure.service.AdminUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Admin - Usuarios")
@Controller
@RequestMapping("/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsuarioController {

  private static final String ATTR_ROLES = "roles";
  private static final String ATTR_CENTROS = "centros";
  private static final String VIEW_FORM = "admin/usuarios/formulario";
  private static final String ATTR_ERROR = "error";
  private static final String REDIRECT_USUARIOS = "redirect:/admin/usuarios";

  private final AdminUsuarioService adminUsuarioService;
  private final RolRepository rolRepository;
  private final CentroRepository centroRepository;

  public AdminUsuarioController(
      AdminUsuarioService adminUsuarioService,
      RolRepository rolRepository,
      CentroRepository centroRepository) {
    this.adminUsuarioService = adminUsuarioService;
    this.rolRepository = rolRepository;
    this.centroRepository = centroRepository;
  }

  @Operation(
      summary = "Listado de usuarios del sistema",
      description =
          "Renderiza la tabla con todos los usuarios. "
              + "Muestra nombre, email, roles asignados, centros y estado activo/inactivo.")
  @ApiResponse(
      responseCode = "200",
      description =
          "Vista HTML: admin/usuarios/lista. Modelo: usuarios (List<AdminUsuarioListDTO>)")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping
  public String lista(
      @org.springframework.web.bind.annotation.RequestParam(required = false) String rolNombre,
      Model model) {
    model.addAttribute("usuarios", adminUsuarioService.listarFiltrado(rolNombre));
    model.addAttribute(
        ATTR_ROLES,
        java.util.List.of("ROLE_ADMIN", "ROLE_PROFESOR", "ROLE_ALUMNO", "ROLE_ADMIN_CENTRO"));
    model.addAttribute("rolNombre", rolNombre);
    return "admin/usuarios/lista";
  }

  @Operation(
      summary = "Formulario: nuevo usuario",
      description =
          "Muestra el formulario vacío para crear un nuevo usuario. "
              + "Carga los selectores de roles disponibles y centros del sistema.")
  @ApiResponse(
      responseCode = "200",
      description =
          "Vista HTML: admin/usuarios/formulario. "
              + "Modelo: form (AdminUsuarioFormDTO vacío), roles (List<Rol>), centros (List<Centro>)")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping("/nuevo")
  public String nuevo(Model model) {
    model.addAttribute("form", new AdminUsuarioFormDTO());
    model.addAttribute(ATTR_ROLES, rolRepository.findAll());
    model.addAttribute(ATTR_CENTROS, centroRepository.findAll());
    return VIEW_FORM;
  }

  @Operation(
      summary = "Acción: crear usuario",
      description =
          "Procesa el formulario AdminUsuarioFormDTO. "
              + "Campos: username (3-50 chars, alfanumérico), nombre, apellidos, email, "
              + "password (@ValidPassword: min 8 chars, mayúscula, número y especial), "
              + "roleIds (Set<Integer>), centroIds (Set<Integer>). "
              + "El email debe ser único. Con éxito: redirect a /admin/usuarios.")
  @ApiResponse(responseCode = "302", description = "Creación exitosa → redirect a /admin/usuarios")
  @ApiResponse(
      responseCode = "200",
      description = "Formulario con errores de validación o email duplicado")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @PostMapping("/nuevo")
  public String crear(
      @Valid @ModelAttribute("form") AdminUsuarioFormDTO form,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      model.addAttribute(ATTR_ROLES, rolRepository.findAll());
      model.addAttribute(ATTR_CENTROS, centroRepository.findAll());
      return VIEW_FORM;
    }
    try {
      adminUsuarioService.crear(form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute(ATTR_ROLES, rolRepository.findAll());
      model.addAttribute(ATTR_CENTROS, centroRepository.findAll());
      model.addAttribute(ATTR_ERROR, ex.getMessage());
      return VIEW_FORM;
    }
    return REDIRECT_USUARIOS;
  }

  @Operation(
      summary = "Formulario: editar usuario",
      description =
          "Carga el formulario pre-relleno con los datos del usuario. "
              + "El campo password se muestra vacío: solo se actualiza si se rellena.")
  @ApiResponse(
      responseCode = "200",
      description =
          "Vista HTML: admin/usuarios/formulario. "
              + "Modelo: form (AdminUsuarioFormDTO con datos actuales), roles, centros")
  @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping("/{id}/editar")
  public String editar(
      @Parameter(description = "ID del usuario", required = true, example = "3")
          @PathVariable
          @Positive
          Integer id,
      Model model) {
    model.addAttribute("form", adminUsuarioService.obtenerParaEditar(id));
    model.addAttribute(ATTR_ROLES, rolRepository.findAll());
    model.addAttribute(ATTR_CENTROS, centroRepository.findAll());
    return VIEW_FORM;
  }

  @Operation(
      summary = "Acción: actualizar usuario",
      description =
          "Persiste los cambios del usuario. "
              + "Si password está vacío, no se modifica la contraseña actual. "
              + "Con éxito: redirect a /admin/usuarios.")
  @ApiResponse(
      responseCode = "302",
      description = "Actualización exitosa → redirect a /admin/usuarios")
  @ApiResponse(responseCode = "200", description = "Formulario con errores de validación")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @PostMapping("/{id}/editar")
  public String actualizar(
      @Parameter(description = "ID del usuario", required = true, example = "3")
          @PathVariable
          @Positive
          Integer id,
      @Valid @ModelAttribute("form") AdminUsuarioFormDTO form,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      model.addAttribute(ATTR_ROLES, rolRepository.findAll());
      model.addAttribute(ATTR_CENTROS, centroRepository.findAll());
      return VIEW_FORM;
    }
    try {
      adminUsuarioService.actualizar(id, form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute(ATTR_ROLES, rolRepository.findAll());
      model.addAttribute(ATTR_CENTROS, centroRepository.findAll());
      model.addAttribute(ATTR_ERROR, ex.getMessage());
      return VIEW_FORM;
    }
    return REDIRECT_USUARIOS;
  }

  @Operation(
      summary = "Acción: activar/desactivar usuario",
      description =
          "Alterna el estado activo del usuario. "
              + "Un usuario inactivo no puede autenticarse en el sistema. "
              + "No se puede desactivar el propio usuario administrador.")
  @ApiResponse(responseCode = "302", description = "Toggle exitoso → redirect a /admin/usuarios")
  @ApiResponse(responseCode = "302", description = "Error de negocio → redirect con flash 'error'")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @PostMapping("/{id}/toggle-activo")
  public String toggleActivo(
      @Parameter(description = "ID del usuario", required = true, example = "3")
          @PathVariable
          @Positive
          Integer id,
      org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
    try {
      adminUsuarioService.toggleActivo(id);
    } catch (IllegalStateException ex) {
      redirectAttributes.addFlashAttribute(ATTR_ERROR, ex.getMessage());
    }
    return REDIRECT_USUARIOS;
  }
}
