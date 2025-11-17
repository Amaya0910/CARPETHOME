package grupo.proyecto_aula_carpethome.repositories;

import grupo.proyecto_aula_carpethome.entities.Gasto;

import java.sql.SQLException;

public interface GastosRepository extends Repository<Gasto, String>{
    public double TotalGastos(String id) throws SQLException;
}
