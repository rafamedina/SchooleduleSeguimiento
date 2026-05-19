-- ==================================================================
-- SCHOOLEDULE — V1 SCHEMA DEFINITIVO
-- DDL puro: sin INSERT, sin ALTER TABLE, sin parches.
-- Consolida V1–V9 anteriores en un único script limpio.
-- ==================================================================

-- ==================================================================
-- 1. TIPOS ENUMERADOS
-- ==================================================================
CREATE TYPE estado_matricula AS ENUM ('ACTIVA', 'BAJA', 'CONVALIDADO');
CREATE TYPE tipo_actividad    AS ENUM ('EXAMEN', 'PRACTICA', 'RECUPERACION', 'ACTITUD');

-- ==================================================================
-- 2. IDENTIDAD Y RBAC
-- ==================================================================
CREATE TABLE roles (
    id     SERIAL,
    nombre VARCHAR(50) NOT NULL,
    CONSTRAINT pk_roles        PRIMARY KEY (id),
    CONSTRAINT uk_roles_nombre UNIQUE      (nombre)
);

CREATE TABLE usuarios (
    id                  SERIAL,
    username            VARCHAR(50)  NOT NULL,
    password_hash       VARCHAR(255) NOT NULL,
    nombre              VARCHAR(100) NOT NULL,
    apellidos           VARCHAR(100) NOT NULL,
    email               VARCHAR(150),
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    must_change_password BOOLEAN     NOT NULL DEFAULT FALSE,
    fecha_registro      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_usuarios          PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_username UNIQUE      (username),
    CONSTRAINT uk_usuarios_email    UNIQUE      (email)
);

CREATE TABLE usuarios_roles (
    usuario_id INT NOT NULL,
    rol_id     INT NOT NULL,
    CONSTRAINT pk_usuarios_roles         PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT fk_usuarios_roles_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_usuarios_roles_rol     FOREIGN KEY (rol_id)     REFERENCES roles(id)    ON DELETE CASCADE
);

CREATE INDEX idx_usuarios_roles_rol ON usuarios_roles(rol_id);

-- ==================================================================
-- 3. ESTRUCTURA ORGANIZATIVA
-- ==================================================================
CREATE TABLE centros (
    id            SERIAL,
    nombre        VARCHAR(100) NOT NULL,
    ubicacion     VARCHAR(200),
    configuracion JSONB        DEFAULT '{}',
    activo        BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_centros PRIMARY KEY (id)
);

CREATE TABLE cursos_academicos (
    id          SERIAL,
    nombre      VARCHAR(20) NOT NULL,
    fecha_inicio DATE        NOT NULL,
    fecha_fin    DATE        NOT NULL,
    activo       BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_cursos_academicos PRIMARY KEY (id)
);

CREATE TABLE profesores_sedes (
    usuario_id INT NOT NULL,
    centro_id  INT NOT NULL,
    CONSTRAINT pk_profesores_sedes         PRIMARY KEY (usuario_id, centro_id),
    CONSTRAINT fk_profesores_sedes_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_profesores_sedes_centro  FOREIGN KEY (centro_id)  REFERENCES centros(id)  ON DELETE CASCADE
);

-- ==================================================================
-- 4. CURRÍCULO ACADÉMICO
-- ==================================================================
CREATE TABLE modulos (
    id     SERIAL,
    codigo VARCHAR(20)  NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    activo BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_modulos      PRIMARY KEY (id),
    CONSTRAINT uk_modulos_codigo UNIQUE    (codigo)
);

CREATE TABLE resultados_aprendizaje (
    id                 SERIAL,
    modulo_id          INT          NOT NULL,
    curso_academico_id INT          NOT NULL,
    codigo             VARCHAR(20)  NOT NULL,
    descripcion        TEXT         NOT NULL,
    peso_sugerido      NUMERIC(5,2),
    CONSTRAINT pk_resultados_aprendizaje     PRIMARY KEY  (id),
    CONSTRAINT fk_ra_modulo                  FOREIGN KEY  (modulo_id)          REFERENCES modulos(id)           ON DELETE CASCADE,
    CONSTRAINT fk_ra_curso                   FOREIGN KEY  (curso_academico_id) REFERENCES cursos_academicos(id) ON DELETE CASCADE,
    CONSTRAINT uk_ra_modulo_curso_codigo     UNIQUE       (modulo_id, curso_academico_id, codigo)
);

CREATE TABLE criterios_evaluacion (
    id                      SERIAL,
    resultado_aprendizaje_id INT         NOT NULL,
    codigo                   VARCHAR(20) NOT NULL,
    descripcion              TEXT        NOT NULL,
    peso                     NUMERIC(5,2) NOT NULL DEFAULT 0,
    instrumento              VARCHAR(50),
    unidad_didactica         VARCHAR(20),
    trimestre                SMALLINT,
    CONSTRAINT pk_criterios_evaluacion PRIMARY KEY (id),
    CONSTRAINT fk_ce_ra                FOREIGN KEY (resultado_aprendizaje_id) REFERENCES resultados_aprendizaje(id) ON DELETE CASCADE,
    CONSTRAINT uk_ce_ra_codigo         UNIQUE      (resultado_aprendizaje_id, codigo),
    CONSTRAINT chk_ce_trimestre        CHECK       (trimestre IN (1, 2))
);

