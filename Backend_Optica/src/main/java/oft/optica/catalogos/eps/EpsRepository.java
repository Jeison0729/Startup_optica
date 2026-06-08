package oft.optica.catalogos.eps;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EpsRepository extends JpaRepository<EpsEntity, Integer> {

    Optional<EpsEntity> findByCodigo(String codigo);
}
