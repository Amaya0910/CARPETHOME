package grupo.proyecto_aula_carpethome.entities;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Usuarios {
    private String idUsuario;      // Mapea a: id_usuario
    private String pNombre;        // Mapea a: p_nombre
    private String sNombre;        // Mapea a: s_nombre
    private String pApellido;      // Mapea a: p_apellido
    private String sApellido;      // Mapea a: s_apellido
    private String correo;         // Mapea a: correo
    private String contrasena;     // Mapea a: contrasena
    private Long telefono;         // Mapea a: telefono

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

    @Override
    public String toString() {
        return """
               Usuario {
                   ID: %s,
                   Nombre: %s,
                   Correo: %s,
                   Teléfono: %d
               }""".formatted(idUsuario, getNombreCompleto(), correo, telefono);
    }
}