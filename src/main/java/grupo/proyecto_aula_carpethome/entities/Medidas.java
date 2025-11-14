package grupo.proyecto_aula_carpethome.entities;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medidas {
    private String idMedida;
    private String nombreMedida;
    private String tipoMedida;
    private double cBusto;
    private double cCintura;
    private double cCadera;
    private double alturaBusto;
    private double separacionBusto;
    private double radioBusto;
    private double bajoBusto;
    private double largoFalda;
    private double largoCadera;
    private double largoVestido;
    private double largoPantalon;
    private double largoManga;

}
