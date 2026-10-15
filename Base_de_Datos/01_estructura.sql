-- =====================================================================
-- Sistema de Gestion para Taller Mecanico Multiespecialidad
-- Programacion II - Universidad Mariano Galvez de Guatemala - Grupo 6
-- Script 01: creacion de la base de datos, tablas, relaciones,
-- restricciones, indices y vistas.
-- Compatible con MySQL 8 o superior y con MariaDB 10.4 (XAMPP).
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
