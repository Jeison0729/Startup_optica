package oft.optica.accesos.usuario;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import oft.optica.accesos.estado_usuario.EstadoUsuario;
import oft.optica.accesos.usuario_rol.UsuarioRolEntity;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "usuario_nombre", nullable = false, length = 64)
    private String usuarioNombre;

    @Column(name = "correo_electronico", unique = true, nullable = false, length = 128)
    private String correoElectronico;

    @Column(name = "contrasena", nullable = false, length = 255)
    private String contrasena;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado_usuario", nullable = false)
    private EstadoUsuario estadoUsuario;

    @Column(name = "intentos_fallidos", columnDefinition = "SMALLINT")
    private Byte intentosFallidos;

    @Column(name = "fecha_ultimo_intento")
    private LocalDateTime fechaUltimoIntento;

    @Column(name = "fecha_alta", nullable = false, insertable = false, updatable = false)
    private LocalDateTime fechaAlta;

    @Column(name = "fecha_baja")
    private LocalDateTime fechaBaja;

    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<UsuarioRolEntity> usuarioRoles;

    public boolean isActivo() {
        return this.estadoUsuario != null &&
                "ACTIVO".equals(this.estadoUsuario.getCodigo());
    }
}
