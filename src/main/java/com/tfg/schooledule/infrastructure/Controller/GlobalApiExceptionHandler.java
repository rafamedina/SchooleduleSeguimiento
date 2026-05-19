package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.infrastructure.security.SecurityAuditLogger;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

  private static final String KEY_ERROR = "error";
  private static final String KEY_MESSAGE = "message";

  private final SecurityAuditLogger securityAuditLogger;

  public GlobalApiExceptionHandler(SecurityAuditLogger securityAuditLogger) {
    this.securityAuditLogger = securityAuditLogger;
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleEntityNotFound(EntityNotFoundException ex) {
    log.debug("[APP] not_found message={}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(Map.of(KEY_ERROR, "not_found", KEY_MESSAGE, ex.getMessage()));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Map<String, String>> handleAccessDenied(
      AccessDeniedException ex, HttpServletRequest request) {
    String principal =
        Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
            .map(a -> a.getName())
            .orElse("anonymous");
    securityAuditLogger.logAccessDenied(principal, request.getRequestURI());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(KEY_ERROR, "forbidden"));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
    List<String> details =
        ex.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
    return ResponseEntity.badRequest().body(Map.of(KEY_ERROR, "validation", "details", details));
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException ex) {
    log.debug("[APP] conflict message={}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Map.of(KEY_ERROR, "conflict", KEY_MESSAGE, ex.getMessage()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
    log.debug("[APP] bad_request message={}", ex.getMessage());
    return ResponseEntity.badRequest()
        .body(Map.of(KEY_ERROR, "bad_request", KEY_MESSAGE, ex.getMessage()));
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException ex) {
    return ResponseEntity.status(ex.getStatusCode())
        .body(Map.of(KEY_ERROR, ex.getReason() != null ? ex.getReason() : ex.getMessage()));
  }
}
