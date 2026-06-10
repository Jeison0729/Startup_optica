# Backend Óptica

API REST para la gestión de una óptica: pacientes, historia clínica optométrica, usuarios y auditoría.

---

## Stack

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.4.5 |
| Base de datos | PostgreSQL |
| Seguridad | Spring Security + JWT (jjwt 0.12.6) |
| ORM | Spring Data JPA + Hibernate |
| Validación | Spring Validation |
| Correo | Spring Mail |
| Documentación | SpringDoc OpenAPI 2.6.0 (Swagger UI) |
| Utilidades | Lombok, MapStruct 1.5.5 |
| Variables de entorno | spring-dotenv |
| Frontend (separado) | Vue + Vite |

---

## Requisitos

- Java 21
- Maven 3.9+
- PostgreSQL 15+
- Servidor SMTP configurado (para envío de códigos de recuperación)

---

## Configuración

Copia el archivo de ejemplo y completa los valores:

```bash
cp src/main/resources/application.yaml.example src/main/resources/application.yaml
```

Variables mínimas requeridas en `application.yaml` (o `.env`):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bd_optica
    username: tu_usuario
    password: tu_contraseña
  mail:
    host: smtp.tuproveedor.com
    port: 587
    username: tu_correo
    password: tu_contraseña

jwt:
  secret: tu_clave_secreta
  expiration: 86400000  # 24h en ms
```

---

## Levantar el proyecto

```bash
./mvnw spring-boot:run
```

Swagger UI disponible en:

```
http://localhost:8080/swagger-ui.html
```

---

## Base de datos

Script de creación completo en `/docs/bd_optica.sql`.

La base de datos incluye datos iniciales de arranque:

| Usuario | Correo | Rol |
|---------|--------|-----|
| Desarrollador Sistema | dev@startupone.com | `ROLE_DEV` |
| Administrador | admin@startupone.com | `ROLE_ADMIN` |

### Módulos

| # | Módulo | Tablas principales |
|---|--------|--------------------|
| 1 | Usuarios y Roles | `usuarios`, `cat_roles`, `usuarios_roles` |
| 2 | Recuperación de contraseña | `solicitudes_recuperacion` |
| 3 | Catálogos | `cat_tipos_documento`, `cat_eps`, `cat_parentescos`, `cat_tipos_lente`, `cat_materiales` |
| 4 | Pacientes | `pacientes` |
| 5 | Consultas | `consultas` |
| 6 | Mediciones optométricas | `mediciones_optometricas` |
| 7 | Acompañantes | `acompanantes` |
| 8 | Archivos adjuntos | `archivos_adjuntos` |
| 9 | Auditoría | `auditoria_log` |

### Relaciones principales

```
pacientes ──────── consultas  (1 paciente → N consultas)
consultas ──────── mediciones_optometricas  (1 consulta → 1 medición)
consultas ──────── acompanantes  (1 consulta → 0..1 acompañante)
consultas ──────── archivos_adjuntos  (1 consulta → N archivos)
usuarios  ──────── consultas  (el optómetra que atiende)
usuarios  ──────── usuarios_roles  (1 usuario → N roles)
```

---

## Estructura del proyecto

```
src/main/java/oft/optica/
├── accesos/
│   ├── roles/              # CRUD de roles
│   ├── solicitudes/        # Recuperación de contraseña
│   ├── usuario/            # CRUD de usuarios
│   └── usuario_rol/        # Asignación de roles a usuarios
├── auditorias/             # Log de auditoría
├── catalogos/              # Tablas maestras (EPS, lentes, materiales, etc.)
├── config/                 # CORS, Swagger, Security
├── consultas/
│   ├── acompanantes/
│   ├── archivos/
│   ├── mediciones/
│   └── ConsultaService...  # Consulta principal
├── exception/              # Excepciones globales
├── pacientes/              # CRUD de pacientes
├── security/
│   ├── auth/               # Login / AuthController
│   └── jwt/                # Filtro y servicio JWT
└── shared/
    ├── auditoria/          # AuditoriaHelper
    ├── catalogo/           # Endpoint GET catálogos
    ├── common/             # CambiarEstadoRequest
    └── correo/             # CorreoService
```

Cada módulo sigue el patrón:

```
Entity → Repository → Mapper → Service / ServiceImpl → Request / Response → RestController
```

Con un `Helper` adicional cuando hay lógica de soporte (resolución de catálogos, validaciones previas, etc.).

---

## Seguridad

Autenticación stateless con JWT. Dos roles definidos:

| Rol | Descripción |
|-----|-------------|
| `ROLE_DEV` | Super usuario — acceso total |
| `ROLE_ADMIN` | Administrador del negocio |

### Rutas públicas

| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/auth/login` | Obtener token JWT |
| `POST` | `/api/solicitudes/solicitar` | Solicitar recuperación de contraseña |
| `POST` | `/api/solicitudes/restablecer` | Restablecer contraseña con código |

### Resumen de permisos por módulo

| Módulo | `ROLE_ADMIN` | `ROLE_DEV` |
|--------|-------------|------------|
| Catálogos (GET) | ✅ | ✅ |
| Pacientes | ✅ (sin DELETE) | ✅ |
| Consultas | ✅ (sin DELETE) | ✅ |
| Mediciones | ✅ | ✅ |
| Acompañantes | ✅ | ✅ |
| Archivos adjuntos | ✅ | ✅ |
| Usuarios | Solo GET y PATCH propio | ✅ |
| Roles / Usuarios-Roles | ❌ | ✅ |
| Auditoría | ❌ | ✅ |
| Solicitudes (gestión) | ✅ | ✅ |

---

## Flujo de recuperación de contraseña

```
Usuario solicita → [activo] código generado y enviado por correo → usuario ingresa código → contraseña restablecida
                 → [bloqueado] solicitud PENDIENTE → admin aprueba → desbloquea + envía código
```

Estados del código: `PENDIENTE → APROBADA → USADA / EXPIRADA`, con estado adicional `CORREO_FALLIDO` si el envío falla pero el código sigue válido.

Límite: 3 solicitudes por hora por usuario.

---

## Auditoría

Toda acción relevante queda registrada en `auditoria_log` con:

- Usuario que ejecutó la acción
- IP de origen (soporta proxies y load balancers con `X-Forwarded-For`)
- Tabla y registro afectado
- Resultado: `1` exitoso / `0` fallido

Acciones registradas: login, bloqueos, CRUD de usuarios, pacientes, consultas, solicitudes de recuperación, entre otras.

---

## Estado del proyecto

| Módulo | Backend | Frontend |
|--------|--------|--------|
| Autenticación JWT | ✅ Completo | ⏳ En desarrollo |
| Usuarios y Roles | ✅ Completo | ⏳ En desarrollo |
| Recuperación de contraseña | ✅ Completo | ⏳ En desarrollo |
| Catálogos | ✅ Completo | ⏳ En desarrollo |
| Pacientes | ✅ Completo | ⏳ En desarrollo |
| Consultas | ✅ Completo | ⏳ En desarrollo |
| Mediciones optométricas | ✅ Completo | ⏳ En desarrollo |
| Acompañantes | ✅ Completo | ⏳ En desarrollo |
| Archivos adjuntos | ✅ Completo | ⏳ En desarrollo |
| Auditoría | ✅ Completo | ⏳ En desarrollo |
---

*Proyecto: Sistema de Historia Clínica Optométrica — `bd_optica`*
