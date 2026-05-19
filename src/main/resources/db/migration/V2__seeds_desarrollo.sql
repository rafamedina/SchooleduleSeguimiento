-- ==================================================================
-- SCHOOLEDULE — V2 SEEDS DE DESARROLLO
-- Datos demo: solo módulo DAM-OPT con RAs y CEs reales del documento
-- "RA_Modulo Optativo_2º DAM (Introducción a la programación ASP .NET)_RA-CE.docx"
-- ==================================================================

-- ==================================================================
-- 1. ROLES
-- ==================================================================
INSERT INTO roles (id, nombre) VALUES
    (1, 'ROLE_ADMIN'),
    (2, 'ROLE_PROFESOR'),
    (3, 'ROLE_ALUMNO'),
    (4, 'ROLE_ADMIN_CENTRO');

-- ==================================================================
-- 2. USUARIOS (contraseña: 1234 — must_change_password=TRUE en todos)
-- Hash BCrypt de '1234': $2a$10$LwjJeRKHaydg2n5bPd.5guuBwZow7V6dZTfit.vl6Re3xgdR88aLi
-- ==================================================================
INSERT INTO usuarios (id, username, password_hash, nombre, apellidos, email, activo, must_change_password) VALUES
    (1, 'admin',        '$2a$10$LwjJeRKHaydg2n5bPd.5guuBwZow7V6dZTfit.vl6Re3xgdR88aLi', 'Super',  'Admin',   'admin@tfg.com', true, true),
    (2, 'profe1',       '$2a$10$LwjJeRKHaydg2n5bPd.5guuBwZow7V6dZTfit.vl6Re3xgdR88aLi', 'Juan',   'Garcia',  'juan@tfg.com',  true, true),
    (3, 'alumno1',      '$2a$10$LwjJeRKHaydg2n5bPd.5guuBwZow7V6dZTfit.vl6Re3xgdR88aLi', 'Ana',    'Lopez',   'ana@tfg.com',   true, true),
    (4, 'profe_alumno', '$2a$10$LwjJeRKHaydg2n5bPd.5guuBwZow7V6dZTfit.vl6Re3xgdR88aLi', 'Pedro',  'Mix',     'pedro@tfg.com', true, true);

-- ==================================================================
-- 3. ASIGNACIÓN DE ROLES
-- ==================================================================
INSERT INTO usuarios_roles (usuario_id, rol_id) VALUES
    (1, 1),  -- admin       → ROLE_ADMIN
    (2, 2),  -- profe1      → ROLE_PROFESOR
    (3, 3),  -- alumno1     → ROLE_ALUMNO
    (4, 2),  -- profe_alumno → ROLE_PROFESOR
    (4, 3);  -- profe_alumno → ROLE_ALUMNO

-- ==================================================================
-- 4. CENTROS
-- ==================================================================
INSERT INTO centros (id, nombre, ubicacion, activo) VALUES
    (1, 'IES Humanes', 'Humanes de Madrid', true),
    (2, 'IES Getafe',  'Getafe',            true);

-- ==================================================================
-- 5. PROFESORES ↔ SEDES
-- ==================================================================
INSERT INTO profesores_sedes (usuario_id, centro_id) VALUES
    (2, 1),  -- profe1 → IES Humanes
    (2, 2),  -- profe1 → IES Getafe
    (4, 1);  -- profe_alumno → IES Humanes

-- ==================================================================
-- 6. CURSO ACADÉMICO
-- ==================================================================
INSERT INTO cursos_academicos (id, nombre, fecha_inicio, fecha_fin, activo) VALUES
    (1, '2025/2026', '2025-09-01', '2026-06-30', true);

-- ==================================================================
-- 7. MÓDULO (solo DAM-OPT — datos de RAs/CEs extraídos del documento oficial)
-- ==================================================================
INSERT INTO modulos (id, codigo, nombre, activo) VALUES
    (1, 'DAM-OPT', 'Introducción a la Programación en ASP.NET', true);

-- ==================================================================
-- 8. GRUPO
-- ==================================================================
INSERT INTO grupos (id, nombre, centro_id, curso_academico_id) VALUES
    (1, '2º DAM-G', 2, 1);  -- IES Getafe, 2025/2026

-- ==================================================================
-- 9. IMPARTICIÓN
-- ==================================================================
INSERT INTO imparticiones (id, modulo_id, grupo_id, profesor_id, centro_id) VALUES
    (1, 1, 1, 2, 2);  -- DAM-OPT / 2º DAM-G / profe1 / IES Getafe

-- ==================================================================
-- 10. MATRÍCULA
-- ==================================================================
INSERT INTO matriculas (id, alumno_id, imparticion_id, centro_id, es_repetidor, estado) VALUES
    (1, 3, 1, 2, false, 'ACTIVA');  -- alumno1 en DAM-OPT

-- ==================================================================
-- 11. PERIODOS DE EVALUACIÓN
-- ==================================================================
INSERT INTO periodos_evaluacion (id, imparticion_id, nombre, peso, cerrado) VALUES
    (1, 1, '1ª Evaluación', 50.00, false),
    (2, 1, '2ª Evaluación', 50.00, false);

