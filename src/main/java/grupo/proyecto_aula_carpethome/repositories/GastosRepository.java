package grupo.proyecto_aula_carpethome.repositories;

import grupo.proyecto_aula_carpethome.entities.Gasto;

import java.sql.SQLException;
import java.util.List;

public interface GastosRepository extends Repository<Gasto, String>{
    public double TotalGastos(String id) throws SQLException;
    public List<Gasto> findByPrenda(String idPrenda) throws SQLException;

}
