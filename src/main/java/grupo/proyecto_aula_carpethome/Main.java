package grupo.proyecto_aula_carpethome;

import grupo.proyecto_aula_carpethome.config.DatabaseConfig;
import grupo.proyecto_aula_carpethome.config.OracleDatabaseConnection;
import grupo.proyecto_aula_carpethome.entities.Administrador;
import grupo.proyecto_aula_carpethome.repositories.AdministradorRepositoryImpl;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

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
        AdministradorRepositoryImpl adminRepository = new AdministradorRepositoryImpl(dbConnection);

        try {
            System.out.println("╔════════════════════════════════════════════════════╗");
            System.out.println("║  PRUEBAS DE ADMINISTRADOR REPOSITORY               ║");
            System.out.println("╚════════════════════════════════════════════════════╝\n");

            // ============================================
            // TEST 1: GUARDAR UN NUEVO ADMINISTRADOR
            // ============================================
            System.out.println("═══ TEST 1: Guardar nuevo administrador ═══");

            Administrador nuevoAdmin = Administrador.builder()
                    .cedula("1234567890")
                    .pNombre("Carlos")
                    .sNombre("Alberto")
                    .pApellido("García")
                    .sApellido("López")
                    .pCorreo("carlos@carpethome.com")
                    .sCorreo("cgarcia@personal.com")
                    .pTelefono(3201234567L)
                    .sTelefono(6012345678L)
                    .contrasena("admin123")
                    .build();

            System.out.println("Insertando administrador:");
            System.out.println("  Nombre: " + nuevoAdmin.getPNombre() + " " + nuevoAdmin.getPApellido());
            System.out.println("  Cédula: " + nuevoAdmin.getCedula());
            System.out.println("  Correo: " + nuevoAdmin.getPCorreo());

            Administrador adminGuardado = adminRepository.save(nuevoAdmin);

            System.out.println("\n✓ Administrador guardado exitosamente");
            System.out.println("  ID generado: " + adminGuardado.getIdAdmin());
            System.out.println("  Nombre completo: " + adminGuardado.getNombreCompleto());

            String idAdminCreado = adminGuardado.getIdAdmin();

            // ============================================
            // TEST 2: BUSCAR POR ID
            // ============================================
            System.out.println("\n\n═══ TEST 2: Buscar administrador por ID ═══");
            System.out.println("Buscando ID: " + idAdminCreado);

            Optional<Administrador> adminEncontrado = adminRepository.findById(idAdminCreado);

            if (adminEncontrado.isPresent()) {
                Administrador admin = adminEncontrado.get();
                System.out.println("\n✓ Administrador encontrado:");
                System.out.println("  ID: " + admin.getIdAdmin());
                System.out.println("  Nombre: " + admin.getNombreCompleto());
                System.out.println("  Cédula: " + admin.getCedula());
                System.out.println("  Correo: " + admin.getPCorreo());
                System.out.println("  Teléfono: " + admin.getPTelefono());
            } else {
                System.out.println("✗ No se encontró el administrador");
            }

            // ============================================
            // TEST 3: BUSCAR POR CÉDULA
            // ============================================
            System.out.println("\n\n═══ TEST 3: Buscar administrador por cédula ═══");
            System.out.println("Buscando cédula: " + nuevoAdmin.getCedula());

            Optional<Administrador> adminPorCedula = adminRepository.findByCedula(nuevoAdmin.getCedula());

            if (adminPorCedula.isPresent()) {
                Administrador admin = adminPorCedula.get();
                System.out.println("\n✓ Administrador encontrado:");
                System.out.println("  ID: " + admin.getIdAdmin());
                System.out.println("  Nombre: " + admin.getNombreCompleto());
            } else {
                System.out.println("✗ No se encontró el administrador");
            }

            // ============================================
            // TEST 4: LISTAR TODOS LOS ADMINISTRADORES
            // ============================================
            System.out.println("\n\n═══ TEST 4: Listar todos los administradores ═══");

            List<Administrador> todosLosAdmins = adminRepository.findAll();

            System.out.println("Total de administradores: " + todosLosAdmins.size());
            System.out.println("\nLista:");

            for (int i = 0; i < todosLosAdmins.size(); i++) {
                Administrador admin = todosLosAdmins.get(i);
                System.out.println("  " + (i + 1) + ". " + admin.getIdAdmin() + " - " +
                        admin.getNombreCompleto() + " (" + admin.getCedula() + ")");
            }

            // ============================================
            // TEST 5: ACTUALIZAR ADMINISTRADOR
            // ============================================
            System.out.println("\n\n═══ TEST 5: Actualizar administrador ═══");

            adminGuardado.setSNombre("José");
            adminGuardado.setPCorreo("carlos.garcia.nuevo@carpethome.com");
            adminGuardado.setContrasena("nuevaContrasena456");
            adminGuardado.setPTelefono(3109876543L);

            System.out.println("Actualizando datos:");
            System.out.println("  Nuevo segundo nombre: " + adminGuardado.getSNombre());
            System.out.println("  Nuevo correo: " + adminGuardado.getPCorreo());
            System.out.println("  Nuevo teléfono: " + adminGuardado.getPTelefono());

            adminRepository.update(adminGuardado);

            // Verificar actualización
            Optional<Administrador> adminActualizado = adminRepository.findById(idAdminCreado);
            if (adminActualizado.isPresent()) {
                Administrador admin = adminActualizado.get();
                System.out.println("\n✓ Administrador actualizado:");
                System.out.println("  Nombre completo: " + admin.getNombreCompleto());
                System.out.println("  Correo: " + admin.getPCorreo());
                System.out.println("  Teléfono: " + admin.getPTelefono());
            }

            // ============================================
            // TEST 6: ELIMINAR ADMINISTRADOR
            // ============================================
            System.out.println("\n\n═══ TEST 6: Eliminar administrador ═══");
            System.out.println("Eliminando ID: " + idAdminCreado);

            adminRepository.delete(idAdminCreado);

            // Verificar eliminación
            Optional<Administrador> adminEliminado = adminRepository.findById(idAdminCreado);

            if (adminEliminado.isEmpty()) {
                System.out.println("\n✓ Administrador eliminado correctamente");
            } else {
                System.out.println("\n✗ Error: El administrador aún existe");
            }

        } catch (Exception e) {
            System.err.println("\n✗ ERROR INESPERADO:");
            e.printStackTrace();
        }
    }
}