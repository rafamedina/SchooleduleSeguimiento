-- ============================================================
-- V3: GRADING GRANULAR POR CRITERIO DE EVALUACIÓN
-- Cada calificación ahora referencia un CriterioEvaluacion,
-- no un ItemEvaluable.  El ItemEvaluable sigue existiendo como
-- metadato de la actividad y queda vinculado a su RA mediante FK.
-- ============================================================

-- 1. RESULTADOS DE APRENDIZAJE ─ Módulo 1 (SGE-0491) ──────────────
INSERT INTO resultados_aprendizaje (id, modulo_id, curso_academico_id, codigo, descripcion) VALUES
(1, 1, 1, 'RA1', 'Identifica sistemas ERP-CRM, reconociendo sus características y clasificándolos.'),
(2, 1, 1, 'RA2', 'Instala y configura sistemas ERP, interpretando la documentación técnica.'),
(3, 1, 1, 'RA3', 'Realiza consultas y análisis de datos en sistemas ERP, utilizando herramientas integradas.'),
(4, 1, 1, 'RA4', 'Adapta sistemas ERP-CRM a las necesidades de la organización, parametrizando módulos.'),
(5, 1, 1, 'RA5', 'Desarrolla componentes para sistemas ERP-CRM, aplicando técnicas de programación.'),
(6, 1, 1, 'RA6', 'Evalúa de forma integrada los sistemas de gestión empresarial implementados.');

-- 2. RESULTADOS DE APRENDIZAJE ─ Módulo 2 (DAM-OPT ASP.NET) ──────
INSERT INTO resultados_aprendizaje (id, modulo_id, curso_academico_id, codigo, descripcion) VALUES
(7,  2, 1, 'RA1', 'Conoce los fundamentos de C# y la programación orientada a objetos.'),
(8,  2, 1, 'RA2', 'Aplica los principios de diseño orientado a objetos en C#.'),
(9,  2, 1, 'RA3', 'Construye aplicaciones web con el patrón MVC usando ASP.NET Core.'),
(10, 2, 1, 'RA4', 'Accede y gestiona datos con Entity Framework Core.'),
(11, 2, 1, 'RA5', 'Implementa operaciones CRUD completas con EF Core y relaciones entre entidades.');

-- 3. CRITERIOS DE EVALUACIÓN ─ SGE RA1 (ids 1-3) ─────────────────
INSERT INTO criterios_evaluacion (id, resultado_aprendizaje_id, codigo, descripcion) VALUES
(1, 1, 'a', 'Distingue los distintos tipos de sistemas ERP y CRM del mercado.'),
(2, 1, 'b', 'Identifica los módulos funcionales de un ERP y sus interdependencias.'),
(3, 1, 'c', 'Valora las ventajas e inconvenientes de los sistemas ERP-CRM.');

-- SGE RA2 (ids 4-6)
INSERT INTO criterios_evaluacion (id, resultado_aprendizaje_id, codigo, descripcion) VALUES
(4, 2, 'a', 'Realiza la instalación del entorno ERP siguiendo la guía técnica.'),
(5, 2, 'b', 'Configura los parámetros iniciales del sistema ERP correctamente.'),
(6, 2, 'c', 'Gestiona usuarios, perfiles y permisos en el sistema instalado.');

-- SGE RA3 (ids 7-9)
INSERT INTO criterios_evaluacion (id, resultado_aprendizaje_id, codigo, descripcion) VALUES
(7, 3, 'a', 'Elabora consultas básicas sobre los módulos de gestión empresarial.'),
(8, 3, 'b', 'Genera informes y cuadros de mando a partir de los datos del ERP.'),
(9, 3, 'c', 'Exporta e importa datos entre el ERP y hojas de cálculo externas.');

-- SGE RA4 (ids 10-12)
INSERT INTO criterios_evaluacion (id, resultado_aprendizaje_id, codigo, descripcion) VALUES
(10, 4, 'a', 'Parametriza módulos del ERP adaptándolos a la casuística de la empresa.'),
(11, 4, 'b', 'Añade campos personalizados y modifica vistas del ERP.'),
(12, 4, 'c', 'Configura workflows y alertas automáticas en el sistema ERP.');

