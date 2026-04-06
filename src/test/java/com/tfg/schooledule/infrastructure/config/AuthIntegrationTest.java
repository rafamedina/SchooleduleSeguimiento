package com.tfg.schooledule.infrastructure.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tfg.schooledule.domain.entity.Rol;
import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.infrastructure.repository.RolRepository;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import java.util.Collections;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private UsuarioRepository usuarioRepository;

  @Autowired private RolRepository rolRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    usuarioRepository.deleteAll();
    rolRepository.deleteAll();

    Rol adminRole = new Rol();
    adminRole.setNombre("ROLE_ADMIN");
    rolRepository.save(adminRole);

    Rol profeRole = new Rol();
    profeRole.setNombre("ROLE_PROFESOR");
    rolRepository.save(profeRole);

    Rol alumnoRole = new Rol();
    alumnoRole.setNombre("ROLE_ALUMNO");
    rolRepository.save(alumnoRole);

    // Create Admin
    Usuario admin = new Usuario();
    admin.setUsername("admin");
    admin.setEmail("admin@tfg.com");
    admin.setPasswordHash(passwordEncoder.encode("1234"));
    admin.setNombre("Admin");
    admin.setApellidos("User");
    admin.setActivo(true);
    admin.setRoles(new HashSet<>(Collections.singletonList(adminRole)));
    usuarioRepository.save(admin);

    // Create Professor
    Usuario profe = new Usuario();
    profe.setUsername("profe");
    profe.setEmail("profe@tfg.com");
    profe.setPasswordHash(passwordEncoder.encode("1234"));
    profe.setNombre("Profe");
    profe.setApellidos("User");
    profe.setActivo(true);
    profe.setRoles(new HashSet<>(Collections.singletonList(profeRole)));
    usuarioRepository.save(profe);

    // Create Alumno
    Usuario alumno = new Usuario();
    alumno.setUsername("alumno");
    alumno.setEmail("alumno@tfg.com");
    alumno.setPasswordHash(passwordEncoder.encode("1234"));
    alumno.setNombre("Alumno");
    alumno.setApellidos("User");
    alumno.setActivo(true);
    alumno.setRoles(new HashSet<>(Collections.singletonList(alumnoRole)));
    usuarioRepository.save(alumno);
  }

  @Test
  void testLoginAdminSuccess() throws Exception {
    mockMvc
        .perform(formLogin().user("admin@tfg.com").password("1234"))
        .andExpect(authenticated())
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/dashboard"));
  }

  @Test
  void testLoginProfessorSuccess() throws Exception {
    mockMvc
        .perform(formLogin().user("profe@tfg.com").password("1234"))
        .andExpect(authenticated())
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/profe/dashboard"));
  }

  @Test
  void testLoginAlumnoSuccess() throws Exception {
    mockMvc
        .perform(formLogin().user("alumno@tfg.com").password("1234"))
        .andExpect(authenticated())
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/alumno/dashboard"));
  }

  @Test
  void testLoginRoleWithoutPrefix() throws Exception {
    // Create a role without ROLE_ prefix
    Rol rawRole = new Rol();
    rawRole.setNombre("PROFESOR");
    rolRepository.save(rawRole);

    Usuario user = new Usuario();
    user.setUsername("rawprofe");
    user.setEmail("rawprofe@tfg.com");
    user.setPasswordHash(passwordEncoder.encode("1234"));
    user.setNombre("Raw");
    user.setApellidos("Profe");
    user.setActivo(true);
    user.setRoles(new HashSet<>(Collections.singletonList(rawRole)));
    usuarioRepository.save(user);

    mockMvc
        .perform(formLogin().user("rawprofe@tfg.com").password("1234"))
        .andExpect(authenticated())
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/profe/dashboard"));
  }

  @Test
  void testLoginInvalidCredentials() throws Exception {
    mockMvc
        .perform(formLogin().user("admin@tfg.com").password("wrong"))
        .andExpect(unauthenticated())
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login?error"));
  }
}
