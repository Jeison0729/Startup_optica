package oft.optica.accesos.solicitudes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oft.optica.accesos.estado_solicitud.EstadoSolicitudEntity;
import oft.optica.accesos.estado_solicitud.EstadoSolicitudRepository;
import oft.optica.accesos.usuario.UsuarioEntity;
import oft.optica.accesos.usuario.UsuarioRepository;
import oft.optica.exception.CredencialesInvalidasException;
import oft.optica.exception.RecursoNoEncontradoException;
import oft.optica.shared.correo.CorreoService;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolicitudHelper {

    private final UsuarioRepository usuarioRepository;
    private final EstadoSolicitudRepository estadoSolicitudRepository;
    private final SolicitudRepository solicitudRepository;
    private final CorreoService correoService;

    private static final SecureRandom RANDOM = new SecureRandom();

    // ─── Búsquedas ────────────────────────────────────────

    public UsuarioEntity buscarUsuario(String correoOUsuario) {
        return usuarioRepository.findByCorreoElectronico(correoOUsuario)
                .or(() -> usuarioRepository.findByUsuarioNombre(correoOUsuario))
                .orElseThrow(CredencialesInvalidasException::new);
    }

    public SolicitudEntity buscarSolicitud(Integer id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Solicitud no encontrada."));
    }

    public EstadoSolicitudEntity buscarEstado(String codigo) {
        return estadoSolicitudRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Estado de solicitud no encontrado: " + codigo));
    }

    // ─── Generación de código ─────────────────────────────

    public String generarCodigo() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    // ─── Correo ───────────────────────────────────────────

    // Retorna true si el correo se envió, false si falló — nunca lanza excepción
    public boolean enviarCorreoRecuperacion(String correo, String codigo, String cuerpo) {
        try {
            correoService.enviarCorreo(correo, "RECUPERACIÓN DE CONTRASEÑA", cuerpo);
            return true;
        } catch (Exception e) {
            log.error("Error enviando correo a {}: {}", correo, e.getMessage());
            return false;
        }
    }

    // ─── Expiración de códigos activos ────────────────────

    public void expirarCodigosActivos(Integer idUsuario) {
        EstadoSolicitudEntity expirada = buscarEstado("EXPIRADA");

        solicitudRepository.findCodigosActivosPorUsuario(idUsuario)
                .forEach(s -> {
                    s.setEstadoSolicitud(expirada);
                    solicitudRepository.save(s);
                });
    }
}