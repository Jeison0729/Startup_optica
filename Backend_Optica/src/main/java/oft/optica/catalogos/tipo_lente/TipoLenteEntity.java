package oft.optica.catalogos.tipo_lente;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cat_tipos_lente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoLenteEntity {

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
