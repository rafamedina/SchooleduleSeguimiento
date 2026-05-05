package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.infrastructure.security.SecurityAuditLogger;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice(basePackages = "com.tfg.schooledule.infrastructure.controller")
public class GlobalApiExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalApiExceptionHandler.class);

  private final SecurityAuditLogger securityAuditLogger;

  public GlobalApiExceptionHandler(SecurityAuditLogger securityAuditLogger) {
    this.securityAuditLogger = securityAuditLogger;
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Map<String, String>> handleAccessDenied(
      AccessDeniedException ex, HttpServletRequest request) {
    String principal =
        Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
            .map(a -> a.getName())
            .orElse("anonymous");
    securityAuditLogger.logAccessDenied(principal, request.getRequestURI());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
    List<String> details =
        ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.toList());
    return ResponseEntity.badRequest().body(Map.of("error", "validation", "details", details));
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException ex) {
    log.debug("[APP] conflict message={}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Map.of("error", "conflict", "message", ex.getMessage()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
    log.debug("[APP] bad_request message={}", ex.getMessage());
    return ResponseEntity.badRequest()
        .body(Map.of("error", "bad_request", "message", ex.getMessage()));
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException ex) {
    return ResponseEntity.status(ex.getStatusCode())
        .body(Map.of("error", ex.getReason() != null ? ex.getReason() : ex.getMessage()));
  }
}
