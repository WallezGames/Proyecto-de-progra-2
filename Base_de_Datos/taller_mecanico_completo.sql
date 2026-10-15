-- =====================================================================
-- Sistema de Gestion para Taller Mecanico Multiespecialidad
-- Programacion II - Universidad Mariano Galvez de Guatemala - Grupo 6
-- SCRIPT COMPLETO: estructura + datos de prueba
-- Ejecutar conectado como root (o un usuario con permiso de crear bases).
-- =====================================================================


DROP DATABASE IF EXISTS taller_mecanico;
CREATE DATABASE taller_mecanico CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE taller_mecanico;

-- ---------------------------------------------------------------------
-- Seguridad: roles y permisos por modulo
-- ---------------------------------------------------------------------
CREATE TABLE rol (
    id_rol        INT          NOT NULL AUTO_INCREMENT,
    nombre        VARCHAR(30)  NOT NULL,
    descripcion   VARCHAR(150) NULL,
    CONSTRAINT pk_rol PRIMARY KEY (id_rol),
    CONSTRAINT uq_rol_nombre UNIQUE (nombre)
) ENGINE = InnoDB;

CREATE TABLE permiso (
    id_rol  INT         NOT NULL,
    modulo  VARCHAR(30) NOT NULL,
    CONSTRAINT pk_permiso PRIMARY KEY (id_rol, modulo),
    CONSTRAINT fk_permiso_rol FOREIGN KEY (id_rol) REFERENCES rol (id_rol)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Talleres y empleados
-- La tabla taller y la tabla empleado se referencian entre si:
-- el taller tiene un jefe (empleado) y el mecanico pertenece a un taller.
-- Por eso la llave foranea del jefe se agrega al final con ALTER TABLE.
-- ---------------------------------------------------------------------
CREATE TABLE taller (
    id_taller    INT          NOT NULL AUTO_INCREMENT,
    nombre       VARCHAR(50)  NOT NULL,
    descripcion  VARCHAR(150) NOT NULL,
    id_jefe      INT          NULL,
    estado       TINYINT(1)   NOT NULL DEFAULT 1,
    CONSTRAINT pk_taller PRIMARY KEY (id_taller),
    CONSTRAINT uq_taller_nombre UNIQUE (nombre),
    CONSTRAINT uq_taller_jefe UNIQUE (id_jefe)
) ENGINE = InnoDB;

CREATE TABLE empleado (
    id_empleado         INT          NOT NULL AUTO_INCREMENT,
    codigo              VARCHAR(10)  NOT NULL,
    nombre              VARCHAR(50)  NOT NULL,
    apellido            VARCHAR(50)  NOT NULL,
    dpi                 CHAR(13)     NOT NULL,
    telefono            CHAR(8)      NOT NULL,
    direccion           VARCHAR(120) NOT NULL,
    correo              VARCHAR(80)  NOT NULL,
    fecha_contratacion  DATE         NOT NULL,
    id_rol              INT          NOT NULL,
    id_taller           INT          NULL,
    especialidad        VARCHAR(60)  NULL,
    area_trabajo        VARCHAR(60)  NULL,
    turno               VARCHAR(20)  NULL,
    nivel_acceso        VARCHAR(20)  NULL,
    hora_entrada        TIME         NULL,
    hora_salida         TIME         NULL,
    estado              TINYINT(1)   NOT NULL DEFAULT 1,
    CONSTRAINT pk_empleado PRIMARY KEY (id_empleado),
    CONSTRAINT uq_empleado_codigo UNIQUE (codigo),
    CONSTRAINT uq_empleado_dpi UNIQUE (dpi),
    CONSTRAINT fk_empleado_rol FOREIGN KEY (id_rol) REFERENCES rol (id_rol),
    CONSTRAINT fk_empleado_taller FOREIGN KEY (id_taller) REFERENCES taller (id_taller)
        ON DELETE SET NULL,
    CONSTRAINT ck_empleado_dpi CHECK (dpi REGEXP '^[0-9]{13}$'),
    CONSTRAINT ck_empleado_telefono CHECK (telefono REGEXP '^[0-9]{8}$'),
    CONSTRAINT ck_empleado_horario CHECK (hora_entrada IS NULL OR hora_salida IS NULL OR hora_salida > hora_entrada)
) ENGINE = InnoDB;

ALTER TABLE taller
    ADD CONSTRAINT fk_taller_jefe FOREIGN KEY (id_jefe) REFERENCES empleado (id_empleado)
        ON DELETE SET NULL;

CREATE TABLE usuario (
    id_usuario     INT         NOT NULL AUTO_INCREMENT,
    username       VARCHAR(30) NOT NULL,
    contrasena     CHAR(64)    NOT NULL,
    id_empleado    INT         NOT NULL,
    estado         TINYINT(1)  NOT NULL DEFAULT 1,
    ultimo_acceso  DATETIME    NULL,
    CONSTRAINT pk_usuario PRIMARY KEY (id_usuario),
    CONSTRAINT uq_usuario_username UNIQUE (username),
    CONSTRAINT uq_usuario_empleado UNIQUE (id_empleado),
    CONSTRAINT fk_usuario_empleado FOREIGN KEY (id_empleado) REFERENCES empleado (id_empleado)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Clientes y vehiculos
-- ---------------------------------------------------------------------
CREATE TABLE cliente (
    id_cliente      INT          NOT NULL AUTO_INCREMENT,
    nombre          VARCHAR(50)  NOT NULL,
    apellido        VARCHAR(50)  NOT NULL,
    dpi             CHAR(13)     NOT NULL,
    nit             VARCHAR(15)  NOT NULL,
    telefono        CHAR(8)      NOT NULL,
    direccion       VARCHAR(120) NOT NULL,
    correo          VARCHAR(80)  NOT NULL,
    fecha_registro  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado          TINYINT(1)   NOT NULL DEFAULT 1,
    CONSTRAINT pk_cliente PRIMARY KEY (id_cliente),
    CONSTRAINT uq_cliente_dpi UNIQUE (dpi),
    CONSTRAINT ck_cliente_dpi CHECK (dpi REGEXP '^[0-9]{13}$'),
    CONSTRAINT ck_cliente_telefono CHECK (telefono REGEXP '^[0-9]{8}$')
) ENGINE = InnoDB;

CREATE TABLE vehiculo (
    id_vehiculo  INT          NOT NULL AUTO_INCREMENT,
    placa        VARCHAR(10)  NOT NULL,
    marca        VARCHAR(30)  NOT NULL,
    modelo       VARCHAR(40)  NOT NULL,
    anio         SMALLINT     NOT NULL,
    color        VARCHAR(20)  NOT NULL,
    kilometraje  INT          NOT NULL DEFAULT 0,
    chasis       VARCHAR(30)  NOT NULL,
    id_cliente   INT          NOT NULL,
    estado       TINYINT(1)   NOT NULL DEFAULT 1,
    CONSTRAINT pk_vehiculo PRIMARY KEY (id_vehiculo),
    CONSTRAINT uq_vehiculo_placa UNIQUE (placa),
    CONSTRAINT uq_vehiculo_chasis UNIQUE (chasis),
    CONSTRAINT fk_vehiculo_cliente FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente),
    CONSTRAINT ck_vehiculo_anio CHECK (anio BETWEEN 1950 AND 2100),
    CONSTRAINT ck_vehiculo_km CHECK (kilometraje >= 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Bodega: categorias, proveedores y productos
-- ---------------------------------------------------------------------
CREATE TABLE categoria (
    id_categoria  INT          NOT NULL AUTO_INCREMENT,
    nombre        VARCHAR(40)  NOT NULL,
    descripcion   VARCHAR(150) NULL,
    CONSTRAINT pk_categoria PRIMARY KEY (id_categoria),
    CONSTRAINT uq_categoria_nombre UNIQUE (nombre)
) ENGINE = InnoDB;

CREATE TABLE proveedor (
    id_proveedor  INT          NOT NULL AUTO_INCREMENT,
    nombre        VARCHAR(80)  NOT NULL,
    nit           VARCHAR(15)  NOT NULL,
    telefono      CHAR(8)      NOT NULL,
    correo        VARCHAR(80)  NULL,
    direccion     VARCHAR(120) NULL,
    estado        TINYINT(1)   NOT NULL DEFAULT 1,
    CONSTRAINT pk_proveedor PRIMARY KEY (id_proveedor),
    CONSTRAINT uq_proveedor_nombre UNIQUE (nombre),
    CONSTRAINT ck_proveedor_telefono CHECK (telefono REGEXP '^[0-9]{8}$')
) ENGINE = InnoDB;

CREATE TABLE producto (
    id_producto    INT           NOT NULL AUTO_INCREMENT,
    codigo         VARCHAR(15)   NOT NULL,
    nombre         VARCHAR(80)   NOT NULL,
    marca          VARCHAR(40)   NOT NULL,
    id_categoria   INT           NOT NULL,
    id_proveedor   INT           NOT NULL,
    precio_compra  DECIMAL(10,2) NOT NULL,
    precio_venta   DECIMAL(10,2) NOT NULL,
    stock          INT           NOT NULL DEFAULT 0,
    stock_minimo   INT           NOT NULL,
    stock_maximo   INT           NOT NULL,
    estado         TINYINT(1)    NOT NULL DEFAULT 1,
    CONSTRAINT pk_producto PRIMARY KEY (id_producto),
    CONSTRAINT uq_producto_codigo UNIQUE (codigo),
    CONSTRAINT uq_producto_nombre UNIQUE (nombre),
    CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria) REFERENCES categoria (id_categoria),
    CONSTRAINT fk_producto_proveedor FOREIGN KEY (id_proveedor) REFERENCES proveedor (id_proveedor),
    CONSTRAINT ck_producto_stock CHECK (stock >= 0),
    CONSTRAINT ck_producto_minimo CHECK (stock_minimo >= 0),
    CONSTRAINT ck_producto_rango CHECK (stock_minimo <= stock_maximo),
    CONSTRAINT ck_producto_precios CHECK (precio_compra >= 0 AND precio_venta >= precio_compra)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Catalogo de servicios que ofrece cada taller
-- ---------------------------------------------------------------------
CREATE TABLE servicio (
    id_servicio  INT           NOT NULL AUTO_INCREMENT,
    nombre       VARCHAR(80)   NOT NULL,
    descripcion  VARCHAR(150)  NULL,
    id_taller    INT           NOT NULL,
    precio       DECIMAL(10,2) NOT NULL,
    estado       TINYINT(1)    NOT NULL DEFAULT 1,
    CONSTRAINT pk_servicio PRIMARY KEY (id_servicio),
    CONSTRAINT uq_servicio_taller UNIQUE (nombre, id_taller),
    CONSTRAINT fk_servicio_taller FOREIGN KEY (id_taller) REFERENCES taller (id_taller),
    CONSTRAINT ck_servicio_precio CHECK (precio > 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Proceso maestro-detalle 1: orden de trabajo y sus servicios
-- ---------------------------------------------------------------------
CREATE TABLE orden_trabajo (
    id_orden             INT          NOT NULL AUTO_INCREMENT,
    numero               VARCHAR(15)  NOT NULL,
    id_vehiculo          INT          NOT NULL,
    id_taller            INT          NOT NULL,
    id_registra          INT          NOT NULL,
    fecha_ingreso        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_salida         DATETIME     NULL,
    descripcion          VARCHAR(200) NOT NULL,
    trabajos_realizados  TEXT         NULL,
    estado               VARCHAR(15)  NOT NULL DEFAULT 'Pendiente',
    CONSTRAINT pk_orden_trabajo PRIMARY KEY (id_orden),
    CONSTRAINT uq_orden_numero UNIQUE (numero),
    CONSTRAINT fk_orden_vehiculo FOREIGN KEY (id_vehiculo) REFERENCES vehiculo (id_vehiculo),
    CONSTRAINT fk_orden_taller FOREIGN KEY (id_taller) REFERENCES taller (id_taller),
    CONSTRAINT fk_orden_registra FOREIGN KEY (id_registra) REFERENCES empleado (id_empleado),
    CONSTRAINT ck_orden_estado CHECK (estado IN ('Pendiente', 'En proceso', 'Terminado', 'Entregado', 'Cancelado')),
    CONSTRAINT ck_orden_fechas CHECK (fecha_salida IS NULL OR fecha_salida >= fecha_ingreso)
) ENGINE = InnoDB;

CREATE TABLE detalle_orden (
    id_detalle       INT           NOT NULL AUTO_INCREMENT,
    id_orden         INT           NOT NULL,
    id_servicio      INT           NOT NULL,
    cantidad         INT           NOT NULL DEFAULT 1,
    precio_unitario  DECIMAL(10,2) NOT NULL,
    CONSTRAINT pk_detalle_orden PRIMARY KEY (id_detalle),
    CONSTRAINT uq_detalle_orden UNIQUE (id_orden, id_servicio),
    CONSTRAINT fk_detalle_orden_orden FOREIGN KEY (id_orden) REFERENCES orden_trabajo (id_orden)
        ON DELETE CASCADE,
    CONSTRAINT fk_detalle_orden_servicio FOREIGN KEY (id_servicio) REFERENCES servicio (id_servicio),
    CONSTRAINT ck_detalle_orden_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_detalle_orden_precio CHECK (precio_unitario >= 0)
) ENGINE = InnoDB;

CREATE TABLE orden_mecanico (
    id_orden          INT      NOT NULL,
    id_mecanico       INT      NOT NULL,
    fecha_asignacion  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_orden_mecanico PRIMARY KEY (id_orden, id_mecanico),
    CONSTRAINT fk_orden_mecanico_orden FOREIGN KEY (id_orden) REFERENCES orden_trabajo (id_orden)
        ON DELETE CASCADE,
    CONSTRAINT fk_orden_mecanico_empleado FOREIGN KEY (id_mecanico) REFERENCES empleado (id_empleado)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Proceso maestro-detalle 2: requerimiento de bodega y sus productos
-- La regla critica del negocio tambien se valida aqui: un requerimiento
-- no puede quedar Procesado si no tiene quien lo autorizo.
-- ---------------------------------------------------------------------
CREATE TABLE requerimiento (
    id_requerimiento    INT          NOT NULL AUTO_INCREMENT,
    numero              VARCHAR(15)  NOT NULL,
    id_orden            INT          NOT NULL,
    id_solicitante      INT          NOT NULL,
    id_autoriza         INT          NULL,
    id_procesa          INT          NULL,
    fecha_solicitud     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_autorizacion  DATETIME     NULL,
    fecha_proceso       DATETIME     NULL,
    estado              VARCHAR(15)  NOT NULL DEFAULT 'Solicitado',
    observacion         VARCHAR(200) NULL,
    CONSTRAINT pk_requerimiento PRIMARY KEY (id_requerimiento),
    CONSTRAINT uq_requerimiento_numero UNIQUE (numero),
    CONSTRAINT fk_requerimiento_orden FOREIGN KEY (id_orden) REFERENCES orden_trabajo (id_orden),
    CONSTRAINT fk_requerimiento_solicitante FOREIGN KEY (id_solicitante) REFERENCES empleado (id_empleado),
    CONSTRAINT fk_requerimiento_autoriza FOREIGN KEY (id_autoriza) REFERENCES empleado (id_empleado),
    CONSTRAINT fk_requerimiento_procesa FOREIGN KEY (id_procesa) REFERENCES empleado (id_empleado),
    CONSTRAINT ck_requerimiento_estado CHECK (estado IN ('Solicitado', 'Autorizado', 'Rechazado', 'Procesado')),
    CONSTRAINT ck_requerimiento_autorizacion CHECK (estado = 'Solicitado' OR id_autoriza IS NOT NULL),
    CONSTRAINT ck_requerimiento_proceso CHECK (estado <> 'Procesado' OR id_procesa IS NOT NULL)
) ENGINE = InnoDB;

CREATE TABLE detalle_requerimiento (
    id_detalle        INT           NOT NULL AUTO_INCREMENT,
    id_requerimiento  INT           NOT NULL,
    id_producto       INT           NOT NULL,
    cantidad          INT           NOT NULL,
    precio_unitario   DECIMAL(10,2) NOT NULL,
    CONSTRAINT pk_detalle_requerimiento PRIMARY KEY (id_detalle),
    CONSTRAINT uq_detalle_requerimiento UNIQUE (id_requerimiento, id_producto),
    CONSTRAINT fk_detalle_req_requerimiento FOREIGN KEY (id_requerimiento) REFERENCES requerimiento (id_requerimiento)
        ON DELETE CASCADE,
    CONSTRAINT fk_detalle_req_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto),
    CONSTRAINT ck_detalle_req_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_detalle_req_precio CHECK (precio_unitario >= 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Auditoria basica: registro de las operaciones principales
-- ---------------------------------------------------------------------
CREATE TABLE bitacora (
    id_bitacora  INT          NOT NULL AUTO_INCREMENT,
    fecha_hora   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_usuario   INT          NULL,
    modulo       VARCHAR(30)  NOT NULL,
    accion       VARCHAR(20)  NOT NULL,
    descripcion  VARCHAR(255) NOT NULL,
    CONSTRAINT pk_bitacora PRIMARY KEY (id_bitacora),
    CONSTRAINT fk_bitacora_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
        ON DELETE SET NULL
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Indices para las busquedas y reportes mas frecuentes
-- ---------------------------------------------------------------------
CREATE INDEX ix_cliente_nombre ON cliente (apellido, nombre);
CREATE INDEX ix_empleado_nombre ON empleado (apellido, nombre);
CREATE INDEX ix_orden_estado ON orden_trabajo (estado);
CREATE INDEX ix_orden_fecha ON orden_trabajo (fecha_ingreso);
CREATE INDEX ix_requerimiento_estado ON requerimiento (estado);
CREATE INDEX ix_requerimiento_fecha ON requerimiento (fecha_solicitud);
CREATE INDEX ix_bitacora_fecha ON bitacora (fecha_hora);

-- ---------------------------------------------------------------------
-- Vista con el total de cada orden. El total no se guarda en una
-- columna porque se puede calcular a partir de los detalles; asi se
-- evita tener el mismo dato guardado dos veces.
-- ---------------------------------------------------------------------
CREATE VIEW vista_total_orden AS
SELECT o.id_orden,
       COALESCE((SELECT SUM(d.cantidad * d.precio_unitario)
                   FROM detalle_orden d
                  WHERE d.id_orden = o.id_orden), 0) AS total_servicios,
       COALESCE((SELECT SUM(dr.cantidad * dr.precio_unitario)
                   FROM requerimiento r
                   JOIN detalle_requerimiento dr ON dr.id_requerimiento = r.id_requerimiento
                  WHERE r.id_orden = o.id_orden
                    AND r.estado = 'Procesado'), 0) AS total_repuestos
  FROM orden_trabajo o;



USE taller_mecanico;

-- ---------------------------------------------------------------------
-- Roles y permisos
-- ---------------------------------------------------------------------
INSERT INTO rol (id_rol, nombre, descripcion) VALUES
(1, 'Administrador', 'Control total del sistema, usuarios, permisos y reportes'),
(2, 'Recepcionista', 'Registra clientes, vehiculos y genera las ordenes de trabajo'),
(3, 'Jefe de Taller', 'Asigna mecanicos y autoriza los requerimientos de su taller'),
(4, 'Mecanico', 'Ejecuta los trabajos y solicita productos a la bodega'),
(5, 'Bodeguero', 'Procesa los requerimientos autorizados y controla el inventario');

INSERT INTO permiso (id_rol, modulo) VALUES
(1, 'Resumen'), (1, 'Ordenes'), (1, 'Clientes'), (1, 'Vehiculos'), (1, 'Empleados'),
(1, 'Talleres'), (1, 'Inventario'), (1, 'Requerimientos'), (1, 'Catalogos'), (1, 'Usuarios'), (1, 'Reportes'),
(2, 'Resumen'), (2, 'Ordenes'), (2, 'Clientes'), (2, 'Vehiculos'), (2, 'Reportes'),
(3, 'Resumen'), (3, 'Ordenes'), (3, 'Empleados'), (3, 'Talleres'), (3, 'Requerimientos'), (3, 'Reportes'),
(4, 'Resumen'), (4, 'Ordenes'), (4, 'Requerimientos'),
(5, 'Resumen'), (5, 'Inventario'), (5, 'Requerimientos'), (5, 'Catalogos'), (5, 'Reportes');

-- ---------------------------------------------------------------------
-- Talleres (el jefe se asigna despues de registrar a los empleados)
-- ---------------------------------------------------------------------
INSERT INTO taller (id_taller, nombre, descripcion) VALUES
(1, 'Mecanica general', 'Reparacion de motor, transmision y mantenimiento preventivo'),
(2, 'Electromecanica', 'Diagnostico y reparacion de sistemas electricos y electronicos'),
(3, 'Rodaje', 'Alineacion, balanceo, frenos y reparacion de suspension'),
(4, 'Llantera', 'Montaje, rotacion y reparacion de llantas'),
(5, 'Pintura y enderezado', 'Enderezado de carroceria y aplicacion de pintura'),
(6, 'Soldadura', 'Trabajos de soldadura en estructura, chasis y escape');

-- ---------------------------------------------------------------------
-- Empleados
-- ---------------------------------------------------------------------
INSERT INTO empleado (id_empleado, codigo, nombre, apellido, dpi, telefono, direccion, correo,
                      fecha_contratacion, id_rol, id_taller, especialidad, area_trabajo, turno,
                      nivel_acceso, hora_entrada, hora_salida) VALUES
(1,  'EMP-001', 'Sofia', 'Herrera', '2541896320101', '55214478', '3a calle 4-15 zona 1, Escuintla', 'sherrera@tallercentral.com', '2022-02-01', 1, NULL, NULL, NULL, NULL, 'Total', '07:30:00', '16:30:00'),
(2,  'EMP-002', 'Karla', 'Morales', '1985472630102', '42318890', '12 avenida 2-30 zona 3, Escuintla', 'kmorales@tallercentral.com', '2024-03-15', 2, NULL, NULL, NULL, 'Matutino', NULL, '07:00:00', '15:00:00'),
(3,  'EMP-003', 'Mario', 'Solis', '1745896320103', '55478812', '5a avenida 8-22 zona 2, Escuintla', 'msolis@tallercentral.com', '2020-06-01', 3, NULL, NULL, 'Mecanica general', NULL, NULL, '07:30:00', '16:30:00'),
(4,  'EMP-004', 'Hugo', 'Perez', '2036985470104', '41125536', '2a calle 1-40 zona 4, Escuintla', 'hperez@tallercentral.com', '2021-01-10', 3, NULL, NULL, 'Electromecanica', NULL, NULL, '07:30:00', '16:30:00'),
(5,  'EMP-005', 'Lucia', 'Ramirez', '2298745630105', '57781145', '6a avenida 3-18 zona 1, Escuintla', 'lramirez@tallercentral.com', '2023-04-03', 3, NULL, NULL, 'Rodaje', NULL, NULL, '07:30:00', '16:30:00'),
(6,  'EMP-006', 'Diego', 'Castillo', '1874569320106', '44582210', '8a calle 5-60 zona 5, Escuintla', 'dcastillo@tallercentral.com', '2023-05-02', 3, NULL, NULL, 'Llantera', NULL, NULL, '07:30:00', '16:30:00'),
(7,  'EMP-007', 'Andrea', 'Lopez', '2145896370107', '56214478', '1a avenida 9-12 zona 2, Escuintla', 'alopez@tallercentral.com', '2024-01-08', 3, NULL, NULL, 'Pintura y enderezado', NULL, NULL, '07:30:00', '16:30:00'),
(8,  'EMP-008', 'Erick', 'Gomez', '1965874120108', '42258896', '4a calle 6-33 zona 3, Escuintla', 'egomez@tallercentral.com', '2022-08-16', 3, NULL, NULL, 'Soldadura', NULL, NULL, '07:30:00', '16:30:00'),
(9,  'EMP-009', 'Byron', 'Chavez', '2587419630109', '55114422', '7a avenida 2-15 zona 1, Escuintla', 'bchavez@tallercentral.com', '2023-02-20', 4, 1, 'Motor y transmision', NULL, NULL, NULL, '08:00:00', '17:00:00'),
(10, 'EMP-010', 'Rodrigo', 'Xoyon', '1478529630110', '41778852', '3a calle 7-28 zona 4, Escuintla', 'rxoyon@tallercentral.com', '2024-06-03', 4, 3, 'Frenos y suspension', NULL, NULL, NULL, '08:00:00', '17:30:00'),
(11, 'EMP-011', 'Elmer', 'Tzoc', '2369854710111', '57446623', '9a avenida 4-50 zona 2, Escuintla', 'etzoc@tallercentral.com', '2025-01-13', 4, 2, 'Sistemas electricos', NULL, NULL, NULL, '07:45:00', '16:45:00'),
(12, 'EMP-012', 'Oscar', 'Ramirez', '1852963740112', '44119977', '5a calle 3-21 zona 3, Escuintla', 'oramirez@tallercentral.com', '2021-09-06', 4, 1, 'Afinamiento', NULL, NULL, NULL, '08:00:00', '17:00:00'),
(13, 'EMP-013', 'Julio', 'Batz', '2741852960113', '56338841', '2a avenida 8-45 zona 5, Escuintla', 'jbatz@tallercentral.com', '2024-03-11', 4, 2, 'Aire acondicionado', NULL, NULL, NULL, '08:00:00', '17:00:00'),
(14, 'EMP-014', 'Walter', 'Ixcoy', '1596374820114', '42887733', '10a calle 1-19 zona 1, Escuintla', 'wixcoy@tallercentral.com', '2025-02-17', 4, 4, 'Balanceo y alineacion', NULL, NULL, NULL, '08:00:00', '17:00:00'),
(15, 'EMP-015', 'Ana', 'Velasquez', '2085214790115', '55663399', '6a calle 2-08 zona 2, Escuintla', 'avelasquez@tallercentral.com', '2022-04-04', 5, NULL, NULL, 'Bodega central', NULL, NULL, '07:00:00', '16:00:00'),
(16, 'EMP-016', 'Pablo', 'Marroquin', '1963258740116', '41556688', '4a avenida 7-71 zona 4, Escuintla', 'pmarroquin@tallercentral.com', '2025-05-05', 5, NULL, NULL, 'Bodega central', NULL, NULL, '08:00:00', '17:00:00'),
(17, 'EMP-017', 'Kevin', 'Coc', '2478963150117', '57002244', '11 calle 3-14 zona 3, Escuintla', 'kcoc@tallercentral.com', '2024-09-02', 4, 5, 'Pintura automotriz', NULL, NULL, NULL, '08:00:00', '17:00:00'),
(18, 'EMP-018', 'Luis', 'Sic', '1847596230118', '44779911', '7a calle 9-02 zona 1, Escuintla', 'lsic@tallercentral.com', '2025-07-14', 4, 6, 'Soldadura MIG y TIG', NULL, NULL, NULL, '08:00:00', '17:00:00');

UPDATE taller SET id_jefe = 3 WHERE id_taller = 1;
UPDATE taller SET id_jefe = 4 WHERE id_taller = 2;
UPDATE taller SET id_jefe = 5 WHERE id_taller = 3;
UPDATE taller SET id_jefe = 6 WHERE id_taller = 4;
UPDATE taller SET id_jefe = 7 WHERE id_taller = 5;
UPDATE taller SET id_jefe = 8 WHERE id_taller = 6;

-- ---------------------------------------------------------------------
-- Usuarios del sistema
-- ---------------------------------------------------------------------
INSERT INTO usuario (id_usuario, username, contrasena, id_empleado) VALUES
(1, 'admin', SHA2('admin123', 256), 1),
(2, 'recepcion', SHA2('recepcion123', 256), 2),
(3, 'jefe', SHA2('jefe123', 256), 3),
(4, 'mecanico', SHA2('mecanico123', 256), 9),
(5, 'bodega', SHA2('bodega123', 256), 15);

-- ---------------------------------------------------------------------
-- Clientes y vehiculos
-- ---------------------------------------------------------------------
INSERT INTO cliente (id_cliente, nombre, apellido, dpi, nit, telefono, direccion, correo, fecha_registro) VALUES
(1, 'Carlos', 'Menendez', '2145874960201', '1548796-2', '55889977', '5a calle 2-10 zona 1, Escuintla', 'cmenendez@correo.com', '2026-07-20 09:15:00'),
(2, 'Maria', 'Gonzalez', '1874596320202', '2896541-K', '42115588', '8a avenida 4-25 zona 3, Escuintla', 'mgonzalez@correo.com', '2026-07-28 10:40:00'),
(3, 'Jorge', 'Alvarado', '2036985470203', '3654789-1', '57744112', '3a calle 9-18 zona 2, Escuintla', 'jalvarado@correo.com', '2026-08-03 08:05:00'),
(4, 'Silvia', 'Reyes', '1963254870204', '4789561-3', '44227799', '11 avenida 1-55 zona 5, Escuintla', 'sreyes@correo.com', '2026-08-10 14:20:00'),
(5, 'Rodolfo', 'Ixtamalic', '2587413690205', '5987412-8', '56112288', '7a calle 6-40 zona 4, Escuintla', 'rixtamalic@correo.com', '2026-08-18 11:00:00'),
(6, 'Patricia', 'Cifuentes', '2369874150206', '6321458-7', '55447788', '2a avenida 5-11 zona 1, Escuintla', 'pcifuentes@correo.com', '2026-08-25 09:30:00'),
(7, 'Esteban', 'Morataya', '1745823690207', '7412369-5', '41889922', '9a calle 1-77 zona 2, Escuintla', 'emorataya@correo.com', '2026-09-02 15:10:00'),
(8, 'Gabriela', 'Pineda', '2698745130208', '8523697-4', '57335566', '14 avenida 6-03 zona 3, Escuintla', 'gpineda@correo.com', '2026-09-14 08:45:00');

INSERT INTO vehiculo (id_vehiculo, placa, marca, modelo, anio, color, kilometraje, chasis, id_cliente) VALUES
(1,  'P-482GHK', 'Toyota', 'Corolla', 2016, 'Blanco', 98500, 'JTDBR32E160123456', 1),
(2,  'P-071BQR', 'Honda', 'CR-V', 2019, 'Gris', 62300, '5J6RW2H89KL045821', 2),
(3,  'P-935LMD', 'Nissan', 'Frontier', 2014, 'Negro', 154200, '1N6AD0EV5EN712903', 3),
(4,  'P-664TRV', 'Mazda', 'Mazda 3', 2018, 'Rojo', 74800, '3MZBN1V37JM254617', 4),
(5,  'P-218WKS', 'Hyundai', 'Elantra', 2020, 'Azul', 41900, '5NPD84LF2LH556214', 5),
(6,  'P-509CFN', 'Isuzu', 'D-Max', 2013, 'Verde', 187600, 'MPATFS85JDT008471', 1),
(7,  'P-347JNX', 'Kia', 'Sportage', 2021, 'Plateado', 38700, 'KNDPM3AC5M7891234', 6),
(8,  'P-126DPL', 'Toyota', 'Hilux', 2017, 'Blanco', 132500, 'MR0FZ29G901234567', 7),
(9,  'P-853RTB', 'Suzuki', 'Swift', 2022, 'Rojo', 21400, 'MA3ZC83S5NA123456', 8),
(10, 'P-790KLS', 'Chevrolet', 'Aveo', 2015, 'Negro', 118900, 'KL1TD5DE5FB234567', 6);

-- ---------------------------------------------------------------------
-- Categorias, proveedores y productos
-- ---------------------------------------------------------------------
INSERT INTO categoria (id_categoria, nombre, descripcion) VALUES
(1, 'Lubricantes', 'Aceites de motor, transmision y grasas'),
(2, 'Filtros', 'Filtros de aceite, aire y combustible'),
(3, 'Frenos', 'Pastillas, discos y liquidos de freno'),
(4, 'Suspension', 'Amortiguadores, rotulas y bujes'),
(5, 'Electrico', 'Baterias, bujias y componentes electricos'),
(6, 'Llantas', 'Llantas y accesorios de rodaje'),
(7, 'Climatizacion', 'Refrigerantes y gas para aire acondicionado'),
(8, 'Motor', 'Bandas, empaques y repuestos de motor');

INSERT INTO proveedor (id_proveedor, nombre, nit, telefono, correo, direccion) VALUES
(1, 'Lubricantes de Guatemala', '2541789-6', '23614477', 'ventas@lubriguate.com', 'Calzada Roosevelt 22-43 zona 11, Guatemala'),
(2, 'Repuestos del Sur', '3698521-4', '78891122', 'pedidos@repuestosdelsur.com', '4a calle 3-50 zona 1, Escuintla'),
(3, 'Frenos y Partes S.A.', '1478523-9', '22547788', 'info@frenosypartes.com', 'Avenida Petapa 45-12 zona 12, Guatemala'),
(4, 'Suspensiones Centroamerica', '9632587-1', '24418899', 'ventas@suspensionesca.com', 'Anillo Periferico 17-36 zona 11, Guatemala'),
(5, 'Electro Repuestos', '7539514-2', '78883344', 'electro@electrorepuestos.com', '6a avenida 2-18 zona 2, Escuintla'),
(6, 'Llantas del Pacifico', '8524567-3', '78895566', 'ventas@llantaspacifico.com', 'Km 58 carretera al Pacifico, Escuintla'),
(7, 'Climatizacion Total', '4561237-K', '22336655', 'servicio@climatotal.com', '15 avenida 8-20 zona 13, Guatemala');

INSERT INTO producto (id_producto, codigo, nombre, marca, id_categoria, id_proveedor, precio_compra, precio_venta,
                      stock, stock_minimo, stock_maximo) VALUES
(1,  'LUB-001', 'Aceite de motor 20W50', 'Castrol', 1, 1, 95.00, 130.00, 44, 20, 120),
(2,  'FIL-001', 'Filtro de aceite', 'Fram', 2, 2, 65.00, 90.00, 11, 15, 80),
(3,  'FIL-002', 'Filtro de aire', 'Mann', 2, 2, 88.50, 120.00, 26, 10, 60),
(4,  'FRE-001', 'Pastillas de freno delanteras', 'Brembo', 3, 3, 385.00, 520.00, 6, 10, 40),
(5,  'SUS-001', 'Amortiguador delantero', 'Monroe', 4, 4, 640.00, 860.00, 12, 6, 30),
(6,  'ELE-001', 'Bujia de encendido', 'NGK', 5, 5, 42.00, 60.00, 52, 24, 150),
(7,  'ELE-002', 'Bateria 12V 700A', 'Bosch', 5, 5, 985.00, 1290.00, 4, 6, 25),
(8,  'LLA-001', 'Llanta 195/65 R15', 'Michelin', 6, 6, 720.00, 950.00, 18, 12, 60),
(9,  'FRE-002', 'Liquido de frenos DOT4', 'Wagner', 3, 3, 75.00, 100.00, 30, 12, 70),
(10, 'REF-001', 'Refrigerante verde 1 galon', 'Prestone', 7, 1, 110.00, 150.00, 18, 10, 50),
(11, 'MOT-001', 'Banda de distribucion', 'Gates', 8, 2, 430.00, 580.00, 4, 8, 30),
(12, 'REF-002', 'Gas refrigerante R134a', 'Dupont', 7, 7, 260.00, 350.00, 14, 8, 40),
(13, 'LUB-002', 'Aceite de transmision ATF', 'Valvoline', 1, 1, 120.00, 165.00, 22, 10, 60),
(14, 'FRE-003', 'Pastillas de freno traseras', 'Brembo', 3, 3, 340.00, 460.00, 15, 8, 40);

-- ---------------------------------------------------------------------
-- Catalogo de servicios por taller
-- ---------------------------------------------------------------------
INSERT INTO servicio (id_servicio, nombre, descripcion, id_taller, precio) VALUES
(1,  'Cambio de aceite y filtro', 'Cambio de aceite de motor y filtro de aceite', 1, 150.00),
(2,  'Afinamiento completo', 'Cambio de bujias, filtros y revision general del motor', 1, 480.00),
(3,  'Cambio de banda de distribucion', 'Reemplazo de banda y tensores', 1, 650.00),
(4,  'Diagnostico de motor', 'Revision de fallas de motor con escaner', 1, 200.00),
(5,  'Diagnostico electrico', 'Revision del sistema electrico y electronico', 2, 275.00),
(6,  'Reparacion de aire acondicionado', 'Revision, carga de gas y reparacion de fugas', 2, 520.00),
(7,  'Cambio de bateria', 'Instalacion y prueba de bateria nueva', 2, 75.00),
(8,  'Revision de alternador', 'Prueba de carga y reparacion de alternador', 2, 250.00),
(9,  'Alineacion', 'Alineacion computarizada de direccion', 3, 150.00),
(10, 'Cambio de pastillas de freno', 'Reemplazo de pastillas y revision de discos', 3, 200.00),
(11, 'Cambio de amortiguadores', 'Reemplazo de amortiguadores delanteros o traseros', 3, 400.00),
(12, 'Balanceo de llantas', 'Balanceo de las cuatro llantas', 4, 120.00),
(13, 'Montaje de llanta', 'Desmontaje y montaje por llanta', 4, 60.00),
(14, 'Reparacion de pinchazo', 'Reparacion de llanta ponchada', 4, 35.00),
(15, 'Enderezado de panel', 'Enderezado de un panel de carroceria', 5, 600.00),
(16, 'Pintura de panel', 'Preparacion y pintura de un panel', 5, 850.00),
(17, 'Soldadura de escape', 'Reparacion del sistema de escape', 6, 300.00),
(18, 'Refuerzo de chasis', 'Soldadura y refuerzo de chasis', 6, 900.00);

-- ---------------------------------------------------------------------
-- Ordenes de trabajo (historicas y activas)
-- ---------------------------------------------------------------------
INSERT INTO orden_trabajo (id_orden, numero, id_vehiculo, id_taller, id_registra, fecha_ingreso, fecha_salida,
                           descripcion, trabajos_realizados, estado) VALUES
(1,  'OT-2026-0001', 1, 1, 2, '2026-08-11 08:20:00', '2026-08-11 15:40:00', 'Mantenimiento de los 95,000 km', 'Cambio de aceite, filtro y bujias', 'Entregado'),
(2,  'OT-2026-0002', 3, 3, 2, '2026-08-13 09:10:00', '2026-08-14 16:00:00', 'Ruido al frenar', 'Cambio de pastillas delanteras y alineacion', 'Entregado'),
(3,  'OT-2026-0003', 4, 2, 2, '2026-08-19 10:00:00', '2026-08-20 12:30:00', 'No arranca por las mananas', 'Cambio de bateria y revision de alternador', 'Entregado'),
(4,  'OT-2026-0004', 7, 4, 2, '2026-08-26 08:45:00', '2026-08-26 13:15:00', 'Llanta ponchada y vibracion', 'Reparacion de pinchazo y balanceo', 'Entregado'),
(5,  'OT-2026-0005', 8, 6, 2, '2026-09-02 07:50:00', '2026-09-04 17:00:00', 'Escape roto', 'Soldadura del tubo de escape', 'Entregado'),
(6,  'OT-2026-0006', 2, 1, 2, '2026-09-08 08:30:00', '2026-09-09 16:20:00', 'Cambio de banda de distribucion por kilometraje', 'Cambio de banda de distribucion y aceite', 'Entregado'),
(7,  'OT-2026-0007', 9, 5, 2, '2026-09-15 09:00:00', '2026-09-18 16:45:00', 'Golpe en puerta trasera', 'Enderezado y pintura de puerta', 'Entregado'),
(8,  'OT-2026-0008', 10, 3, 2, '2026-09-30 08:15:00', '2026-10-01 15:30:00', 'Suspension golpea en tumulos', 'Cambio de amortiguadores delanteros y alineacion', 'Terminado'),
(9,  'OT-2026-0009', 6, 1, 2, '2026-10-02 10:20:00', '2026-10-03 11:00:00', 'Afinamiento', 'Afinamiento completo', 'Terminado'),
(10, 'OT-2026-0010', 1, 1, 2, '2026-10-05 08:10:00', NULL, 'Cambio de aceite y filtro', 'Drenado de aceite', 'En proceso'),
(11, 'OT-2026-0011', 2, 3, 2, '2026-10-05 09:30:00', NULL, 'Frenos delanteros', NULL, 'En proceso'),
(12, 'OT-2026-0012', 3, 2, 2, '2026-10-06 08:00:00', NULL, 'Diagnostico electrico, luces intermitentes', NULL, 'Pendiente'),
(13, 'OT-2026-0013', 4, 1, 2, '2026-10-06 10:15:00', NULL, 'Afinamiento completo', NULL, 'Pendiente'),
(14, 'OT-2026-0014', 5, 2, 2, '2026-10-06 14:00:00', NULL, 'El aire acondicionado no enfria', 'Revision de fugas', 'En proceso'),
(15, 'OT-2026-0015', 6, 3, 2, '2026-10-07 08:05:00', NULL, 'Cambio de amortiguadores', NULL, 'Pendiente');

INSERT INTO detalle_orden (id_orden, id_servicio, cantidad, precio_unitario) VALUES
(1, 1, 1, 150.00), (1, 2, 1, 480.00),
(2, 10, 1, 200.00), (2, 9, 1, 150.00),
(3, 7, 1, 75.00), (3, 8, 1, 250.00),
(4, 14, 1, 35.00), (4, 12, 1, 120.00),
(5, 17, 1, 300.00),
(6, 3, 1, 650.00), (6, 1, 1, 150.00),
(7, 15, 1, 600.00), (7, 16, 1, 850.00),
(8, 11, 1, 400.00), (8, 9, 1, 150.00),
(9, 2, 1, 480.00),
(10, 1, 1, 150.00),
(11, 10, 1, 200.00),
(12, 5, 1, 275.00),
(13, 2, 1, 480.00),
(14, 6, 1, 520.00),
(15, 11, 1, 400.00);

INSERT INTO orden_mecanico (id_orden, id_mecanico, fecha_asignacion) VALUES
(1, 9, '2026-08-11 08:30:00'),
(2, 10, '2026-08-13 09:20:00'),
(3, 11, '2026-08-19 10:10:00'),
(4, 14, '2026-08-26 09:00:00'),
(5, 18, '2026-09-02 08:00:00'),
(6, 12, '2026-09-08 08:40:00'), (6, 9, '2026-09-08 08:40:00'),
(7, 17, '2026-09-15 09:10:00'),
(8, 10, '2026-09-30 08:20:00'),
(9, 12, '2026-10-02 10:30:00'),
(10, 9, '2026-10-05 08:15:00'),
(11, 10, '2026-10-05 09:40:00'),
(12, 11, '2026-10-06 08:10:00'),
(13, 12, '2026-10-06 10:20:00'),
(14, 13, '2026-10-06 14:10:00'),
(15, 10, '2026-10-07 08:10:00');

-- ---------------------------------------------------------------------
-- Requerimientos de bodega (maestro) y sus productos (detalle)
-- ---------------------------------------------------------------------
INSERT INTO requerimiento (id_requerimiento, numero, id_orden, id_solicitante, id_autoriza, id_procesa,
                           fecha_solicitud, fecha_autorizacion, fecha_proceso, estado, observacion) VALUES
(1,  'REQ-2026-0001', 1, 9, 3, 15, '2026-08-11 09:00:00', '2026-08-11 09:15:00', '2026-08-11 09:30:00', 'Procesado', NULL),
(2,  'REQ-2026-0002', 2, 10, 5, 15, '2026-08-13 10:00:00', '2026-08-13 10:20:00', '2026-08-13 10:40:00', 'Procesado', NULL),
(3,  'REQ-2026-0003', 3, 11, 4, 16, '2026-08-19 10:30:00', '2026-08-19 10:45:00', '2026-08-19 11:00:00', 'Procesado', NULL),
(4,  'REQ-2026-0004', 6, 12, 3, 15, '2026-09-08 09:00:00', '2026-09-08 09:10:00', '2026-09-08 09:30:00', 'Procesado', NULL),
(5,  'REQ-2026-0005', 8, 10, 5, 16, '2026-09-30 09:00:00', '2026-09-30 09:25:00', '2026-09-30 09:50:00', 'Procesado', NULL),
(6,  'REQ-2026-0006', 9, 12, 3, 15, '2026-10-02 11:00:00', '2026-10-02 11:10:00', '2026-10-02 11:30:00', 'Procesado', NULL),
(7,  'REQ-2026-0007', 10, 9, 3, 15, '2026-10-05 08:40:00', '2026-10-05 08:55:00', '2026-10-05 09:10:00', 'Procesado', NULL),
(8,  'REQ-2026-0008', 11, 10, 5, NULL, '2026-10-05 10:00:00', '2026-10-05 10:30:00', NULL, 'Autorizado', NULL),
(9,  'REQ-2026-0009', 12, 11, NULL, NULL, '2026-10-06 09:00:00', NULL, NULL, 'Solicitado', 'Se sospecha falla en la bateria'),
(10, 'REQ-2026-0010', 14, 13, 4, 16, '2026-10-06 15:00:00', '2026-10-06 15:20:00', '2026-10-06 15:40:00', 'Procesado', NULL),
(11, 'REQ-2026-0011', 15, 10, 5, NULL, '2026-10-07 08:30:00', '2026-10-07 08:45:00', NULL, 'Rechazado', 'Se solicito cantidad mayor a la necesaria');

INSERT INTO detalle_requerimiento (id_requerimiento, id_producto, cantidad, precio_unitario) VALUES
(1, 1, 4, 130.00), (1, 2, 1, 90.00), (1, 6, 4, 60.00),
(2, 4, 1, 520.00), (2, 9, 1, 100.00),
(3, 7, 1, 1290.00),
(4, 11, 1, 580.00), (4, 1, 4, 130.00), (4, 2, 1, 90.00),
(5, 5, 2, 860.00),
(6, 6, 4, 60.00), (6, 3, 1, 120.00), (6, 2, 1, 90.00),
(7, 1, 4, 130.00), (7, 2, 1, 90.00),
(8, 4, 1, 520.00), (8, 9, 1, 100.00),
(9, 7, 1, 1290.00),
(10, 12, 2, 350.00),
(11, 5, 4, 860.00);

-- ---------------------------------------------------------------------
-- Algunas entradas de bitacora de ejemplo
-- ---------------------------------------------------------------------
INSERT INTO bitacora (fecha_hora, id_usuario, modulo, accion, descripcion) VALUES
('2026-10-05 08:10:00', 2, 'Ordenes', 'Alta', 'Se genero la orden OT-2026-0010'),
('2026-10-05 08:55:00', 3, 'Requerimientos', 'Autorizar', 'Se autorizo el requerimiento REQ-2026-0007'),
('2026-10-05 09:10:00', 5, 'Requerimientos', 'Procesar', 'Se proceso la salida del requerimiento REQ-2026-0007'),
('2026-10-06 08:00:00', 2, 'Ordenes', 'Alta', 'Se genero la orden OT-2026-0012'),
('2026-10-07 08:45:00', 3, 'Requerimientos', 'Rechazar', 'Se rechazo el requerimiento REQ-2026-0011');
