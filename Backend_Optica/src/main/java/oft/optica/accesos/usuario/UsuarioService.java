package oft.optica.accesos.usuario;

import java.util.List;

public interface UsuarioService {

    List<UsuarioResponse> listar(String correoAutenticado);

    UsuarioResponse obtenerPorId(Integer id);

    UsuarioResponse crear(UsuarioRequest request, String ip);

    UsuarioResponse actualizar(Integer id, UsuarioRequest request, String ip);  // agregar en Impl

    void eliminar(Integer id, String ip);

    UsuarioResponse bloquear(Integer id, String correoAutenticado, String ip);

    UsuarioResponse desbloquear(Integer id, String correoAutenticado, String ip);

    UsuarioResponse reactivar(Integer id, String correoAutenticado, String ip);
}
