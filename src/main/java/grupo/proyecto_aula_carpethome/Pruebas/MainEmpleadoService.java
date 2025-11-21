package grupo.proyecto_aula_carpethome.Pruebas;

import grupo.proyecto_aula_carpethome.config.DatabaseConfig;
import grupo.proyecto_aula_carpethome.config.OracleDatabaseConnection;
import grupo.proyecto_aula_carpethome.entities.Empleado;
import grupo.proyecto_aula_carpethome.repositories.EmpleadoRepositoryImpl;
import grupo.proyecto_aula_carpethome.services.EmpleadoService;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class MainEmpleadoService {
    public static void main(String[] args) {
        var config = DatabaseConfig.builder()
                .host("localhost")
                .port("1521")
                .service("xepdb1")
                .user("U_ADMIN_CARPET")
                .password("ADMIN")
                .build();

        var dbConnection = new OracleDatabaseConnection(config);
        var empleadoRepo = new EmpleadoRepositoryImpl(dbConnection);
        var empleadoService = new EmpleadoService(empleadoRepo);

        try {
            // 1. Registrar nuevo empleado
            System.out.println("═══ Registrando empleado ═══");
            Empleado nuevoEmpleado = Empleado.builder()
                    .cedula("1029385756")
                    .pNombre("Pedro")
                    .sNombre("Luis")
                    .pApellido("Ramirez")
                    .sApellido("Martinez")
                    .pCorreo("pedro.empleado@email.com")
                    .sCorreo("backup.pedro@email.com")
                    .pTelefono(3112233445L)
                    .sTelefono(3109876543L)
                    .cargo("SASTRE")
                    .contrasena("empleado123")
                    .build();

            Empleado empGuardado = empleadoService.registrarEmpleado(nuevoEmpleado);
            System.out.println("✓ Empleado registrado con ID: " + empGuardado.getIdEmpleado());

            // 2. Buscar por ID
            System.out.println("\n═══ Buscando empleado por ID ═══");
            Optional<Empleado> empleadoPorId = empleadoService.buscarPorId(empGuardado.getIdEmpleado());
            empleadoPorId.ifPresent(emp -> System.out.println("✓ Encontrado: " + emp.getNombreCompleto()));

            // 3. Buscar por cédula
            System.out.println("\n═══ Buscando empleado por cédula ═══");
            Optional<Empleado> empleadoPorCedula = empleadoService.buscarPorCedula("1029384756");
            empleadoPorCedula.ifPresent(emp -> System.out.println("✓ Encontrado: " + emp.getNombreCompleto()));

            // 4. Listar todos
            System.out.println("\n═══ Listando empleados ═══");
            List<Empleado> lista = empleadoService.listarTodos();
            for (Empleado e : lista) {
                System.out.println("  - " + e.getNombreCompleto());
            }

            // 5. Actualizar empleado
            System.out.println("\n═══ Actualizando datos de empleado ═══");
            empGuardado.setPNombre("Pedrito");
            empGuardado.setPCorreo("pedrito.nuevo@email.com");
            empleadoService.actualizarEmpleado(empGuardado);
            System.out.println("✓ Empleado actualizado. Nombre: " + empGuardado.getPNombre());

            // 6. Cambiar contraseña (validar credenciales)
            System.out.println("\n═══ Validando credenciales ═══");
            Optional<Empleado> validado = empleadoService.validarCredenciales("pedrito.nuevo@email.com", "empleado123");
            if (validado.isPresent()) {
                System.out.println("✓ Login exitoso para: " + validado.get().getNombreCompleto());
            } else {
                System.out.println("✗ Login fallido");
            }

            // 7. Contar empleados
            System.out.println("\n═══ Contando empleados ═══");
            int total = empleadoService.contarEmpleados();
            System.out.println("Total de empleados: " + total);

            // 8. Verificar existencia
            System.out.println("\n¿Existe por cédula 1029385756? " + empleadoService.existePorCedula("1029384756"));
            System.out.println("¿Existe por ID " + empGuardado.getIdEmpleado() + "? " + empleadoService.existePorId(empGuardado.getIdEmpleado()));

            // 9. Eliminar empleado
            System.out.println("\n═══ Eliminando empleado ═══");
            empleadoService.eliminarEmpleado(empGuardado.getIdEmpleado());
            System.out.println("✓ Empleado eliminado");

        } catch (SQLException e) {
            System.err.println("✗ Error SQL: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("✗ Error de validación: " + e.getMessage());
        }
    }
}