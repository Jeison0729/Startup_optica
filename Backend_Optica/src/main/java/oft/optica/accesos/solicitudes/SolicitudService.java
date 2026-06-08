package oft.optica.accesos.solicitudes;

import java.util.List;
import java.util.Map;

public interface SolicitudService {

    // Usuario solicita recuperación — retorna DTO con estado para el controller
    SolicitudResponse solicitarRecuperacion(String correoOUsuario, String ip);

    // Admin lista solicitudes pendientes o con correo fallido
    List<SolicitudResponse> listarPendientes();

    // Admin lista historial completo
    List<SolicitudResponse> listarTodas();

    // Admin aprueba — desbloquea usuario y genera código
    Map<String, String> aprobarSolicitud(Integer idSolicitud, String ip);

    // Usuario usa el código para restablecer contraseña
    String restablecerContrasena(String correoOUsuario, String codigo, String nuevaContrasena, String ip);

    // Admin reenvía código cuando el correo falló (estado=4)
    void reenviarCodigo(Integer idSolicitud, String ip);
}
