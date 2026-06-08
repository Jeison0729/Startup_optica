-- 0. CREACIÓN DE LA BASE DE DATOS
-- DROP SCHEMA IF EXISTS public CASCADE;
-- CREATE SCHEMA public;
 
-- Limpia cualquier transacción abortada antes de iniciar
-- ROLLBACK;

-- ==========================================================
-- LIMPIEZA TOTAL (EJECUTAR SI YA HAY DATOS INCORRECTOS)
-- Se omite automáticamente si las tablas aún no existen
-- ==========================================================


-- BASE DE DATOS: bd_optica

-- Módulo: Registro de usuarios y credenciales

-- TABLA: cat_estados_usuario
-- ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS cat_estados_usuario (
    id          	SERIAL PRIMARY KEY,
    codigo      	VARCHAR(20) NOT NULL UNIQUE,
    nombre      	VARCHAR(30) NOT NULL UNIQUE,
    descripcion 	VARCHAR(64)
);

INSERT INTO cat_estados_usuario (codigo, nombre, descripcion) VALUES
('ACTIVO',    'Activo',    'Usuario activo y operativo'),
('BLOQUEADO', 'Bloqueado', 'Usuario bloqueado por intentos fallidos'),
('INACTIVO',  'Inactivo',  'Usuario deshabilitado');
 -- ─────────────────────────────────────────────────────────
 
-- TABLA: usuarios
-- ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS usuarios (
    id                    	SERIAL PRIMARY KEY,
    usuario_nombre        	VARCHAR(64) NOT NULL,
    correo_electronico    	VARCHAR(128) UNIQUE NOT NULL,
    contrasena            	VARCHAR(255) NOT NULL,
	id_estado_usuario     	INT NOT NULL DEFAULT 1,
	intentos_fallidos     	SMALLINT DEFAULT 0,
    fecha_ultimo_intento  	TIMESTAMP NULL,
    fecha_alta            	TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_baja            	TIMESTAMP NULL,
	CONSTRAINT fk_estado_usuario FOREIGN KEY (id_estado_usuario) REFERENCES cat_estados_usuario(id) ON DELETE RESTRICT
);
-- Usuarios base (contraseñas hasheadas - bcrypt)
INSERT INTO usuarios (usuario_nombre, correo_electronico, contrasena, id_estado_usuario) VALUES
('Desarrollador Sistema', 'dev@startupone.com', '$2a$12$anVRTYupEAOg05t9ATVuG.hF/35f3U2WgKJjkk.7Dzgibm/ff.83W', 1),
('Administrador',         'admin@startupone.com', '$2a$12$TaJF2IGQx9aW9.LtOd9Xue.uMQ6c9mRkvlfrQKG5adg5O47/AB4Bq', 1);
-- ─────────────────────────────────────────────────────────
 
-- TABLA: cat_roles 
-- ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS cat_roles  (
    id            	SERIAL PRIMARY KEY,
    codigo      	VARCHAR(20) NOT NULL UNIQUE,
    nombre      	VARCHAR(32) NOT NULL UNIQUE,
    descripcion   	VARCHAR(128)
);
 
INSERT INTO cat_roles (codigo, nombre, descripcion) VALUES
('ROLE_DEV',   'Desarrollador', 'Super usuario con control total del sistema'),
('ROLE_ADMIN', 'Administrador', 'Administrador del negocio');
-- ─────────────────────────────────────────────────────────
 
-- TABLA: usuarios_roles
-- ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS usuarios_roles (
    id                SERIAL PRIMARY KEY,
    id_usuario        INT NOT NULL,
    id_rol            INT NOT NULL,
    fecha_asignacion  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_usuario_rol 	UNIQUE (id_usuario, id_rol),
    CONSTRAINT fk_usuario 		FOREIGN KEY (id_usuario) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_rol     		FOREIGN KEY (id_rol)     REFERENCES cat_roles(id) ON DELETE CASCADE
);
 
CREATE INDEX idx_usuarios_roles_usuario ON usuarios_roles (id_usuario);
 
INSERT INTO usuarios_roles (id_usuario, id_rol) VALUES
(1, 1),
(2, 2);
-- ─────────────────────────────────────────────────────────