-- ==================================================================
-- 12. RESULTADOS DE APRENDIZAJE — DAM-OPT (5 RAs reales del documento)
-- peso_sugerido = % del RA sobre la nota final del módulo
-- ==================================================================
INSERT INTO resultados_aprendizaje (id, modulo_id, curso_academico_id, codigo, descripcion, peso_sugerido) VALUES
    (1, 1, 1, 'RA1', 'Comprender y aplicar los fundamentos del lenguaje C# en el desarrollo de aplicaciones',       15.00),
    (2, 1, 1, 'RA2', 'Utilizar características avanzadas de C# para optimizar el desarrollo de aplicaciones',       10.00),
    (3, 1, 1, 'RA3', 'Diseñar aplicaciones web con ASP.NET Core, comprendiendo la arquitectura MVC',               25.00),
    (4, 1, 1, 'RA4', 'Gestionar el acceso a datos mediante Entity Framework Core, aplicando principios de ORM',    25.00),
    (5, 1, 1, 'RA5', 'Implementar operaciones completas de persistencia y relaciones de datos en ASP.NET Core',    25.00);

-- ==================================================================
-- 13. CRITERIOS DE EVALUACIÓN — 19 CEs reales con pesos del documento
-- peso = % dentro del RA (suma por RA = 100)
-- instrumento: PRUEBA_OBJETIVA / ACTIVIDAD_EVALUABLE
-- ==================================================================

-- RA1 (15% módulo) — 4 CEs — UD 1 — 1º trimestre — suma=100
INSERT INTO criterios_evaluacion (id, resultado_aprendizaje_id, codigo, descripcion, peso, instrumento, unidad_didactica, trimestre) VALUES
    (1,  1, '1a', 'Se han identificado los fundamentos de la programación estructurada y el lenguaje C# (tipos de datos, variables, operadores)', 20.00, 'PRUEBA_OBJETIVA',     'UD 1', 1),
    (2,  1, '1b', 'Se han identificado los fundamentos de la programación orientada a objetos',                                                   20.00, 'PRUEBA_OBJETIVA',     'UD 1', 1),
    (3,  1, '1c', 'Se han implementado clases con atributos, métodos y constructores aplicando encapsulación',                                    30.00, 'ACTIVIDAD_EVALUABLE', 'UD 1', 1),
    (4,  1, '1d', 'Se han aplicado los principios de herencia y polimorfismo en ejercicios prácticos',                                            30.00, 'ACTIVIDAD_EVALUABLE', 'UD 1', 1);

-- RA2 (10% módulo) — 3 CEs — UD 2 — 1º trimestre — suma=100
INSERT INTO criterios_evaluacion (id, resultado_aprendizaje_id, codigo, descripcion, peso, instrumento, unidad_didactica, trimestre) VALUES
    (5,  2, '2a', 'Se han utilizado expresiones lambda en colecciones y métodos',                30.00, 'PRUEBA_OBJETIVA', 'UD 2', 1),
    (6,  2, '2b', 'Se han implementado delegados y eventos en programas sencillos',             35.00, 'PRUEBA_OBJETIVA', 'UD 2', 1),
    (7,  2, '2c', 'Se han realizado consultas LINQ sobre colecciones y objetos',                35.00, 'PRUEBA_OBJETIVA', 'UD 2', 1);

-- RA3 (25% módulo) — 4 CEs — UD 3 — 1º trimestre — suma=100
INSERT INTO criterios_evaluacion (id, resultado_aprendizaje_id, codigo, descripcion, peso, instrumento, unidad_didactica, trimestre) VALUES
    (8,  3, '3a', 'Se ha creado un proyecto MVC en ASP.NET Core y se han configurado Program.cs y appsettings.json',                                        10.00, 'ACTIVIDAD_EVALUABLE', 'UD 3', 1),
    (9,  3, '3b', 'Se han identificado todos los elementos del modelo MVC y el flujo de las peticiones a través del middleware y el enrutamiento',           10.00, 'PRUEBA_OBJETIVA',     'UD 3', 1),
    (10, 3, '3c', 'Se han creado controladores y vistas Razor enviando datos estáticos',                                                                     40.00, 'ACTIVIDAD_EVALUABLE', 'UD 3', 1),
    (11, 3, '3d', 'Se ha demostrado la capacidad de pasar datos del controlador a la vista',                                                                 40.00, 'PRUEBA_OBJETIVA',     'UD 3', 1);

