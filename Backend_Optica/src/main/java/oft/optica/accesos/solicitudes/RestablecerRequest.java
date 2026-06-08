package oft.optica.accesos.solicitudes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RestablecerRequest(

        @NotBlank(message = "El correo o usuario es obligatorio")
        String correoOUsuario,

        @NotBlank(message = "El código es obligatorio")
        @Size(min = 6, max = 6, message = "El código debe tener 6 dígitos")
        String codigo,

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 6, message = "Mínimo 6 caracteres")
        String nuevaContrasena

) {
}
