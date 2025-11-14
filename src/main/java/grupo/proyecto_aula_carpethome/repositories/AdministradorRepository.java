package grupo.proyecto_aula_carpethome.repositories;

import grupo.proyecto_aula_carpethome.entities.Administradores;

import java.sql.SQLException;
import java.util.Optional;

public interface AdministradorRepository extends Repository<Administradores, String>{
    public Optional<Administradores> findByCedula(String cedula) throws SQLException;
}
