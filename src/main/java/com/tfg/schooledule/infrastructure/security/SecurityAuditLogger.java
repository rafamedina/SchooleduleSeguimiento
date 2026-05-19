package com.tfg.schooledule.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SecurityAuditLogger {

  private static final Logger log = LoggerFactory.getLogger(SecurityAuditLogger.class);

  public void logLoginFailure(String email, String ipAddress) {
    if (log.isWarnEnabled()) {
      log.warn(
          "[SECURITY] event=login_failure user={} ip={}", sanitize(email), sanitize(ipAddress));
    }
  }

  public void logAccessDenied(String principal, String resource) {
    if (log.isWarnEnabled()) {
      log.warn(
          "[SECURITY] event=access_denied user={} resource={}",
          sanitize(principal),
          sanitize(resource));
    }
  }

  String sanitize(String value) {
    if (value == null) return "unknown";
    return value.replaceAll("[\r\n\t]", "_").substring(0, Math.min(value.length(), 200));
  }
}
