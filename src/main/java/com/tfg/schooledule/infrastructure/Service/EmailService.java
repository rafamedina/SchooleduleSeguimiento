package com.tfg.schooledule.infrastructure.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class EmailService {

  private static final Logger log = LoggerFactory.getLogger(EmailService.class);

  private final SpringTemplateEngine templateEngine;
  private final Resend resend;

  @Value("${app.mail.from:Schooledule <no-reply@schooledule.com>}")
  private String fromAddress;

  @Value("${app.base-url:https://schooledule.up.railway.app}")
  private String baseUrl;

  public EmailService(
      @Value("${resend.api-key:}") String apiKey, SpringTemplateEngine templateEngine) {
    this.templateEngine = templateEngine;
    this.resend = new Resend(apiKey);
  }

  @Async
  public void enviarBienvenida(String destinatario, String username, String plainPassword) {
    try {
      Context ctx = new Context(Locale.forLanguageTag("es"));
      ctx.setVariable("username", username);
      ctx.setVariable("password", plainPassword);
      ctx.setVariable("loginUrl", baseUrl + "/login");

      String html = templateEngine.process("email/bienvenida", ctx);

      CreateEmailOptions params =
          CreateEmailOptions.builder()
              .from(fromAddress)
              .to(destinatario)
              .subject("Bienvenido/a a Schooledule — tus credenciales de acceso")
              .html(html)
              .build();

      resend.emails().send(params);
    } catch (ResendException e) {
      log.warn("No se pudo enviar email de bienvenida a {}: {}", destinatario, e.getMessage());
    }
  }
}
