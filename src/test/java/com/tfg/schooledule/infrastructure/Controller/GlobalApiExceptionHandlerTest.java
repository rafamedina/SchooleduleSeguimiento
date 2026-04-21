package com.tfg.schooledule.infrastructure.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

/** Tests unitarios directos sobre el handler de excepciones — sin contexto Spring MVC. */
class GlobalApiExceptionHandlerTest {

  private final GlobalApiExceptionHandler handler = new GlobalApiExceptionHandler();

  @Test
  void handleAccessDenied_retorna403ConBodyForbidden() {
    ResponseEntity<Map<String, String>> resp =
        handler.handleAccessDenied(new AccessDeniedException("no"));
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(resp.getBody()).containsEntry("error", "forbidden");
  }

  @Test
  void handleConflict_retorna409ConMensaje() {
    ResponseEntity<Map<String, String>> resp =
        handler.handleConflict(new IllegalStateException("periodo cerrado"));
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(resp.getBody()).containsEntry("error", "conflict");
    assertThat(resp.getBody()).containsEntry("message", "periodo cerrado");
  }

  @Test
  void handleBadRequest_retorna400ConMensaje() {
    ResponseEntity<Map<String, String>> resp =
        handler.handleBadRequest(new IllegalArgumentException("item no válido"));
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(resp.getBody()).containsEntry("error", "bad_request");
    assertThat(resp.getBody()).containsEntry("message", "item no válido");
  }

  @Test
  void handleResponseStatus_propagaStatusYReason() {
    ResponseStatusException ex =
        new ResponseStatusException(HttpStatus.NOT_FOUND, "recurso no encontrado");
    ResponseEntity<Map<String, String>> resp = handler.handleResponseStatus(ex);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(resp.getBody()).containsEntry("error", "recurso no encontrado");
  }

  @Test
  void handleResponseStatus_sinReason_usaMensaje() {
    // ResponseStatusException sin reason explícito — fallback a getMessage()
    ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    ResponseEntity<Map<String, String>> resp = handler.handleResponseStatus(ex);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(resp.getBody()).containsKey("error");
  }
}
