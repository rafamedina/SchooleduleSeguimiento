package com.tfg.schooledule.infrastructure.security;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.stereotype.Component;

@Component
public class SecurityEventListener {

  private final SecurityAuditLogger auditLogger;

  public SecurityEventListener(SecurityAuditLogger auditLogger) {
    this.auditLogger = auditLogger;
  }

  @EventListener
  public void onAuthFailure(AbstractAuthenticationFailureEvent event) {
    String email = event.getAuthentication().getName();
    auditLogger.logLoginFailure(email, "N/A");
  }
}
