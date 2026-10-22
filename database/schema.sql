-- =====================================================================
-- Examen 1 Programación III — Caso 5: Gestión de biblioteca
-- Script definitivo. Motor: MySQL 8.x, InnoDB.
-- Etapa 3: se agregan las columnas de multa (cambio de requerimiento).
--
-- En un servidor compartido (phpMyAdmin) donde no se puede crear ni borrar
-- la base de datos, ejecute el script desde la sección "Tablas" en adelante,
-- directamente sobre la base asignada.
-- =====================================================================

DROP DATABASE IF EXISTS examen_progra3;
CREATE DATABASE examen_progra3
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_spanish_ci;

USE examen_progra3;

-- ---------------------------------------------------------------------
-- Tablas
-- Primero las tablas sin llaves foráneas (usuario, libro) y luego las
-- que dependen de ellas (ejemplar, prestamo).
-- ---------------------------------------------------------------------

-- Permite re-ejecutar solo esta sección sobre una base ya existente
DROP TABLE IF EXISTS prestamo;
DROP TABLE IF EXISTS ejemplar;
DROP TABLE IF EXISTS libro;
DROP TABLE IF EXISTS usuario;

-- ---------------------------------------------------------------------
-- usuario: lector registrado
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
    identificacion  VARCHAR(20)   NOT NULL,
    nombre          VARCHAR(100)  NOT NULL,
    correo          VARCHAR(120)  NOT NULL,
    activo          BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_usuario PRIMARY KEY (identificacion),
    CONSTRAINT uq_usuario_correo UNIQUE (correo),
    CONSTRAINT ck_usuario_correo CHECK (correo LIKE '%_@_%._%')
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- libro: título del catálogo
-- ---------------------------------------------------------------------
CREATE TABLE libro (
    isbn    VARCHAR(17)   NOT NULL,
    titulo  VARCHAR(200)  NOT NULL,
    autor   VARCHAR(120)  NOT NULL,
    anio    SMALLINT      NOT NULL,
    CONSTRAINT pk_libro PRIMARY KEY (isbn),
    CONSTRAINT ck_libro_anio CHECK (anio BETWEEN 1450 AND 2100)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- ejemplar: copia física de un libro
-- ---------------------------------------------------------------------
CREATE TABLE ejemplar (
    codigo      VARCHAR(20)  NOT NULL,
    libro_isbn  VARCHAR(17)  NOT NULL,
    estado      ENUM('DISPONIBLE', 'PRESTADO', 'DANADO') NOT NULL DEFAULT 'DISPONIBLE',
    CONSTRAINT pk_ejemplar PRIMARY KEY (codigo),
    CONSTRAINT fk_ejemplar_libro FOREIGN KEY (libro_isbn)
        REFERENCES libro (isbn)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE INDEX ix_ejemplar_libro_estado ON ejemplar (libro_isbn, estado);

-- ---------------------------------------------------------------------
-- prestamo: registro de la operación entre usuario y ejemplar
-- "Atrasado" no se guarda: estado = 'ACTIVO' AND fecha_limite < CURDATE()
-- ---------------------------------------------------------------------
CREATE TABLE prestamo (
    id                INT          NOT NULL AUTO_INCREMENT,
    usuario_id        VARCHAR(20)  NOT NULL,
    ejemplar_codigo   VARCHAR(20)  NOT NULL,
    fecha_prestamo    DATE         NOT NULL,
    fecha_limite      DATE         NOT NULL,
    fecha_devolucion  DATE         NULL,
    estado            ENUM('ACTIVO', 'DEVUELTO') NOT NULL DEFAULT 'ACTIVO',
    multa             DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    multa_pagada      BOOLEAN       NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_prestamo PRIMARY KEY (id),
    CONSTRAINT fk_prestamo_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuario (identificacion)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_prestamo_ejemplar FOREIGN KEY (ejemplar_codigo)
        REFERENCES ejemplar (codigo)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_prestamo_limite CHECK (fecha_limite >= fecha_prestamo),
    CONSTRAINT ck_prestamo_devolucion CHECK (
        fecha_devolucion IS NULL OR fecha_devolucion >= fecha_prestamo
    ),
    CONSTRAINT ck_prestamo_multa CHECK (multa >= 0),
    -- Coherencia estado/fecha: ACTIVO sin devolución, DEVUELTO con devolución
    CONSTRAINT ck_prestamo_estado CHECK (
        (estado = 'ACTIVO'   AND fecha_devolucion IS NULL) OR
        (estado = 'DEVUELTO' AND fecha_devolucion IS NOT NULL)
    )
) ENGINE = InnoDB;

CREATE INDEX ix_prestamo_usuario_estado ON prestamo (usuario_id, estado);
CREATE INDEX ix_prestamo_estado_limite  ON prestamo (estado, fecha_limite);
CREATE INDEX ix_prestamo_usuario_multa  ON prestamo (usuario_id, multa_pagada);

-- ---------------------------------------------------------------------
-- Datos de prueba
-- ---------------------------------------------------------------------
INSERT INTO usuario (identificacion, nombre, correo, activo) VALUES
    ('101110111', 'Ana Mora Solís',      'ana.mora@correo.com',    TRUE),
    ('202220222', 'Luis Vargas Rojas',   'luis.vargas@correo.com', TRUE),
    ('303330333', 'Carla Quesada Arias', 'carla.q@correo.com',     FALSE),
    ('404440444', 'Sofía Castro Ureña',  'sofia.castro@correo.com', TRUE),
    ('505550555', 'Diego Rojas Mena',    'diego.rojas@correo.com', TRUE);

INSERT INTO libro (isbn, titulo, autor, anio) VALUES
    ('978-9968-12-001-1', 'Cuentos de mi tía Panchita', 'Carmen Lyra',        1920),
    ('978-9968-12-002-8', 'Mamita Yunai',               'Carlos Luis Fallas', 1941),
    ('978-0-13-468599-1', 'Effective Java',             'Joshua Bloch',       2018);

INSERT INTO ejemplar (codigo, libro_isbn, estado) VALUES
    ('EJ-001', '978-9968-12-001-1', 'DISPONIBLE'),
    ('EJ-002', '978-9968-12-001-1', 'DISPONIBLE'),
    ('EJ-003', '978-9968-12-002-8', 'PRESTADO'),   -- préstamo atrasado de Diego
    ('EJ-004', '978-0-13-468599-1', 'DISPONIBLE'),
    ('EJ-005', '978-0-13-468599-1', 'DANADO'),
    ('EJ-006', '978-9968-12-002-8', 'DISPONIBLE');

-- Fechas relativas a hoy para que los casos de prueba sirvan cualquier día:
--  1. Ana: devuelto con 2 días de atraso, multa de ₡400 ya pagada.
--  2. Sofía: devuelto con 5 días de atraso, multa de ₡1000 PENDIENTE (RN-N2).
--  3. Diego: préstamo ACTIVO vencido hace 7 días (RN-02).
INSERT INTO prestamo (usuario_id, ejemplar_codigo, fecha_prestamo, fecha_limite,
                      fecha_devolucion, estado, multa, multa_pagada) VALUES
    ('101110111', 'EJ-001', CURDATE() - INTERVAL 40 DAY, CURDATE() - INTERVAL 32 DAY,
     CURDATE() - INTERVAL 30 DAY, 'DEVUELTO',  400.00, TRUE),
    ('404440444', 'EJ-002', CURDATE() - INTERVAL 30 DAY, CURDATE() - INTERVAL 22 DAY,
     CURDATE() - INTERVAL 17 DAY, 'DEVUELTO', 1000.00, FALSE),
    ('505550555', 'EJ-003', CURDATE() - INTERVAL 15 DAY, CURDATE() - INTERVAL 7 DAY,
     NULL,                        'ACTIVO',      0.00, FALSE);