-- TABLA: cat_estados_solicitud_recuperacion
-- ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS cat_estados_solicitud_recuperacion (
     id          	SERIAL PRIMARY KEY,
    codigo      	VARCHAR(20) NOT NULL UNIQUE,
    nombre      	VARCHAR(30) NOT NULL UNIQUE,
    descripcion 	VARCHAR(64)
);

INSERT INTO cat_estados_solicitud_recuperacion (codigo, nombre, descripcion) VALUES
('PENDIENTE',      'Pendiente',      'Espera aprobación del administrador'),
('APROBADA',       'Aprobada',       'Código activo y enviado'),
('USADA',          'Usada',          'Código usado para recuperar contraseña'),
('EXPIRADA',       'Expirada',       'Código vencido'),
('CORREO_FALLIDO', 'Correo fallido', 'Correo falló pero el código sigue válido');
-- ─────────────────────────────────────────────────────────
 
-- TABLA: solicitudes_recuperacion
-- ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS solicitudes_recuperacion (
    id                	SERIAL PRIMARY KEY,
    id_usuario        	INT NOT NULL,
    codigo            	VARCHAR(6) NOT NULL,
    fecha_solicitud   	TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_uso         	TIMESTAMP NULL,
    id_estado          	INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_solicitud_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id) ON DELETE CASCADE,
	CONSTRAINT fk_solicitud_estado FOREIGN KEY (id_estado) REFERENCES cat_estados_solicitud_recuperacion(id) ON DELETE RESTRICT
);
-- ─────────────────────────────────────────────────────────
 
-- MÓDULO: CATÁLOGOS (TABLAS MAESTRAS)
 
-- TABLA: cat_tipos_documento
-- ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS cat_tipos_documento (
     id       		SERIAL PRIMARY KEY,
    codigo   		VARCHAR(10) NOT NULL UNIQUE,
    nombre   		VARCHAR(64) NOT NULL,
    descripcion 	VARCHAR(128)
);
 
INSERT INTO cat_tipos_documento (codigo, nombre, descripcion) VALUES
('CC', 'Cédula de Ciudadanía', 	'Documento de identificación principal para mayores de edad'),
('TI', 'Tarjeta de Identidad', 	'Documento de identificación para menores de edad'),
('CE', 'Cédula de Extranjería', 'Documento para residentes extranjeros'),
('RC', 'Registro Civil', 		'Documento de identificación para recién nacidos');
-- ─────────────────────────────────────────────────────────
 
-- TABLA: cat_eps
-- ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS cat_eps (
    id       		SERIAL PRIMARY KEY,
    codigo   		VARCHAR(20) NOT NULL UNIQUE,
    nombre   		VARCHAR(128) NOT NULL UNIQUE,
    descripcion 	VARCHAR(256)
);
 
INSERT INTO cat_eps (codigo, nombre, descripcion) VALUES
('SURA',     'Sura',     	'EPS Sura - Administradora de riesgos laborales y salud'),
('SANITAS',  'Sanitas',  	'EPS Sanitas - Medicina prepagada y planes de salud'),
('COMPENSAR','Compensar', 	'EPS Compensar - Caja de compensación familiar'),
('FAMISANAR','Famisanar',	'EPS Famisanar - Enfocada en familias colombianas'),
('SIN_EPS',  'Sin EPS',  	'Paciente sin afiliación a EPS');
-- ─────────────────────────────────────────────────────────
 
-- TABLA: cat_parentescos
-- ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS cat_parentescos (
    id       		SERIAL PRIMARY KEY,
    codigo   		VARCHAR(20) NOT NULL UNIQUE,
    nombre   		VARCHAR(32) NOT NULL UNIQUE,
    descripcion 	VARCHAR(128)
);
 
INSERT INTO cat_parentescos (codigo, nombre, descripcion) VALUES
('MADRE',     'Madre',     'Relación de parentesco por consanguinidad directa'),
('PADRE',     'Padre',     'Relación de parentesco por consanguinidad directa'),
('HIJO',      'Hijo(a)',   'Descendiente directo'),
('CONYUGE',   'Cónyuge',   'Cónyuge o compañero(a) permanente'),
('HERMANO',   'Hermano(a)', 'Relación fraternal'),
('AMIGO',     'Amigo(a)',  'Sin parentesco familiar'),
('OTRO',      'Otro',      'Otro tipo de relación no especificada');
-- ─────────────────────────────────────────────────────────
 
