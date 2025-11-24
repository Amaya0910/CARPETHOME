package grupo.proyecto_aula_carpethome.entities;

import java.util.Date;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistEtapa {
    private String idPrenda;        // FK de prendas
    private String idEtapa;         // FK de etapas
    private Date fechaInicio;
    private Date fechaFinal;
    private String observaciones;
    private String idEmpleado;      // NUEVO: FK de empleados
}