package grupo.proyecto_aula_carpethome.repositories;

import grupo.proyecto_aula_carpethome.entities.Cliente;

import java.sql.SQLException;
import java.util.Optional;

public interface ClientesRepository extends Repository<Cliente, String>{
    public Optional<Cliente> findByCedula(String cedula) throws SQLException;
    public Optional<String> findIdClienteByCedula(String cedula) throws SQLException;
}
