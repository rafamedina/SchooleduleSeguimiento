# 🛠️ Stack Tecnológico - Schooledule

Este documento enumera las tecnologías y herramientas fundamentales empleadas en el desarrollo de la plataforma **Schooledule**. Cada elección técnica se ha realizado priorizando la robustez, la mantenibilidad y la seguridad del ecosistema académico.

## 💻 Lenguajes y Frameworks Principales

**Java 21**

- **Descripción de la herramienta:** Lenguaje de programación robusto y multiplataforma con soporte nativo para características modernas de alto rendimiento.
- **Descripción del uso de la herramienta en el proyecto:** Base fundamental para la implementación de la lógica de negocio, servicios del backend y gestión de la concurrencia.

**Spring Boot 3.3.5**

- **Descripción de la herramienta:** Framework de código abierto diseñado para simplificar el arranque y desarrollo de aplicaciones Java empresariales.
- **Descripción del uso de la herramienta en el proyecto:** Orquestador central de la aplicación, gestionando la inyección de dependencias, la seguridad (RBAC) y los servicios web.

**Thymeleaf**

- **Descripción de la herramienta:** Motor de plantillas Java moderno que permite procesar HTML, XML y otros formatos en el lado del servidor.
- **Descripción del uso de la herramienta en el proyecto:** Generación dinámica de la capa de presentación y visualización de datos académicos para los diferentes perfiles de usuario.

---

## 🗄️ Persistencia y Gestión de Datos

**PostgreSQL 16**

- **Descripción de la herramienta:** Sistema de gestión de bases de datos relacionales potente y extensible con soporte avanzado para JSONB y auditoría.
- **Descripción del uso de la herramienta en el proyecto:** Almacenamiento persistente de datos académicos, perfiles de usuario y rastro inmutable de calificaciones.

**Flyway**

- **Descripción de la herramienta:** Herramienta de control de versiones para bases de datos que automatiza la aplicación de cambios en el esquema SQL.
- **Descripción del uso de la herramienta en el proyecto:** Gestión de migraciones y mantenimiento de la coherencia estructural de la base de datos en todos los entornos.

---

## 🐳 Infraestructura y Automatización

**Docker & Docker Compose**

- **Descripción de la herramienta:** Plataforma de contenedores que permite empaquetar aplicaciones y sus dependencias en entornos aislados y portables.
- **Descripción del uso de la herramienta en el proyecto:** Garantía de paridad entre entornos mediante la orquestación de servicios y la base de datos en contenedores.

**GitHub Actions**

- **Descripción de la herramienta:** Plataforma de automatización de flujos de trabajo integrada en el ecosistema de control de versiones de GitHub.
- **Descripción del uso de la herramienta en el proyecto:** Ejecución del pipeline de CI/CD, validación de calidad de código y automatización del despliegue en producción.

**Maven**

- **Descripción de la herramienta:** Herramienta de automatización de construcción que gestiona el ciclo de vida del proyecto y sus dependencias externas.
- **Descripción del uso de la herramienta en el proyecto:** Estandarización del proceso de compilación, gestión de librerías y ejecución de las baterías de pruebas.

---

## 🧪 Calidad y Pruebas

**JUnit 5 & Mockito**

- **Descripción de la herramienta:** Frameworks líderes para la realización de pruebas unitarias y simulaciones de comportamiento en aplicaciones Java.
- **Descripción del uso de la herramienta en el proyecto:** Implementación de la estrategia de pruebas para validar la lógica de negocio y asegurar la integridad del software.

**JaCoCo**

- **Descripción de la herramienta:** Librería de código abierto para medir la cobertura de código en proyectos Java durante la ejecución de pruebas.
- **Descripción del uso de la herramienta en el proyecto:** Monitorización del porcentaje de código validado para asegurar el cumplimiento del estándar de calidad del 80%.

## 🛡️ Calidad y Seguridad en el Desarrollo

**pre-commit**

- **Descripción de la herramienta:** Marco de trabajo para la gestión y ejecución de ganchos (hooks) antes de confirmar cambios en el control de versiones.
- **Descripción del uso de la herramienta en el proyecto:** Automatización de la validación de código en el entorno local para prevenir errores y asegurar el cumplimiento de estándares.

**Spotless (Google Style)**

- **Descripción de la herramienta:** Plugin de formateo de código para proyectos Java que aplica estándares consistentes de legibilidad.
- **Descripción del uso de la herramienta en el proyecto:** Mantenimiento automático del estilo de código de Google en todos los archivos fuente de Java mediante el ciclo de vida de Maven.

**Gitleaks**

- **Descripción de la herramienta:** Herramienta de seguridad diseñada para detectar y prevenir la inclusión de secretos, claves API y credenciales en el historial de Git.
- **Descripción del uso de la herramienta en el proyecto:** Auditoría continua de los commits para garantizar que no se filtre información sensible en el repositorio.

**Prettier**

- **Descripción de la herramienta:** Formateador de código con soporte para múltiples lenguajes que garantiza un estilo consistente en archivos de frontend y configuración.
- **Descripción del uso de la herramienta en el proyecto:** Estandarización del formato en archivos CSS, JavaScript, HTML, YAML y Markdown.

---

_Listado tecnológico consolidado para la Memoria de TFG - Schooledule 2026_