-- TABLA: cat_tipos_lente
-- ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS cat_tipos_lente (
    id      		SERIAL PRIMARY KEY,
    codigo  		VARCHAR(20) NOT NULL UNIQUE,
    nombre  		VARCHAR(64) NOT NULL UNIQUE,
    descripcion 	VARCHAR(256)
);
 
INSERT INTO cat_tipos_lente (codigo, nombre, descripcion) VALUES
('MONO',     'Monofocal',     	'Lente de un solo foco o distancia'),
('BI',       'Bifocal',       	'Lente con dos focos (lejos y cerca)'),
('CONTACTO', 'Lente Contacto', 	'Lente que se coloca directamente sobre el ojo'),
('PROGRE',   'Progresivo',    	'Lente multifocal sin línea de separación visible');
-- ─────────────────────────────────────────────────────────
 
-- TABLA: cat_materiales
-- ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS cat_materiales (
    id      		SERIAL PRIMARY KEY,
    codigo  		VARCHAR(20) NOT NULL UNIQUE,
    nombre  		VARCHAR(64) NOT NULL UNIQUE,
    descripcion 	VARCHAR(256)
);
 
INSERT INTO cat_materiales (codigo, nombre, descripcion) VALUES
('CR39',	'CR-39',         	'Material plástico estándar, excelente óptica'),
('PC',      'Policarbonato', 	'Alta resistencia a impactos, ideal para niños'),
('HI_167',  'Alto Índice 1.67',	'Material delgado para graduaciones altas'),
('HI_174',  'Alto Índice 1.74',	'Material ultradelgado para graduaciones muy altas'),
('TRIVEX',  'Trivex',        	'Material con excelente claridad óptica'),
('VIDRIO',  'Vidrio',        	'Material tradicional, alta resistencia a rayones');
-- ─────────────────────────────────────────────────────────
 
-- TABLA: cat_estados_consulta
-- ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS cat_estados_consulta (
    id          	SERIAL PRIMARY KEY,
    codigo      	VARCHAR(20) NOT NULL UNIQUE,
    nombre      	VARCHAR(30) NOT NULL UNIQUE,
    descripcion 	VARCHAR(64)
);

INSERT INTO cat_estados_consulta (codigo, nombre, descripcion) VALUES
('BORRADOR',    'Borrador',    'Consulta iniciada pero no completada'),
('EN_PROCESO',  'En proceso',  'Consulta en curso, llenado progresivo'),
('FINALIZADA',  'Finalizada',  'Consulta cerrada con diagnóstico'),
('ANULADA',     'Anulada',     'Consulta cancelada o inválida');
-- ─────────────────────────────────────────────────────────
 
-- MÓDULO: PACIENTES

-- TABLA: cat_estados_paciente
-- ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS cat_estados_paciente (
    id          	SERIAL PRIMARY KEY,
    codigo      	VARCHAR(20) NOT NULL UNIQUE,
    nombre      	VARCHAR(30) NOT NULL UNIQUE,
    descripcion 	VARCHAR(64)
);

INSERT INTO cat_estados_paciente (codigo, nombre, descripcion) VALUES
('ACTIVO',     'Activo',     'Paciente activo y atendible'),
('INACTIVO',   'Inactivo',   'Paciente sin actividad reciente'),
('SUSPENDIDO', 'Suspendido', 'Paciente temporalmente fuera de atención');
 -- ─────────────────────────────────────────────────────────
 
-- TABLA: pacientes
-- ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS pacientes (
    id					SERIAL PRIMARY KEY,
    id_tipo_documento	INT NOT NULL,
    numero_documento	VARCHAR(20) UNIQUE NOT NULL,
    nombre_completo		VARCHAR(128) NOT NULL,
    fecha_nacimiento	DATE NOT NULL,
    sexo				VARCHAR(1) NOT NULL CHECK (sexo IN ('M', 'F','O')),
    estado_civil		VARCHAR(32),
    ocupacion			VARCHAR(64),
    direccion			VARCHAR(128),
    telefono			VARCHAR(20),
    id_eps				INT,
    tipo_vinculacion	VARCHAR(32),
	id_estado			INT NOT NULL DEFAULT 1,	
    fecha_registro       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pac_tipo_doc FOREIGN KEY (id_tipo_documento) REFERENCES cat_tipos_documento(id),
    CONSTRAINT fk_pac_eps      FOREIGN KEY (id_eps) REFERENCES cat_eps(id),
	CONSTRAINT fk_pac_estado FOREIGN KEY (id_estado) REFERENCES cat_estados_paciente(id) ON DELETE RESTRICT
);
-- ─────────────────────────────────────────────────────────
 
