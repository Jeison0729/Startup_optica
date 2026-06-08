package oft.optica.accesos.solicitudes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import oft.optica.accesos.estado_solicitud.EstadoSolicitudEntity;
import oft.optica.accesos.usuario.UsuarioEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_recuperacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioEntity usuario;

    @Column(name = "codigo", nullable = false, length = 6)
    private String codigo;

    @Column(name = "fecha_solicitud", insertable = false, updatable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_uso")
    private LocalDateTime fechaUso;

    // 0 = pendiente (espera aprobación admin)
    // 1 = aprobada — código activo y enviado
    // 2 = usada por usuario
    // 3 = expirada
    // 4 = CORREO_FALLIDO (correo falló, código válido)
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado", nullable = false)
    private EstadoSolicitudEntity estadoSolicitud;
}
