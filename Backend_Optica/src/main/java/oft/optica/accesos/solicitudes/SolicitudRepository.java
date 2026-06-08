package oft.optica.accesos.solicitudes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudRepository extends JpaRepository<SolicitudEntity, Integer> {

    // Buscar código activo para restablecer contraseña
    @Query("""
            SELECT s FROM SolicitudEntity s
            WHERE s.codigo = :codigo
              AND s.estadoSolicitud.codigo IN ('APROBADA', 'CORREO_FALLIDO')
            """)
    Optional<SolicitudEntity> findCodigoActivo(@Param("codigo") String codigo);

    // Contar solicitudes recientes para límite por hora
    @Query("""
            SELECT COUNT(s) FROM SolicitudEntity s
            WHERE s.usuario.id = :idUsuario
              AND s.estadoSolicitud.codigo <> 'USADA'
              AND s.fechaSolicitud >= :desde
            """)
    long contarSolicitudesHora(
            @Param("idUsuario") Integer idUsuario,
            @Param("desde") LocalDateTime desde);

    // Listar pendientes y con correo fallido para el admin
    @Query("""
            SELECT s FROM SolicitudEntity s
            WHERE s.estadoSolicitud.codigo IN ('PENDIENTE', 'CORREO_FALLIDO')
            ORDER BY s.fechaSolicitud ASC
            """)
    List<SolicitudEntity> findPendientes();

    // Historial completo ordenado por fecha
    @Query("""
            SELECT s FROM SolicitudEntity s
            ORDER BY s.fechaSolicitud DESC
            """)
    List<SolicitudEntity> findAllOrdenadas();

    // Buscar códigos activos de un usuario para expirarlos
    @Query("""
            SELECT s FROM SolicitudEntity s
            WHERE s.usuario.id = :idUsuario
              AND s.estadoSolicitud.codigo IN ('APROBADA', 'CORREO_FALLIDO')
            """)
    List<SolicitudEntity> findCodigosActivosPorUsuario(
            @Param("idUsuario") Integer idUsuario);
}