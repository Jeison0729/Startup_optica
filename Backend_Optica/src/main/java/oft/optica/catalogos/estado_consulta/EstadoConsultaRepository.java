package oft.optica.catalogos.estado_consulta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoConsultaRepository extends JpaRepository<EstadoConsultaEntity, Integer> {

    Optional<EstadoConsultaEntity> findByCodigo(String codigo);
}


