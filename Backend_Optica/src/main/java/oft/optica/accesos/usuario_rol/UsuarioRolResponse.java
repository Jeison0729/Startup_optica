package oft.optica.accesos.usuario_rol;

public record UsuarioRolResponse(

        Integer idUsuario,

        String nombreUsuario,

        String rol,

        String accion,

        String mensaje
) {
}
