package oft.optica.accesos.estado_usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoUsuarioRepository extends JpaRepository<EstadoUsuario, Integer> {

    Optional<EstadoUsuario> findByCodigo(String codigo);
}
