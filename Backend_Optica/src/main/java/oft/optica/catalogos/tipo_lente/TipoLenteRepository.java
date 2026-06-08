package oft.optica.catalogos.tipo_lente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoLenteRepository extends JpaRepository<TipoLenteEntity, Integer> {

    Optional<TipoLenteEntity> findByCodigo(String codigo);

}

