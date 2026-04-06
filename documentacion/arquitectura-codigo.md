# Memoria de Arquitectura de Código (src/main)

Este documento detalla la estructura interna, los patrones de diseño y la lógica de negocio implementada en el núcleo de **Schooledule**. La arquitectura sigue un modelo orientado al dominio con una separación clara de responsabilidades.

## 1. Visión General de la Arquitectura (Explicación)

Schooledule utiliza una arquitectura de capas que separa el **Dominio** (la lógica pura del negocio) de la **Infraestructura** (la implementación técnica y los detalles de persistencia).

### 1.1 Principios de Diseño

- **Aislamiento del Dominio:** Las entidades de negocio son independientes de los controladores o servicios externos.
- **Transferencia de Datos (DTO):** Se emplean objetos DTO para enviar información a la vista (Thymeleaf), evitando exponer directamente las entidades JPA y mejorando el rendimiento.
- **Control de Acceso (RBAC):** La lógica de negocio está protegida mediante seguridad basada en roles (Admin, Profesor, Alumno), integrada directamente en la capa de infraestructura.

---

## 2. Modelo de Dominio (Referencia)

El dominio es el corazón de la aplicación y se encuentra en `com.tfg.schooledule.domain`.

### 2.1 Entidades Principales (`entity/`)

- **Usuario:** Gestiona los datos personales, credenciales y roles. Soporta múltiples roles por usuario (Many-to-Many).
- **Centro:** Representa la institución académica. Es el eje central para el aislamiento de datos.
- **Matrícula:** Vincula a un Alumno con una Impartición específica en un Centro y Curso Académico.
- **Calificación:** Almacena los resultados académicos vinculados a criterios de evaluación y periodos específicos.
- **Estructura Académica:** Incluye entidades como `Modulo`, `Grupo`, `Imparticion`, `CriterioEvaluacion` y `ResultadoAprendizaje`, que modelan la jerarquía educativa.

### 2.2 Tipos de Datos (`enums/`)

Se utilizan enums para estandarizar estados críticos, como `EstadoMatricula` (ACTIVA, BAJA, etc.) y `TipoActividad`.

---

## 3. Capa de Infraestructura (Referencia)

Ubicada en `com.tfg.schooledule.infrastructure`, esta capa conecta el dominio con el mundo exterior.

### 3.1 Servicios (`Service/`)

El componente principal es el **`UsuarioService`**, que orquestra la lógica compleja:

- **Gestión de Perfiles:** Transforma datos de múltiples entidades (`Usuario`, `Matricula`, `Grupo`) en un objeto `AlumnoProfileDTO` consolidado.
- **Gestión de Notas:** Procesa las calificaciones de un alumno para generar cuadros de mando (`GradeDashboardDTO`).
- **Seguridad:** Implementa la codificación de contraseñas y la búsqueda de usuarios para la autenticación.

### 3.2 Controladores (`Controller/`)

La comunicación con el usuario se divide por roles para facilitar el mantenimiento:

- **`AlumnoController`:** Gestiona el panel del alumno, la visualización de notas y el perfil personal.
- **`ProfeController` / `AdminController`:** Puntos de entrada específicos para la gestión docente y administrativa.
- **`LoginController`:** Gestiona el flujo de autenticación y selección de roles.

### 3.3 Repositorios (`repository/`)

Interfaces de Spring Data JPA que abstraen las consultas a la base de datos PostgreSQL, utilizando convenciones de nombres para búsquedas complejas (ej. `findFirstByAlumnoIdOrderBy...`).

---

## 4. Flujos de Trabajo (Explicación)

### 4.1 Ciclo de Consulta de Perfil

1.  El usuario accede a `/alumno/perfil`.
2.  El `AlumnoController` obtiene la identidad del usuario a través del objeto `Principal`.
3.  Se invoca al `UsuarioService`, el cual:
    - Recupera el `Usuario`.
    - Busca su `Matricula` activa más reciente.
    - Navega por las relaciones para obtener el nombre del centro y del grupo.
4.  Se construye un `AlumnoProfileDTO` y se envía a la vista `alumno/perfil.html`.

### 4.2 Selección de Rol

Dado que un usuario puede tener varios roles (ej. un usuario que es administrador y también profesor), el sistema incluye una etapa intermedia tras el login donde el usuario debe seleccionar el perfil con el que desea interactuar en la sesión actual.

---

_Memoria técnica de Schooledule - Actualizado abril 2026_
