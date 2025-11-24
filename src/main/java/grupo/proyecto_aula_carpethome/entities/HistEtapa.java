package grupo.proyecto_aula_carpethome.entities;

import java.util.Date;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistEtapa {
    private String idPrenda;              // FK de prendas
    private String idEtapa;               // FK de etapas
    private Date fechaInicio;
    private Date fechaFinal;
    private String observaciones;
    private String idEmpleado;            // FK de empleados

    // NUEVAS COLUMNAS PARA IMAGEN
    private String urlImagen;             // URL o ruta de la imagen
    private String descripcionImagen;     // Descripción de la imagen
}