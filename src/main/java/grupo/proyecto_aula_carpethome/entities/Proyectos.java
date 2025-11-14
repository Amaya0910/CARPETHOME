package grupo.proyecto_aula_carpethome.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proyectos {
    private String idProyecto;
    private String nombreProyecto;
    private String tipoProduccion;
    private Date fechaInicio;
    private Date fechaEntregaEstimada;
    private Date fechaEntregaReal;
    private String estado;
    private double costoEstimado;
    private String idCliente;


}
