package oft.optica.catalogos.parentesco;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cat_parentescos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentescoEntity {
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