-- SGE RA5 (ids 13-15)
INSERT INTO criterios_evaluacion (id, resultado_aprendizaje_id, codigo, descripcion) VALUES
(13, 5, 'a', 'Crea módulos básicos en el lenguaje nativo del ERP.'),
(14, 5, 'b', 'Integra el componente desarrollado con los módulos existentes del ERP.'),
(15, 5, 'c', 'Documenta el componente desarrollado y realiza pruebas unitarias.');

-- SGE RA6 (ids 16-18)
INSERT INTO criterios_evaluacion (id, resultado_aprendizaje_id, codigo, descripcion) VALUES
(16, 6, 'a', 'Demuestra comprensión global de los módulos funcionales del ERP.'),
(17, 6, 'b', 'Resuelve supuestos prácticos integradores de gestión empresarial.'),
(18, 6, 'c', 'Argumenta decisiones de implantación y configuración tomadas.');

-- ASP.NET RA1 (ids 19-21)
INSERT INTO criterios_evaluacion (id, resultado_aprendizaje_id, codigo, descripcion) VALUES
(19, 7, 'a', 'Declara y utiliza tipos básicos, colecciones y null-safety en C#.'),
(20, 7, 'b', 'Implementa clases, interfaces y herencia siguiendo el modelo POO.'),
(21, 7, 'c', 'Aplica LINQ para consultar y transformar colecciones de datos.');

-- ASP.NET RA2 (ids 22-24)
INSERT INTO criterios_evaluacion (id, resultado_aprendizaje_id, codigo, descripcion) VALUES
(22, 8, 'a', 'Aplica encapsulamiento y principio de responsabilidad única.'),
(23, 8, 'b', 'Utiliza patrones de diseño comunes como repositorio y fábrica.'),
(24, 8, 'c', 'Refactoriza código para mejorar cohesión y reducir acoplamiento.');

-- ASP.NET RA3 (ids 25-27)
INSERT INTO criterios_evaluacion (id, resultado_aprendizaje_id, codigo, descripcion) VALUES
(25, 9, 'a', 'Configura el enrutamiento y middleware de una aplicación ASP.NET Core.'),
(26, 9, 'b', 'Implementa controladores y vistas con Razor siguiendo el patrón MVC.'),
(27, 9, 'c', 'Valida la entrada del usuario usando Data Annotations y model binding.');

-- ASP.NET RA4 (ids 28-30)
INSERT INTO criterios_evaluacion (id, resultado_aprendizaje_id, codigo, descripcion) VALUES
(28, 10, 'a', 'Configura un DbContext y mapea entidades con EF Core Migrations.'),
(29, 10, 'b', 'Ejecuta consultas LINQ-to-SQL con relaciones cargadas bajo demanda.'),
(30, 10, 'c', 'Gestiona transacciones y controla errores de concurrencia en EF Core.');

-- ASP.NET RA5 (ids 31-33)
INSERT INTO criterios_evaluacion (id, resultado_aprendizaje_id, codigo, descripcion) VALUES
(31, 11, 'a', 'Implementa operaciones Create, Read, Update y Delete completas.'),
(32, 11, 'b', 'Gestiona relaciones 1:N y N:M con includes y navegación explícita.'),
(33, 11, 'c', 'Expone los datos a través de una API REST mínima con resultados paginados.');

-- 4. VINCULAR items_evaluables A SU RA ────────────────────────────
-- Nota: item 7 tiene "(RA1-RA2)" en el nombre; se colapsa a RA1 del mod2 (id=7).
ALTER TABLE items_evaluables ADD COLUMN resultado_aprendizaje_id INT;
ALTER TABLE items_evaluables
    ADD CONSTRAINT fk_items_ra FOREIGN KEY (resultado_aprendizaje_id)
    REFERENCES resultados_aprendizaje(id);

