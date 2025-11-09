package grupo.proyecto_aula_carpethome.config;

import java.sql.Connection;
import java.sql.SQLException;

public interface IDatabaseConnection {
    Connection connect() throws SQLException;
    void disconnect() throws SQLException;
}
