package oft.optica.accesos.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Integer> {

    @Query("SELECT usuario FROM UsuarioEntity usuario " +
            "JOIN usuario.usuarioRoles u_rol " +
            "JOIN u_rol.rol rol " +
            "WHERE rol.nombre = :rolNombre")
    List<UsuarioEntity> findByRolNombre(@Param("rolNombre") String rolNombre);

    Optional<UsuarioEntity> findByCorreoElectronico(String correoElectronico);

    Optional<UsuarioEntity> findByUsuarioNombre(String usuarioNombre);
}
