package com.tfg.schooledule.infrastructure.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.tfg.schooledule.domain.dto.AdminModuloPesosFormDTO;
import com.tfg.schooledule.domain.dto.AdminModuloResumenDTO;
import com.tfg.schooledule.domain.dto.ModuloImportErrorDTO;
import com.tfg.schooledule.domain.exception.ModuloImportException;
import com.tfg.schooledule.infrastructure.repository.CursoAcademicoRepository;
import com.tfg.schooledule.infrastructure.security.SecurityAuditLogger;
import com.tfg.schooledule.infrastructure.service.AdminCursoActivoService;
import com.tfg.schooledule.infrastructure.service.AdminModuloService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminModuloController.class)
@Import(AdminModuloControllerTest.MethodSecurityTestConfig.class)
class AdminModuloControllerTest {

  @EnableMethodSecurity
  static class MethodSecurityTestConfig {}

  @Autowired private MockMvc mockMvc;

  @MockBean private AdminModuloService adminModuloService;
  @MockBean private CursoAcademicoRepository cursoAcademicoRepository;
  @MockBean private AdminCursoActivoService adminCursoActivoService;
  @MockBean private SecurityAuditLogger securityAuditLogger;

  private static final String XLSX_MIME =
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

  private MockMultipartFile xlsxFile() {
    return new MockMultipartFile("archivo", "datos.xlsx", XLSX_MIME, new byte[] {1, 2, 3});
  }

  // ── Lista ─────────────────────────────────────────────────────────────────

