package oft.optica.accesos.roles;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Roles", description = "Roles del Sistema")
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolRestController {

    private final RolService service;

    @Operation(summary = "Listar roles", description = "Retorna todos los roles del sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de roles"),
            @ApiResponse(responseCode = "204", description = "Sin roles registrados"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping
    public ResponseEntity<List<RolResponse>> listar() {
        List<RolResponse> lista = service.listar();
        if (lista.isEmpty()) return ResponseEntity.noContent().build(); //204
        return ResponseEntity.ok(lista);
    }

    @Operation(summary = "Obtener rol por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rol encontrado"),
            @ApiResponse(responseCode = "404", description = "Rol no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<RolResponse> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }


    @Operation(summary = "Actualizar rol")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rol actualizado"),
            @ApiResponse(responseCode = "404", description = "Rol no encontrado"),
            @ApiResponse(responseCode = "409", description = "Nombre ya en uso")
    })
    @PutMapping("/{id}")
    public ResponseEntity<RolResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody RolRequest request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }
}
