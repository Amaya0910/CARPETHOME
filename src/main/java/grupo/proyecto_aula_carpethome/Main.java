package grupo.proyecto_aula_carpethome;

import grupo.proyecto_aula_carpethome.config.DatabaseConfig;
import grupo.proyecto_aula_carpethome.config.OracleDatabaseConnection;
import grupo.proyecto_aula_carpethome.repositories.AdministradorRepositoryImpl;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        var config = DatabaseConfig.builder()
                .host("localhost")
                .port("1521")
                .service("xepdb1")
                .user("U_ADMIN_CARPET")
                .password("ADMIN")
                .build();

        var dbConnection = new OracleDatabaseConnection(config);

        try {
            dbConnection.connect();
            System.out.println("Conexión exitosa!\n");


            // Aquí usamos AdministradorRepositoryImpl
            var adminRepo = new AdministradorRepositoryImpl(dbConnection);

            System.out.println("\n📋 Listado de administradores:");
            adminRepo.findAll().forEach(System.out::println);

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                dbConnection.disconnect();
            } catch (SQLException e) {
                System.err.println("Error al cerrar: " + e.getMessage());
            }
        }
    }
}