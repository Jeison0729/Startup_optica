package oft.optica.accesos.estado_solicitud;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstadoSolicitudRepository extends JpaRepository<EstadoSolicitudEntity, Integer> {

    Optional<EstadoSolicitudEntity> findByCodigo(String codigo);

    List<EstadoSolicitudEntity> findByCodigoIn(List<String> codigos);

}
