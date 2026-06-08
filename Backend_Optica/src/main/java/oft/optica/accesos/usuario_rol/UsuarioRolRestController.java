package oft.optica.accesos.usuario_rol;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Usuarios - Roles", description = "Gestión de roles de usuarios")
@RestController
@RequestMapping("/api/usuarios-roles")
@RequiredArgsConstructor
public class UsuarioRolRestController {
    private final UsuarioRolService service;

    @Operation(summary = "Listar todas las asignaciones")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida"),
            @ApiResponse(responseCode = "204", description = "Sin asignaciones")
    })
    @GetMapping
    public ResponseEntity<List<UsuarioRolListadoResponse>> listar() {
        List<UsuarioRolListadoResponse> lista = service.listar();
        if (lista.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(lista);
    }

    @Operation(summary = "Listar roles de un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Roles del usuario"),
            @ApiResponse(responseCode = "204", description = "Sin roles asignados")
    })
    @GetMapping("/{idUsuario}")
    public ResponseEntity<List<UsuarioRolListadoResponse>> listarPorUsuario(@PathVariable Integer idUsuario) {
        List<UsuarioRolListadoResponse> lista = service.listarPorUsuario(idUsuario);
        if (lista.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(lista);
    }

    @Operation(summary = "Cambiar rol de un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rol cambiado"),
            @ApiResponse(responseCode = "404", description = "Usuario o rol no encontrado"),
            @ApiResponse(responseCode = "409", description = "Sin permisos para gestionar este usuario")
    })
    @PatchMapping("/{idUsuario}/rol")
    public ResponseEntity<UsuarioRolResponse> cambiarRol(
            @PathVariable Integer idUsuario,
            @Valid @RequestBody UsuarioRolRequest dto,
            Authentication auth) {
        return ResponseEntity.ok(service.cambiarRol(idUsuario, dto.idRolNuevo(), auth.getName()));
    }

    @Operation(summary = "Eliminar rol de un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Rol eliminado"),
            @ApiResponse(responseCode = "404", description = "Asignación no encontrada"),
            @ApiResponse(responseCode = "409", description = "Sin permisos")
    })
    @DeleteMapping("/{idUsuario}/roles/{idRol}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Integer idUsuario,
            @PathVariable Integer idRol,
            Authentication auth) {
        service.eliminar(idUsuario, idRol, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
