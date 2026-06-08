package oft.optica.accesos.usuario;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Usuarios", description = "Usuarios del Sistema")
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioRestController {

    private final UsuarioService service;

    @Operation(summary = "Listar usuarios", description = "Retorna todos los usuarios del sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista los usuarios"),
            @ApiResponse(responseCode = "204", description = "Sin usuarios registrados"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar(Authentication auth) {
        List<UsuarioResponse> lista = service.listar(auth.getName());
        if (lista.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(lista);
    }

    @Operation(summary = "Obtener usuario por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @Operation(summary = "Crear usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado"),
            @ApiResponse(responseCode = "409", description = "El usuario ya existe")
    })
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request,
                                                 HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.crear(request, httpRequest.getRemoteAddr()));
    }

    @Operation(summary = "Actualizar usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "409", description = "Correo ya en uso")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody UsuarioRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(service.actualizar(id, request, httpRequest.getRemoteAddr()));
    }

    @Operation(summary = "Eliminar usuario (baja lógica)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "409", description = "El usuario ya está dado de baja")
    })
    @PatchMapping("/{id}/eliminar")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id,
                                         HttpServletRequest httpRequest) {
        service.eliminar(id, httpRequest.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Bloquear usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario bloqueado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para gestionar este usuario")
    })
    @PatchMapping("/{id}/bloquear")
    public ResponseEntity<UsuarioResponse> bloquear(
            @PathVariable Integer id,
            Authentication auth,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(service.bloquear(id, auth.getName(), httpRequest.getRemoteAddr()));
    }

    @Operation(summary = "Desbloquear usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario desbloqueado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para gestionar este usuario")
    })
    @PatchMapping("/{id}/desbloquear")
    public ResponseEntity<UsuarioResponse> desbloquear(
            @PathVariable Integer id,
            Authentication auth,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(service.desbloquear(id, auth.getName(), httpRequest.getRemoteAddr()));
    }

    @Operation(summary = "Reactivar usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario reactivado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "409", description = "El usuario no está inactivo")
    })
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<UsuarioResponse> reactivar(
            @PathVariable Integer id,
            Authentication auth,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(service.reactivar(id, auth.getName(), httpRequest.getRemoteAddr()));
    }
}