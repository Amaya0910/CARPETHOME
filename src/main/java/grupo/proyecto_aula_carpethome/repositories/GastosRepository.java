package grupo.proyecto_aula_carpethome.repositories;

import grupo.proyecto_aula_carpethome.entities.Etapas;
import grupo.proyecto_aula_carpethome.entities.Gastos;

import java.sql.SQLException;

public interface GastosRepository extends Repository<Gastos, String>{
    public double TotalGastos(String id) throws SQLException;
}
