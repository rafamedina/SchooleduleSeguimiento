package com.tfg.schooledule.infrastructure.config;

import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

  private final UsuarioRepository usuarioRepository;

  public CustomLoginSuccessHandler(UsuarioRepository usuarioRepository) {
    this.usuarioRepository = usuarioRepository;
  }

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {

    String email = authentication.getName();
    boolean mustChange =
        usuarioRepository
            .findUsuarioByEmail(email)
            .map(u -> Boolean.TRUE.equals(u.getMustChangePassword()))
            .orElse(false);

    if (mustChange) {
      response.sendRedirect("/change-password");
      return;
    }

    java.util.Set<String> roles =
        org.springframework.security.core.authority.AuthorityUtils.authorityListToSet(
            authentication.getAuthorities());

    java.util.Set<String> functionalRoles =
        roles.stream()
            .filter(
                role ->
                    role.equals("ROLE_ADMIN")
                        || role.equals("ROLE_ADMIN_CENTRO")
                        || role.equals("ROLE_PROFESOR")
                        || role.equals("ROLE_ALUMNO"))
            .collect(java.util.stream.Collectors.toSet());

    if (functionalRoles.size() > 1) {
      response.sendRedirect("/seleccionar-rol");
      return;
    }

    if (roles.contains("ROLE_ADMIN")) {
      response.sendRedirect("/admin/dashboard");
    } else if (roles.contains("ROLE_ADMIN_CENTRO")) {
      response.sendRedirect("/centro-admin/dashboard");
    } else if (roles.contains("ROLE_PROFESOR")) {
      response.sendRedirect("/profe/dashboard");
    } else if (roles.contains("ROLE_ALUMNO")) {
      response.sendRedirect("/alumno/dashboard");
    } else {
      response.sendRedirect("/login?norole");
    }
  }
}
