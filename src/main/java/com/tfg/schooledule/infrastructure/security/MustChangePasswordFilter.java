package com.tfg.schooledule.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

public class MustChangePasswordFilter extends OncePerRequestFilter {

  private static final String SESSION_KEY = "mustChangePassword";
  private static final String CHANGE_PASSWORD_URL = "/change-password";

  private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
  private static final List<String> SKIP_PATHS =
      List.of(
          CHANGE_PASSWORD_URL,
          "/logout",
          "/login",
          "/login/**",
          "/css/**",
          "/js/**",
          "/images/**",
          "/error");

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (!isAuthenticatedUser(auth)) {
      chain.doFilter(request, response);
      return;
    }

    String path = request.getRequestURI();
    if (shouldSkip(path)) {
      chain.doFilter(request, response);
      return;
    }

    HttpSession session = request.getSession(false);
    if (session != null && Boolean.TRUE.equals(session.getAttribute(SESSION_KEY))) {
      response.sendRedirect(request.getContextPath() + CHANGE_PASSWORD_URL);
      return;
    }

    chain.doFilter(request, response);
  }

  private boolean isAuthenticatedUser(Authentication auth) {
    if (auth == null || !auth.isAuthenticated()) return false;
    return auth.getAuthorities().stream().noneMatch(a -> "ROLE_ANONYMOUS".equals(a.getAuthority()));
  }

  private boolean shouldSkip(String path) {
    return SKIP_PATHS.stream().anyMatch(p -> PATH_MATCHER.match(p, path));
  }
}
