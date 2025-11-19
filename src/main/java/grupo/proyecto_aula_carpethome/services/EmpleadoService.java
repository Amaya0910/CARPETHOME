package grupo.proyecto_aula_carpethome.services;

import grupo.proyecto_aula_carpethome.Utilidades.Validador;
import grupo.proyecto_aula_carpethome.entities.Administrador;
import grupo.proyecto_aula_carpethome.entities.Empleado;
import grupo.proyecto_aula_carpethome.repositories.EmpleadoRepository;
import lombok.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class EmpleadoService {
    private final EmpleadoRepository empleadoRepository;

    private void validarEmpleado (Empleado empleado) {
        if(empleado == null) throw new IllegalArgumentException("El empleado no puede ser nulo");
        Validador.validarCedula(empleado.getCedula());
        Validador.validarNombre(empleado.getPNombre(), "Primer nombre: ");
        Validador.validarNombre(empleado.getPApellido(), "Primer Apellido: ");
        Validador.validarCorreo(empleado.getPCorreo());
        if (empleado.getPCorreo() == null || empleado.getPCorreo().trim().isEmpty()) {
            throw new IllegalArgumentException("El correo principal es obligatorio");
        }
        Validador.validarCorreo(empleado.getSCorreo());

        Validador.validarTelefono(empleado.getPTelefono());
        if (empleado.getPTelefono() == null) {
            throw new IllegalArgumentException("El teléfono principal es obligatorio");
        }
        Validador.validarTelefono(empleado.getSTelefono());
        Validador.validarContrasena(empleado.getContrasena());

    }

    private void validarEmpleadoParaActualizar(Empleado empleado) {
        validarEmpleado(empleado);

        // Validar que tenga ID
        if (empleado.getIdEmpleado() == null || empleado.getIdEmpleado().trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del administrador es obligatorio para actualizar");
        }
    }

    public Optional<Empleado> validarCredenciales(String correo, String contrasena) throws SQLException {
        if (correo == null || correo.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo es obligatorio");
        }
        if (contrasena == null || contrasena.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }

        // Buscar todos los administradores y filtrar por correo y contraseña
        List<Empleado> admins = empleadoRepository.findAll();
        return admins.stream()
                .filter(admin -> admin.getPCorreo().equals(correo) &&
                        admin.getContrasena().equals(contrasena))
                .findFirst();
    }

    public int contarEmpleados() throws SQLException {
        return empleadoRepository.findAll().size();
    }

    public Empleado registrarEmpleado(Empleado empleado) throws SQLException {
        validarEmpleado(empleado);
        return empleadoRepository.save(empleado);
    }

    public Optional<Empleado> buscarPorId(String id) throws SQLException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID no puede ser nulo o vacío");
        }
        return empleadoRepository.findById(id);
    }

    public Optional<Empleado> buscarPorCedula(String cedula) throws SQLException {
        if (cedula == null || cedula.trim().isEmpty()) {
            throw new IllegalArgumentException("La cédula no puede ser nula o vacía");
        }
        return empleadoRepository.findByCedula(cedula);
    }

    public List<Empleado> listarTodos() throws SQLException {
        return empleadoRepository.findAll();
    }

    public void actualizarEmpleado(Empleado empleado) throws SQLException {
        validarEmpleadoParaActualizar(empleado);
        empleadoRepository.update(empleado);
    }

    public void eliminarEmpleado(String id) throws SQLException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID no puede ser nulo o vacío");
        }

        Optional<Empleado> empleado = empleadoRepository.findById(id);
        if (empleado.isEmpty()) {
            throw new SQLException("No existe un empleado con el ID: " + id);
        }

        empleadoRepository.delete(id);
    }

    public boolean existePorCedula(String cedula) throws SQLException {
        if (cedula == null || cedula.trim().isEmpty()) {
            return false;
        }
        return empleadoRepository.findByCedula(cedula).isPresent();
    }


    public boolean existePorId(String id) throws SQLException {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        return empleadoRepository.findById(id).isPresent();
    }

}
