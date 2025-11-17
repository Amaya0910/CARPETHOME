package grupo.proyecto_aula_carpethome.entities;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gasto {
    private String idGasto;
    private String nombreGasto;
    private String idPrenda;
    private double precio;
    private String descripcionGasto;
}
