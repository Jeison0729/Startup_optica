package oft.optica.accesos.roles;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<RolEntity, Integer> {

    Optional<RolEntity> findByCodigo(String codigo);

    Optional<RolEntity> findByNombre(String nombre);
}
