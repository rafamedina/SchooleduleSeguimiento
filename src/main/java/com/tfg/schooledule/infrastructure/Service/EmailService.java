package com.tfg.schooledule.infrastructure.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class EmailService {

  private static final Logger log = LoggerFactory.getLogger(EmailService.class);

  private final SpringTemplateEngine templateEngine;
  private final RestClient restClient;

  @Value("${app.mail.from:Schooledule <no-reply@schooledule.com>}")
  private String fromAddress;

  @Value("${app.base-url:https://schooledule.up.railway.app}")
  private String baseUrl;

  public EmailService(
      @Value("${resend.api-key:}") String apiKey, SpringTemplateEngine templateEngine) {
    this.templateEngine = templateEngine;
    this.restClient =
        RestClient.builder()
            .baseUrl("https://api.resend.com")
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .build();
  }

  @Async
  public void enviarBienvenida(String destinatario, String username, String plainPassword) {
    try {
      Context ctx = new Context(Locale.forLanguageTag("es"));
      ctx.setVariable("username", username);
      ctx.setVariable("password", plainPassword);
      ctx.setVariable("loginUrl", baseUrl + "/login");

      String html = templateEngine.process("email/bienvenida", ctx);

      Map<String, Object> body =
          Map.of(
              "from",
              fromAddress,
              "to",
              List.of(destinatario),
              "subject",
              "Bienvenido/a a Schooledule — tus credenciales de acceso",
              "html",
              html);

      restClient
          .post()
          .uri("/emails")
          .contentType(MediaType.APPLICATION_JSON)
          .body(body)
          .retrieve()
          .toBodilessEntity();

    } catch (Exception e) {
      log.warn("No se pudo enviar email de bienvenida a {}: {}", destinatario, e.getMessage());
    }
  }
}
