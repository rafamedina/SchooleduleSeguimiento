package com.tfg.schooledule.infrastructure.security;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

  private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    if ("POST".equalsIgnoreCase(request.getMethod()) && "/login".equals(request.getServletPath())) {
      String ip = resolveClientIp(request);
      Bucket bucket =
          buckets.computeIfAbsent(
              ip,
              k ->
                  Bucket.builder()
                      .addLimit(
                          limit -> limit.capacity(10).refillGreedy(10, Duration.ofMinutes(15)))
                      .build());
      if (!bucket.tryConsume(1)) {
        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"Demasiados intentos. Espera 15 minutos.\"}");
        return;
      }
    }
    chain.doFilter(request, response);
  }

  private String resolveClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isBlank()) {
      return xForwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
