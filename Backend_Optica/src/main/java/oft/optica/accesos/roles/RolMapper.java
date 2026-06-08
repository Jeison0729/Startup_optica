package oft.optica.accesos.roles;

import org.springframework.stereotype.Component;

@Component
public class RolMapper {

    public RolResponse toDTO(RolEntity rol) {
        return new RolResponse(
                rol.getId(),
                rol.getCodigo(),
                rol.getNombre(),
                rol.getDescripcion()
        );
    }

    public RolEntity toEntity(RolRequest dto) {
        return RolEntity.builder()
                .codigo(dto.codigo())
                .nombre(dto.nombre())
                .descripcion(dto.descripcion())
                .build();
    }
}
