package com.tfg.schooledule.infrastructure.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.infrastructure.repository.CursoAcademicoRepository;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import com.tfg.schooledule.infrastructure.security.SecurityAuditLogger;
import com.tfg.schooledule.infrastructure.service.AdminAuditoriaService;
import com.tfg.schooledule.infrastructure.service.AdminCursoActivoService;
import com.tfg.schooledule.infrastructure.service.AuditoriaExcelExportService;
import com.tfg.schooledule.infrastructure.service.CentroAdminContextService;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CentroAdminAuditoriaController.class)
@Import(CentroAdminAuditoriaControllerTest.MethodSecurityTestConfig.class)
class CentroAdminAuditoriaControllerTest {

  @EnableMethodSecurity
  static class MethodSecurityTestConfig {}

  @Autowired private MockMvc mockMvc;

  @MockBean private AdminAuditoriaService adminAuditoriaService;
  @MockBean private AuditoriaExcelExportService auditoriaExcelExportService;
  @MockBean private CentroAdminContextService contextService;
  @MockBean private UsuarioRepository usuarioRepository;
  @MockBean private CursoAcademicoRepository cursoAcademicoRepository;
  @MockBean private AdminCursoActivoService adminCursoActivoService;
  @MockBean private SecurityAuditLogger securityAuditLogger;

  private void mockAdminCentro(String email, Integer adminId, Set<Integer> centroIds) {
    Usuario usuario = Usuario.builder().id(adminId).email(email).build();
    when(usuarioRepository.findUsuarioByEmail(email)).thenReturn(Optional.of(usuario));
    when(contextService.getCentroIdsDelAdmin(adminId)).thenReturn(centroIds);
  }

  @Test
  void lista_sinAuth_redirige302() throws Exception {
    mockMvc
        .perform(get("/centro-admin/auditoria").header("Accept", "text/html"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void lista_conAdminGlobal_403() throws Exception {
    mockMvc.perform(get("/centro-admin/auditoria")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ALUMNO")
  void lista_conAlumno_403() throws Exception {
    mockMvc.perform(get("/centro-admin/auditoria")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "admin@centro.es", roles = "ADMIN_CENTRO")
  void lista_conAdminCentro_200_retornaVista() throws Exception {
    mockAdminCentro("admin@centro.es", 5, Set.of(1, 2));
    when(adminAuditoriaService.buscar(null, null, null, null, Set.of(1, 2)))
        .thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/centro-admin/auditoria"))
        .andExpect(status().isOk())
        .andExpect(view().name("centro-admin/auditoria/lista"))
        .andExpect(model().attributeExists("registros"));
  }

  @Test
  @WithMockUser(username = "admin@centro.es", roles = "ADMIN_CENTRO")
  void lista_conAdminCentro_pasaCentroIdsDelContexto() throws Exception {
    Set<Integer> centroIds = Set.of(3);
    mockAdminCentro("admin@centro.es", 5, centroIds);
    when(adminAuditoriaService.buscar(null, null, null, null, centroIds))
        .thenReturn(Collections.emptyList());

    mockMvc.perform(get("/centro-admin/auditoria")).andExpect(status().isOk());

    verify(adminAuditoriaService).buscar(null, null, null, null, centroIds);
  }

  @Test
  void exportarExcel_sinAuth_redirige302() throws Exception {
    mockMvc
        .perform(get("/centro-admin/auditoria/exportar").header("Accept", "text/html"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(username = "admin@centro.es", roles = "ADMIN_CENTRO")
  void exportarExcel_conAdminCentro_200_retornaOctetStream() throws Exception {
    mockAdminCentro("admin@centro.es", 5, Set.of(1));
    when(adminAuditoriaService.buscar(null, null, null, null, Set.of(1)))
        .thenReturn(Collections.emptyList());
    when(auditoriaExcelExportService.exportar(Collections.emptyList())).thenReturn(new byte[0]);

    mockMvc
        .perform(get("/centro-admin/auditoria/exportar"))
        .andExpect(status().isOk())
        .andExpect(
            header()
                .string(
                    "Content-Type",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .andExpect(
            header()
                .string(
                    "Content-Disposition",
                    org.hamcrest.Matchers.startsWith("attachment; filename=auditoria_")));
  }

  @Test
  @WithMockUser(username = "admin@centro.es", roles = "ADMIN_CENTRO")
  void exportarExcel_conAdminCentro_usaCentroIdsDelContexto() throws Exception {
    Set<Integer> centroIds = Set.of(7);
    mockAdminCentro("admin@centro.es", 5, centroIds);
    when(adminAuditoriaService.buscar(null, null, null, null, centroIds))
        .thenReturn(Collections.emptyList());
    when(auditoriaExcelExportService.exportar(Collections.emptyList())).thenReturn(new byte[0]);

    mockMvc.perform(get("/centro-admin/auditoria/exportar")).andExpect(status().isOk());

    verify(adminAuditoriaService).buscar(null, null, null, null, centroIds);
    verify(contextService).getCentroIdsDelAdmin(5);
  }
}