-- ==================================================================
-- 5. EJECUCIÓN Y MATRÍCULA
-- ==================================================================
CREATE TABLE grupos (
    id                 SERIAL,
    nombre             VARCHAR(50) NOT NULL,
    centro_id          INT         NOT NULL,
    curso_academico_id INT         NOT NULL,
    tutor_id           INT,
    CONSTRAINT pk_grupos       PRIMARY KEY (id),
    CONSTRAINT fk_grupos_centro FOREIGN KEY (centro_id)          REFERENCES centros(id)           ON DELETE CASCADE,
    CONSTRAINT fk_grupos_curso  FOREIGN KEY (curso_academico_id) REFERENCES cursos_academicos(id) ON DELETE CASCADE,
    CONSTRAINT fk_grupos_tutor  FOREIGN KEY (tutor_id)           REFERENCES usuarios(id)          ON DELETE SET NULL
);

CREATE INDEX idx_grupos_tutor ON grupos(tutor_id);

CREATE TABLE imparticiones (
    id                       SERIAL,
    modulo_id                INT  NOT NULL,
    grupo_id                 INT  NOT NULL,
    profesor_id              INT  NOT NULL,
    centro_id                INT  NOT NULL,
    configuracion_evaluacion JSONB DEFAULT '{}',
    CONSTRAINT pk_imparticiones          PRIMARY KEY (id),
    CONSTRAINT fk_imparticiones_modulo   FOREIGN KEY (modulo_id)   REFERENCES modulos(id)   ON DELETE CASCADE,
    CONSTRAINT fk_imparticiones_grupo    FOREIGN KEY (grupo_id)    REFERENCES grupos(id)    ON DELETE CASCADE,
    CONSTRAINT fk_imparticiones_profesor FOREIGN KEY (profesor_id) REFERENCES usuarios(id)  ON DELETE CASCADE,
    CONSTRAINT fk_imparticiones_centro   FOREIGN KEY (centro_id)   REFERENCES centros(id)   ON DELETE CASCADE
);

CREATE TABLE matriculas (
    id             SERIAL,
    alumno_id      INT     NOT NULL,
    imparticion_id INT     NOT NULL,
    centro_id      INT     NOT NULL,
    es_repetidor   BOOLEAN DEFAULT FALSE,
    estado         estado_matricula DEFAULT 'ACTIVA',
    CONSTRAINT pk_matriculas                   PRIMARY KEY (id),
    CONSTRAINT fk_matriculas_alumno            FOREIGN KEY (alumno_id)      REFERENCES usuarios(id)      ON DELETE CASCADE,
    CONSTRAINT fk_matriculas_imparticion       FOREIGN KEY (imparticion_id) REFERENCES imparticiones(id) ON DELETE CASCADE,
    CONSTRAINT fk_matriculas_centro            FOREIGN KEY (centro_id)      REFERENCES centros(id)       ON DELETE CASCADE,
    CONSTRAINT uk_matricula_alumno_imparticion UNIQUE      (alumno_id, imparticion_id)
);

-- ==================================================================
-- 6. EVALUACIÓN
-- ==================================================================
CREATE TABLE periodos_evaluacion (
    id             SERIAL,
    imparticion_id INT          NOT NULL,
    nombre         VARCHAR(50)  NOT NULL,
    peso           NUMERIC(5,2),
    cerrado        BOOLEAN      DEFAULT FALSE,
    CONSTRAINT pk_periodos_evaluacion   PRIMARY KEY (id),
    CONSTRAINT fk_periodos_imparticion  FOREIGN KEY (imparticion_id) REFERENCES imparticiones(id) ON DELETE CASCADE
);

CREATE TABLE items_evaluables (
    id                       SERIAL,
    imparticion_id           INT          NOT NULL,
    periodo_evaluacion_id    INT          NOT NULL,
    nombre                   VARCHAR(100) NOT NULL,
    fecha                    DATE,
    tipo                     tipo_actividad NOT NULL,
    resultado_aprendizaje_id INT          NOT NULL,
    configuracion_rubrica    JSONB        DEFAULT '{}',
    CONSTRAINT pk_items_evaluables    PRIMARY KEY (id),
    CONSTRAINT fk_items_imparticion   FOREIGN KEY (imparticion_id)        REFERENCES imparticiones(id)        ON DELETE CASCADE,
    CONSTRAINT fk_items_periodo       FOREIGN KEY (periodo_evaluacion_id)  REFERENCES periodos_evaluacion(id)  ON DELETE CASCADE,
    CONSTRAINT fk_items_ra            FOREIGN KEY (resultado_aprendizaje_id) REFERENCES resultados_aprendizaje(id)
);

