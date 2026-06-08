package oft.optica.accesos.solicitudes;

import java.time.LocalDateTime;

public record SolicitudResponse(

        Integer id,

        Integer idUsuario,

        String nombreUsuario,

        String correoUsuario,

        String codigo,

        LocalDateTime fechaSolicitud,

        LocalDateTime fechaUso,

        Integer estado
) {
}
