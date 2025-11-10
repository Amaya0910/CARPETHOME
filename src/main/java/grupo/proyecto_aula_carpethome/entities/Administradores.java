package grupo.proyecto_aula_carpethome.entities;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)


public class Administradores extends Usuarios {

    @Override
    public String toString() {
        return """
               Administrador {
                   ID: %s,
                   Nombre: %s,
                   Correo: %s,
                   Teléfono: %d
               }""".formatted(getIdUsuario(), getNombreCompleto(), getCorreo(), getTelefono());
    }
}