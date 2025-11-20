package grupo.proyecto_aula_carpethome.Pruebas;

import grupo.proyecto_aula_carpethome.config.DatabaseConfig;
import grupo.proyecto_aula_carpethome.config.OracleDatabaseConnection;
import grupo.proyecto_aula_carpethome.entities.Administrador;
import grupo.proyecto_aula_carpethome.repositories.AdministradorRepositoryImpl;
import grupo.proyecto_aula_carpethome.services.AdministradorService;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class MainAdminService {
    public static void main(String[] args) {
        var config = DatabaseConfig.builder()
                .host("localhost")
                .port("1521")
                .service("xepdb1")
                .user("U_ADMIN_CARPET")
                .password("ADMIN")
                .build();

        var dbConnection = new OracleDatabaseConnection(config);
        var adminRepo = new AdministradorRepositoryImpl(dbConnection);
        var adminService = new AdministradorService(adminRepo);

        try {
            System.out.println("═══ Registrando administrador ═══");

            Administrador nuevoAdmin = Administrador.builder()
                    .cedula("1098765432")
                    .pNombre("Juan")
                    .sNombre("Carlos")
                    .pApellido("Torres")
                    .sApellido("Gomez")
                    .pCorreo("juan.admin@email.com")
                    .sCorreo("backup.juan@email.com")
                    .pTelefono(3109876543L)
                    .sTelefono(3171234567L)
                    .contrasena("admin123")
                    .build();

            Administrador adminGuardado = adminService.registrarAdministrador(nuevoAdmin);
            System.out.println("✓ Administrador registrado con ID: " + adminGuardado.getIdAdmin());

            // 2. Buscar por ID
            System.out.println("\n═══ Buscando por ID ═══");
            Optional<Administrador> adminPorId = adminService.buscarPorId(adminGuardado.getIdAdmin());
            if (adminPorId.isPresent()) {
                System.out.println("✓ Admin encontrado: " + adminPorId.get().getNombreCompleto());
            }

            // 3. Buscar por cédula
            System.out.println("\n═══ Buscando por cédula ═══");
            Optional<Administrador> adminPorCedula = adminService.buscarPorCedula("1098765432");
            if (adminPorCedula.isPresent()) {
                System.out.println("✓ Admin encontrado: " + adminPorCedula.get().getNombreCompleto());
            }

            // 4. Listar todos
            System.out.println("\n═══ Listando administradores ═══");
            List<Administrador> lista = adminService.listarTodos();
            for (Administrador a : lista) {
                System.out.println("  - " + a.getNombreCompleto());
            }

            // 5. Actualizar administrador
            System.out.println("\n═══ Actualizando nombres y correo ═══");
            adminGuardado.setPNombre("Juanito");
            adminGuardado.setPCorreo("juanito.nuevo@email.com");
            adminService.actualizarAdministrador(adminGuardado);
            System.out.println("✓ Admin actualizado. Nombre: " + adminGuardado.getPNombre());

            // 6. Cambiar contraseña
            System.out.println("\n═══ Cambiando contraseña ═══");
            adminService.cambiarContrasena(adminGuardado.getIdAdmin(), "admin123", "nuevaClave456");
            System.out.println("✓ Contraseña cambiada correctamente");

            // 7. Validar credenciales (login)
            System.out.println("\n═══ Validando credenciales (login) ═══");
            Optional<Administrador> login = adminService.validarCredenciales("juanito.nuevo@email.com", "nuevaClave456");
            if (login.isPresent()) {
                System.out.println("✓ Login exitoso para: " + login.get().getNombreCompleto());
            } else {
                System.out.println("✗ Login fallido");
            }

            // 8. Nombre completo
            System.out.println("\n═══ Obteniendo nombre completo por ID ═══");
            String nombreCompleto = adminService.obtenerNombreCompleto(adminGuardado.getIdAdmin());
            System.out.println("Nombre completo: " + nombreCompleto);

            // 9. Contar administradores
            System.out.println("\n═══ Contando administradores ═══");
            System.out.println("Total de administradores: " + adminService.contarAdministradores());

            // 10. Existencia por cédula e ID
            System.out.println("\n¿Existe por cédula 1098765432? " + adminService.existePorCedula("1098765432"));
            System.out.println("¿Existe por ID "+adminGuardado.getIdAdmin()+"? " + adminService.existePorId(adminGuardado.getIdAdmin()));

            // 11. Eliminar administrador
            System.out.println("\n═══ Eliminando administrador ═══");
            adminService.eliminarAdministrador(adminGuardado.getIdAdmin());
            System.out.println("✓ Administrador eliminado");

        } catch (SQLException e) {
            System.err.println("✗ Error SQL: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("✗ Error de validación: " + e.getMessage());
        }
    }
}