-- MÓDULO: CONSULTAS - cita del paciente
 
-- TABLA: consultas
-- ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS consultas (
    id                   SERIAL PRIMARY KEY,
    id_paciente          INT NOT NULL,
    id_optometra         INT NOT NULL,
    fecha_consulta       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 
    -- Anamnesis
    motivo_consulta      TEXT,
    ultimo_control       VARCHAR(128),
    ant_personales       TEXT,
    ant_familiares       TEXT,
 
    -- Hallazgos Clínicos
    examen_externo       TEXT,
    tonometria_od        VARCHAR(16),
    tonometria_oi        VARCHAR(16),
    test_color           VARCHAR(64),
    fondo_ojo            TEXT,
 
    -- Cierre
    diagnostico          TEXT,
    conducta             TEXT,
    control_sugerido     VARCHAR(128),
    remision             VARCHAR(128),
 
    -- Control de flujo
    id_estado_consulta   INT NOT NULL DEFAULT 1,
    fecha_cierre         TIMESTAMP NULL,
 
    CONSTRAINT fk_consulta_paciente  FOREIGN KEY (id_paciente) REFERENCES pacientes(id),
    CONSTRAINT fk_consulta_optometra FOREIGN KEY (id_optometra) REFERENCES usuarios(id),
    CONSTRAINT fk_consulta_estado    FOREIGN KEY (id_estado_consulta) REFERENCES cat_estados_consulta(id)
);

CREATE INDEX idx_consultas_paciente_estado ON consultas (id_paciente, id_estado_consulta);
-- ─────────────────────────────────────────────────────────
 
-- MÓDULO: MEDICIONES TÉCNICAS (DETALLE DE LA RECETA)
 
-- TABLA: mediciones_optometricas
-- ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS mediciones_optometricas (
    id                      SERIAL PRIMARY KEY,
    id_consulta             INT NOT NULL,
 
    -- Rx en Uso (fórmula que el paciente ya trae puesta)
    rx_uso_od_esfera        DECIMAL(4,2),
    rx_uso_od_cilindro      DECIMAL(4,2),
    rx_uso_od_eje           INT CHECK (rx_uso_od_eje BETWEEN 0 AND 180),
    rx_uso_od_av_vl         VARCHAR(16),
    rx_uso_oi_esfera        DECIMAL(4,2),
    rx_uso_oi_cilindro      DECIMAL(4,2),
    rx_uso_oi_eje           INT CHECK (rx_uso_oi_eje BETWEEN 0 AND 180),
    rx_uso_oi_av_vl         VARCHAR(16),
 
    -- Visión Próxima (VP)
    vp_od                   VARCHAR(16),
    vp_oi                   VARCHAR(16),
 
    -- Tipo de lente en uso (el que ya trae el paciente)
    lente_uso               VARCHAR(32),
 
    -- Queratometría (K/M)
    km_od                   VARCHAR(64),
    km_od_observaciones     TEXT,
    km_oi                   VARCHAR(64),
    km_oi_observaciones     TEXT,
 
    -- Refracción intermedia (Rx antes de la Rx Final)
    rx_od                   VARCHAR(64),
    rx_od_observaciones     TEXT,
    rx_oi                   VARCHAR(64),
    rx_oi_observaciones     TEXT,
 
    -- Modalidad (cover test)
    modalidad_ppc           VARCHAR(32),
    modalidad_lejos         VARCHAR(32),
    modalidad_cerca         VARCHAR(32),
 
    -- Test de Titmus (estereopsis)
    test_titmus             VARCHAR(32),
 
    -- Rx Final Ojo Derecho (OD)
    od_esfera               DECIMAL(4,2),
    od_cilindro             DECIMAL(4,2),
    od_eje                  INT CHECK (od_eje BETWEEN 0 AND 180),
    od_av_vl                VARCHAR(16),
 
    -- Rx Final Ojo Izquierdo (OI)
    oi_esfera               DECIMAL(4,2),
    oi_cilindro             DECIMAL(4,2),
    oi_eje                  INT CHECK (oi_eje BETWEEN 0 AND 180),
    oi_av_vl                VARCHAR(16),
 
    -- Datos Complementarios
    adicion                 DECIMAL(4,2),
    distancia_pupilar       VARCHAR(16),
    id_material             INT,
    id_tipo_lente           INT,
    observaciones_rx        TEXT,
 
    CONSTRAINT uq_medicion_consulta UNIQUE 		(id_consulta),
    CONSTRAINT fk_med_consulta      FOREIGN KEY (id_consulta)   REFERENCES 		consultas(id) ON DELETE CASCADE,
    CONSTRAINT fk_med_material      FOREIGN KEY (id_material)   REFERENCES 		cat_materiales(id),
    CONSTRAINT fk_med_lente         FOREIGN KEY (id_tipo_lente) REFERENCES 		cat_tipos_lente(id),
	CONSTRAINT chk_od_esfera_step 	CHECK 		(od_esfera 		IS NULL OR MOD(od_esfera * 100, 25) = 0)
);
-- ─────────────────────────────────────────────────────────
 
