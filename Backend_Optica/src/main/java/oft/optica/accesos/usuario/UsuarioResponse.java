package oft.optica.accesos.usuario;

import java.time.LocalDateTime;
import java.util.List;

public record UsuarioResponse(

        Integer id,

        String usuarioNombre,

        String correoElectronico,

        String estadoUsuario,

        Byte intentosFallidos,

        LocalDateTime fechaUltimoIntento,

        LocalDateTime fechaAlta,

        LocalDateTime fechaBaja,

        List<String> roles
) {
}
