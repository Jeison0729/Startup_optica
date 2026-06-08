package oft.optica.accesos.roles;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RolRequest(

        @NotBlank(message = "El código del rol es obligatorio")
        @Size(max = 20)
        String codigo,

        @NotBlank(message = "El nombre del rol es obligatorio")
        @Size(max = 32)
        String nombre,

        @Size(max = 128)
        String descripcion
) {
}
