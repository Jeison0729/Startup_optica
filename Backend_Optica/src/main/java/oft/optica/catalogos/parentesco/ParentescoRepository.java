package oft.optica.catalogos.parentesco;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParentescoRepository extends JpaRepository<ParentescoEntity, Integer> {

    Optional<ParentescoEntity> findByCodigo(String codigo);

}

