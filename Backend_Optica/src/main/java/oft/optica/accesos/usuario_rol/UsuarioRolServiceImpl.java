package oft.optica.accesos.usuario_rol;

import lombok.RequiredArgsConstructor;
import oft.optica.accesos.roles.RolEntity;
import oft.optica.accesos.roles.RolRepository;
import oft.optica.accesos.usuario.UsuarioEntity;
import oft.optica.accesos.usuario.UsuarioRepository;
import oft.optica.exception.OperacionInvalidaException;
import oft.optica.exception.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioRolServiceImpl implements UsuarioRolService {


    private final UsuarioRolRepository usuarioRolRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolMapper mapper;

    private static final List<String> JERARQUIA = List.of("ROLE_DEV", "ROLE_ADMIN", "ROLE_EMPLEADO");

    @Override
    @Transactional
    public UsuarioRolResponse cambiarRol(
            Integer idUsuario,
            Integer idRolNuevo,
            String correoAutenticado) {

        UsuarioEntity autenticado = buscarPorCorreo(correoAutenticado);
        UsuarioEntity objetivo = buscarUsuario(idUsuario);
        RolEntity rolNuevo = buscarRol(idRolNuevo);

        validarJerarquia(autenticado, objetivo);
        validarRolAsignable(rolNuevo, autenticado);

        // Elimina el rol actual (un usuario = un rol)
        usuarioRolRepository.deleteByUsuarioId(idUsuario);

        // Asigna el nuevo
        UsuarioRolEntity nueva = UsuarioRolEntity.builder()
                .usuario(objetivo)
                .rol(rolNuevo)
                .build();
        usuarioRolRepository.save(nueva);

        return mapper.toDTO(objetivo, rolNuevo, "CAMBIAR_ROL", "Rol actualizado correctamente.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioRolListadoResponse> listar() {
        return usuarioRolRepository.findAll().stream()
                .map(mapper::toListadoDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioRolListadoResponse> listarPorUsuario(Integer idUsuario) {
        return usuarioRolRepository.findByUsuarioId(idUsuario).stream()
                .map(mapper::toListadoDTO)
                .toList();
    }

    @Override
    @Transactional
    public void eliminar(Integer idUsuario, Integer idRol, String correoAutenticado) {
        UsuarioEntity autenticado = buscarPorCorreo(correoAutenticado);
        UsuarioEntity objetivo = buscarUsuario(idUsuario);

        validarJerarquia(autenticado, objetivo);

        UsuarioRolEntity asignacion = usuarioRolRepository
                .findByUsuarioIdAndRolId(idUsuario, idRol)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asignación no encontrada."));

        usuarioRolRepository.delete(asignacion);
    }


























    // ── helpers ───────────────────────────────────────────
    private UsuarioEntity buscarUsuario(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id: " + id));
    }

    private UsuarioEntity buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreoElectronico(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario autenticado no encontrado."));
    }

    private RolEntity buscarRol(Integer id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado con id: " + id));
    }

    private List<String> getRoles(UsuarioEntity usuario) {
        return usuario.getUsuarioRoles().stream()
                .map(ur -> ur.getRol().getNombre())
                .toList();
    }

    private void validarJerarquia(UsuarioEntity autenticado, UsuarioEntity objetivo) {
        int nivelAutenticado = getRoles(autenticado).stream()
                .mapToInt(JERARQUIA::indexOf)
                .filter(i -> i >= 0)
                .min()
                .orElse(999);

        int nivelObjetivo = getRoles(objetivo).stream()
                .mapToInt(JERARQUIA::indexOf)
                .filter(i -> i >= 0)
                .min()
                .orElse(999);

        if (nivelAutenticado >= nivelObjetivo) {
            throw new OperacionInvalidaException("No tienes permisos para gestionar este usuario.");
        }
    }

    private void validarRolAsignable(RolEntity rolNuevo, UsuarioEntity autenticado) {
        List<String> rolesAutenticado = getRoles(autenticado);
        String nombreRol = rolNuevo.getNombre();

        // Solo DEV puede asignar ROLE_DEV o ROLE_ADMIN
        if ((nombreRol.equals("ROLE_DEV") || nombreRol.equals("ROLE_ADMIN"))
                && !rolesAutenticado.contains("ROLE_DEV")) {
            throw new OperacionInvalidaException("Solo DEV puede asignar ese rol.");
        }
    }

}
