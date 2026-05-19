package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.domain.dto.AdminImparticionFormDTO;
import com.tfg.schooledule.domain.dto.ImparticionFiltroDTO;
import com.tfg.schooledule.infrastructure.repository.CentroRepository;
import com.tfg.schooledule.infrastructure.repository.CursoAcademicoRepository;
import com.tfg.schooledule.infrastructure.repository.GrupoRepository;
import com.tfg.schooledule.infrastructure.repository.ModuloRepository;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import com.tfg.schooledule.infrastructure.service.AdminImparticionService;
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

@Tag(name = "Admin - Imparticiones")
@Controller
@RequestMapping("/admin/imparticiones")
@PreAuthorize("hasRole('ADMIN')")
public class AdminImparticionController {

  private static final String VIEW_FORM = "admin/imparticiones/formulario";
  private static final String ATTR_ERROR = "error";
  private static final String REDIRECT_IMPARTICIONES = "redirect:/admin/imparticiones";

  private final AdminImparticionService adminImparticionService;
  private final ModuloRepository moduloRepository;
  private final GrupoRepository grupoRepository;
  private final UsuarioRepository usuarioRepository;
  private final CentroRepository centroRepository;
  private final CursoAcademicoRepository cursoAcademicoRepository;

  public AdminImparticionController(
      AdminImparticionService adminImparticionService,
      ModuloRepository moduloRepository,
      GrupoRepository grupoRepository,
      UsuarioRepository usuarioRepository,
      CentroRepository centroRepository,
      CursoAcademicoRepository cursoAcademicoRepository) {
    this.adminImparticionService = adminImparticionService;
    this.moduloRepository = moduloRepository;
    this.grupoRepository = grupoRepository;
    this.usuarioRepository = usuarioRepository;
    this.centroRepository = centroRepository;
    this.cursoAcademicoRepository = cursoAcademicoRepository;
  }

  @Operation(
      summary = "Listado de imparticiones",
      description =
          "Renderiza la tabla con todas las imparticiones del sistema. "
              + "Una impartición relaciona un módulo formativo, un grupo de alumnos, "
              + "un profesor y un centro educativo.")
  @ApiResponse(
      responseCode = "200",
      description =
          "Vista HTML: admin/imparticiones/lista. Modelo: imparticiones (List<AdminImparticionListDTO>)")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping
  public String lista(
      @RequestParam(required = false) Integer centroId,
      @RequestParam(required = false) Integer grupoId,
      @RequestParam(required = false) Integer moduloId,
      @RequestParam(required = false) Integer profesorId,
      @RequestParam(required = false) Integer cursoAcademicoId,
      Model model) {
    ImparticionFiltroDTO filtro =
        new ImparticionFiltroDTO(centroId, grupoId, moduloId, profesorId, cursoAcademicoId);
    model.addAttribute("imparticiones", adminImparticionService.listarFiltrado(filtro));
    model.addAttribute("centros", centroRepository.findAllByOrderByNombreAsc());
    model.addAttribute("grupos", grupoRepository.findAllByOrderByCentroNombreAscNombreAsc());
    model.addAttribute("modulos", moduloRepository.findByActivoTrueOrderByNombreAsc());
    model.addAttribute("profesores", usuarioRepository.findAllProfesoresOrdenados());
    model.addAttribute("cursos", cursoAcademicoRepository.findAllByOrderByNombreAsc());
    model.addAttribute("filtro", filtro);
    return "admin/imparticiones/lista";
  }

  @Operation(
      summary = "Formulario: nueva impartición",
      description =
          "Muestra el formulario de creación. "
              + "Carga los selectores de módulos activos, grupos, profesores y centros activos.")
  @ApiResponse(
      responseCode = "200",
      description =
          "Vista HTML: admin/imparticiones/formulario. "
              + "Modelo: form (AdminImparticionFormDTO vacío), modulos, grupos, profesores, centros")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping("/nuevo")
  public String nuevo(Model model) {
    model.addAttribute("form", new AdminImparticionFormDTO());
    cargarListas(model);
    return VIEW_FORM;
  }

