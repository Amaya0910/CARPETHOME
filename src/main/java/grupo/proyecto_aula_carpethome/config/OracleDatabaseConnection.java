package grupo.proyecto_aula_carpethome.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class OracleDatabaseConnection implements IDatabaseConnection {
    private final DatabaseConfig config;
    private Connection connection;

    public OracleDatabaseConnection(DatabaseConfig config) {
        this.config = config;
    }

    @Override
    public Connection connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }

        try {
            connection = DriverManager.getConnection(
                    config.getJdbcUrl(),  // Este método sí existe en tu Record
                    config.user(),         // Acceso directo
                    config.password()      // Acceso directo
            );

            System.out.println("""
                ✅ Conexión establecida exitosamente
                   Host: %s:%s
                   Service: %s
                   Usuario: %s
                """.formatted(config.host(), config.port(), config.service(), config.user()));

            return connection;

        } catch (SQLException e) {
            System.err.println("Error al conectar con Oracle: " + e.getMessage());
            throw new SQLException("No se pudo establecer conexión con la base de datos", e);
        }
    }

    @Override
    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            connection = null;
            System.out.println("Conexión cerrada correctamente");
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            return connect();
        }
        return connection;
    }
}