package com.tfg.schooledule.infrastructure.config;

import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class CustomLoginSuccessHandlerTest {

  private CustomLoginSuccessHandler handler;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private Authentication authentication;

  @BeforeEach
  void setUp() {
    handler = new CustomLoginSuccessHandler();
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    authentication = mock(Authentication.class);
  }

  @Test
  void testRedirectAdmin() throws Exception {
    doReturn(Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")))
        .when(authentication)
        .getAuthorities();

    handler.onAuthenticationSuccess(request, response, authentication);

    verify(response).sendRedirect("/admin/dashboard");
  }

  @Test
  void testRedirectProfesor() throws Exception {
    doReturn(Collections.singleton(new SimpleGrantedAuthority("ROLE_PROFESOR")))
        .when(authentication)
        .getAuthorities();

    handler.onAuthenticationSuccess(request, response, authentication);

    verify(response).sendRedirect("/profe/dashboard");
  }

  @Test
  void testRedirectAlumno() throws Exception {
    doReturn(Collections.singleton(new SimpleGrantedAuthority("ROLE_ALUMNO")))
        .when(authentication)
        .getAuthorities();

    handler.onAuthenticationSuccess(request, response, authentication);

    verify(response).sendRedirect("/alumno/dashboard");
  }

  @Test
  void testRedirectMultipleRoles() throws Exception {
    java.util.List<SimpleGrantedAuthority> authorities =
        java.util.Arrays.asList(
            new SimpleGrantedAuthority("ROLE_ALUMNO"), new SimpleGrantedAuthority("ROLE_PROFESOR"));
    doReturn(authorities).when(authentication).getAuthorities();

    handler.onAuthenticationSuccess(request, response, authentication);

    verify(response).sendRedirect("/seleccionar-rol");
  }

  @Test
  void testRedirectDefault() throws Exception {
    doReturn(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")))
        .when(authentication)
        .getAuthorities();

    handler.onAuthenticationSuccess(request, response, authentication);

    verify(response).sendRedirect("/");
  }
}
