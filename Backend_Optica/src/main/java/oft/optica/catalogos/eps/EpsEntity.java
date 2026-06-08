package oft.optica.catalogos.eps;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cat_eps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EpsEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "nombre", nullable = false, unique = true, length = 128)
    private String nombre;

    @Column(name = "descripcion", length = 256)
    private String descripcion;
}
