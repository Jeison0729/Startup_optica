package oft.optica.accesos.usuario;

import lombok.RequiredArgsConstructor;
import oft.optica.accesos.usuario_rol.UsuarioRolRepository;
import oft.optica.exception.OperacionInvalidaException;
import oft.optica.exception.RecursoNoEncontradoException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UsuarioHelper {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;

    // Roles jerárquicos — DEV > ADMIN (menor índice = mayor jerarquía)
    private static final List<String> JERARQUIA = List.of("ROLE_DEV", "ROLE_ADMIN");

    public UsuarioEntity buscarPorId(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id: " + id));
    }

    public UsuarioEntity buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreoElectronico(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario autenticado no encontrado."));
    }

    public void validarJerarquia(UsuarioEntity autenticado, UsuarioEntity objetivo) {

        int nivelAutenticado = getRoles(autenticado).stream()
                .mapToInt(JERARQUIA::indexOf)
                .min()
                .orElse(999);

        int nivelObjetivo = getRoles(objetivo).stream()
                .mapToInt(JERARQUIA::indexOf)
                .min()
                .orElse(999);

        if (nivelAutenticado >= nivelObjetivo) {
            throw new OperacionInvalidaException("No tienes permisos para gestionar este usuario.");
        }
    }

    // Usa getCodigo() — campo técnico estable, no el nombre visible
    public List<String> getRoles(UsuarioEntity usuario) {
        return usuario.getUsuarioRoles().stream()
                .map(ur -> ur.getRol().getCodigo())
                .toList();
    }
}