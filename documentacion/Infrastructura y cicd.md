# Infraestructura y Despliegue (DevOps)

Este documento proporciona una visión completa de la infraestructura, el ciclo de vida del software y las estrategias de despliegue de **Schooledule**. Sigue el estándar de arquitectura de contenedores y automatización profesional.

## 1. Conceptos de Diseño (Explicación)

### 1.1 Filosofía de "Registro Limpio" (Clean Registry)

Schooledule implementa una separación estricta entre el entorno de desarrollo y el de producción. Durante el desarrollo, el repositorio incluye **herramientas de trabajo** y documentación de soporte técnico que facilitan la construcción del sistema. Sin embargo, para garantizar la seguridad, ligereza y limpieza del código final, el sistema de CI/CD realiza una **purga automática** de estas herramientas antes de promocionar el código a las ramas de pre-producción y producción.

### 1.2 Aislamiento y Seguridad de Red

La infraestructura está diseñada bajo el principio de privilegio mínimo:

- **Red Privada (`db_net`):** La base de datos PostgreSQL reside en una red interna aislada. No es accesible desde el exterior, solo por el contenedor de la aplicación.
- **Usuarios no-root:** Los procesos dentro de los contenedores se ejecutan con usuarios con permisos limitados, reduciendo la superficie de ataque en caso de vulnerabilidad.

---

## 2. Referencia Técnica (Referencia)

### 2.1 Especificación del Dockerfile (Multi-stage)

Utilizamos una construcción optimizada en varias etapas para minimizar el tamaño de la imagen y separar las fases de construcción y ejecución:

| Etapa       | Imagen Base                     | Propósito                                                         |
| :---------- | :------------------------------ | :---------------------------------------------------------------- |
| **Builder** | `maven:3.9-eclipse-temurin-21`  | Compilación del proyecto y gestión de dependencias.               |
| **Test**    | (Derivada de Builder)           | Ejecución de pruebas unitarias e integrales en el CI/CD.          |
| **Runtime** | `eclipse-temurin:21-jre-alpine` | Imagen final ligera que contiene solo el JRE y el binario `.jar`. |

### 2.2 Servicios de Infraestructura (Docker Compose)

El archivo `infraestructura/docker-compose.yml` define tres servicios clave:

1.  **`app`:** La aplicación Spring Boot expuesta en el puerto configurado (`APP_PORT`).
2.  **`postgres`:** Base de datos relacional con persistencia de datos en volumen.
3.  **`pgadmin`:** Herramienta visual para la administración de la base de datos (puerto `PGADMIN_PORT`).

### 2.3 Pipeline CI/CD y Flujo de Promoción (PR)

El flujo de automatización se activa ante cualquier cambio en la rama de desarrollo (`dev`).

- **Quality Gates (Barreras de Calidad):**
  - **Linting:** Validación de sintaxis en código, Dockerfile y archivos de configuración.
  - **Tests:** Ejecución obligatoria de pruebas dentro de un contenedor idéntico al de producción.
  - **Cobertura (JaCoCo):** El despliegue se bloquea si la cobertura de código es inferior al **80%**.
- **Promoción mediante PR:** Si los tests en `dev` son exitosos, el CI/CD realiza lo siguiente:
  1.  Crea la rama `pre-prod` con el código purgado de herramientas de trabajo.
  2.  Genera automáticamente un **Pull Request** desde `pre-prod` hacia `main`.
  3.  El equipo revisa el PR y, al aprobarlo, el código llega a la rama de producción definitiva.

---

## 3. Guía de Despliegue y Validación (How-to)

### 3.1 Cómo levantar el entorno local

Para poner en marcha el sistema completo en tu máquina local:

1.  Asegúrate de tener configurado el archivo `.env` con las credenciales necesarias.
2.  Desde la carpeta raíz, ejecuta:
    ```powershell
    cd infraestructura
    docker-compose up -d
    ```
3.  La aplicación estará disponible en `http://localhost:[APP_PORT]`.

### 3.2 Validación Local con Pre-commit

Antes de subir cualquier cambio al servidor, el sistema utiliza `pre-commit` para asegurar que el código cumple con los estándares mínimos.

- **Instalación:** Ejecuta `pre-commit install` la primera vez.
- **Qué hace:** Al hacer `git commit`, el sistema valida automáticamente:
  - **Seguridad:** Escaneo de secretos y credenciales con `gitleaks`.
  - **Estilo:** Formateo automático de Java con `Spotless` y otros archivos con `Prettier`.
  - **Integridad:** Verificación de conflictos de fusión, sintaxis YAML/JSON y validación de `pom.xml`.
- **Ejecución manual:** Si quieres validar todos los archivos sin hacer commit:
  ```powershell
  pre-commit run --all-files
  ```

### 3.3 Cómo interpretar los resultados del CI/CD

Cada vez que subas código, revisa la pestaña **Actions** en GitHub:

- **Verde (Success):** El código ha pasado todas las pruebas, se ha purgado y se ha creado el PR hacia producción.
- **Rojo (Failure):** Alguna prueba ha fallado o se han detectado secretos con gitleaks. Puedes descargar el artefacto `test-execution-reports` para ver el detalle.

---

## 4. Mantenimiento y Versiones (Referencia)

### 4.1 Variables de Entorno Requeridas

El sistema depende de las siguientes variables (configuradas en el entorno o en secretos de GitHub):

- `POSTGRES_USER` / `POSTGRES_PASSWORD`: Credenciales de la base de datos.
- `APP_PORT`: Puerto de escucha de la aplicación.
- `DB_PORT`: Puerto de conexión a la base de datos.

### 4.2 Estrategia de Etiquetado (Tagging)

Por cada despliegue exitoso hacia `pre-prod`, el sistema genera automáticamente una etiqueta (Tag) de Git con el formato:
`build-[número_ejecución]-[sha_corto]`

Esta etiqueta permite realizar auditorías y "rollbacks" precisos a versiones anteriores en caso de errores en producción.

---

_Documentación técnica de Schooledule - Actualizado abril 2026_
