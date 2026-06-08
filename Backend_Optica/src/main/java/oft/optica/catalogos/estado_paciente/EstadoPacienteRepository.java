package oft.optica.catalogos.estado_paciente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoPacienteRepository extends JpaRepository<EstadoPacienteEntity, Integer> {

    Optional<EstadoPacienteEntity> findByCodigo(String codigo);
}