CREATE TABLE calificaciones (
    id                    SERIAL,
    matricula_id          INT          NOT NULL,
    criterio_evaluacion_id INT         NOT NULL,
    valor                 NUMERIC(5,2),
    comentario            TEXT,
    fecha_modificacion    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_calificaciones                    PRIMARY KEY (id),
    CONSTRAINT fk_calificaciones_matricula          FOREIGN KEY (matricula_id)           REFERENCES matriculas(id)           ON DELETE CASCADE,
    CONSTRAINT fk_calificaciones_criterio           FOREIGN KEY (criterio_evaluacion_id) REFERENCES criterios_evaluacion(id) ON DELETE CASCADE,
    CONSTRAINT uk_calificacion_matricula_criterio   UNIQUE      (matricula_id, criterio_evaluacion_id)
);

CREATE INDEX idx_calificaciones_criterio ON calificaciones(criterio_evaluacion_id);

-- ==================================================================
-- 7. AUDITORÍA FORENSE
-- ==================================================================
CREATE TABLE auditoria_notas (
    id                   SERIAL,
    calificacion_id      INT          NOT NULL,
    valor_anterior       NUMERIC(5,2),
    valor_nuevo          NUMERIC(5,2),
    usuario_responsable  VARCHAR(100),
    fecha_cambio         TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    motivo               VARCHAR(255),
    CONSTRAINT pk_auditoria_notas       PRIMARY KEY (id),
    CONSTRAINT fk_auditoria_calificacion FOREIGN KEY (calificacion_id) REFERENCES calificaciones(id) ON DELETE CASCADE
);

-- Función: app.usuario_actual es inyectado por TeacherDashboardService
-- via: SET CONFIG 'app.usuario_actual' = :username, true
CREATE OR REPLACE FUNCTION registrar_cambio_nota()
RETURNS TRIGGER AS $$
DECLARE
    app_user TEXT;
BEGIN
    BEGIN
        app_user := current_setting('app.usuario_actual', true);
    EXCEPTION WHEN OTHERS THEN
        app_user := current_user;
    END;

    IF app_user IS NULL OR app_user = '' THEN
        app_user := 'SYSTEM_DB';
    END IF;

    IF (TG_OP = 'UPDATE' AND OLD.valor IS DISTINCT FROM NEW.valor) THEN
        INSERT INTO auditoria_notas (calificacion_id, valor_anterior, valor_nuevo, usuario_responsable, motivo)
        VALUES (NEW.id, OLD.valor, NEW.valor, app_user, 'Modificación registrada via Trigger');
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_auditoria_notas
    AFTER UPDATE ON calificaciones
    FOR EACH ROW
    EXECUTE FUNCTION registrar_cambio_nota();

-- ==================================================================
-- 8. SPRING SESSION (JDBC)
-- ==================================================================
CREATE TABLE SPRING_SESSION (
    PRIMARY_ID            CHAR(36)     NOT NULL,
    SESSION_ID            CHAR(36)     NOT NULL,
    CREATION_TIME         BIGINT       NOT NULL,
    LAST_ACCESS_TIME      BIGINT       NOT NULL,
    MAX_INACTIVE_INTERVAL INT          NOT NULL,
    EXPIRY_TIME           BIGINT       NOT NULL,
    PRINCIPAL_NAME        VARCHAR(100),
    CONSTRAINT pk_spring_session PRIMARY KEY (PRIMARY_ID)
);

CREATE UNIQUE INDEX idx_spring_session_id        ON SPRING_SESSION (SESSION_ID);
CREATE        INDEX idx_spring_session_expiry     ON SPRING_SESSION (EXPIRY_TIME);
CREATE        INDEX idx_spring_session_principal  ON SPRING_SESSION (PRINCIPAL_NAME);

CREATE TABLE SPRING_SESSION_ATTRIBUTES (
    SESSION_PRIMARY_ID CHAR(36)     NOT NULL,
    ATTRIBUTE_NAME     VARCHAR(200) NOT NULL,
    ATTRIBUTE_BYTES    BYTEA        NOT NULL,
    CONSTRAINT pk_spring_session_attributes    PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
    CONSTRAINT fk_session_attributes_session   FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
);

-- ==================================================================
-- 9. COMENTARIOS DE DOCUMENTACIÓN
-- ==================================================================
COMMENT ON TABLE  auditoria_notas IS 'Registro inmutable de cambios en calificaciones para fines forenses.';
COMMENT ON COLUMN auditoria_notas.usuario_responsable IS 'Inyectado desde Spring Boot via SET LOCAL app.usuario_actual.';
COMMENT ON COLUMN criterios_evaluacion.peso IS 'Porcentaje del CE dentro de su RA (0-100). Suma por RA debe ser 100.';
COMMENT ON COLUMN resultados_aprendizaje.peso_sugerido IS 'Porcentaje del RA sobre la nota final del módulo (0-100).';
