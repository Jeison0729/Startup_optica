package oft.optica.accesos.solicitudes;

import lombok.RequiredArgsConstructor;
import oft.optica.accesos.estado_solicitud.EstadoSolicitudEntity;
import oft.optica.accesos.estado_usuario.EstadoUsuarioRepository;
import oft.optica.accesos.usuario.UsuarioEntity;
import oft.optica.accesos.usuario.UsuarioRepository;
import oft.optica.auditorias.AccionLog;
import oft.optica.auditorias.LogAuditoriaService;
import oft.optica.exception.CodigoInvalidoException;
import oft.optica.exception.OperacionInvalidaException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SolicitudServiceImpl implements SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final PasswordEncoder passwordEncoder;
    private final SolicitudMapper mapper;
    private final LogAuditoriaService logService;
    private final EstadoUsuarioRepository estadoUsuarioRepository;
    private final SolicitudHelper helper;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public SolicitudResponse solicitarRecuperacion(String correoOUsuario, String ip) {

        // valida existencia de usuario en el sistema
        UsuarioEntity usuario = helper.buscarUsuario(correoOUsuario);

        // Límite de 3 solicitudes por hora
        long recientes = solicitudRepository.contarSolicitudesHora(
                usuario.getId(),
                LocalDateTime.now().minusHours(1)
        );

        if (recientes >= 3) {
            logService.registrar(usuario, "solicitudes_recuperacion", null, AccionLog.SOLICITUD_CREADA,
                    "Límite de solicitudes alcanzado", ip, false);
            throw new OperacionInvalidaException("Límite de solicitudes alcanzado. Intenta en 1 hora.");
        }

        // Flujo 1: usuario bloqueado → espera aprobación admin
        EstadoSolicitudEntity pendiente = helper.buscarEstado("PENDIENTE");

        // quedará en estado PENDIENTE  si esta bloqueado
        if (!usuario.isActivo()) {
            SolicitudEntity solicitud = solicitudRepository.save(
                    SolicitudEntity.builder()
                            .usuario(usuario)
                            .codigo("000000")
                            .estadoSolicitud(pendiente)
                            .build()
            );

            logService.registrar(
                    usuario, "solicitudes_recuperacion", solicitud.getId(), AccionLog.SOLICITUD_CREADA,
                    "Solicitud pendiente de aprobación admin", ip, true);

            return mapper.toDTO(solicitud);
        }

        // Flujo 2: usuario activo → invalidar anteriores, generar y enviar código
        helper.expirarCodigosActivos(usuario.getId());

        // codigo generado
        String codigo = helper.generarCodigo();
        EstadoSolicitudEntity aprobada = helper.buscarEstado("APROBADA");
        EstadoSolicitudEntity fallido = helper.buscarEstado("CORREO_FALLIDO");

        SolicitudEntity solicitud = solicitudRepository.save(
                SolicitudEntity.builder()
                        .usuario(usuario)
                        .codigo(codigo)
                        .estadoSolicitud(aprobada)
                        .build()
        );

        // genera el codigo aun cuando el envio al correo haya fallado
        boolean correoEnviado = helper.enviarCorreoRecuperacion(
                usuario.getCorreoElectronico(),
                codigo,
                "Código de recuperación: " + codigo + "\nExpira en 5 minutos."
        );

        solicitud.setEstadoSolicitud(correoEnviado ? aprobada : fallido);
        SolicitudEntity guardada = solicitudRepository.save(solicitud);

        logService.registrar(
                usuario,
                "solicitudes_recuperacion",
                guardada.getId(),
                AccionLog.SOLICITUD_CREADA,
                correoEnviado
                        ? "Código enviado al correo"
                        : "Código generado — correo falló",
                ip,
                true
        );

        return mapper.toDTO(guardada);
    }

    @Override
    @Transactional
    public Map<String, String> aprobarSolicitud(Integer idSolicitud, String ip) {

        // busca la solicitud
        SolicitudEntity solicitud = helper.buscarSolicitud(idSolicitud);

        // solo aprueba solicitudes PENDIENTES
        if (!"PENDIENTE".equals(solicitud.getEstadoSolicitud().getCodigo())) {
            throw new OperacionInvalidaException("La solicitud ya fue procesada.");
        }

        // genera codigo reemplazando el 000000 que se dio anteriormente en PENDIENTE del metodo anterior
        String codigo = helper.generarCodigo();
        solicitud.setCodigo(codigo);
        solicitud.setEstadoSolicitud(helper.buscarEstado("APROBADA"));
        solicitudRepository.save(solicitud);

        // desbloquea el usuario
        // resetea fecha y ultimos intentos para ACTIVAR usuario
        UsuarioEntity usuario = solicitud.getUsuario();
        usuario.setEstadoUsuario(
                estadoUsuarioRepository.findByCodigo("ACTIVO").orElseThrow()
        );
        usuario.setIntentosFallidos((byte) 0);
        usuario.setFechaUltimoIntento(null);
        usuarioRepository.save(usuario);

        // igual, si falla el envio genera tambien codigo
        boolean correoEnviado = helper.enviarCorreoRecuperacion(
                usuario.getCorreoElectronico(),
                codigo,
                "Solicitud aprobada. Código: " + codigo + "\nExpira en 5 minutos."
        );
        logService.registrar(
                usuario, "solicitudes_recuperacion", solicitud.getId(), AccionLog.SOLICITUD_APROBADA,
                correoEnviado ? "Solicitud aprobada y código enviado" : "Solicitud aprobada — correo falló", ip, true);

        // muestra datos al ADMIN o encargado de activar
        Map<String, String> resultado = new HashMap<>();
        resultado.put("codigo", codigo);
        resultado.put("usuario", usuario.getUsuarioNombre());
        resultado.put("mensaje", correoEnviado
                ? "Solicitud aprobada. Código enviado al correo."
                : "Solicitud aprobada. Correo no enviado, entrega el código manualmente.");
        return resultado;
    }


    @Override
    @Transactional
    public String restablecerContrasena(String correoOUsuario, String codigo, String nuevaContrasena, String ip) {

        // verifica credenciales
        UsuarioEntity usuario = helper.buscarUsuario(correoOUsuario);

        SolicitudEntity solicitud = solicitudRepository
                .findCodigoActivo(codigo)
                .orElseThrow(() -> {
                    logService.registrar(
                            usuario, "solicitudes_recuperacion", null, AccionLog.CODIGO_USADO,
                            "Código inválido o inexistente", ip, false);
                    return new CodigoInvalidoException("Código inválido.");
                });

        // confirma que codigo enviado sea al usuario correcot
        if (!solicitud.getUsuario().getId().equals(usuario.getId())) {
            logService.registrar(
                    usuario, "solicitudes_recuperacion", solicitud.getId(), AccionLog.CODIGO_USADO,
                    "Código no pertenece al usuario", ip, false);
            throw new CodigoInvalidoException("Código inválido.");
        }

        // se valida que el tiempo 5 min no haya sido superado
        // de lo contrario el codigo sera EXPIRADO
        if (solicitud.getFechaSolicitud().isBefore(LocalDateTime.now().minusMinutes(5))) {
            solicitud.setEstadoSolicitud(helper.buscarEstado("EXPIRADA"));
            solicitudRepository.save(solicitud);
            logService.registrar(
                    usuario, "solicitudes_recuperacion", solicitud.getId(), AccionLog.CODIGO_EXPIRADO,
                    "Código expirado al intentar restablecer", ip, false);
            throw new CodigoInvalidoException("El código ha expirado.");
        }

        // HASH  a contrasena
        usuario.setContrasena(passwordEncoder.encode(nuevaContrasena));
        usuarioRepository.save(usuario);

        // el codigo pasa a ser USADO
        solicitud.setEstadoSolicitud(helper.buscarEstado("USADA"));
        solicitud.setFechaUso(LocalDateTime.now());
        solicitudRepository.save(solicitud);
        logService.registrar(
                usuario, "solicitudes_recuperacion", solicitud.getId(), AccionLog.CODIGO_USADO,
                "Contraseña restablecida correctamente", ip, true);
        return "Contraseña restablecida correctamente.";
    }

    @Override
    @Transactional
    public void reenviarCodigo(Integer idSolicitud, String ip) {

        // Validacion de solicitud
        SolicitudEntity solicitud = helper.buscarSolicitud(idSolicitud);

        // envio unicamente a CORREO_FALLIDO 
        if (!"CORREO_FALLIDO".equals(solicitud.getEstadoSolicitud().getCodigo())) {
            throw new OperacionInvalidaException("La solicitud no requiere reenvío.");
        }

        // revision de codigo no EXPIRADO 
        if (solicitud.getFechaSolicitud().isBefore(LocalDateTime.now().minusMinutes(5))) {
            solicitud.setEstadoSolicitud(helper.buscarEstado("EXPIRADA"));
            solicitudRepository.save(solicitud);
            logService.registrar(
                    solicitud.getUsuario(), "solicitudes_recuperacion", solicitud.getId(), AccionLog.CODIGO_EXPIRADO,
                    "Código expirado al intentar reenviar", ip, false);
            throw new OperacionInvalidaException("El código ha expirado. El usuario debe crear una nueva solicitud.");
        }

        // reutiliza codigo para enviarse al usuario y que siga intentando
        boolean correoEnviado = helper.enviarCorreoRecuperacion(
                solicitud.getUsuario().getCorreoElectronico(),
                solicitud.getCodigo(),
                "Reenvío de código: " + solicitud.getCodigo() + "\nExpira en 5 minutos."
        );

        // correo exitoso entonces cambia  APROBADA 
        if (correoEnviado) {
            solicitud.setEstadoSolicitud(helper.buscarEstado("APROBADA"));
            solicitudRepository.save(solicitud);
            logService.registrar(
                    solicitud.getUsuario(),
                    "solicitudes_recuperacion",
                    solicitud.getId(),
                    AccionLog.SOLICITUD_APROBADA,
                    "Código reenviado correctamente",
                    ip,
                    true
            );
        } else {
            logService.registrar(
                    solicitud.getUsuario(), "solicitudes_recuperacion", solicitud.getId(), AccionLog.SOLICITUD_APROBADA,
                    "Reenvío fallido — código sigue activo", ip, false);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudResponse> listarPendientes() {
        return solicitudRepository.findPendientes()
                .stream()
                .map(mapper::toDTO)
                .toList();

    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudResponse> listarTodas() {
        return solicitudRepository.findAllOrdenadas()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }


}
