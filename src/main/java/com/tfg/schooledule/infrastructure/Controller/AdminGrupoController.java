package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.domain.dto.AdminGrupoFormDTO;
import com.tfg.schooledule.domain.dto.GrupoFiltroDTO;
import com.tfg.schooledule.infrastructure.repository.CentroRepository;
import com.tfg.schooledule.infrastructure.repository.CursoAcademicoRepository;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import com.tfg.schooledule.infrastructure.service.AdminGrupoService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Tag(name = "Admin - Grupos")
@Controller
@RequestMapping("/admin/grupos")
@PreAuthorize("hasRole('ADMIN')")
public class AdminGrupoController {

  private static final String VIEW_FORM = "admin/grupos/formulario";
  private static final String ATTR_ERROR = "error";
  private static final String REDIRECT_GRUPOS = "redirect:/admin/grupos";

  private final AdminGrupoService adminGrupoService;
  private final CentroRepository centroRepository;
  private final CursoAcademicoRepository cursoAcademicoRepository;
  private final UsuarioRepository usuarioRepository;

  public AdminGrupoController(
      AdminGrupoService adminGrupoService,
      CentroRepository centroRepository,
      CursoAcademicoRepository cursoAcademicoRepository,
      UsuarioRepository usuarioRepository) {
    this.adminGrupoService = adminGrupoService;
    this.centroRepository = centroRepository;
    this.cursoAcademicoRepository = cursoAcademicoRepository;
    this.usuarioRepository = usuarioRepository;
  }

  @Operation(
      summary = "Listado de grupos",
      description =
          "Renderiza la tabla con todos los grupos de alumnos. "
              + "Muestra nombre, centro asociado y curso académico.")
  @ApiResponse(
      responseCode = "200",
      description = "Vista HTML: admin/grupos/lista. Modelo: grupos (List<AdminGrupoListDTO>)")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping
  public String lista(
      @RequestParam(required = false) Integer centroId,
      @RequestParam(required = false) Integer cursoAcademicoId,
      Model model) {
    GrupoFiltroDTO filtro = new GrupoFiltroDTO(centroId, cursoAcademicoId);
    model.addAttribute("grupos", adminGrupoService.listarFiltrado(filtro));
    model.addAttribute("centros", centroRepository.findAllByOrderByNombreAsc());
    model.addAttribute("cursos", cursoAcademicoRepository.findAllByOrderByNombreAsc());
    model.addAttribute("filtro", filtro);
    return "admin/grupos/lista";
  }

  @Operation(
      summary = "Formulario: nuevo grupo",
      description =
          "Muestra el formulario de creación. "
              + "Carga los selectores de centros activos y cursos académicos disponibles.")
  @ApiResponse(
      responseCode = "200",
      description =
          "Vista HTML: admin/grupos/formulario. "
              + "Modelo: form (AdminGrupoFormDTO vacío), centros (List<Centro>), cursos (List<CursoAcademico>)")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping("/nuevo")
  public String nuevo(Model model) {
    model.addAttribute("form", new AdminGrupoFormDTO());
    cargarListas(model);
    return VIEW_FORM;
  }

  @Operation(
      summary = "Acción: crear grupo",
      description =
          "Procesa el formulario AdminGrupoFormDTO. "
              + "Campos: nombre (max 50), centroId (FK), cursoAcademicoId (FK). "
              + "Con éxito: redirect a /admin/grupos.")
  @ApiResponse(responseCode = "302", description = "Creación exitosa → redirect a /admin/grupos")
  @ApiResponse(responseCode = "200", description = "Formulario con errores de validación")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @PostMapping("/nuevo")
  public String crear(
      @Valid @ModelAttribute("form") AdminGrupoFormDTO form,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      cargarListas(model);
      return VIEW_FORM;
    }
    try {
      adminGrupoService.crear(form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute(ATTR_ERROR, ex.getMessage());
      cargarListas(model);
      return VIEW_FORM;
    }
    return REDIRECT_GRUPOS;
  }

  @Operation(
      summary = "Formulario: editar grupo",
      description =
          "Carga el formulario pre-relleno con los datos del grupo para su edición. "
              + "Incluye los selectores de centros y cursos.")
  @ApiResponse(
      responseCode = "200",
      description =
          "Vista HTML: admin/grupos/formulario. Modelo: form (AdminGrupoFormDTO con datos actuales), centros, cursos")
  @ApiResponse(responseCode = "404", description = "Grupo no encontrado")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping("/{id}/editar")
  public String editar(
      @Parameter(description = "ID del grupo", required = true, example = "5")
          @PathVariable
          @Positive
          Integer id,
      Model model) {
    model.addAttribute("form", adminGrupoService.obtenerParaEditar(id));
    cargarListas(model);
    return VIEW_FORM;
  }

  @Operation(
      summary = "Acción: actualizar grupo",
      description = "Persiste los cambios del grupo. Con éxito: redirect a /admin/grupos.")
  @ApiResponse(
      responseCode = "302",
      description = "Actualización exitosa → redirect a /admin/grupos")
  @ApiResponse(responseCode = "200", description = "Formulario con errores de validación")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @PostMapping("/{id}/editar")
  public String actualizar(
      @Parameter(description = "ID del grupo", required = true, example = "5")
          @PathVariable
          @Positive
          Integer id,
      @Valid @ModelAttribute("form") AdminGrupoFormDTO form,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      cargarListas(model);
      return VIEW_FORM;
    }
    try {
      adminGrupoService.actualizar(id, form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute(ATTR_ERROR, ex.getMessage());
      cargarListas(model);
      return VIEW_FORM;
    }
    return REDIRECT_GRUPOS;
  }

  @Operation(
      summary = "Acción: eliminar grupo",
      description =
          "Elimina el grupo si no tiene matrículas activas ni imparticiones asociadas. "
              + "Si hay dependencias, redirige con mensaje de error flash.")
  @ApiResponse(responseCode = "302", description = "Eliminación exitosa → redirect a /admin/grupos")
  @ApiResponse(
      responseCode = "302",
      description = "Error de integridad → redirect a /admin/grupos con flash 'error'")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @PostMapping("/{id}/eliminar")
  public String eliminar(
      @Parameter(description = "ID del grupo", required = true, example = "5")
          @PathVariable
          @Positive
          Integer id,
      RedirectAttributes redirectAttributes) {
    try {
      adminGrupoService.eliminar(id);
    } catch (IllegalStateException ex) {
      redirectAttributes.addFlashAttribute(ATTR_ERROR, ex.getMessage());
    }
    return REDIRECT_GRUPOS;
  }

  private void cargarListas(Model model) {
    model.addAttribute("centros", centroRepository.findAllByOrderByNombreAsc());
    model.addAttribute("cursos", cursoAcademicoRepository.findAllByOrderByNombreAsc());
    model.addAttribute("profesores", usuarioRepository.findAllProfesoresOrdenados());
  }
}
