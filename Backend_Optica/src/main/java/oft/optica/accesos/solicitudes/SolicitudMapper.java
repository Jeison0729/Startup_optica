package oft.optica.accesos.solicitudes;

import org.springframework.stereotype.Component;

@Component
public class SolicitudMapper {

    public SolicitudResponse toDTO(SolicitudEntity solicitud) {
        return new SolicitudResponse(
                solicitud.getId(),
                solicitud.getUsuario().getId(),
                solicitud.getUsuario().getUsuarioNombre(),
                solicitud.getUsuario().getCorreoElectronico(),
                solicitud.getCodigo(),
                solicitud.getFechaSolicitud(),
                solicitud.getFechaUso(),
                solicitud.getEstadoSolicitud().getId()
        );

    }
}
