package oft.optica.catalogos.tipo_documento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoDocumentoRepository extends JpaRepository<TipoDocumentoEntity, Integer> {

    Optional<TipoDocumentoEntity> findByCodigo(String  codigo);

}