-- MÓDULO: ACOMPAÑANTES en el caso de tutores y demas
 
-- TABLA: acompanantes
-- ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS acompanantes (
    id               	SERIAL PRIMARY KEY,
    id_consulta      	INT NOT NULL,
    nombre           	VARCHAR(128) NOT NULL,
    id_parentesco    	INT,
    telefono         	VARCHAR(20),
    CONSTRAINT fk_acomp_consulta    FOREIGN KEY (id_consulta)   REFERENCES consultas(id)        ON DELETE CASCADE,
    CONSTRAINT fk_acomp_parentesco  FOREIGN KEY (id_parentesco) REFERENCES cat_parentescos(id)
);
 
CREATE INDEX idx_acompanantes_consulta ON acompanantes (id_consulta);
-- ─────────────────────────────────────────────────────────
 
-- MÓDULO: ARCHIVOS ADJUNTOS
 
-- TABLA: archivos_adjuntos
-- ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS archivos_adjuntos (
    id                   	SERIAL PRIMARY KEY,
    id_consulta          	INT NOT NULL,
    nombre_archivo       	VARCHAR(255) NOT NULL,
    ruta_almacenamiento 	VARCHAR(512) NOT NULL,
    tipo_contenido       	VARCHAR(64),
    fecha_subida         	TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_arch_consulta FOREIGN KEY (id_consulta) REFERENCES consultas(id) ON DELETE CASCADE
);
 
CREATE INDEX idx_archivos_consulta ON archivos_adjuntos (id_consulta);
-- ─────────────────────────────────────────────────────────
 
-- COMPLEMENTO: MÓDULO DE AUDITORÍA
 
-- TABLA: auditoria_log
-- ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS auditoria_log (
    id               	SERIAL PRIMARY KEY,
    tabla_afectada   	VARCHAR(64) NOT NULL,
    id_registro      	INT NOT NULL,
    id_usuario			INT NOT NULL,
	usuario_nombre 		VARCHAR(64) NULL,
	ip             		VARCHAR(45) NULL,
    accion            	VARCHAR(64)NOT NULL,
    detalle          	TEXT,
    fecha_evento     	TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	resultado      		SMALLINT NOT NULL DEFAULT 1,
    CONSTRAINT fk_auditoria_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id)
);
 
-- Índices de rendimiento
CREATE INDEX idx_paciente_busqueda ON pacientes (numero_documento, nombre_completo);
CREATE INDEX idx_consulta_fecha    ON consultas (fecha_consulta);
CREATE INDEX idx_auditoria_fecha   ON auditoria_log (fecha_evento);
CREATE INDEX idx_auditoria_usuario ON auditoria_log (id_usuario);
CREATE INDEX idx_auditoria_accion  ON auditoria_log (accion);
-- ─────────────────────────────────────────────────────────