package oft.optica.accesos.roles;

import lombok.RequiredArgsConstructor;
import oft.optica.exception.DuplicadoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolServiceImpl implements RolService {

    private final RolRepository repository;
    private final RolMapper mapper;
    private final RolHelper helper;

    @Override
    @Transactional(readOnly = true)
    public List<RolResponse> listar() {
        return repository.findAll().stream()
                .map(mapper::toDTO).
                toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RolResponse obtenerPorId(Integer id) {
        return mapper.toDTO(helper.buscarPorId(id));
    }


    @Override
    @Transactional
    public RolResponse actualizar(Integer id, RolRequest request) {

        RolEntity existente = helper.buscarPorId(id);

        // VALIDACIÓN: evitar duplicados en update
        if (!existente.getCodigo().equals(request.codigo())
                && repository.findByCodigo(request.codigo()).isPresent()) {
            throw new DuplicadoException("El rol '" + request.codigo() + "' ya está en uso");
        }
        if (!existente.getNombre().equals(request.nombre())
                && repository.findByNombre(request.nombre()).isPresent()) {
            throw new DuplicadoException("El nombre '" + request.nombre() + "' ya está en uso");
        }

        existente.setCodigo(request.codigo());
        existente.setNombre(request.nombre());
        existente.setDescripcion(request.descripcion());

        return mapper.toDTO(repository.save(existente));
    }

}
