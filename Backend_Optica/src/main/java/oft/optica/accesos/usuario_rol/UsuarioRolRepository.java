package oft.optica.accesos.usuario_rol;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRolRepository extends JpaRepository<UsuarioRolEntity, Integer> {

    Optional<UsuarioRolEntity> findByUsuarioIdAndRolId(Integer idUsuario, Integer idRol);

    List<UsuarioRolEntity> findByUsuarioId(Integer idUsuario);

    void deleteByUsuarioId(Integer idUsuario);
}
