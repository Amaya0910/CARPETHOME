package grupo.proyecto_aula_carpethome.entities;

import java.util.Date;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistEtapa {
    private String idProyecto;      // FK de proyectos
    private String idEtapa;         // FK de etapas
    private Date fechaInicio;
    private Date fechaFinal;
    private String observaciones;
}
