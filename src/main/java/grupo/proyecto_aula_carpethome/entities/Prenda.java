package grupo.proyecto_aula_carpethome.entities;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prenda {
    private String idPrenda;
    private String nombrePrenda;
    private String descripcionPrenda;
    private double costoMateriales;
    private double costoTotalEstimado;
    private String idProyecto;
    private String idMedida ;

}
