package com.tfg.schooledule.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.tfg.schooledule.domain.dto.GradeUpsertRequest;
import com.tfg.schooledule.infrastructure.service.TeacherDashboardService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Verifica que el trigger PL/pgSQL registra cambios en auditoria_notas con el usuario correcto.
 * Requiere Postgres real — no puede ejecutarse en H2.
 */
@SpringBootTest
@Testcontainers
@TestPropertySource(
    properties = {
      "spring.flyway.enabled=true",
      "spring.jpa.hibernate.ddl-auto=validate",
      "spring.session.jdbc.initialize-schema=never",
      "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect"
    })
class AuditoriaNotaIntegrationTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private TeacherDashboardService teacherService;

  @Autowired private AuditoriaNotaRepository auditoriaRepo;

  @Test
  void upsertGrades_registraAuditoria_conUsuarioResponsable() {
    // V2 seed: calificacion id=1 → matricula 1, item 1, valor=7.50 (profe1 como propietario)
    var req =
        new GradeUpsertRequest(
            1,
            List.of(
                new GradeUpsertRequest.Entry(1, new BigDecimal("8.50"), "Actualizado en test")));

    teacherService.upsertGrades(2, "juan@tfg.com", req);

    var registros = auditoriaRepo.findByCalificacionId(1);
    assertThat(registros).isNotEmpty();

    var ultimo = registros.get(registros.size() - 1);
    assertThat(ultimo.getUsuarioResponsable()).isEqualTo("juan@tfg.com");
    assertThat(ultimo.getValorAnterior()).isEqualByComparingTo("7.50");
    assertThat(ultimo.getValorNuevo()).isEqualByComparingTo("8.50");
    assertThat(ultimo.getMotivo()).isNotBlank();
  }
}
