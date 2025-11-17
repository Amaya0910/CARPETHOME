package grupo.proyecto_aula_carpethome.repositories;

import grupo.proyecto_aula_carpethome.entities.Empleado;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface EmpleadoRepository extends Repository<Empleado, String>{
    public List<Empleado> findByCargo(String cargo) throws SQLException;
    public Optional<Empleado> findByCedula(String cedula) throws SQLException;
    public int countByCargo(String cargo) throws SQLException;
    public List<String> getAllCargos() throws SQLException;

}


