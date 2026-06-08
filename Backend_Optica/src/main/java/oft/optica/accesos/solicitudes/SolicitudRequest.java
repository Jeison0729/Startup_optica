package oft.optica.accesos.solicitudes;

import jakarta.validation.constraints.NotBlank;

public record SolicitudRequest(

        @NotBlank(message = "El correo o usuario es obligatorio")
        String correoOUsuario
) {
}
