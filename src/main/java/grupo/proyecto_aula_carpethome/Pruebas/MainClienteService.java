package grupo.proyecto_aula_carpethome.Pruebas;

import grupo.proyecto_aula_carpethome.config.DatabaseConfig;
import grupo.proyecto_aula_carpethome.config.OracleDatabaseConnection;
import grupo.proyecto_aula_carpethome.entities.Cliente;
import grupo.proyecto_aula_carpethome.repositories.ClientesRepositoryImpl;
import grupo.proyecto_aula_carpethome.services.ClienteService;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class MainClienteService {
    public static void main(String[] args) {
        var config = DatabaseConfig.builder()
                .host("localhost")
                .port("1521")
                .service("xepdb1")
                .user("U_ADMIN_CARPET")
                .password("ADMIN")
                .build();

        var dbConnection = new OracleDatabaseConnection(config);
        var clienteRepository = new ClientesRepositoryImpl(dbConnection);
        var clienteService = new ClienteService(clienteRepository);

        try {
            // ============================================
            // 1. REGISTRAR NUEVO CLIENTE
            // ============================================
            System.out.println("═══ Registrando cliente ═══");

            Cliente nuevoCliente = Cliente.builder()
                    .cedula("1234567890")
                    .pNombre("María")
                    .sNombre("Fernanda")
                    .pApellido("González")
                    .sApellido("Pérez")
                    .pCorreo("maria@email.com")
                    .sCorreo("mgonzalez@otro.com")
                    .pTelefono(3201234567L)
                    .sTelefono(6012345678L)
                    .build();

            Cliente clienteGuardado = clienteService.registrarCliente(nuevoCliente);
            System.out.println("✓ Cliente registrado: " + clienteGuardado.getIdCliente());

            // ============================================
            // 2. BUSCAR POR CÉDULA
            // ============================================
            System.out.println("\n═══ Buscando por cédula ═══");

            Optional<Cliente> clientePorCedula = clienteService.buscarPorCedula("1234567890");
            if (clientePorCedula.isPresent()) {
                System.out.println("✓ Cliente encontrado: " + clientePorCedula.get().getNombreCompleto());
            }

            // ============================================
            // 3. BUSCAR POR NOMBRE
            // ============================================
            System.out.println("\n═══ Buscando por nombre ═══");

            List<Cliente> clientesPorNombre = clienteService.buscarPorNombre("María");
            System.out.println("Clientes encontrados: " + clientesPorNombre.size());
            clientesPorNombre.forEach(c ->
                    System.out.println("  - " + c.getNombreCompleto())
            );

            // ============================================
            // 4. BUSCAR POR CORREO
            // ============================================
            System.out.println("\n═══ Buscando por correo ═══");

            Optional<Cliente> clientePorCorreo = clienteService.buscarPorCorreo("maria@email.com");
            if (clientePorCorreo.isPresent()) {
                System.out.println("✓ Cliente encontrado: " + clientePorCorreo.get().getIdCliente());
            }

            // ============================================
            // 5. LISTAR ORDENADOS POR NOMBRE
            // ============================================
            System.out.println("\n═══ Listando clientes ordenados ═══");

            List<Cliente> clientesOrdenados = clienteService.listarOrdenadosPorNombre();
            clientesOrdenados.forEach(c ->
                    System.out.println("  - " + c.getNombreCompleto())
            );

            // ============================================
            // 6. OBTENER RESUMEN
            // ============================================
            System.out.println("\n═══ Resumen del cliente ═══");

            String resumen = clienteService.obtenerResumenCliente(clienteGuardado.getIdCliente());
            System.out.println(resumen);



            // ============================================
            // 8. ACTUALIZAR CORREO
            // ============================================
            System.out.println("═══ Actualizando correo ═══");

            clienteService.actualizarCorreo(clienteGuardado.getIdCliente(), "maria.nuevo@email.com");
            System.out.println("✓ Correo actualizado");

            // ============================================
            // 9. CONTAR CLIENTES
            // ============================================
            System.out.println("\n═══ Contando clientes ═══");

            int total = clienteService.contarClientes();
            System.out.println("Total de clientes: " + total);

            // ============================================
            // 10. ELIMINAR CLIENTE
            // ============================================
            System.out.println("\n═══ Eliminando cliente ═══");

            clienteService.eliminarCliente(clienteGuardado.getIdCliente());
            System.out.println("✓ Cliente eliminado");

        } catch (SQLException e) {
            System.err.println("✗ Error SQL: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("✗ Error de validación: " + e.getMessage());
        }
    }
}