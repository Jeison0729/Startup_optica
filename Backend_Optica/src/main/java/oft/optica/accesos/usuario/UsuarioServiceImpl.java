package oft.optica.accesos.usuario;

import lombok.RequiredArgsConstructor;
import oft.optica.accesos.estado_usuario.EstadoUsuario;
import oft.optica.accesos.estado_usuario.EstadoUsuarioRepository;
import oft.optica.accesos.roles.RolEntity;
import oft.optica.accesos.roles.RolRepository;
import oft.optica.accesos.usuario_rol.UsuarioRolEntity;
import oft.optica.accesos.usuario_rol.UsuarioRolRepository;
import oft.optica.auditorias.AccionLog;
import oft.optica.auditorias.LogAuditoriaService;
import oft.optica.exception.DuplicadoException;
import oft.optica.exception.OperacionInvalidaException;
import oft.optica.exception.RecursoNoEncontradoException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper mapper;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final LogAuditoriaService logService;
    private final EstadoUsuarioRepository estadoUsuarioRepository;
    private final UsuarioHelper usuarioHelper;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar(String correoAutenticado) {
        // WebSecurityConfig garantiza que solo ROLE_DEV y ROLE_ADMIN llegan aquí
        return repository.findAll().stream()
                .map(usuario -> mapper.toDTO(usuario, usuarioHelper.getRoles(usuario)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Integer id) {
        UsuarioEntity usuario = usuarioHelper.buscarPorId(id);
        return mapper.toDTO(usuario, usuarioHelper.getRoles(usuario));
    }
    
    @Override
    @Transactional
    public UsuarioResponse crear(UsuarioRequest request, String ip) {

        if (repository.findByCorreoElectronico(request.correoElectronico()).isPresent()) {
            throw new DuplicadoException("El correo ya está registrado.");
        }

        EstadoUsuario estado = estadoUsuarioRepository.findById(1)
                .orElseThrow(() -> new RecursoNoEncontradoException("Estado no encontrado."));

        // Solo existe ROLE_ADMIN como rol asignable — ROLE_DEV es exclusivo del sistema
        RolEntity rol = rolRepository.findByCodigo("ROLE_ADMIN")
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado."));

        UsuarioEntity usuario = mapper.toEntity(request, estado);
        usuario.setContrasena(passwordEncoder.encode(request.contrasena()));
        UsuarioEntity guardado = repository.save(usuario);

        UsuarioRolEntity relacion = UsuarioRolEntity.builder()
                .usuario(guardado)
                .rol(rol)
                .build();
        usuarioRolRepository.save(relacion);

        logService.registrar(
                guardado, "usuarios", guardado.getId(), AccionLog.USUARIO_CREADO,
                "Usuario creado: " + guardado.getCorreoElectronico(), ip, true);

        return mapper.toDTO(guardado, List.of(rol.getCodigo()));
    }

    @Override
    @Transactional
    public UsuarioResponse actualizar(Integer id, UsuarioRequest request, String ip) {

        UsuarioEntity usuario = usuarioHelper.buscarPorId(id);

        // Validar que el nuevo correo no pertenezca a otro usuario
        repository.findByCorreoElectronico(request.correoElectronico())
                .ifPresent(existente -> {
                    if (!existente.getId().equals(id)) {
                        throw new DuplicadoException("El correo ya está en uso por otro usuario.");
                    }
                });

        usuario.setUsuarioNombre(request.usuarioNombre());
        usuario.setCorreoElectronico(request.correoElectronico());

        UsuarioEntity guardado = repository.save(usuario);

        logService.registrar(
                guardado, "usuarios", guardado.getId(), AccionLog.USUARIO_ACTUALIZADO,
                "Usuario actualizado: " + guardado.getCorreoElectronico(), ip, true);

        return mapper.toDTO(guardado, usuarioHelper.getRoles(guardado));
    }

    @Override
    @Transactional
    public void eliminar(Integer id, String ip) {

        UsuarioEntity usuario = usuarioHelper.buscarPorId(id);

        if (!usuario.isActivo()) {
            throw new OperacionInvalidaException("El usuario ya está dado de baja.");
        }

        EstadoUsuario inactivo = estadoUsuarioRepository.findByCodigo("INACTIVO")
                .orElseThrow(() -> new RecursoNoEncontradoException("Estado no encontrado."));

        usuario.setEstadoUsuario(inactivo);
        usuario.setFechaBaja(LocalDateTime.now());

        UsuarioEntity guardado = repository.save(usuario);

        logService.registrar(
                guardado, "usuarios", guardado.getId(), AccionLog.USUARIO_DESACTIVADO,
                "Baja lógica de usuario: " + guardado.getCorreoElectronico(), ip, true);
    }

    @Override
    @Transactional
    public UsuarioResponse bloquear(Integer id, String correoAutenticado, String ip) {

        UsuarioEntity autenticado = usuarioHelper.buscarPorCorreo(correoAutenticado);
        UsuarioEntity objetivo    = usuarioHelper.buscarPorId(id);
        usuarioHelper.validarJerarquia(autenticado, objetivo);

        EstadoUsuario bloqueado = estadoUsuarioRepository.findByCodigo("BLOQUEADO")
                .orElseThrow(() -> new RecursoNoEncontradoException("Estado no encontrado."));

        objetivo.setEstadoUsuario(bloqueado);
        UsuarioEntity guardado = repository.save(objetivo);

        logService.registrar(
                autenticado, "usuarios", objetivo.getId(), AccionLog.CUENTA_BLOQUEADA,
                "Admin. bloqueó a: " + objetivo.getCorreoElectronico(), ip, true);

        return mapper.toDTO(guardado, usuarioHelper.getRoles(guardado));
    }

    @Override
    @Transactional
    public UsuarioResponse desbloquear(Integer id, String correoAutenticado, String ip) {

        UsuarioEntity autenticado = usuarioHelper.buscarPorCorreo(correoAutenticado);
        UsuarioEntity objetivo    = usuarioHelper.buscarPorId(id);
        usuarioHelper.validarJerarquia(autenticado, objetivo);

        EstadoUsuario activo = estadoUsuarioRepository.findByCodigo("ACTIVO")
                .orElseThrow(() -> new RecursoNoEncontradoException("Estado no encontrado."));

        objetivo.setEstadoUsuario(activo);
        objetivo.setIntentosFallidos((byte) 0);
        UsuarioEntity guardado = repository.save(objetivo);

        logService.registrar(
                autenticado, "usuarios", guardado.getId(), AccionLog.CUENTA_DESBLOQUEADA,
                "Admin. desbloqueó a: " + guardado.getCorreoElectronico(), ip, true);

        return mapper.toDTO(guardado, usuarioHelper.getRoles(guardado));
    }

    @Override
    @Transactional
    public UsuarioResponse reactivar(Integer id, String correoAutenticado, String ip) {

        UsuarioEntity autenticado = usuarioHelper.buscarPorCorreo(correoAutenticado);
        UsuarioEntity objetivo    = usuarioHelper.buscarPorId(id);
        usuarioHelper.validarJerarquia(autenticado, objetivo);

        if (!objetivo.getEstadoUsuario().getCodigo().equals("INACTIVO")) {
            throw new OperacionInvalidaException("Solo se pueden reactivar usuarios inactivos.");
        }

        EstadoUsuario activo = estadoUsuarioRepository.findByCodigo("ACTIVO")
                .orElseThrow(() -> new RecursoNoEncontradoException("Estado no encontrado."));

        objetivo.setEstadoUsuario(activo);
        objetivo.setFechaBaja(null);
        objetivo.setIntentosFallidos((byte) 0);

        UsuarioEntity guardado = repository.save(objetivo);

        logService.registrar(
                autenticado, "usuarios", guardado.getId(), AccionLog.USUARIO_REACTIVADO,
                "Usuario reactivado: " + guardado.getCorreoElectronico(), ip, true);

        return mapper.toDTO(guardado, usuarioHelper.getRoles(guardado));
    }
}