# 🗄️ Modelo de Datos y Esquema (PostgreSQL)

Este documento detalla la arquitectura de persistencia de **Schooledule**, incluyendo el modelo relacional, las restricciones de integridad y los mecanismos de auditoría programada.

## 1. Arquitectura de Datos (Explicación)

### 1.1 Diseño de Identidad N:M

A diferencia de modelos rígidos, Schooledule permite que un usuario posea **múltiples roles simultáneamente** (ej. un usuario que es profesor y también alumno). Esto se logra mediante la tabla intermedia `usuarios_roles`, permitiendo una gran flexibilidad en la gestión de permisos.

### 1.2 Cortafuegos de Seguridad (Data Isolation)

Para garantizar el aislamiento de datos entre centros educativos (Multi-site), las tablas críticas como `imparticiones` y `matriculas` incluyen una columna redundante `centro_id`. Esta arquitectura permite implementar políticas de **Seguridad por Filas (RLS)** de forma eficiente, asegurando que un usuario solo vea datos de su propio centro.

---

## 2. Tipos y Catálogos (Referencia)

### 2.1 Tipos Enumerados (ENUM)

| Tipo               | Valores                                         | Propósito                              |
| :----------------- | :---------------------------------------------- | :------------------------------------- |
| `estado_matricula` | `ACTIVA`, `BAJA`, `CONVALIDADO`                 | Ciclo de vida del alumno en un módulo. |
| `tipo_actividad`   | `EXAMEN`, `PRACTICA`, `RECUPERACION`, `ACTITUD` | Clasificación de los ítems evaluables. |

### 2.2 Roles del Sistema

Los roles están estandarizados con el prefijo `ROLE_` para la compatibilidad con Spring Security:

- `ROLE_ADMIN`: Gestión global y de centros.
- `ROLE_PROFESOR`: Gestión de grupos, evaluaciones y notas.
- `ROLE_ALUMNO`: Consulta de perfil y expediente académico.

---

## 3. Diccionario de Tablas (Referencia)

### 3.1 Módulo de Identidad

- **`usuarios`**: Almacena credenciales (BCrypt) y datos básicos.
- **`roles`**: Catálogo maestro de perfiles de acceso.
- **`usuarios_roles`**: Tabla de vinculación para el acceso basado en roles (RBAC).

### 3.2 Módulo Organizativo y Académico

- **`centros`**: Sedes físicas con configuración específica en formato JSONB.
- **`cursos_academicos`**: Periodos temporales (ej. "2023-2024") con control de estado activo/inactivo.
- **`modulos`**: Unidades formativas o asignaturas.
- **`resultados_aprendizaje`** y **`criterios_evaluacion`**: Estructura curricular detallada vinculada a cada módulo.

### 3.3 Módulo de Evaluación

- **`grupos`**: Agrupaciones de alumnos por centro y curso.
- **`imparticiones`**: El nexo entre un profesor, un módulo y un grupo.
- **`matriculas`**: Inscripción oficial de un alumno en una impartición.
- **`calificaciones`**: Notas numéricas vinculadas a un ítem evaluable y una matrícula.

---

## 4. Lógica Programada: Auditoría Forense (Explicación)

Schooledule implementa un sistema de **auditoría de integridad** para las notas mediante disparadores (triggers) de base de datos.

### 4.1 Trigger `trigger_auditoria_notas`

Cada vez que se actualiza una fila en la tabla `calificaciones`, la función `registrar_cambio_nota()` se ejecuta automáticamente:

1.  Detecta si el valor de la nota ha cambiado.
2.  Extrae el usuario responsable de la sesión (inyectado desde Spring Boot o detectado por la base de datos).
3.  Inserta un registro en `auditoria_notas` con el valor anterior, el nuevo, el autor y la marca de tiempo.

### 4.2 Inyección de Contexto

El sistema utiliza la variable de sesión de PostgreSQL `app.current_user` para capturar quién realiza los cambios desde la aplicación web, garantizando la trazabilidad incluso si la conexión a la DB usa un usuario genérico.

---

## 5. Persistencia de Sesión (Referencia)

Para soportar alta disponibilidad y evitar la pérdida de sesiones tras reinicios del servidor, se utilizan las tablas:

- **`SPRING_SESSION`**: Cabecera de la sesión y metadatos de expiración.
- **`SPRING_SESSION_ATTRIBUTES`**: Almacén de los datos de usuario y contexto de seguridad serializados.

---

_Documentación técnica de base de datos - Actualizado abril 2026_
