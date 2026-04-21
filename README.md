# 🏫 Schooledule

**Plataforma Integral de Gestión Académica (ERP Multi-Sede)**

Schooledule es una solución empresarial de gestión académica diseñada para centros educativos que requieren un control estricto de su estructura organizativa, currículo y procesos de evaluación. Desarrollada bajo estándares modernos de arquitectura de software, la plataforma garantiza la integridad de los datos y la seguridad en entornos multi-centro.

---

## 🚀 Características Principales

- **Arquitectura Multi-Sede:** Aislamiento lógico de datos que permite la gestión de múltiples centros educativos en una única instancia.
- **Gestión de Identidad (RBAC):** Sistema de control de acceso basado en roles (Admin, Profesor, Alumno) con soporte para perfiles múltiples por usuario.
- **Auditoría Forense de Calificaciones:** Registro automático e inmutable de cualquier modificación en las notas mediante disparadores (triggers) a nivel de base de datos.
- **Infraestructura como Código:** Despliegue orquestado mediante Docker para asegurar la paridad absoluta entre entornos.

---

## 🛠️ Stack Tecnológico

- **Backend:** Java 21 (LTS) & Spring Boot 3.3.5.
- **Persistencia:** PostgreSQL 16 (con soporte para tipos JSONB y Auditoría PL/pgSQL).
- **Seguridad:** Spring Security (BCrypt, RBAC, Gestión de Sesiones Persistente).
- **Frontend:** Thymeleaf + Vanilla CSS/JS (Diseño moderno y ligero).
- **Infraestructura:** Docker & Docker Compose.

---

## 📂 Documentación Técnica

Para una comprensión profunda del sistema, consulte las guías detalladas en la carpeta `documentacion/`:

1.  [**Arquitectura de Código**](./documentacion/arquitectura-codigo.md): Patrones de diseño, capas (Domain/Infrastructure) y flujo de datos (DTOs).
2.  [**Modelo de Datos**](./documentacion/modelo-datos.md): Esquema relacional, diccionario de tablas y lógica de auditoría de base de datos.
3.  [**Despliegue y DevOps**](./documentacion/despliegue-y-devops.md): Guía de puesta en marcha, flujo de validación local y políticas de calidad.

---

## ⚡ Inicio Rápido (Despliegue con Docker)

La forma más rápida de evaluar el sistema es utilizando la infraestructura de contenedores proporcionada:

1.  **Preparación:** Asegúrese de tener Docker y Docker Compose instalados.
2.  **Configuración:** Revise el archivo `.env` para ajustar puertos o credenciales si fuera necesario.
3.  **Ejecución:**
    ```powershell
    cd infraestructura
    docker-compose up -d
    ```
4.  **Acceso:** La aplicación estará disponible en `http://localhost:8080` (o el puerto definido en `APP_PORT`).

---

## 🧪 Pruebas y Calidad

El proyecto mantiene un estándar de calidad riguroso:

- **Pruebas Unitarias e Integrales:** Ejecutables mediante `./mvnw test`.
- **Cobertura de Código:** Umbral mínimo del 80% garantizado por JaCoCo.
- **Validación Local:** Integración de hooks de pre-commit para asegurar el estilo de código y la seguridad.

---

_Schooledule - Proyecto Final de Grado 2026_
