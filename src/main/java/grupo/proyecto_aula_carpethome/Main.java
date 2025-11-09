package grupo.proyecto_aula_carpethome;

import grupo.proyecto_aula_carpethome.config.DatabaseConfig;
import grupo.proyecto_aula_carpethome.config.OracleDatabaseConnection;
import grupo.proyecto_aula_carpethome.entities.Administrador;
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

            // Ejemplo de uso
            var admin = Administrador.builder()
                    .idUsuario("ADM001")
                    .pNombre("Carlos")
                    .pApellido("Rodríguez")
                    .correo("carlos@carpethome.com")
                    .contrasena("admin123")
                    .telefono(3101234567L)
                    .build();

            adminRepo.save(admin);

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