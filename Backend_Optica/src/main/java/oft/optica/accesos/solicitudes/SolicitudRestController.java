package oft.optica.accesos.solicitudes;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Solicitudes de recuperación", description = "Recuperación y restablecimiento de contraseña")
@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class SolicitudRestController {

    private final SolicitudService service;
    @Operation(summary = "Solicitar recuperación de contraseña", description = "Pública — no requiere token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud registrada"),
            @ApiResponse(responseCode = "409", description = "Ya tiene una solicitud pendiente")
    })
    @PostMapping("/solicitar")
    public ResponseEntity<?> solicitar(
            @Valid @RequestBody SolicitudRequest dto, HttpServletRequest httpRequest) {

        SolicitudResponse solicitud =
                service.solicitarRecuperacion(dto.correoOUsuario(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(Map.of("mensaje", mensajePorEstado(solicitud.estado())));
    }

    @Operation(summary = "Listar solicitudes pendientes", description = "Solo ROLE_ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de solicitudes pendientes"),
            @ApiResponse(responseCode = "204", description = "Sin solicitudes pendientes"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping("/pendientes")
    public ResponseEntity<List<SolicitudResponse>> listarPendientes() {
        List<SolicitudResponse> lista = service.listarPendientes();
        if (lista.isEmpty()) return ResponseEntity.noContent().build(); // 204
        return ResponseEntity.ok(lista);
    }

    @Operation(summary = "Listar historial completo", description = "Solo ROLE_ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial de solicitudes"),
            @ApiResponse(responseCode = "204", description = "Sin solicitudes"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping
    public ResponseEntity<List<SolicitudResponse>> listarTodas() {
        List<SolicitudResponse> lista = service.listarTodas();
        if (lista.isEmpty()) return ResponseEntity.noContent().build(); // 204
        return ResponseEntity.ok(lista);
    }

    @Operation(summary = "Aprobar solicitud", description = "Admin desbloquea usuario y genera código")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud aprobada"),
            @ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
            @ApiResponse(responseCode = "409", description = "Solicitud ya procesada")
    })
    @PatchMapping("/{id}/aprobar")
    public ResponseEntity<Map<String, String>> aprobar(
            @PathVariable Integer id,HttpServletRequest httpRequest) {

        return ResponseEntity.ok(service.aprobarSolicitud(id, httpRequest.getRemoteAddr()));
    }

    @Operation(summary = "Restablecer contraseña", description = "Pública — requiere código válido")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contraseña restablecida"),
            @ApiResponse(responseCode = "422", description = "Código inválido o expirado")
    })
    @PostMapping("/restablecer")
    public ResponseEntity<Map<String, String>> restablecer(
            @Valid @RequestBody RestablecerRequest dto,HttpServletRequest httpRequest) {

        String mensaje = service.restablecerContrasena(
                dto.correoOUsuario(), dto.codigo(), dto.nuevaContrasena(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(Map.of("mensaje", mensaje));
    }

    @Operation(summary = "Reenviar código", description = "Admin reenvía cuando el correo falló — estado 4")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Código reenviado"),
            @ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
            @ApiResponse(responseCode = "409", description = "La solicitud no requiere reenvío")
    })
    @PatchMapping("/{id}/reenviar")
    public ResponseEntity<Map<String, String>> reenviar(
            @PathVariable Integer id,HttpServletRequest httpRequest) {
        service.reenviarCodigo(id,httpRequest.getRemoteAddr());
        return ResponseEntity.ok(Map.of("mensaje", "Código reenviado correctamente."));
    }

    // ─── HELPER ───────────────────────────────────────────

    private String mensajePorEstado(Integer estado) {
        return switch (estado) {
            case 1 -> "Solicitud enviada. Espera la aprobación del administrador."; // PENDIENTEWWW
            case 2 -> "Código de recuperación enviado a tu correo."; // APROBADA
            case 5 -> "No se pudo enviar el correo. Contacta al administrador."; // CORREO_FALLIDO
            default -> "Solicitud registrada.";
        };
    }

}
