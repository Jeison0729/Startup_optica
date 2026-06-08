package oft.optica.accesos.roles;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cat_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "nombre", nullable = false, unique = true, length = 32)
    private String nombre;

    @Column(name = "descripcion", length = 128)
    private String descripcion;


}