  @Test
  void lista_sinAuth_redirige302() throws Exception {
    mockMvc
        .perform(get("/admin/modulos").header("Accept", "text/html"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(roles = "ALUMNO")
  void lista_conAlumno_403() throws Exception {
    mockMvc.perform(get("/admin/modulos")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "PROFESOR")
  void lista_conProfesor_403() throws Exception {
    mockMvc.perform(get("/admin/modulos")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void lista_conAdmin_200_retornaVista() throws Exception {
    when(adminModuloService.listarFiltrado(any())).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/admin/modulos"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/modulos/lista"))
        .andExpect(model().attributeExists("modulos"));
  }

  // ── Resumen (JSON) ────────────────────────────────────────────────────────

  @Test
  void resumen_sinAuth_401() throws Exception {
    mockMvc
        .perform(get("/admin/modulos/1/resumen").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "PROFESOR")
  void resumen_conProfesor_403() throws Exception {
    mockMvc
        .perform(get("/admin/modulos/1/resumen").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void resumen_conAdmin_200_devuelveJson() throws Exception {
    AdminModuloResumenDTO dto =
        new AdminModuloResumenDTO(1, "DAW01", "Programación", true, 2, 1, 3, 8, List.of());
    when(adminModuloService.getResumen(1)).thenReturn(dto);

    mockMvc
        .perform(get("/admin/modulos/1/resumen").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.codigo").value("DAW01"))
        .andExpect(jsonPath("$.numRasTotal").value(3))
        .andExpect(jsonPath("$.cursos").isArray());
  }

  // ── Importar módulo ───────────────────────────────────────────────────────

  @Test
  void importar_sinAuth_redirige302() throws Exception {
    mockMvc
        .perform(get("/admin/modulos/importar").header("Accept", "text/html"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void formularioImportar_conAdmin_200() throws Exception {
    when(cursoAcademicoRepository.findAllByOrderByFechaInicioDesc())
        .thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/admin/modulos/importar"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/modulos/importar"))
        .andExpect(model().attributeExists("form"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void importar_archivoVacio_muestraError() throws Exception {
    MockMultipartFile vacio =
        new MockMultipartFile("archivo", "vacio.xlsx", XLSX_MIME, new byte[0]);

    mockMvc
        .perform(
            multipart("/admin/modulos/importar")
                .file(vacio)
                .param("codigo", "DAW01")
                .param("nombre", "Desarrollo Web")
                .param("cursoAcademicoId", "1")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/modulos/importar"))
        .andExpect(model().attributeExists("error"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void importar_archivoNoXlsx_muestraError() throws Exception {
    MockMultipartFile csv =
        new MockMultipartFile("archivo", "datos.csv", "text/csv", new byte[] {1, 2, 3});

    mockMvc
        .perform(
            multipart("/admin/modulos/importar")
                .file(csv)
                .param("codigo", "DAW01")
                .param("nombre", "Desarrollo Web")
                .param("cursoAcademicoId", "1")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/modulos/importar"))
        .andExpect(model().attributeExists("error"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void importar_codigoBlanco_reRenderizaFormulario() throws Exception {
    mockMvc
        .perform(
            multipart("/admin/modulos/importar")
                .file(xlsxFile())
                .param("codigo", "")
                .param("nombre", "Desarrollo Web")
                .param("cursoAcademicoId", "1")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/modulos/importar"))
        .andExpect(model().attributeHasFieldErrors("form", "codigo"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void importar_archivoValido_redirigeLista() throws Exception {
    when(adminModuloService.importarModulo(eq("DAW01"), any(), eq(1), any())).thenReturn(12);

    mockMvc
        .perform(
            multipart("/admin/modulos/importar")
                .file(xlsxFile())
                .param("codigo", "DAW01")
                .param("nombre", "Desarrollo Web")
                .param("cursoAcademicoId", "1")
                .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/modulos"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void importar_servicioLanzaModuloImportException_muestraErrores() throws Exception {
    List<ModuloImportErrorDTO> errores =
        List.of(new ModuloImportErrorDTO(1, "ra_codigo", "El código de RA es obligatorio"));
    when(adminModuloService.importarModulo(any(), any(), any(), any()))
        .thenThrow(new ModuloImportException(errores));

    mockMvc
        .perform(
            multipart("/admin/modulos/importar")
                .file(xlsxFile())
                .param("codigo", "DAW01")
                .param("nombre", "Desarrollo Web")
                .param("cursoAcademicoId", "1")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/modulos/importar"))
        .andExpect(model().attributeExists("errores"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void importar_rasYaExisten_muestraError() throws Exception {
    when(adminModuloService.importarModulo(any(), any(), any(), any()))
        .thenThrow(new IllegalStateException("Ya existen RAs para este módulo y curso."));

    mockMvc
        .perform(
            multipart("/admin/modulos/importar")
                .file(xlsxFile())
                .param("codigo", "DAW01")
                .param("nombre", "Desarrollo Web")
                .param("cursoAcademicoId", "1")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/modulos/importar"))
        .andExpect(model().attributeExists("error"));
  }

  // ── Editar pesos ──────────────────────────────────────────────────────────

  @Test
  void editarPesos_sinAuth_redirige302() throws Exception {
    mockMvc
        .perform(get("/admin/modulos/1/editar").header("Accept", "text/html"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(roles = "PROFESOR")
  void editarPesos_conProfesor_403() throws Exception {
    mockMvc.perform(get("/admin/modulos/1/editar")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void editarPesos_conAdmin_200_retornaVista() throws Exception {
    when(adminModuloService.obtenerParaEditarPesos(1))
        .thenReturn(new AdminModuloPesosFormDTO("COD", "Nombre", List.of()));

    mockMvc
        .perform(get("/admin/modulos/1/editar"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/modulos/editar-pesos"))
        .andExpect(model().attributeExists("form"))
        .andExpect(model().attribute("moduloId", 1));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void actualizarPesos_datosValidos_redirigeLista() throws Exception {
    mockMvc
        .perform(
            post("/admin/modulos/1/editar")
                .param("codigo", "DAW02")
                .param("nombre", "Bases de Datos")
                .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/modulos"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void actualizarPesos_codigoBlanco_reRenderizaFormulario() throws Exception {
    mockMvc
        .perform(
            post("/admin/modulos/1/editar")
                .param("codigo", "")
                .param("nombre", "Bases de Datos")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/modulos/editar-pesos"))
        .andExpect(model().attributeHasFieldErrors("form", "codigo"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void actualizarPesos_servicioLanzaIllegalArgument_muestraError() throws Exception {
    doThrow(new IllegalArgumentException("El RA 5 no pertenece al módulo 1"))
        .when(adminModuloService)
        .actualizarPesos(eq(1), any());

    mockMvc
        .perform(
            post("/admin/modulos/1/editar")
                .param("codigo", "DAW02")
                .param("nombre", "Bases de Datos")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/modulos/editar-pesos"))
        .andExpect(model().attributeExists("error"));
  }

  // ── Toggle activo ─────────────────────────────────────────────────────────

  @Test
  @WithMockUser(roles = "ADMIN")
  void toggleActivo_conImparticiones_redirigeLista() throws Exception {
    doThrow(new IllegalStateException("Tiene imparticiones"))
        .when(adminModuloService)
        .toggleActivo(1);

    mockMvc
        .perform(post("/admin/modulos/1/toggle-activo").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/modulos"));
  }
}
