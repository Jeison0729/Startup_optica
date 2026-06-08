package oft.optica.auditorias;

public enum AccionLog {

    // Seguridad
    LOGIN_OK,
    LOGIN_FALLIDO,
    CUENTA_BLOQUEADA,
    CUENTA_DESBLOQUEADA,

    // Usuarios
    USUARIO_CREADO,
    USUARIO_ACTUALIZADO,
    USUARIO_DESACTIVADO,
    USUARIO_REACTIVADO,

    // Roles
    ROL_ASIGNADO,
    ROL_REMOVIDO,

    // Recuperación
    SOLICITUD_CREADA,
    SOLICITUD_APROBADA,
    CODIGO_USADO,
    CODIGO_EXPIRADO,

    // Pacientes
    PACIENTE_CREADO,
    PACIENTE_ACTUALIZADO,
    PACIENTE_ESTADO_CAMBIADO,  // ← cubre desactivar, suspender, reactivar
    //   PACIENTE_DESACTIVADO queda redundante, puedes quitarlo

    // Consultas
    CONSULTA_CREADA,
    CONSULTA_ACTUALIZADA,
    CONSULTA_FINALIZADA,
    CONSULTA_ANULADA,

    // Acompañantes
    ACOMPANANTE_CREADO,
    ACOMPANANTE_ACTUALIZADO,
    ACOMPANANTE_ELIMINADO,

    // Mediciones optométricas
    MEDICION_CREADA,
    MEDICION_ACTUALIZADA,

    // Archivos adjuntos
    ARCHIVO_SUBIDO,
    ARCHIVO_ELIMINADO,
}
