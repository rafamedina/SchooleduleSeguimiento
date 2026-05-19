package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.domain.dto.GradeUpsertRequest;
import com.tfg.schooledule.domain.dto.ItemEvaluableFormDTO;
import com.tfg.schooledule.domain.dto.TeacherStudentGradesDTO;
import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.infrastructure.repository.ItemEvaluableRepository;
import com.tfg.schooledule.infrastructure.repository.PeriodoEvaluacionRepository;
import com.tfg.schooledule.infrastructure.repository.ResultadoAprendizajeRepository;
import com.tfg.schooledule.infrastructure.service.TeacherDashboardService;
import com.tfg.schooledule.infrastructure.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Tag(name = "Profesor")
@Controller
@RequestMapping("/profe")
@PreAuthorize("hasRole('PROFESOR')")
@SuppressWarnings({"java:S1075", "java:S6833"})
public class ProfeController {

  private static final String ATTR_ERROR_ITEM = "errorItem";
  private static final String REDIRECT_IMPARTICION = "redirect:/profe/imparticion/";
  private static final String PATH_ALUMNOS = "/alumnos";

  private final UsuarioService usuarioService;
  private final TeacherDashboardService teacherService;
  private final ItemEvaluableRepository itemEvaluableRepository;
  private final PeriodoEvaluacionRepository periodoRepository;
  private final ResultadoAprendizajeRepository raRepository;

  public ProfeController(
      UsuarioService usuarioService,
      TeacherDashboardService teacherService,
      ItemEvaluableRepository itemEvaluableRepository,
      PeriodoEvaluacionRepository periodoRepository,
      ResultadoAprendizajeRepository raRepository) {
    this.usuarioService = usuarioService;
    this.teacherService = teacherService;
    this.itemEvaluableRepository = itemEvaluableRepository;
    this.periodoRepository = periodoRepository;
    this.raRepository = raRepository;
  }

  @Operation(
      summary = "Dashboard del profesor — Paso 1: selección de centro",
      description =
          "Vista principal del profesor. Muestra la lista de centros educativos a los que "
              + "pertenece el profesor autenticado. El profesor selecciona un centro para "
              + "continuar al paso 2 (asignaturas).")
  @ApiResponse(
      responseCode = "200",
      description =
          "Vista HTML: profe/dashboard. "
              + "Modelo: centros (List<TeacherCenterDTO>), teacherName (String)")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_PROFESOR")
  @GetMapping("/dashboard")
  public String dashboard(Principal principal, Model model) {
    Usuario profesor = resolveProfesor(principal);
    model.addAttribute("centros", teacherService.getCentersForTeacher(profesor));
    model.addAttribute("teacherName", profesor.getNombre() + " " + profesor.getApellidos());
    return "profe/dashboard";
  }

  @Operation(
      summary = "Asignaturas del profesor en un centro — Paso 2",
      description =
          "Muestra las asignaturas (imparticiones) que el profesor imparte en el centro indicado. "
              + "Validación de ownership: el profesor solo puede ver centros a los que pertenece. "
              + "Desde aquí navega al cuaderno de notas de cada asignatura (paso 3).")
  @ApiResponse(
      responseCode = "200",
      description =
          "Vista HTML: profe/asignaturas. "
              + "Modelo: asignaturas (List<TeacherSubjectDTO>), centroId, centroNombre")
  @ApiResponse(responseCode = "403", description = "El centro no pertenece al profesor autenticado")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_PROFESOR")
  @GetMapping("/centro/{centroId}/asignaturas")
  public String asignaturas(
      @Parameter(description = "ID del centro educativo", required = true, example = "1")
          @PathVariable
          Integer centroId,
      Principal principal,
      Model model) {
    Usuario profesor = resolveProfesor(principal);
    teacherService.validateCentroOwnership(profesor.getId(), centroId);
    model.addAttribute(
        "asignaturas", teacherService.getSubjectsForTeacherAndCenter(profesor.getId(), centroId));
    model.addAttribute("centroId", centroId);
    profesor.getCentros().stream()
        .filter(c -> c.getId().equals(centroId))
        .findFirst()
        .ifPresent(c -> model.addAttribute("centroNombre", c.getNombre()));
    return "profe/asignaturas";
  }

