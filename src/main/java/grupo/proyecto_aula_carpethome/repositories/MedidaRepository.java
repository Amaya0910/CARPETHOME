package grupo.proyecto_aula_carpethome.repositories;

import grupo.proyecto_aula_carpethome.entities.Medida;

import java.sql.SQLException;
import java.util.List;

public interface MedidaRepository extends Repository<Medida, String>{
    public List<Medida> findByTipoMedida(String tipo) throws SQLException;
}
