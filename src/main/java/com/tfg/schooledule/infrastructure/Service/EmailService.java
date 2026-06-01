package com.tfg.schooledule.infrastructure.service;

import jakarta.mail.internet.MimeMessage;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class EmailService {

  private static final Logger log = LoggerFactory.getLogger(EmailService.class);

  private final JavaMailSender mailSender;
  private final SpringTemplateEngine templateEngine;

  @Value("${app.mail.from:no-reply@schooledule.com}")
  private String fromAddress;

  @Value("${app.base-url:http://localhost:8080}")
  private String baseUrl;

  public EmailService(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
    this.mailSender = mailSender;
    this.templateEngine = templateEngine;
  }

  @Async
  public void enviarBienvenida(String destinatario, String username, String plainPassword) {
    try {
      Context ctx = new Context(Locale.forLanguageTag("es"));
      ctx.setVariable("username", username);
      ctx.setVariable("password", plainPassword);
      ctx.setVariable("loginUrl", baseUrl + "/login");

      String html = templateEngine.process("email/bienvenida", ctx);

      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setFrom(fromAddress);
      helper.setTo(destinatario);
      helper.setSubject("Bienvenido/a a Schooledule — tus credenciales de acceso");
      helper.setText(html, true);

      mailSender.send(message);
    } catch (Exception e) {
      // El fallo de email nunca interrumpe la creación del usuario
      log.warn("No se pudo enviar email de bienvenida a {}: {}", destinatario, e.getMessage());
    }
  }
}
