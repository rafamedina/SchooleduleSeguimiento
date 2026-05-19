package com.tfg.schooledule.infrastructure.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tfg.schooledule.infrastructure.repository.CursoAcademicoRepository;
import com.tfg.schooledule.infrastructure.security.SecurityAuditLogger;
import com.tfg.schooledule.infrastructure.service.AdminCursoActivoService;
import com.tfg.schooledule.infrastructure.service.ModuloPlantillaExcelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminModuloImportController.class)
@Import(AdminModuloImportControllerTest.MethodSecurityTestConfig.class)
class AdminModuloImportControllerTest {

  @EnableMethodSecurity
  static class MethodSecurityTestConfig {}

  @Autowired private MockMvc mockMvc;

  @MockBean private ModuloPlantillaExcelService plantillaService;
  @MockBean private CursoAcademicoRepository cursoAcademicoRepository;
  @MockBean private AdminCursoActivoService adminCursoActivoService;
  @MockBean private SecurityAuditLogger securityAuditLogger;

  private static final byte[] BYTES_DUMMY = new byte[] {1, 2, 3, 4};

  @Test
  void descargarPlantilla_sinAuth_redirige302() throws Exception {
    mockMvc
        .perform(get("/admin/modulos/plantilla-excel").header("Accept", "text/html"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(roles = "ALUMNO")
  void descargarPlantilla_conAlumno_403() throws Exception {
    mockMvc.perform(get("/admin/modulos/plantilla-excel")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void descargarPlantilla_conAdmin_200_contentTypeExcel() throws Exception {
    when(plantillaService.generarPlantilla()).thenReturn(BYTES_DUMMY);

    mockMvc
        .perform(get("/admin/modulos/plantilla-excel"))
        .andExpect(status().isOk())
        .andExpect(
            header()
                .string(
                    "Content-Type",
                    org.hamcrest.Matchers.containsString(
                        "openxmlformats-officedocument.spreadsheetml.sheet")));
  }
}
