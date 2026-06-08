package oft.optica.accesos.usuario;

import oft.optica.accesos.estado_usuario.EstadoUsuario;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UsuarioMapper {

    // Entidad a DTO de respuesta

    public UsuarioResponse toDTO(UsuarioEntity usuario, List<String> roles) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getUsuarioNombre(),
                usuario.getCorreoElectronico(),
                usuario.getEstadoUsuario().getCodigo(),
                usuario.getIntentosFallidos(),
                usuario.getFechaUltimoIntento(),
                usuario.getFechaAlta(),
                usuario.getFechaBaja(),
                roles
        );
    }

    public UsuarioEntity toEntity(UsuarioRequest dto, EstadoUsuario estado) {
        return UsuarioEntity.builder()
                .usuarioNombre(dto.usuarioNombre())
                .correoElectronico(dto.correoElectronico())
                .estadoUsuario(estado)
                .intentosFallidos((byte) 0)
                .build();
    }
}
