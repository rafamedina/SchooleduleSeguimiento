# Autenticación y Autorización — Schooledule

## Por qué este diseño

Schooledule es una aplicación web MVC tradicional: el servidor renderiza vistas Thymeleaf y el navegador navega entre páginas HTML. Este modelo hace que la autenticación basada en **sesión + cookie** sea la elección natural, en lugar de JWT.

Las razones concretas de cada decisión de diseño son:

| Decisión                                           | Alternativa descartada         | Motivo                                                                                                                                 |
| -------------------------------------------------- | ------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------- |
| Spring Security form login                         | JWT / OAuth2                   | No hay cliente SPA ni API pública. El formulario HTML es suficiente y más seguro en este contexto.                                     |
| `spring-session-jdbc` (PostgreSQL)                 | Sesión en memoria del servidor | Permite escalar horizontalmente sin perder sesiones y sobrevive reinicios de la JVM.                                                   |
| 1 sesión máxima por usuario                        | Sesiones ilimitadas            | Previene uso concurrente no autorizado de la misma cuenta.                                                                             |
| Doble capa de autorización (URL + `@PreAuthorize`) | Solo matchers de URL           | Los matchers de URL son la primera barrera; `@PreAuthorize` garantiza que ningún refactor accidental exponga un método sin protección. |

---

## Cómo funciona el sistema

### El ciclo completo: login → sesión → logout

```
┌─────────────────────────────────────────────────────────────────────┐
│  1. Navegador            GET /login                                  │
│     ──────────────────────────────────────────────►                  │
│                          Vista: formulario (email + contraseña)      │
│     ◄──────────────────────────────────────────────                  │
│                                                                      │
│  2. Navegador            POST /login                                 │
│     ──────────────────────────────────────────────►                  │
│                          Spring Security valida credenciales         │
│                          ┌─────────────────────────────────────┐    │
│                          │ Fallo → redirect /login?error        │    │
│                          │ Éxito → crea cookie SESSION          │    │
│                          │         → CustomLoginSuccessHandler  │    │
│                          └─────────────────────────────────────┘    │
│                                                                      │
│  3. CustomLoginSuccessHandler evalúa roles del usuario               │
│                          ┌─────────────────────────────────────┐    │
│                          │ 1 rol  → redirect /rol/dashboard     │    │
│                          │ N roles → redirect /seleccionar-rol  │    │
│                          └─────────────────────────────────────┘    │
│                                                                      │
│  4. Navegador            GET /seleccionar-rol   (solo si N roles)    │
│     ──────────────────────────────────────────────►                  │
│                          Vista: lista de roles disponibles           │
│                          → usuario selecciona → redirect /dashboard  │
│                                                                      │
│  5. Navegador            POST /logout                                │
│     ──────────────────────────────────────────────►                  │
│                          Invalida sesión + elimina cookie SESSION    │
│                          → redirect /login?logout                    │
└─────────────────────────────────────────────────────────────────────┘
```

**Nota importante:** `POST /login` y `POST /logout` **no están** en ningún `@Controller` del código fuente. Los procesa Spring Security internamente antes de que lleguen a los controladores de la aplicación.

### Gestión de múltiples roles

Un usuario puede tener más de un rol asignado (por ejemplo, ser ADMIN en un centro y PROFESOR en otro). En ese caso, `CustomLoginSuccessHandler` detecta que hay más de un rol y redirige a `/seleccionar-rol`.

La vista de selección de rol (`LoginController.vistaSeleccionarRol`) carga el `Set<String>` de roles de la `Authentication` activa y los presenta al usuario. La selección del rol determina el dashboard de destino para esa sesión.

### Aislamiento multi-centro (`centro_id`)

Un usuario puede pertenecer a **múltiples centros educativos**. Esto es el núcleo del modelo de datos multi-tenant de Schooledule.

El aislamiento funciona así:

1. En el login, Spring Security carga el usuario completo (con sus centros) desde `CustomUserDetailsService`.
2. En cada petición, los servicios reciben el `Usuario` autenticado (resuelto por el controlador desde `Principal`).
3. Cada consulta a la base de datos filtra siempre por `centro_id` del usuario autenticado.

Como resultado, un administrador del Centro A **nunca puede ver ni modificar** datos del Centro B, aunque ambos estén en la misma instancia de la aplicación.

---

## Referencia técnica

