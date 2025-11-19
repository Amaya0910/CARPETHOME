package grupo.proyecto_aula_carpethome.entities;


import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Persona {

    private String cedula;
    private String pNombre;
    private String sNombre;
    private String pApellido;
    private String sApellido;
    private String pCorreo;
    private String sCorreo;
    private Long pTelefono;
    private Long sTelefono;

    public String getNombreCompleto() {
        StringBuilder nombre = new StringBuilder();
        nombre.append(pNombre);
        if (sNombre != null && !sNombre.isBlank()) {
            nombre.append(" ").append(sNombre);
        }
        nombre.append(" ").append(pApellido);
        if (sApellido != null && !sApellido.isBlank()) {
            nombre.append(" ").append(sApellido);
        }
        return nombre.toString();
    }


}

