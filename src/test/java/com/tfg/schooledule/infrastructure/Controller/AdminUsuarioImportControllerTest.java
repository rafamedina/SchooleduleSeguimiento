package com.tfg.schooledule.infrastructure.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.tfg.schooledule.domain.dto.UsuarioImportErrorDTO;
import com.tfg.schooledule.domain.dto.UsuarioImportResultado;
import com.tfg.schooledule.domain.exception.UsuarioImportException;
import com.tfg.schooledule.infrastructure.repository.CursoAcademicoRepository;
import com.tfg.schooledule.infrastructure.security.SecurityAuditLogger;
import com.tfg.schooledule.infrastructure.service.AdminCursoActivoService;
import com.tfg.schooledule.infrastructure.service.UsuarioImportService;
import com.tfg.schooledule.infrastructure.service.UsuarioPlantillaExcelService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminUsuarioImportController.class)
@Import(AdminUsuarioImportControllerTest.MethodSecurityTestConfig.class)
class AdminUsuarioImportControllerTest {

  @EnableMethodSecurity
  static class MethodSecurityTestConfig {}

  @Autowired private MockMvc mockMvc;

  @MockBean private UsuarioImportService usuarioImportService;
  @MockBean private UsuarioPlantillaExcelService plantillaService;
  @MockBean private CursoAcademicoRepository cursoAcademicoRepository;
  @MockBean private AdminCursoActivoService adminCursoActivoService;
  @MockBean private SecurityAuditLogger securityAuditLogger;

  private static final String XLSX_MIME =
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

  private MockMultipartFile xlsxFile() {
    return new MockMultipartFile("archivo", "usuarios.xlsx", XLSX_MIME, new byte[] {1, 2, 3});
  }

  // ── Plantilla ─────────────────────────────────────────────────────────────

  @Test
  void descargarPlantilla_sinAuth_redirige302() throws Exception {
    mockMvc
        .perform(get("/admin/usuarios/plantilla-excel").header("Accept", "text/html"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(roles = "ALUMNO")
  void descargarPlantilla_conAlumno_403() throws Exception {
    mockMvc.perform(get("/admin/usuarios/plantilla-excel")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void descargarPlantilla_conAdmin_200_contentTypeExcel() throws Exception {
    when(plantillaService.generarPlantilla()).thenReturn(new byte[] {1, 2, 3});

    mockMvc
        .perform(get("/admin/usuarios/plantilla-excel"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(XLSX_MIME));
  }

  // ── Formulario ────────────────────────────────────────────────────────────

  @Test
  void formulario_sinAuth_redirige302() throws Exception {
    mockMvc
        .perform(get("/admin/usuarios/importar").header("Accept", "text/html"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void formulario_conAdmin_200_retornaVista() throws Exception {
    mockMvc
        .perform(get("/admin/usuarios/importar"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/usuarios/importar"));
  }

  // ── POST importar ─────────────────────────────────────────────────────────

  @Test
  void importar_sinAuth_redirige302() throws Exception {
    mockMvc
        .perform(
            multipart("/admin/usuarios/importar")
                .file(xlsxFile())
                .header("Accept", "text/html")
                .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(roles = "ALUMNO")
  void importar_conAlumno_403() throws Exception {
    mockMvc
        .perform(multipart("/admin/usuarios/importar").file(xlsxFile()).with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "PROFESOR")
  void importar_conProfesor_403() throws Exception {
    mockMvc
        .perform(multipart("/admin/usuarios/importar").file(xlsxFile()).with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void importar_archivoVacio_muestraError() throws Exception {
    MockMultipartFile vacio =
        new MockMultipartFile("archivo", "usuarios.xlsx", XLSX_MIME, new byte[0]);

    mockMvc
        .perform(multipart("/admin/usuarios/importar").file(vacio).with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/usuarios/importar"))
        .andExpect(model().attributeExists("error"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void importar_archivoNoXlsx_csv_muestraError() throws Exception {
    MockMultipartFile csv =
        new MockMultipartFile("archivo", "datos.csv", "text/csv", new byte[] {1, 2, 3});

    mockMvc
        .perform(multipart("/admin/usuarios/importar").file(csv).with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/usuarios/importar"))
        .andExpect(model().attributeExists("error"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void importar_archivoValido_redirige() throws Exception {
    when(usuarioImportService.importar(any())).thenReturn(new UsuarioImportResultado(3, 9));

    mockMvc
        .perform(multipart("/admin/usuarios/importar").file(xlsxFile()).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/usuarios"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void importar_servicioLanzaUsuarioImportException_muestraErrores() throws Exception {
    List<UsuarioImportErrorDTO> errores =
        List.of(
            new UsuarioImportErrorDTO(1, "username", "ya existe"),
            new UsuarioImportErrorDTO(2, "email", "ya existe"));
    when(usuarioImportService.importar(any())).thenThrow(new UsuarioImportException(errores));

    mockMvc
        .perform(multipart("/admin/usuarios/importar").file(xlsxFile()).with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/usuarios/importar"))
        .andExpect(model().attributeExists("errores"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void importar_servicioLanzaIllegalArgument_muestraError() throws Exception {
    when(usuarioImportService.importar(any()))
        .thenThrow(new IllegalArgumentException("No es xlsx"));

    mockMvc
        .perform(multipart("/admin/usuarios/importar").file(xlsxFile()).with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/usuarios/importar"))
        .andExpect(model().attributeExists("error"));
  }
}
