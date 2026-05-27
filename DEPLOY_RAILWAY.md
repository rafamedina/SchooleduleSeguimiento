# Guía de Despliegue en Railway — Schooledule

Tiempo estimado: **20-30 minutos**
Coste: **$0** (crédito trial de $5, más que suficiente para 15 días)
Requisitos: cuenta de GitHub con el proyecto pusheado

---

## Índice

1. [Preparar el repositorio](#1-preparar-el-repositorio)
2. [Crear cuenta en Railway](#2-crear-cuenta-en-railway)
3. [Crear el proyecto en Railway](#3-crear-el-proyecto-en-railway)
4. [Añadir la base de datos PostgreSQL](#4-añadir-la-base-de-datos-postgresql)
5. [Desplegar la aplicación Spring Boot](#5-desplegar-la-aplicación-spring-boot)
6. [Configurar las variables de entorno](#6-configurar-las-variables-de-entorno)
7. [Configurar el dominio público](#7-configurar-el-dominio-público)
8. [Verificar que todo funciona](#8-verificar-que-todo-funciona)
9. [Solución de problemas comunes](#9-solución-de-problemas-comunes)

---

## 1. Preparar el repositorio

Antes de tocar Railway, el proyecto tiene que estar en GitHub y necesitamos añadir un fichero de configuración.

### 1.1 Verificar que el proyecto está en GitHub

Abre tu repositorio en GitHub y comprueba que la rama `main` está actualizada con los últimos cambios.

> Si no está en GitHub aún, sube el proyecto con:
> ```bash
> git remote add origin https://github.com/TU_USUARIO/TU_REPO.git
> git push -u origin main
> ```

### 1.2 Verificar el .gitignore

Asegúrate de que `.gitignore` incluye estos ficheros (no deben subirse nunca):

```
infraestructura/.env
infraestructura/.env.prod
```

### 1.3 Crear el fichero `railway.toml` en la raíz del proyecto

Este fichero le dice a Railway dónde está el Dockerfile y cómo arrancar la app.

Crea el fichero `railway.toml` en la raíz del proyecto (al lado de `pom.xml`) con este contenido exacto:

```toml
[build]
dockerfilePath = "infraestructura/Dockerfile"

[deploy]
healthcheckPath = "/login"
healthcheckTimeout = 300
restartPolicyType = "on_failure"
restartPolicyMaxRetries = 3
```

**Explicación de cada línea:**
- `dockerfilePath` — le dice a Railway que el Dockerfile no está en la raíz sino en `infraestructura/`
- `healthcheckPath` — Railway comprobará esta URL para saber si la app arrancó correctamente
- `healthcheckTimeout = 300` — da 5 minutos para que Spring Boot levante (la JVM tarda un poco)
- `restartPolicyType` — si la app cae, Railway la reinicia automáticamente

### 1.4 Hacer commit y push del `railway.toml`

```bash
git add railway.toml
git commit -m "chore: add railway deployment config"
git push origin main
```

---

## 2. Crear cuenta en Railway

### 2.1 Ir a railway.app

Abre el navegador y ve a: **https://railway.app**

### 2.2 Registrarse con GitHub

1. Haz clic en el botón **"Start a New Project"** o **"Login"** (arriba a la derecha)
2. Selecciona **"Login with GitHub"**
3. GitHub te pedirá autorizar a Railway — haz clic en **"Authorize Railway"**
4. Railway te redirige a su dashboard

### 2.3 Verificar el email (si lo pide)

Si Railway pide verificar tu email, revisa tu bandeja de entrada y haz clic en el enlace de verificación.

### 2.4 El crédito gratuito

Nada más crear la cuenta verás un banner con **"$5.00 of credits"**. No necesitas introducir tarjeta. Este crédito es suficiente para más de 15 días con tu stack.

---

## 3. Crear el proyecto en Railway

### 3.1 Nuevo proyecto

En el dashboard de Railway:

1. Haz clic en el botón **"New Project"** (arriba a la derecha, botón morado)

### 3.2 Seleccionar el tipo de deploy

Aparece un menú con varias opciones. Selecciona:

**"Deploy from GitHub repo"**

### 3.3 Conectar tu repositorio

1. Railway te mostrará un buscador con tus repositorios de GitHub
2. Escribe el nombre de tu repo en el buscador
3. Haz clic en el repo de Schooledule cuando aparezca
4. Railway comenzará a analizar el repositorio

### 3.4 Configurar la rama y el directorio raíz

Aparece una pantalla de configuración. Deja todo por defecto:

| Campo | Valor |
|---|---|
| Branch | `main` |
| Root Directory | `/` (vacío, dejar como está) |

> Railway encontrará el `railway.toml` en la raíz y sabrá dónde está el Dockerfile automáticamente.

### 3.5 NO hacer deploy todavía

Railway querrá hacer deploy inmediatamente. **Haz clic en "Add Variables"** en lugar de "Deploy Now" si aparece esa opción, o simplemente continúa — el deploy fallará en este momento porque aún no tiene las variables de entorno ni la base de datos. Eso es normal, lo solucionamos en los pasos siguientes.

---

## 4. Añadir la base de datos PostgreSQL

Ahora estás dentro del canvas del proyecto (verás un recuadro con tu app). Aquí añadimos la base de datos.

### 4.1 Crear el servicio PostgreSQL

1. Haz clic en el botón **"Create"** (arriba a la derecha dentro del proyecto)
2. En el menú que aparece, selecciona **"Database"**
3. Selecciona **"Add PostgreSQL"**

Railway desplegará automáticamente una instancia de PostgreSQL. Verás un nuevo recuadro en el canvas llamado **"Postgres"**.

### 4.2 Esperar a que PostgreSQL esté listo

El recuadro de Postgres mostrará un indicador de carga. Espera a que aparezca el tick verde (suele tardar 30-60 segundos).

### 4.3 Ver las credenciales de PostgreSQL (para referencia)

Haz clic en el recuadro de **"Postgres"** → pestaña **"Variables"**.

Verás variables como estas (los valores serán distintos en tu caso):

```
PGHOST       = roundhouse.proxy.rlwy.net
PGPORT       = 12345
PGDATABASE   = railway
PGUSER       = postgres
PGPASSWORD   = AbCdEfGhIjKlMnOpQr
DATABASE_URL = postgresql://postgres:AbCdEfGhIjKlMnOpQr@roundhouse.proxy.rlwy.net:12345/railway
```

**No copies estos valores manualmente** — en el siguiente paso los referenciamos directamente desde la app.

---

## 5. Desplegar la aplicación Spring Boot

### 5.1 Ir al servicio de la app

Haz clic en el recuadro de tu aplicación (el que tiene el nombre de tu repositorio).

### 5.2 Verificar la configuración de build

Ve a la pestaña **"Settings"** dentro del servicio de la app.

Busca la sección **"Build"** y verifica que:

| Campo | Valor que debería tener |
|---|---|
| Build Method | Dockerfile |
| Dockerfile Path | `infraestructura/Dockerfile` |
| Build Context | `/` |

Si el `Dockerfile Path` no aparece automáticamente (porque Railway leyó el `railway.toml`), rellénalo manualmente:
- Haz clic en el campo **"Dockerfile Path"**
- Escribe: `infraestructura/Dockerfile`

### 5.3 Configurar el puerto interno

En la misma sección **"Settings"**, busca **"Networking"** o **"Private Networking"**:

- **Internal Port**: `8080`

Si no aparece este campo, déjalo — Railway lo detecta del `EXPOSE 8080` del Dockerfile.

---

## 6. Configurar las variables de entorno

Este es el paso más importante. Sin las variables correctas, Spring Boot no puede conectarse a la base de datos.

### 6.1 Ir a Variables del servicio de la app

Con el servicio de la app seleccionado, haz clic en la pestaña **"Variables"**.

### 6.2 Añadir las variables una por una

Haz clic en **"New Variable"** y añade cada una de las siguientes:

---

**Variable 1 — URL de la base de datos**

| Campo | Valor |
|---|---|
| Name | `SPRING_DATASOURCE_URL` |
| Value | `jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}` |

> La sintaxis `${{Postgres.NOMBRE_VARIABLE}}` hace que Railway tome el valor directamente del servicio PostgreSQL que creaste. No escribas los valores reales.

---

**Variable 2 — Usuario de la base de datos**

| Campo | Valor |
|---|---|
| Name | `SPRING_DATASOURCE_USERNAME` |
| Value | `${{Postgres.PGUSER}}` |

---

**Variable 3 — Contraseña de la base de datos**

| Campo | Valor |
|---|---|
| Name | `SPRING_DATASOURCE_PASSWORD` |
| Value | `${{Postgres.PGPASSWORD}}` |

---

**Variable 4 — Perfil de Spring**

| Campo | Valor |
|---|---|
| Name | `SPRING_PROFILES_ACTIVE` |
| Value | `prod` |

---

**Variable 5 — Puerto (por si acaso)**

| Campo | Valor |
|---|---|
| Name | `SERVER_PORT` |
| Value | `8080` |

---

### 6.3 Guardar y hacer redeploy

1. Haz clic en **"Deploy"** o espera a que Railway detecte los cambios automáticamente
2. Verás que aparece un nuevo deploy en la pestaña **"Deployments"**

---

## 7. Configurar el dominio público

Sin dominio público, nadie puede acceder a la app desde internet.

### 7.1 Ir a Settings → Networking del servicio de la app

1. Haz clic en el servicio de la app
2. Ve a la pestaña **"Settings"**
3. Busca la sección **"Networking"** o **"Public Networking"**

### 7.2 Generar el dominio

1. Haz clic en **"Generate Domain"**
2. Railway asigna automáticamente una URL con formato:

```
https://tu-proyecto-produccion.up.railway.app
```

3. Haz clic en la URL para copiarla — la necesitarás para acceder a la app

---

## 8. Verificar que todo funciona

### 8.1 Monitorear los logs del deploy

1. Dentro del servicio de la app, ve a la pestaña **"Deployments"**
2. Haz clic en el deploy más reciente (el que está en progreso)
3. Verás los logs en tiempo real

**Secuencia de logs esperada:**

```
[Railway] Building Docker image...
[Railway] Step 1/X : FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
...
[Railway] Successfully built IMAGE_ID
[Railway] Deploying...
...
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::

Started SchooleduleSeguimientoApplication in XX.XXX seconds
```

> La primera vez puede tardar **5-10 minutos** porque Maven descarga todas las dependencias y compila el proyecto.

### 8.2 Verificar el healthcheck

Una vez que los logs muestren "Started ... in XX seconds", Railway hará automáticamente una petición a `/login` para verificar que la app responde.

Si ves en los logs:
```
[Railway] Healthcheck passed ✓
```
La app está lista.

### 8.3 Acceder a la aplicación

1. Copia la URL que generaste en el paso 7
2. Ábrela en el navegador
3. Deberías ver la pantalla de login de Schooledule

---

## 9. Solución de problemas comunes

### El deploy falla con "Out of Memory"

**Síntoma:** Los logs muestran `java.lang.OutOfMemoryError` o el container se reinicia solo.

**Solución:** Añade esta variable de entorno en el servicio de la app:

| Name | Value |
|---|---|
| `JAVA_TOOL_OPTIONS` | `-XX:+UseContainerSupport -XX:MaxRAMPercentage=65.0` |

### La app tarda mucho en arrancar (timeout en healthcheck)

**Síntoma:** Railway marca el deploy como fallido antes de que Spring Boot termine de arrancar.

**Solución:** El `railway.toml` ya tiene `healthcheckTimeout = 300` (5 minutos). Si sigue fallando, auméntalo a `600` en el fichero y haz push.

### Error de conexión a la base de datos

**Síntoma:** Los logs muestran `Connection refused` o `could not connect to server`.

**Comprobaciones:**
1. Verifica que el servicio de PostgreSQL está desplegado y con tick verde
2. Verifica que la variable `SPRING_DATASOURCE_URL` contiene exactamente:
   ```
   jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}
   ```
3. Comprueba que `SPRING_DATASOURCE_USERNAME` es `${{Postgres.PGUSER}}` (no `${{Postgres.PGUSERNAME}}`)

### El Dockerfile no se detecta

**Síntoma:** Railway intenta usar Nixpacks en lugar del Dockerfile.

**Solución:** Ve a Settings → Build → cambia "Build Method" a "Dockerfile" manualmente y escribe `infraestructura/Dockerfile` en el campo de ruta.

### Error 502 Bad Gateway al abrir la URL

**Síntoma:** La URL funciona pero muestra error 502.

**Causa:** La app aún no terminó de arrancar o el puerto no está bien configurado.

**Solución:**
1. Espera 2-3 minutos más y recarga
2. Verifica en Settings → Networking que el puerto interno es `8080`

---

## Resumen de variables de entorno necesarias

| Variable | Valor en Railway |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}` |
| `SPRING_DATASOURCE_USERNAME` | `${{Postgres.PGUSER}}` |
| `SPRING_DATASOURCE_PASSWORD` | `${{Postgres.PGPASSWORD}}` |
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SERVER_PORT` | `8080` |

---

## Coste estimado para 15 días

```
App Spring Boot (1 GB RAM, 0.5 vCPU × 15 días):  ~$0.15
PostgreSQL (512 MB RAM × 15 días):                ~$0.08
                                           Total:  ~$0.23
```

El crédito gratuito de $5 cubre esto con creces. No se te cobrará nada.
