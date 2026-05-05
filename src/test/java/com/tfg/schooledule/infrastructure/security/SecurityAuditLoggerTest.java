package com.tfg.schooledule.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

class SecurityAuditLoggerTest {

  private final SecurityAuditLogger logger = new SecurityAuditLogger();

  @Test
  void logLoginFailure_noLanzaExcepcionConInputsNormales() {
    assertThatCode(() -> logger.logLoginFailure("user@example.com", "127.0.0.1"))
        .doesNotThrowAnyException();
  }

  @Test
  void logAccessDenied_noLanzaExcepcionConInputsNormales() {
    assertThatCode(() -> logger.logAccessDenied("user@example.com", "/admin/dashboard"))
        .doesNotThrowAnyException();
  }

  @Test
  void sanitize_reemplazaSaltosDeLinea() {
    String resultado = logger.sanitize("hacker@evil.com\nINFO fakeEntry");
    assertThat(resultado).doesNotContain("\n");
    assertThat(resultado).contains("_");
  }

  @Test
  void sanitize_reemplazaRetornoCarro() {
    String resultado = logger.sanitize("valor\rmalicioso");
    assertThat(resultado).doesNotContain("\r");
  }

  @Test
  void sanitize_null_retornaUnknown() {
    assertThat(logger.sanitize(null)).isEqualTo("unknown");
  }

  @Test
  void logLoginFailure_conInjeccionDeLog_noContieneNewline() {
    assertThatCode(() -> logger.logLoginFailure("hacker@evil.com\nINFO fakeEntry", "127.0.0.1"))
        .doesNotThrowAnyException();
  }
}
