package grupo.proyecto_aula_carpethome.entities;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)

public class Empleados extends Usuarios {
    private String cargo;

    @Override
    public String toString() {
        return """
               Empleado {
                   ID: %s,
                   Nombre: %s,
                   Cargo: %s,
                   Correo: %s,
                   Teléfono: %d
               }""".formatted(getIdUsuario(), getNombreCompleto(), cargo, getCorreo(), getTelefono());
    }
}