### Cookie SESSION

| Propiedad            | Valor                                                                  |
| -------------------- | ---------------------------------------------------------------------- |
| Nombre               | `SESSION`                                                              |
| HttpOnly             | `true` — inaccesible desde JavaScript                                  |
| SameSite             | `Strict` — nunca se envía en peticiones cross-site                     |
| Duración             | 30 minutos de inactividad                                              |
| Almacenamiento       | PostgreSQL — tabla `SPRING_SESSION`                                    |
| Sesiones simultáneas | Máximo 1 por usuario. Una nueva sesión invalida la anterior.           |
| Renovación de ID     | Se regenera en cada login exitoso (protección contra session fixation) |

### Roles y zonas protegidas

| Rol             | URL base     | Dashboard           |
| --------------- | ------------ | ------------------- |
| `ROLE_ADMIN`    | `/admin/**`  | `/admin/dashboard`  |
| `ROLE_PROFESOR` | `/profe/**`  | `/profe/dashboard`  |
| `ROLE_ALUMNO`   | `/alumno/**` | `/alumno/dashboard` |

Las URLs públicas (sin autenticación) son: `/`, `/login`, `/register`, `/css/**`, `/js/**`, `/images/**`, `/error`, `/swagger-ui/**`, `/v3/api-docs/**`.

### Endpoints de autenticación

| Método | Ruta               | Quién lo procesa  | Comportamiento                                 |
| ------ | ------------------ | ----------------- | ---------------------------------------------- |
| `GET`  | `/login`           | `LoginController` | Renderiza el formulario de login               |
| `POST` | `/login`           | Spring Security   | Autentica credenciales, crea sesión            |
| `GET`  | `/seleccionar-rol` | `LoginController` | Vista de selección de rol (usuarios multi-rol) |
| `POST` | `/logout`          | Spring Security   | Invalida sesión, elimina cookie                |

### Capas de seguridad OWASP

| Medida                  | Implementación                                           | Efecto                                                                                                                            |
| ----------------------- | -------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------- |
| Rate limiting en login  | `LoginRateLimitFilter` (bucket4j)                        | Bloquea ataques de fuerza bruta por IP                                                                                            |
| Session fixation        | `changeSessionId()` en `SecurityConfig`                  | El ID de sesión se regenera tras cada login exitoso                                                                               |
| Content Security Policy | Header `Content-Security-Policy` en todas las respuestas | Restringe las fuentes permitidas de scripts, estilos e imágenes                                                                   |
| Referrer Policy         | `strict-origin-when-cross-origin`                        | Limita la información enviada en la cabecera `Referer`                                                                            |
| Permissions Policy      | `camera=(), microphone=(), geolocation=()`               | Deshabilita APIs de dispositivo no necesarias                                                                                     |
| CSP de Swagger UI       | `SecurityFilterChain` separado con `@Order(1)`           | Permite `unsafe-inline` y `unsafe-eval` solo en rutas `/swagger-ui/**` y `/v3/api-docs/**`, sin afectar al resto de la aplicación |

---

## Cómo probar los endpoints con Swagger UI

Swagger UI está disponible en `/swagger-ui.html` sin autenticación. Sin embargo, todos los endpoints de la aplicación requieren una sesión activa para devolver datos reales.

**Pasos para autenticarte antes de usar Swagger UI:**

1. Abre el navegador y accede a `http://localhost:8080/login`.
2. Introduce tus credenciales (email + contraseña) y envía el formulario.
3. Completa la selección de rol si te la pide.
4. Una vez en el dashboard, abre `http://localhost:8080/swagger-ui.html` en la misma pestaña o en una nueva del mismo navegador.
5. La cookie `SESSION` ya está almacenada y se enviará automáticamente en todas las peticiones de Swagger UI al mismo origen.

> **Qué puedes probar:** Los endpoints REST que devuelven JSON (`GET /alumno/api/matricula/{id}/notas`, `GET /profe/api/matricula/{id}/notas`, `POST /profe/api/matricula/{id}/notas`) son totalmente funcionales desde Swagger UI una vez autenticado.

> **Qué no puedes probar:** Los endpoints MVC que devuelven vistas HTML o redirects (`/admin/**`, `/profe/dashboard`, etc.) están documentados en Swagger UI por razones de comprensión del código, pero su respuesta real en ese contexto es HTML, no JSON.
