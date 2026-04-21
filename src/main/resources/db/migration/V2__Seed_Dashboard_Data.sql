-- ==================================================================
-- V2: DATOS SEMILLA PARA EL DASHBOARD DEL PROFESOR
-- Ciclo: 2º DAM  Centros: IES Humanes (Humanes de Madrid) y IES Getafe
-- Módulos reales: 0491 SGE y Optativo ASP.NET
-- ==================================================================

-- Renombrar centro 1 al nombre real
UPDATE centros SET nombre = 'IES Humanes', ubicacion = 'Humanes de Madrid' WHERE id = 1;

-- Centro 2
INSERT INTO centros (id, nombre, ubicacion) VALUES
(2, 'IES Getafe', 'Getafe');

-- Sedes: profe1 (id=2) ↔ ambos centros; profe_alumno (id=4) ↔ centro 1
INSERT INTO profesores_sedes (usuario_id, centro_id) VALUES
(2, 1),
(2, 2),
(4, 1);

-- Curso académico
INSERT INTO cursos_academicos (id, nombre, fecha_inicio, fecha_fin, activo) VALUES
(1, '2025/2026', '2025-09-01', '2026-06-30', true);

-- Módulos (ciclo 2º DAM)
INSERT INTO modulos (id, codigo, nombre) VALUES
(1, '0491',    'Sistemas de Gestión Empresarial'),
(2, 'DAM-OPT', 'Introducción a la Programación en ASP.NET');

-- Grupos
INSERT INTO grupos (id, nombre, centro_id, curso_academico_id) VALUES
(1, '2º DAM-A', 1, 1),
(2, '2º DAM-B', 1, 1),
(3, '2º DAM-G', 2, 1);

-- Imparticiones
-- profe1: SGE en 2º DAM-A (Humanes), ASP.NET en 2º DAM-G (Getafe)
-- profe_alumno: SGE en 2º DAM-B (Humanes)
INSERT INTO imparticiones (id, modulo_id, grupo_id, profesor_id, centro_id) VALUES
(1, 1, 1, 2, 1),
(2, 2, 3, 2, 2),
(3, 1, 2, 4, 1);

-- Matrículas: alumno1 (id=3) en las 3 imparticiones
INSERT INTO matriculas (id, alumno_id, imparticion_id, centro_id, es_repetidor, estado) VALUES
(1, 3, 1, 1, false, 'ACTIVA'),
(2, 3, 2, 2, false, 'ACTIVA'),
(3, 3, 3, 1, true,  'ACTIVA');

-- Periodos de evaluación
-- SGE (impartición 1): 1ª eval 40%, 2ª eval 60%
-- ASP.NET (impartición 2): 1ª eval 50%, 2ª eval 50%
-- SGE (impartición 3): 1ª eval 50%, 2ª eval 50%
INSERT INTO periodos_evaluacion (id, imparticion_id, nombre, peso, cerrado) VALUES
(1, 1, '1ª Evaluación', 40.00, false),
(2, 1, '2ª Evaluación', 60.00, false),
(3, 2, '1ª Evaluación', 50.00, false),
(4, 2, '2ª Evaluación', 50.00, false),
(5, 3, '1ª Evaluación', 50.00, false),
(6, 3, '2ª Evaluación', 50.00, false);

-- Ítems evaluables basados en los RAs reales de cada módulo
-- SGE – impartición 1
INSERT INTO items_evaluables (id, imparticion_id, periodo_evaluacion_id, nombre, fecha, tipo) VALUES
(1,  1, 1, 'Prueba objetiva – Identificación ERP-CRM (RA1)',       '2025-10-15', 'EXAMEN'),
(2,  1, 1, 'Actividad – Instalación y configuración ERP (RA2)',    '2025-11-10', 'PRACTICA'),
(3,  1, 1, 'Actividad – Consulta y análisis de datos (RA3)',       '2025-12-05', 'PRACTICA'),
(4,  1, 1, 'Actividad – Adaptación del sistema ERP (RA4)',         '2026-01-20', 'PRACTICA'),
(5,  1, 2, 'Actividad – Desarrollo de componentes ERP (RA5)',      '2026-03-10', 'PRACTICA'),
(6,  1, 2, 'Prueba final – Sistemas de Gestión Empresarial',       '2026-05-15', 'EXAMEN'),
-- ASP.NET – impartición 2
(7,  2, 3, 'Prueba objetiva – Fundamentos C# y POO (RA1-RA2)',     '2025-10-20', 'EXAMEN'),
(8,  2, 3, 'Actividad – Aplicación MVC con ASP.NET Core (RA3)',    '2025-12-01', 'PRACTICA'),
(9,  2, 4, 'Prueba objetiva – Entity Framework Core (RA4)',        '2026-02-20', 'EXAMEN'),
(10, 2, 4, 'Actividad CRUD – EF Core y relaciones (RA5)',          '2026-04-10', 'PRACTICA'),
-- SGE – impartición 3
(11, 3, 5, 'Prueba objetiva – Identificación ERP-CRM (RA1)',       '2025-10-15', 'EXAMEN'),
(12, 3, 5, 'Actividad – Instalación y configuración ERP (RA2)',    '2025-11-10', 'PRACTICA'),
(13, 3, 5, 'Actividad – Consulta y análisis de datos (RA3)',       '2025-12-05', 'PRACTICA'),
(14, 3, 6, 'Actividad – Adaptación del sistema ERP (RA4)',         '2026-01-20', 'PRACTICA'),
(15, 3, 6, 'Actividad – Desarrollo de componentes ERP (RA5)',      '2026-03-10', 'PRACTICA');

-- Calificaciones pre-pobladas para la matrícula 1 (alumno en SGE, impartición 1)
INSERT INTO calificaciones (id, matricula_id, item_evaluable_id, valor, comentario) VALUES
(1, 1, 1, 7.50, 'Prueba superada'),
(2, 1, 2, 8.00, 'Buena instalación de Odoo'),
(3, 1, 3, 9.00, 'Consultas y formularios correctos'),
(4, 2, 7, 6.50, 'Conceptos básicos de C# asimilados');

-- Sincronizar secuencias
SELECT setval('centros_id_seq',             (SELECT MAX(id) FROM centros));
SELECT setval('cursos_academicos_id_seq',   (SELECT MAX(id) FROM cursos_academicos));
SELECT setval('modulos_id_seq',             (SELECT MAX(id) FROM modulos));
SELECT setval('grupos_id_seq',              (SELECT MAX(id) FROM grupos));
SELECT setval('imparticiones_id_seq',       (SELECT MAX(id) FROM imparticiones));
SELECT setval('matriculas_id_seq',          (SELECT MAX(id) FROM matriculas));
SELECT setval('periodos_evaluacion_id_seq', (SELECT MAX(id) FROM periodos_evaluacion));
SELECT setval('items_evaluables_id_seq',    (SELECT MAX(id) FROM items_evaluables));
SELECT setval('calificaciones_id_seq',      (SELECT MAX(id) FROM calificaciones));

-- Renombrar variable de sesión del trigger: 'app.current_user' colisiona con keyword
-- reservada de PostgreSQL en SET LOCAL. Se usa 'app.usuario_actual' en su lugar.
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

    IF (TG_OP = 'UPDATE' AND OLD.valor <> NEW.valor) THEN
        INSERT INTO auditoria_notas (calificacion_id, valor_anterior, valor_nuevo, usuario_responsable, motivo)
        VALUES (NEW.id, OLD.valor, NEW.valor, app_user, 'Modificación registrada via Trigger');
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
