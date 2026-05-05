# Schooledule

**Plataforma Integral de Gestión Académica (ERP Multi-Sede)**

[![Java](https://img.shields.io/badge/Java-21_LTS-orange?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.5-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)
[![Coverage](https://img.shields.io/badge/coverage-≥80%25-success?logo=jacoco)](https://www.jacoco.org/)

Schooledule es un sistema ERP académico para centros de formación profesional que requieren control estricto sobre su estructura organizativa, currículo y evaluación. Ofrece aislamiento de datos multi-sede, auditoría forense de calificaciones y una arquitectura segura lista para entornos de producción.

---

## Características

- **Multi-sede con aislamiento real:** Cada centro educativo opera de forma completamente aislada (`centro_id`) dentro de una única instancia, compartiendo solo los recursos que explícitamente lo permiten.
- **RBAC con perfiles múltiples:** Un mismo usuario puede tener distintos roles (Admin, Profesor, Alumno) en distintos centros, con redirección automática al dashboard correspondiente tras el inicio de sesión.
- **Motor de evaluación híbrido:** Cálculo de notas flexible mediante JSONB, que soporta criterios de evaluación personalizados (RAs, Criterios, Ítems Evaluables) con recuperación integrada.
- **Auditoría forense inmutable:** Cualquier modificación de calificaciones queda registrada de forma permanente a nivel de base de datos mediante triggers PL/pgSQL, cumpliendo requisitos de trazabilidad legal.
- **Seguridad OWASP por diseño:** CSRF, HttpOnly cookies, rate limiting en login, validación Bean Validation en todos los DTOs, y `@PreAuthorize` en todos los controladores.
- **Infraestructura como código:** Stack completo orquestado con Docker Compose (app, PostgreSQL, pgAdmin, SonarQube), con healthchecks y límites de recursos definidos.

---

## Stack Tecnológico

| Capa          | Tecnología                                    |
| ------------- | --------------------------------------------- |
| Lenguaje      | Java 21 (LTS) + Lombok                        |
| Framework     | Spring Boot 3.3.5                             |
| Seguridad     | Spring Security 6 (RBAC, CSRF, sesiones JDBC) |
| Persistencia  | Spring Data JPA + Hibernate 6 + PostgreSQL 16 |
| Migraciones   | Flyway                                        |
| Mapeo de DTOs | MapStruct 1.5.5 (compile-time)                |
| Frontend      | Thymeleaf + Bootstrap 5 + Vanilla JS          |
| Rate Limiting | Bucket4j 8.10                                 |
| API Docs      | SpringDoc OpenAPI (Swagger UI)                |
| Contenedores  | Docker + Docker Compose                       |
| Calidad       | SonarQube 10.7, Spotless, Error Prone, JaCoCo |
| Testing       | JUnit 5, Mockito, Testcontainers, H2          |
| Build         | Maven 3                                       |

---

## Arquitectura

El proyecto sigue una arquitectura en dos capas claras bajo el paquete `com.tfg.schooledule`:

```
domain/           → Entidades JPA, DTOs (records), Enums, validaciones custom
infrastructure/   → Controllers, Services, Repositories, Mappers, Security, Config
```

La capa `domain` no tiene dependencias de Spring y contiene el modelo de datos central (14 entidades: `Centro`, `Usuario`, `Rol`, `Modulo`, `CursoAcademico`, `Grupo`, `Matricula`, `Imparticion`, `PeriodoEvaluacion`, `CriterioEvaluacion`, `ResultadoAprendizaje`, `ItemEvaluable`, `Calificacion`, `AuditoriaNota`).

La capa `infrastructure` implementa toda la lógica de negocio, expone endpoints HTTP mediante Thymeleaf y protege cada recurso con `@PreAuthorize`.

---

## Requisitos Previos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (para el inicio rápido)
- Java 21 + Maven 3 (para desarrollo local sin Docker)
- PostgreSQL 16 accesible en `localhost:5432` (si no se usa Docker)

---

## Inicio Rápido con Docker

La forma más rápida de ejecutar el sistema completo:

```powershell
cd infraestructura
docker-compose up -d
```

Esto levanta cuatro servicios:

| Servicio   | URL                     | Descripción                    |
| ---------- | ----------------------- | ------------------------------ |
| Aplicación | `http://localhost:8080` | Schooledule (Spring Boot)      |
| pgAdmin    | `http://localhost:5050` | Gestor visual de PostgreSQL    |
| SonarQube  | `http://localhost:9000` | Análisis de calidad de código  |
| PostgreSQL | `localhost:5432`        | Base de datos (acceso interno) |

> [!NOTE]
> Revisa el archivo `infraestructura/.env` para ajustar puertos y credenciales antes de arrancar por primera vez.

> [!TIP]
> El Dockerfile es multi-etapa: incluye una fase de tests que debe pasar antes de generar la imagen de producción. Si los tests fallan, el build se detiene.

---

## Desarrollo Local (sin Docker)

**1. Iniciar solo la base de datos:**

```powershell
cd infraestructura
docker-compose up -d postgres
```

**2. Ejecutar la aplicación:**

```powershell
./mvnw spring-boot:run
```

**3. Aplicar formateo de código:**

```powershell
./mvnw spotless:apply
```

---

## Variables de Entorno

| Variable                 | Descripción              | Valor por defecto (dev) |
| ------------------------ | ------------------------ | ----------------------- |
| `DB_USERNAME`            | Usuario de PostgreSQL    | `postgres`              |
| `DB_PASSWORD`            | Contraseña de PostgreSQL | `root123`               |
| `DB_PORT`                | Puerto expuesto de la BD | `5432`                  |
| `APP_PORT`               | Puerto de la aplicación  | `8080`                  |
| `SPRING_PROFILES_ACTIVE` | Perfil de Spring activo  | `dev`                   |

> [!WARNING]
> En producción, `DB_PASSWORD` no tiene fallback seguro. Define siempre `SPRING_PROFILES_ACTIVE=prod` y gestiona las credenciales mediante secretos externos.

---

## Tests y Calidad

| Comando                      | Descripción                                            |
| ---------------------------- | ------------------------------------------------------ |
| `./mvnw test`                | Ejecuta la suite completa (JUnit 5 + Mockito + H2)     |
| `./mvnw verify`              | Build completo con tests e informe JaCoCo              |
| `./mvnw sonar:sonar`         | Envía métricas a SonarQube (requiere instancia activa) |
| `pre-commit run --all-files` | Valida estilo y reglas de seguridad localmente         |

El umbral de cobertura mínimo es **80%**, verificado automáticamente por JaCoCo en cada build. Los tests de seguridad son obligatorios en cada track: se valida 401 sin autenticación, 403 con rol incorrecto, y 403/404 al acceder a datos de otro centro.

---

## Documentación API

Con la aplicación en ejecución, la documentación interactiva de la API está disponible en:

```
http://localhost:8080/swagger-ui.html
```

El esquema OpenAPI en formato JSON se expone en `/v3/api-docs`.

---

## Estructura del Proyecto

```
.
├── src/
│   ├── main/
│   │   ├── java/com/tfg/schooledule/
│   │   │   ├── domain/          # Entidades, DTOs, Enums, validaciones
│   │   │   └── infrastructure/  # Controllers, Services, Repos, Security
│   │   └── resources/
│   │       ├── db/migration/    # Scripts Flyway (V1..V5)
│   │       ├── templates/       # Plantillas Thymeleaf
│   │       └── static/          # CSS y JavaScript
│   └── test/                    # Suite de tests (JUnit 5)
├── infraestructura/
│   ├── Dockerfile               # Build multi-etapa (Maven → JRE 21 Alpine)
│   └── docker-compose.yml       # Orquestación completa (app + db + tools)
├── conductor/                   # Documentación de proyecto y decisiones técnicas
│   ├── product.md
│   ├── tech-stack.md
│   ├── workflow.md
│   └── tracks/                  # Planes de implementación por funcionalidad
└── pom.xml
```

---

## Flujo de Ramas

```
dev  ──▶  pre-prod (purge automático)  ──▶  main (PR manual)
```

- **`dev`**: desarrollo activo con herramientas de IA y documentación técnica.
- **`pre-prod`**: rama purgada automáticamente al pasar los tests en `dev`. Solo código fuente e infraestructura.
- **`main`**: producción. Accesible únicamente vía PR manual desde `pre-prod`.

---

_Schooledule — Trabajo Final de Grado · CampusFP 2026_
