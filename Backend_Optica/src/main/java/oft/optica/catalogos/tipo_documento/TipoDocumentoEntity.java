package oft.optica.catalogos.tipo_documento;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cat_tipos_documento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoDocumentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "codigo", nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(name = "nombre", nullable = false, unique = true, length = 64)
    private String nombre;

    @Column(name = "descripcion", length = 128)
    private String descripcion;
    
}
