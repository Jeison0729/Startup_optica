package oft.optica.accesos.solicitudes;

import lombok.RequiredArgsConstructor;
import oft.optica.accesos.estado_solicitud.EstadoSolicitudEntity;
import oft.optica.accesos.estado_solicitud.EstadoSolicitudRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SolicitudScheduler {

    private static final Logger log = LoggerFactory.getLogger(SolicitudScheduler.class);

    private final SolicitudRepository solicitudRepository;
    private final EstadoSolicitudRepository estadoSolicitudRepository;

    // Se ejecuta cada 60 segundos
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void expirarCodigosVencidos() {

        LocalDateTime hace5Minutos = LocalDateTime.now().minusMinutes(5);

        // Trae todos los códigos activos del sistema (APROBADA o CORREO_FALLIDO)
        // que superaron los 5 minutos
        List<SolicitudEntity> vencidas = solicitudRepository
                .findPendientes()
                .stream()
                .filter(s -> s.getFechaSolicitud().isBefore(hace5Minutos))
                .toList();

        if (!vencidas.isEmpty()) {
            EstadoSolicitudEntity expirada = estadoSolicitudRepository
                    .findByCodigo("EXPIRADA")
                    .orElseThrow();

            vencidas.forEach(s -> s.setEstadoSolicitud(expirada));
            solicitudRepository.saveAll(vencidas);

            log.info("Códigos expirados actualizados: {}", vencidas.size());
        }
    }
}