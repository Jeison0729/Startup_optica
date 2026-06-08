package oft.optica.catalogos.material;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cat_materiales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "nombre", nullable = false, unique = true, length = 64)
    private String nombre;

    @Column(name = "descripcion", length = 256)
    private String descripcion;
}
