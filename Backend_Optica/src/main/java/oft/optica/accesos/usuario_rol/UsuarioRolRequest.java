package oft.optica.accesos.usuario_rol;

import jakarta.validation.constraints.NotNull;

// idRolActual solo se usa en actualizar — en asignar y eliminar se ignora
public record UsuarioRolRequest(
        
        @NotNull(message = "El rol nuevo es obligatorio")
        Integer idRolNuevo
) {
}
