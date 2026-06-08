package oft.optica.accesos.usuario_rol;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import oft.optica.accesos.roles.RolEntity;
import oft.optica.accesos.usuario.UsuarioEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios_roles",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"id_usuario", "id_rol"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRolEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioEntity usuario;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol", nullable = false)
    private RolEntity rol;

    // BD maneja el valor con DEFAULT CURRENT_TIMESTAMP
    @Column(name = "fecha_asignacion", nullable = false, insertable = false, updatable = false)
    private LocalDateTime fechaAsignacion;
}
