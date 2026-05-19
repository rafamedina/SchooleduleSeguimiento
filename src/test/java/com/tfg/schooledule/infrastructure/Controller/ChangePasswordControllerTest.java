package com.tfg.schooledule.infrastructure.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tfg.schooledule.domain.entity.Rol;
import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.infrastructure.repository.CursoAcademicoRepository;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import com.tfg.schooledule.infrastructure.security.SecurityAuditLogger;
import com.tfg.schooledule.infrastructure.service.AdminCursoActivoService;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChangePasswordController.class)
@Import(ChangePasswordControllerTest.MethodSecurityTestConfig.class)
class ChangePasswordControllerTest {

  @EnableMethodSecurity
  static class MethodSecurityTestConfig {}

  @Autowired private MockMvc mockMvc;

  @MockBean private UsuarioRepository usuarioRepository;
  @MockBean private PasswordEncoder passwordEncoder;
  @MockBean private SecurityAuditLogger securityAuditLogger;
  @MockBean private CursoAcademicoRepository cursoAcademicoRepository;
  @MockBean private AdminCursoActivoService adminCursoActivoService;

  private Usuario buildUsuario(boolean mustChange, String rolNombre) {
    Rol rol = new Rol();
    rol.setNombre(rolNombre);
    Usuario u = new Usuario();
    u.setEmail("admin@tfg.com");
    u.setMustChangePassword(mustChange);
    u.setRoles(Set.of(rol));
    return u;
  }

  @Test
  void get_sinAuth_redirige302() throws Exception {
    mockMvc
        .perform(get("/change-password").header("Accept", "text/html"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(username = "admin@tfg.com", roles = "ADMIN")
  void get_mustChangePasswordTrue_muestraFormulario() throws Exception {
    when(usuarioRepository.findUsuarioByEmail("admin@tfg.com"))
        .thenReturn(Optional.of(buildUsuario(true, "ROLE_ADMIN")));

    mockMvc
        .perform(get("/change-password"))
        .andExpect(status().isOk())
        .andExpect(view().name("change-password"))
        .andExpect(model().attributeExists("form"));
  }

  @Test
  @WithMockUser(username = "admin@tfg.com", roles = "ADMIN")
  void get_mustChangePasswordFalse_redirigeDashboard() throws Exception {
    when(usuarioRepository.findUsuarioByEmail("admin@tfg.com"))
        .thenReturn(Optional.of(buildUsuario(false, "ROLE_ADMIN")));

    mockMvc
        .perform(get("/change-password"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/dashboard"));
  }

  @Test
  @WithMockUser(username = "admin@tfg.com", roles = "ADMIN")
  void post_passwordInvalida_reRenderizaFormulario() throws Exception {
    when(usuarioRepository.findUsuarioByEmail("admin@tfg.com"))
        .thenReturn(Optional.of(buildUsuario(true, "ROLE_ADMIN")));

    mockMvc
        .perform(
            post("/change-password")
                .param("nuevaPassword", "corta")
                .param("confirmarPassword", "corta")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("change-password"))
        .andExpect(model().attributeHasFieldErrors("form", "nuevaPassword"));
  }

  @Test
  @WithMockUser(username = "admin@tfg.com", roles = "ADMIN")
  void post_passwordsNoCoinciden_reRenderizaConError() throws Exception {
    when(usuarioRepository.findUsuarioByEmail("admin@tfg.com"))
        .thenReturn(Optional.of(buildUsuario(true, "ROLE_ADMIN")));

    mockMvc
        .perform(
            post("/change-password")
                .param("nuevaPassword", "Password1")
                .param("confirmarPassword", "OtraPassword1")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("change-password"))
        .andExpect(model().attributeExists("errorConfirm"));
  }

  @Test
  @WithMockUser(username = "admin@tfg.com", roles = "ADMIN")
  void post_datosValidos_guardaYRedirige() throws Exception {
    Usuario usuario = buildUsuario(true, "ROLE_ADMIN");
    when(usuarioRepository.findUsuarioByEmail("admin@tfg.com")).thenReturn(Optional.of(usuario));
    when(passwordEncoder.encode("Password1")).thenReturn("$2a$hash");

    mockMvc
        .perform(
            post("/change-password")
                .param("nuevaPassword", "Password1")
                .param("confirmarPassword", "Password1")
                .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/dashboard"));

    verify(usuarioRepository).save(any(Usuario.class));
  }
}
