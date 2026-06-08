package oft.optica.accesos.usuario_rol;

import java.util.List;

public interface UsuarioRolService {

    UsuarioRolResponse cambiarRol(Integer idUsuario, Integer idRolNuevo, String correoAutenticado);

    List<UsuarioRolListadoResponse> listar();

    List<UsuarioRolListadoResponse> listarPorUsuario(Integer idUsuario);

    void eliminar(Integer idUsuario, Integer idRol, String correoAutenticado);
}
