package com.tfg.schooledule.infrastructure.config;

import static org.mockito.Mockito.*;

import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class CustomLoginSuccessHandlerTest {

  private CustomLoginSuccessHandler handler;
  private UsuarioRepository usuarioRepository;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private Authentication authentication;

  @BeforeEach
  void setUp() {
    usuarioRepository = mock(UsuarioRepository.class);
    handler = new CustomLoginSuccessHandler(usuarioRepository);
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    authentication = mock(Authentication.class);
    when(authentication.getName()).thenReturn("user@test.com");
  }

  private void givenUserMustChangePassword(boolean mustChange) {
    Usuario usuario = mock(Usuario.class);
    when(usuario.getMustChangePassword()).thenReturn(mustChange);
    when(usuarioRepository.findUsuarioByEmail("user@test.com")).thenReturn(Optional.of(usuario));
  }

  @Test
  void testRedirectToCambioPasswordCuandoMustChangePasswordEsTrue() throws Exception {
    givenUserMustChangePassword(true);
    doReturn(Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")))
        .when(authentication)
        .getAuthorities();

    handler.onAuthenticationSuccess(request, response, authentication);

    verify(response).sendRedirect("/change-password");
  }

  @ParameterizedTest(name = "rol={0} → redirect={1}")
  @CsvSource({
    "ROLE_ADMIN, /admin/dashboard",
    "ROLE_PROFESOR, /profe/dashboard",
    "ROLE_ALUMNO, /alumno/dashboard"
  })
  void testRedirectPorRol(String rol, String urlEsperada) throws Exception {
    givenUserMustChangePassword(false);
    doReturn(Collections.singleton(new SimpleGrantedAuthority(rol)))
        .when(authentication)
        .getAuthorities();

    handler.onAuthenticationSuccess(request, response, authentication);

    verify(response).sendRedirect(urlEsperada);
  }

  @Test
  void testRedirectMultipleRoles() throws Exception {
    givenUserMustChangePassword(false);
    doReturn(
            Arrays.asList(
                new SimpleGrantedAuthority("ROLE_ALUMNO"),
                new SimpleGrantedAuthority("ROLE_PROFESOR")))
        .when(authentication)
        .getAuthorities();

    handler.onAuthenticationSuccess(request, response, authentication);

    verify(response).sendRedirect("/seleccionar-rol");
  }

  @Test
  void testRedirectDefault() throws Exception {
    givenUserMustChangePassword(false);
    doReturn(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")))
        .when(authentication)
        .getAuthorities();

    handler.onAuthenticationSuccess(request, response, authentication);

    verify(response).sendRedirect("/login?norole");
  }
}
