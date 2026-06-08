package oft.optica.accesos.roles;

import java.util.List;

public interface RolService {

    List<RolResponse> listar();

    RolResponse obtenerPorId(Integer id);

    RolResponse actualizar(Integer id, RolRequest request);

}
