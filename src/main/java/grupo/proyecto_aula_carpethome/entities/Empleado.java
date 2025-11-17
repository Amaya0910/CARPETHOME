package grupo.proyecto_aula_carpethome.entities;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)

public class Empleado extends Persona {
    private String idEmpleado;   // El id lo genera el trigger de la bd
    private String cargo;
    private String contrasena;

}