  @Operation(
      summary = "Cuaderno del profesor — Paso 3: alumnos de una impartición",
      description =
          "Vista principal del cuaderno de notas. Muestra el listado de alumnos matriculados "
              + "en la impartición indicada, junto con la gestión de ítems evaluables. "
              + "Carga: alumnos con sus notas resumidas, ítems evaluables activos, "
              + "períodos de evaluación y resultados de aprendizaje (RAs) del módulo. "
              + "Validación de ownership: el profesor solo puede acceder a sus propias imparticiones.")
  @ApiResponse(
      responseCode = "200",
      description =
          "Vista HTML: profe/alumnos. "
              + "Modelo: alumnos (List<TeacherStudentRowDTO>), imparticionId, imparticionLabel, "
              + "itemForm (ItemEvaluableFormDTO vacío), items (List<ItemEvaluable>), "
              + "periodos (List<PeriodoEvaluacion>), ras (List<ResultadoAprendizaje>)")
  @ApiResponse(responseCode = "403", description = "La impartición no pertenece al profesor")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_PROFESOR")
  @GetMapping("/imparticion/{imparticionId}/alumnos")
  public String alumnos(
      @Parameter(description = "ID de la impartición", required = true, example = "12")
          @PathVariable
          @Positive
          Integer imparticionId,
      Principal principal,
      Model model) {
    Usuario profesor = resolveProfesor(principal);
    model.addAttribute(
        "alumnos", teacherService.getRosterForImparticion(profesor.getId(), imparticionId));
    model.addAttribute("imparticionId", imparticionId);
    model.addAttribute(
        "imparticionLabel",
        teacherService
            .getSubjectsForTeacherAndCenter(
                profesor.getId(),
                teacherService.getCentroIdByImparticion(profesor.getId(), imparticionId))
            .stream()
            .filter(s -> s.imparticionId().equals(imparticionId))
            .findFirst()
            .map(s -> s.grupoNombre() + " · " + s.moduloNombre())
            .orElse(""));

    model.addAttribute("itemForm", new ItemEvaluableFormDTO());
    model.addAttribute(
        "items",
        itemEvaluableRepository.findByImparticionIdOrderByPeriodoEvaluacionIdAscFechaAsc(
            imparticionId));
    model.addAttribute("periodos", periodoRepository.findByImparticionId(imparticionId));
    Integer moduloId = teacherService.getModuloIdByImparticion(profesor.getId(), imparticionId);
    model.addAttribute("ras", raRepository.findByModuloIdOrderByCodigoAsc(moduloId));

    return "profe/alumnos";
  }

