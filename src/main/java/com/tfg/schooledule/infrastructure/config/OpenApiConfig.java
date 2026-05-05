package com.tfg.schooledule.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI schooleduleOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Schooledule — Sistema de Gestión Académica")
                .version("1.0.0")
                .description(
                    "ERP académico multi-centro construido con Spring Boot 3.3 + Thymeleaf. "
                        + "Documenta tanto los endpoints REST (JSON) como todas las rutas MVC "
                        + "(vistas HTML + formularios). "
                        + "Autenticación: sesión basada en cookie SESSION (POST /login). "
                        + "Roles disponibles: ROLE_ADMIN, ROLE_PROFESOR, ROLE_ALUMNO. "
                        + "Aislamiento multi-centro garantizado por centro_id en cada entidad.")
                .contact(new Contact().email("rafamedinaa01@gmail.com")))
        .addSecurityItem(new SecurityRequirement().addList("cookieAuth"))
        .components(
            new Components()
                .addSecuritySchemes(
                    "cookieAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .name("SESSION")
                        .description(
                            "Cookie de sesión gestionada por Spring Session + JDBC. "
                                + "Se obtiene con POST /login (email + password). "
                                + "Duración: 30 min de inactividad. "
                                + "Máximo 1 sesión simultánea por usuario.")))
        .tags(
            List.of(
                new Tag()
                    .name("Autenticación")
                    .description("Login con formulario, logout y selección de rol"),
                new Tag()
                    .name("Admin - Dashboard")
                    .description(
                        "Panel de control del administrador con estadísticas globales del sistema"),
                new Tag()
                    .name("Admin - Centros")
                    .description(
                        "CRUD de centros educativos. Unidad raíz del aislamiento multi-centro"),
                new Tag()
                    .name("Admin - Módulos")
                    .description(
                        "CRUD de módulos formativos (asignaturas). Pueden activarse/desactivarse"),
                new Tag()
                    .name("Admin - Grupos")
                    .description(
                        "CRUD de grupos de alumnos. Asociados a un centro y curso académico"),
                new Tag()
                    .name("Admin - Alumnos")
                    .description(
                        "Listado de alumnos (rol ALUMNO) y gestión de sus matrículas en imparticiones"),
                new Tag()
                    .name("Admin - Cursos Académicos")
                    .description(
                        "Ciclo de vida de cursos: BORRADOR → ACTIVO → CERRADO. Solo un curso activo a la vez"),
                new Tag()
                    .name("Admin - Imparticiones")
                    .description(
                        "Asignación de un módulo a un grupo con un profesor en un centro. "
                            + "Unidad central de la lógica de notas"),
                new Tag()
                    .name("Admin - Usuarios")
                    .description(
                        "CRUD de usuarios del sistema. Un usuario puede tener múltiples roles y pertenecer a varios centros"),
                new Tag()
                    .name("Admin - Auditoría")
                    .description(
                        "Registro histórico de todos los cambios de calificaciones con filtros por alumno, módulo y fecha"),
                new Tag()
                    .name("Profesor")
                    .description(
                        "Área del profesor: navegación por centros/asignaturas/alumnos "
                            + "y API REST para gestión de calificaciones e ítems evaluables"),
                new Tag()
                    .name("Alumno")
                    .description(
                        "Área del alumno: dashboard, perfil y consulta de notas "
                            + "vía vista MVC y API REST")));
  }
}