  @Operation(
      summary = "Acción: crear impartición",
      description =
          "Procesa el formulario AdminImparticionFormDTO. "
              + "Campos: moduloId, grupoId, profesorId, centroId (todos FKs obligatorios). "
              + "El grupo debe pertenecer al centro indicado. "
              + "Con éxito: redirect a /admin/imparticiones.")
  @ApiResponse(
      responseCode = "302",
      description = "Creación exitosa → redirect a /admin/imparticiones")
  @ApiResponse(responseCode = "200", description = "Formulario con errores de validación")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @PostMapping("/nuevo")
  public String crear(
      @Valid @ModelAttribute("form") AdminImparticionFormDTO form,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      cargarListas(model);
      return VIEW_FORM;
    }
    try {
      adminImparticionService.crear(form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute(ATTR_ERROR, ex.getMessage());
      cargarListas(model);
      return VIEW_FORM;
    }
    return REDIRECT_IMPARTICIONES;
  }

  @Operation(
      summary = "Formulario: editar impartición",
      description = "Carga el formulario pre-relleno con los datos de la impartición.")
  @ApiResponse(
      responseCode = "200",
      description =
          "Vista HTML: admin/imparticiones/formulario. "
              + "Modelo: form (AdminImparticionFormDTO con datos actuales), modulos, grupos, profesores, centros")
  @ApiResponse(responseCode = "404", description = "Impartición no encontrada")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping("/{id}/editar")
  public String editar(
      @Parameter(description = "ID de la impartición", required = true, example = "7")
          @PathVariable
          @Positive
          Integer id,
      Model model) {
    model.addAttribute("form", adminImparticionService.obtenerParaEditar(id));
    cargarListas(model);
    return VIEW_FORM;
  }

  @Operation(
      summary = "Acción: actualizar impartición",
      description =
          "Persiste los cambios de la impartición. "
              + "Con éxito: redirect a /admin/imparticiones.")
  @ApiResponse(
      responseCode = "302",
      description = "Actualización exitosa → redirect a /admin/imparticiones")
  @ApiResponse(responseCode = "200", description = "Formulario con errores de validación")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @PostMapping("/{id}/editar")
  public String actualizar(
      @Parameter(description = "ID de la impartición", required = true, example = "7")
          @PathVariable
          @Positive
          Integer id,
      @Valid @ModelAttribute("form") AdminImparticionFormDTO form,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      cargarListas(model);
      return VIEW_FORM;
    }
    try {
      adminImparticionService.actualizar(id, form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute(ATTR_ERROR, ex.getMessage());
      cargarListas(model);
      return VIEW_FORM;
    }
    return REDIRECT_IMPARTICIONES;
  }

  @Operation(
      summary = "Acción: eliminar impartición",
      description =
          "Elimina la impartición si no tiene matrículas ni ítems evaluables asociados. "
              + "Si hay dependencias, redirige con mensaje de error flash.")
  @ApiResponse(
      responseCode = "302",
      description = "Eliminación exitosa → redirect a /admin/imparticiones")
  @ApiResponse(
      responseCode = "302",
      description = "Error de integridad → redirect con flash 'error'")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @PostMapping("/{id}/eliminar")
  public String eliminar(
      @Parameter(description = "ID de la impartición", required = true, example = "7")
          @PathVariable
          @Positive
          Integer id,
      RedirectAttributes redirectAttributes) {
    try {
      adminImparticionService.eliminar(id);
    } catch (IllegalStateException ex) {
      redirectAttributes.addFlashAttribute(ATTR_ERROR, ex.getMessage());
    }
    return REDIRECT_IMPARTICIONES;
  }

  private void cargarListas(Model model) {
    model.addAttribute("modulos", moduloRepository.findByActivoTrueOrderByNombreAsc());
    model.addAttribute("grupos", grupoRepository.findAllByOrderByCentroNombreAscNombreAsc());
    model.addAttribute("profesores", usuarioRepository.findAllProfesoresOrdenados());
    model.addAttribute("centros", centroRepository.findAllByActivoTrueOrderByNombreAsc());
  }
}
