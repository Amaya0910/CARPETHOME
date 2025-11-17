package grupo.proyecto_aula_carpethome.repositories;

import grupo.proyecto_aula_carpethome.entities.Administrador;

import java.sql.SQLException;
import java.util.Optional;

public interface AdministradorRepository extends Repository<Administrador, String>{
    public Optional<Administrador> findByCedula(String cedula) throws SQLException;
}
