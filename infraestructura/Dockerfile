# --- STAGE 1: Build ---
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copiamos el pom.xml y descargamos dependencias para aprovechar la caché de Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiamos el código fuente y construimos el proyecto
COPY src ./src
RUN mvn spotless:apply clean package -DskipTests

# --- STAGE 1.5: Test ---
FROM builder AS test
# Usamos CMD para que sea sobreescribible fácilmente en el CI
CMD ["mvn", "verify", "-DskipTests=false", "-B"]

# --- STAGE 2: Runtime ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Creamos un usuario no raíz por seguridad y establecemos permisos
RUN addgroup -S spring && adduser -S spring -G spring
RUN chown -R spring:spring /app

USER spring:spring

# Copiamos el JAR generado desde la etapa de construcción
COPY --from=builder --chown=spring:spring /app/target/*.jar app.jar

# Exponemos el puerto
EXPOSE 8080

# Chequeo de salud nativo de Docker (usa /login que sabemos que existe)
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/login || exit 1

# Parámetros de JVM optimizados para contenedores
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
