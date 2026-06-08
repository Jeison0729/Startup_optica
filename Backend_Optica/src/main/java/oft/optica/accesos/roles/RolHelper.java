package oft.optica.accesos.roles;

import lombok.RequiredArgsConstructor;
import oft.optica.exception.RecursoNoEncontradoException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RolHelper {

    private final RolRepository rolRepository;

    public RolEntity buscarPorId(Integer id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado con id: " + id));
    }
}