UPDATE items_evaluables SET resultado_aprendizaje_id = 1  WHERE id = 1;  -- SGE RA1
UPDATE items_evaluables SET resultado_aprendizaje_id = 2  WHERE id = 2;  -- SGE RA2
UPDATE items_evaluables SET resultado_aprendizaje_id = 3  WHERE id = 3;  -- SGE RA3
UPDATE items_evaluables SET resultado_aprendizaje_id = 4  WHERE id = 4;  -- SGE RA4
UPDATE items_evaluables SET resultado_aprendizaje_id = 5  WHERE id = 5;  -- SGE RA5
UPDATE items_evaluables SET resultado_aprendizaje_id = 6  WHERE id = 6;  -- SGE RA6 (prueba final)
UPDATE items_evaluables SET resultado_aprendizaje_id = 7  WHERE id = 7;  -- ASP.NET RA1 (collapsed from RA1-RA2)
UPDATE items_evaluables SET resultado_aprendizaje_id = 9  WHERE id = 8;  -- ASP.NET RA3
UPDATE items_evaluables SET resultado_aprendizaje_id = 10 WHERE id = 9;  -- ASP.NET RA4
UPDATE items_evaluables SET resultado_aprendizaje_id = 11 WHERE id = 10; -- ASP.NET RA5
UPDATE items_evaluables SET resultado_aprendizaje_id = 1  WHERE id = 11; -- SGE RA1 (imparticion 3)
UPDATE items_evaluables SET resultado_aprendizaje_id = 2  WHERE id = 12; -- SGE RA2
UPDATE items_evaluables SET resultado_aprendizaje_id = 3  WHERE id = 13; -- SGE RA3
UPDATE items_evaluables SET resultado_aprendizaje_id = 4  WHERE id = 14; -- SGE RA4
UPDATE items_evaluables SET resultado_aprendizaje_id = 5  WHERE id = 15; -- SGE RA5

ALTER TABLE items_evaluables ALTER COLUMN resultado_aprendizaje_id SET NOT NULL;

-- 5. REFACTORIZAR calificaciones: nota por CE ─────────────────────
-- Dev-only: reset completo de auditoría y notas para aplicar el nuevo esquema.
DELETE FROM auditoria_notas;
DELETE FROM calificaciones;

ALTER TABLE calificaciones DROP CONSTRAINT uk_calificacion_matricula_item;
ALTER TABLE calificaciones DROP COLUMN item_evaluable_id;

ALTER TABLE calificaciones
    ADD COLUMN criterio_evaluacion_id INT NOT NULL
    REFERENCES criterios_evaluacion(id) ON DELETE CASCADE;

ALTER TABLE calificaciones
    ADD CONSTRAINT uk_calificacion_matricula_criterio
    UNIQUE (matricula_id, criterio_evaluacion_id);

CREATE INDEX idx_calificaciones_criterio ON calificaciones(criterio_evaluacion_id);

-- 6. RE-SEMBRAR calificaciones a nivel de CE ──────────────────────
-- Matrícula 1 (alumno1 en SGE impartición 1): CEs de RA1, RA2, RA3 calificadas.
INSERT INTO calificaciones (matricula_id, criterio_evaluacion_id, valor, comentario) VALUES
-- RA1 CEs (ids 1-3)
(1, 1, 7.50, 'Distingue bien los sistemas ERP-CRM'),
(1, 2, 8.00, 'Identifica correctamente los módulos'),
(1, 3, 7.00, 'Valoración correcta con matices'),
-- RA2 CEs (ids 4-6)
(1, 4, 8.00, 'Instalación de Odoo correcta'),
(1, 5, 7.50, 'Configuración inicial bien resuelta'),
(1, 6, 8.50, 'Gestión de usuarios sin errores'),
-- RA3 CEs (ids 7-9)
(1, 7, 9.00, 'Consultas correctas y bien planteadas'),
(1, 8, 8.50, 'Informes con buen diseño'),
(1, 9, 9.50, 'Exportación perfecta');

-- Matrícula 2 (alumno1 en ASP.NET impartición 2): CEs de RA1 calificadas.
INSERT INTO calificaciones (matricula_id, criterio_evaluacion_id, valor, comentario) VALUES
(2, 19, 6.50, 'Conceptos básicos de C# asimilados'),
(2, 20, 7.00, 'Herencia y clases bien implementadas'),
(2, 21, 6.00, 'LINQ con errores menores');

-- 7. SINCRONIZAR SECUENCIAS ───────────────────────────────────────
SELECT setval('resultados_aprendizaje_id_seq', (SELECT MAX(id) FROM resultados_aprendizaje));
SELECT setval('criterios_evaluacion_id_seq',   (SELECT MAX(id) FROM criterios_evaluacion));
SELECT setval('calificaciones_id_seq',         (SELECT MAX(id) FROM calificaciones));
