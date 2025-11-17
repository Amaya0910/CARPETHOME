package grupo.proyecto_aula_carpethome.entities;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)

public class Administrador extends Persona {
    private String idAdmin;      // Esto lo genera el trigger
    private String contrasena;


}

