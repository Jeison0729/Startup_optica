package oft.optica.accesos.usuario_rol;

import oft.optica.accesos.roles.RolEntity;
import oft.optica.accesos.usuario.UsuarioEntity;
import org.springframework.stereotype.Component;

@Component
public class UsuarioRolMapper {

    // Para operaciones — asignar, actualizar (con accion y mensaje)
    public UsuarioRolResponse toDTO(
            UsuarioEntity usuario,
            RolEntity rol,
            String accion,
            String mensaje) {
        return new UsuarioRolResponse(usuario.getId(),
                usuario.getUsuarioNombre(), rol.getNombre(),
                accion,
                mensaje
        );
    }

    // Para listados — limpio, sin nulls
    public UsuarioRolListadoResponse toListadoDTO(UsuarioRolEntity u_rol) {
        return new UsuarioRolListadoResponse(
                u_rol.getUsuario().getId(),
                u_rol.getUsuario().getUsuarioNombre(),
                u_rol.getRol().getNombre()
        );
    }
}