-- RA4 (25% módulo) — 4 CEs — UD 4 — 2º trimestre (4a sin trimestre definido) — suma=100
INSERT INTO criterios_evaluacion (id, resultado_aprendizaje_id, codigo, descripcion, peso, instrumento, unidad_didactica, trimestre) VALUES
    (12, 4, '4a', 'Se ha comprendido el concepto de ORM y su utilidad en el desarrollo de aplicaciones',                                                     15.00, 'PRUEBA_OBJETIVA', 'UD 4', NULL),
    (13, 4, '4b', 'Se ha configurado Entity Framework Core, definiendo el contexto de la base de datos y la cadena de conexión',                             15.00, 'PRUEBA_OBJETIVA', 'UD 4', 2),
    (14, 4, '4c', 'Se han definido las entidades que representan las tablas de la base de datos y se ha implementado el patrón Repositorio',                 30.00, 'PRUEBA_OBJETIVA', 'UD 4', 2),
    (15, 4, '4d', 'Se han implementado consultas simples (listar, detalle) y se han creado métodos en controladores para mostrar datos',                     40.00, 'PRUEBA_OBJETIVA', 'UD 4', 2);

-- RA5 (25% módulo) — 4 CEs — UD 5 — 2º trimestre — suma=100
INSERT INTO criterios_evaluacion (id, resultado_aprendizaje_id, codigo, descripcion, peso, instrumento, unidad_didactica, trimestre) VALUES
    (16, 5, '5a', 'Se han utilizado migraciones para gestionar los cambios en la estructura de la base de datos',                15.00, 'ACTIVIDAD_EVALUABLE', 'UD 5', 2),
    (17, 5, '5b', 'Se han diseñado y modelado las relaciones entre las tablas',                                                  15.00, 'ACTIVIDAD_EVALUABLE', 'UD 5', 2),
    (18, 5, '5c', 'Se ha aplicado AutoMapper para automatizar el mapeo entre las entidades del modelo y los DTOs',              15.00, 'ACTIVIDAD_EVALUABLE', 'UD 5', 2),
    (19, 5, '5d', 'Se ha implementado la funcionalidad CRUD completa (Crear, Leer, Actualizar y Borrar)',                        55.00, 'ACTIVIDAD_EVALUABLE', 'UD 5', 2);

-- ==================================================================
-- 14. ITEMS EVALUABLES (5 — uno por RA, con ra_id desde el inicio)
-- ==================================================================
INSERT INTO items_evaluables (id, imparticion_id, periodo_evaluacion_id, nombre, fecha, tipo, resultado_aprendizaje_id) VALUES
    (1, 1, 1, 'Prueba — Fundamentos C# y POO (RA1)',               '2025-10-20', 'EXAMEN',    1),
    (2, 1, 1, 'Prueba — C# avanzado: lambda, eventos y LINQ (RA2)', '2025-11-10', 'EXAMEN',    2),
    (3, 1, 1, 'Actividad — Aplicación MVC con ASP.NET Core (RA3)', '2025-12-01', 'PRACTICA',  3),
    (4, 1, 2, 'Prueba — Entity Framework Core (RA4)',              '2026-02-20', 'EXAMEN',    4),
    (5, 1, 2, 'Actividad CRUD — EF Core y relaciones (RA5)',       '2026-04-10', 'PRACTICA',  5);

-- ==================================================================
-- 15. CALIFICACIONES DEMO (alumno1 calificado en CEs de RA1 y RA3)
-- ==================================================================
INSERT INTO calificaciones (matricula_id, criterio_evaluacion_id, valor, comentario) VALUES
    (1,  1, 7.50, 'Fundamentos C# bien asimilados'),
    (1,  2, 8.00, 'POO comprendido correctamente'),
    (1,  3, 8.50, 'Clases y encapsulación sin errores'),
    (1,  4, 7.00, 'Herencia y polimorfismo con algún matiz'),
    (1,  8, 9.00, 'Proyecto MVC configurado perfectamente'),
    (1,  9, 8.50, 'Flujo MVC y middleware bien entendido');

-- ==================================================================
-- 16. SINCRONIZACIÓN DE SECUENCIAS
-- ==================================================================
SELECT setval('roles_id_seq',                    (SELECT MAX(id) FROM roles));
SELECT setval('usuarios_id_seq',                 (SELECT MAX(id) FROM usuarios));
SELECT setval('centros_id_seq',                  (SELECT MAX(id) FROM centros));
SELECT setval('cursos_academicos_id_seq',        (SELECT MAX(id) FROM cursos_academicos));
SELECT setval('modulos_id_seq',                  (SELECT MAX(id) FROM modulos));
SELECT setval('grupos_id_seq',                   (SELECT MAX(id) FROM grupos));
SELECT setval('imparticiones_id_seq',            (SELECT MAX(id) FROM imparticiones));
SELECT setval('matriculas_id_seq',               (SELECT MAX(id) FROM matriculas));
SELECT setval('periodos_evaluacion_id_seq',      (SELECT MAX(id) FROM periodos_evaluacion));
SELECT setval('resultados_aprendizaje_id_seq',   (SELECT MAX(id) FROM resultados_aprendizaje));
SELECT setval('criterios_evaluacion_id_seq',     (SELECT MAX(id) FROM criterios_evaluacion));
SELECT setval('items_evaluables_id_seq',         (SELECT MAX(id) FROM items_evaluables));
SELECT setval('calificaciones_id_seq',           (SELECT MAX(id) FROM calificaciones));
