package grupo.proyecto_aula_carpethome.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class OracleDatabaseConnection implements IDatabaseConnection {
    private final DatabaseConfig config;
    private Connection connection;


    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
        return connection;
    }


    public OracleDatabaseConnection(DatabaseConfig config) {
        this.config = config;
    }

    @Override
    public Connection connect() throws SQLException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection(
                    config.getJdbcUrl(),
                    config.getUser(),
                    config.getPassword()
            );
            System.out.println("Conexión establecida con Oracle");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró el driver JDBC de Oracle", e);
        }
        return connection;
    }

    @Override
    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("Conexión cerrada correctamente");
        }
    }
}
