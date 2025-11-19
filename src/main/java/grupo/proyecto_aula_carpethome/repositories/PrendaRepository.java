package grupo.proyecto_aula_carpethome.repositories;

import grupo.proyecto_aula_carpethome.entities.Prenda;

import java.sql.SQLException;

public interface PrendaRepository extends Repository<Prenda, String>{
    public Prenda recalcularCostos(String idPrenda) throws SQLException;

}
