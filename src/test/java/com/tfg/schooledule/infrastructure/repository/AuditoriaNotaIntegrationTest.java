package com.tfg.schooledule.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.tfg.schooledule.domain.dto.GradeUpsertRequest;
import com.tfg.schooledule.infrastructure.service.TeacherDashboardService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Verifica que el trigger PL/pgSQL registra cambios en auditoria_notas con el usuario correcto.
 * Requiere Postgres real — no puede ejecutarse en H2.
 *
 * <p>Se ha modificado para ser compatible con entornos de CI donde no hay acceso al socket de
 * Docker (como dentro de un contenedor en GitHub Actions). Si se detecta una URL de base de datos
 * externa, se omite el uso de Testcontainers.
 */
@SpringBootTest
@TestPropertySource(
    properties = {
      "spring.flyway.enabled=true",
      "spring.jpa.hibernate.ddl-auto=validate",
      "spring.session.jdbc.initialize-schema=never",
      "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect"
    })
class AuditoriaNotaIntegrationTest {

  /** Contenedor de Postgres para ejecución local. */
  static PostgreSQLContainer<?> postgres;

  static {
    // Si detectamos TEST_DB_URL, la usamos (caso CI).
    // Si no, y no hay SPRING_DATASOURCE_URL, arrancamos Testcontainers (caso local).
    String ciDbUrl = System.getenv("TEST_DB_URL");
    if (ciDbUrl == null && System.getenv("SPRING_DATASOURCE_URL") == null) {
      postgres = new PostgreSQLContainer<>("postgres:16-alpine");
      postgres.start();
    }
  }

  /** Registra las propiedades de conexión de forma dinámica. */
  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    String ciDbUrl = System.getenv("TEST_DB_URL");
    if (ciDbUrl != null) {
      registry.add("spring.datasource.url", () -> ciDbUrl);
      registry.add("spring.datasource.username", () -> System.getenv("TEST_DB_USERNAME"));
      registry.add("spring.datasource.password", () -> System.getenv("TEST_DB_PASSWORD"));
    } else if (postgres != null) {
      registry.add("spring.datasource.url", postgres::getJdbcUrl);
      registry.add("spring.datasource.username", postgres::getUsername);
      registry.add("spring.datasource.password", postgres::getPassword);
    }
  }

  @Autowired private TeacherDashboardService teacherService;

  @Autowired private AuditoriaNotaRepository auditoriaRepo;

  @Autowired private CalificacionRepository calificacionRepo;

  @Test
  void upsertGrades_registraAuditoria_conUsuarioResponsable() {
    // Obtenemos el ID real de la calificación para el criterio 1 (se sembró en V3)
    var calificacion =
        calificacionRepo
            .findByMatriculaIdAndCriterioEvaluacionId(1, 1)
            .orElseThrow(() -> new IllegalStateException("No existe la calificación semilla"));
    Integer calId = calificacion.getId();

    // Actualizamos el valor de 7.50 a 8.50
    var req =
        new GradeUpsertRequest(
            1,
            List.of(
                new GradeUpsertRequest.Entry(1, new BigDecimal("8.50"), "Actualizado en test")));

    teacherService.upsertGrades(2, "juan@tfg.com", req);

    // Verificamos que el trigger haya insertado en la tabla de auditoría
    var registros = auditoriaRepo.findByCalificacionId(calId);
    assertThat(registros).as("Se debería haber registrado un cambio en la auditoría").isNotEmpty();

    var ultimo = registros.get(registros.size() - 1);
    assertThat(ultimo.getUsuarioResponsable()).isEqualTo("juan@tfg.com");
    assertThat(ultimo.getValorAnterior()).isEqualByComparingTo("7.50");
    assertThat(ultimo.getValorNuevo()).isEqualByComparingTo("8.50");
    assertThat(ultimo.getMotivo()).isNotBlank();
  }
}
