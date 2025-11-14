package grupo.proyecto_aula_carpethome.repositories;

import grupo.proyecto_aula_carpethome.entities.Administradores;
import grupo.proyecto_aula_carpethome.entities.Clientes;

import java.sql.SQLException;
import java.util.Optional;

public interface ClientesRepository extends Repository<Clientes, String>{
    public Optional<Clientes> findByCedula(String cedula) throws SQLException;
}