  @Operation(
      summary = "API — Notas detalladas de una matrícula (profesor)",
      description =
          "Devuelve el desglose completo de calificaciones de la matrícula indicada en JSON. "
              + "Usado por el frontend JavaScript del cuaderno de notas para mostrar "
              + "el desglose por período → ítem evaluable → criterio. "
              + "Validación de ownership: el profesor solo puede ver matrículas de sus propias imparticiones.")
  @ApiResponse(
      responseCode = "200",
      description = "Calificaciones completas de la matrícula",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = TeacherStudentGradesDTO.class)))
  @ApiResponse(
      responseCode = "403",
      description = "La matrícula no pertenece a una impartición del profesor")
  @ApiResponse(responseCode = "401", description = "No autenticado")
  @GetMapping("/api/matricula/{matriculaId}/notas")
  @ResponseBody
  public TeacherStudentGradesDTO getNotas(
      @Parameter(description = "ID de la matrícula", required = true, example = "42") @PathVariable
          Integer matriculaId,
      Principal principal) {
    Usuario profesor = resolveProfesor(principal);
    return teacherService.getStudentGrades(profesor.getId(), matriculaId);
  }

  @Operation(
      summary = "API — Guardar/actualizar notas de una matrícula",
      description =
          "Upsert de calificaciones: si la calificación del criterio ya existe la actualiza, "
              + "si no existe la crea. Devuelve el estado completo actualizado de la matrícula. "
              + "El matriculaId del path debe coincidir con el del body (validación explícita). "
              + "Cada entrada especifica un ítem evaluable, un criterio y el valor (0.00-10.00). "
              + "Los cambios se registran automáticamente en la tabla de auditoría (trigger PL/pgSQL).")
  @ApiResponse(
      responseCode = "200",
      description = "Notas actualizadas — devuelve el TeacherStudentGradesDTO completo",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = TeacherStudentGradesDTO.class)))
  @ApiResponse(
      responseCode = "400",
      description = "matriculaId del path no coincide con el del body, o validación fallida")
  @ApiResponse(
      responseCode = "403",
      description = "La matrícula no pertenece a una impartición del profesor")
  @ApiResponse(responseCode = "401", description = "No autenticado")
  @PostMapping("/api/matricula/{matriculaId}/notas")
  @ResponseBody
  public TeacherStudentGradesDTO postNotas(
      @Parameter(description = "ID de la matrícula", required = true, example = "42") @PathVariable
          Integer matriculaId,
      @Valid @RequestBody GradeUpsertRequest req,
      Principal principal) {
    if (!matriculaId.equals(req.matriculaId())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "matriculaId en path y body no coinciden");
    }
    Usuario profesor = resolveProfesor(principal);
    return teacherService.upsertGrades(profesor.getId(), profesor.getEmail(), req);
  }

  @Operation(
      summary = "Acción: crear ítem evaluable",
      description =
          "Crea un nuevo ítem evaluable en la impartición (ej: examen, práctica, proyecto). "
              + "Datos: nombre, tipo (TipoActividad), fecha, períodoEvaluacionId, resultadoAprendizajeId. "
              + "Con errores de validación: redirect con flash 'errorItem'. "
              + "Con éxito: redirect al cuaderno de la impartición.")
  @ApiResponse(
      responseCode = "302",
      description = "Creación exitosa → redirect a /profe/imparticion/{imparticionId}/alumnos")
  @ApiResponse(
      responseCode = "302",
      description = "Error de validación o negocio → redirect con flash 'errorItem'")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_PROFESOR")
  @PostMapping("/imparticion/{imparticionId}/items")
  public String crearItem(
      @Parameter(description = "ID de la impartición", required = true, example = "12")
          @PathVariable
          @Positive
          Integer imparticionId,
      @Valid @ModelAttribute("itemForm") ItemEvaluableFormDTO dto,
      BindingResult result,
      Principal principal,
      RedirectAttributes ra) {
    if (result.hasErrors()) {
      ra.addFlashAttribute(ATTR_ERROR_ITEM, "Datos del ítem no válidos.");
      return REDIRECT_IMPARTICION + imparticionId + PATH_ALUMNOS;
    }
    Usuario profesor = resolveProfesor(principal);
    try {
      teacherService.crearItem(imparticionId, profesor.getId(), dto);
      ra.addFlashAttribute("okItem", "Ítem añadido correctamente.");
    } catch (Exception ex) {
      ra.addFlashAttribute(ATTR_ERROR_ITEM, ex.getMessage());
    }
    return REDIRECT_IMPARTICION + imparticionId + PATH_ALUMNOS;
  }

  @Operation(
      summary = "Acción: eliminar ítem evaluable",
      description =
          "Elimina el ítem evaluable indicado. "
              + "Validación de ownership: el ítem debe pertenecer a una impartición del profesor. "
              + "Si tiene calificaciones registradas, el servicio puede lanzar error. "
              + "Con éxito: redirect al cuaderno de la impartición correspondiente.")
  @ApiResponse(
      responseCode = "302",
      description = "Eliminación exitosa → redirect a /profe/imparticion/{imparticionId}/alumnos")
  @ApiResponse(
      responseCode = "302",
      description = "Error → redirect a /profe/dashboard con flash 'errorItem'")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_PROFESOR")
  @PostMapping("/items/{itemId}/eliminar")
  public String eliminarItem(
      @Parameter(description = "ID del ítem evaluable", required = true, example = "8")
          @PathVariable
          @Positive
          Integer itemId,
      Principal principal,
      RedirectAttributes ra) {
    Usuario profesor = resolveProfesor(principal);
    try {
      Integer imparticionId = teacherService.eliminarItem(itemId, profesor.getId());
      ra.addFlashAttribute("okItem", "Ítem eliminado.");
      return REDIRECT_IMPARTICION + imparticionId + PATH_ALUMNOS;
    } catch (Exception ex) {
      ra.addFlashAttribute(ATTR_ERROR_ITEM, ex.getMessage());
      return "redirect:/profe/dashboard";
    }
  }

  private Usuario resolveProfesor(Principal principal) {
    return usuarioService
        .buscarPorCorreo(principal.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
  }
}
