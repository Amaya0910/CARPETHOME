package grupo.proyecto_aula_carpethome.services;

import grupo.proyecto_aula_carpethome.Utilidades.Validador;
import grupo.proyecto_aula_carpethome.entities.Administrador;
import grupo.proyecto_aula_carpethome.repositories.AdministradorRepository;
import lombok.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class AdministradorService {
    private final AdministradorRepository administradorRepository;


    public Administrador registrarAdministrador(Administrador administrador) throws SQLException {
        validarAdministrador(administrador);
        return administradorRepository.save(administrador);
    }


    public Optional<Administrador> buscarPorId(String id) throws SQLException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID no puede ser nulo o vacío");
        }
        return administradorRepository.findById(id);
    }

    public Optional<Administrador> buscarPorCedula(String cedula) throws SQLException {
        if (cedula == null || cedula.trim().isEmpty()) {
            throw new IllegalArgumentException("La cédula no puede ser nula o vacía");
        }
        return administradorRepository.findByCedula(cedula);
    }


    public List<Administrador> listarTodos() throws SQLException {
        return administradorRepository.findAll();
    }

    public void actualizarAdministrador(Administrador administrador) throws SQLException {
        validarAdministradorParaActualizar(administrador);
        administradorRepository.update(administrador);
    }

    public void eliminarAdministrador(String id) throws SQLException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID no puede ser nulo o vacío");
        }

        Optional<Administrador> admin = administradorRepository.findById(id);
        if (admin.isEmpty()) {
            throw new SQLException("No existe un administrador con el ID: " + id);
        }

        administradorRepository.delete(id);
    }


    public boolean existePorCedula(String cedula) throws SQLException {
        if (cedula == null || cedula.trim().isEmpty()) {
            return false;
        }
        return administradorRepository.findByCedula(cedula).isPresent();
    }


    public boolean existePorId(String id) throws SQLException {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        return administradorRepository.findById(id).isPresent();
    }


    private void validarAdministrador(Administrador administrador) {
        if (administrador == null) {
            throw new IllegalArgumentException("El administrador no puede ser nulo");
        }

        Validador.validarCedula(administrador.getCedula());

        Validador.validarNombre(administrador.getPNombre(), "Primer nombre: ");
        Validador.validarNombre(administrador.getPApellido(), "Primer Apellido: ");

        Validador.validarCorreo(administrador.getPCorreo());
        if (administrador.getPCorreo() == null || administrador.getPCorreo().trim().isEmpty()) {
            throw new IllegalArgumentException("El correo principal es obligatorio");
        }
        Validador.validarCorreo(administrador.getSCorreo());

        Validador.validarTelefono(administrador.getPTelefono());
        if (administrador.getPTelefono() == null) {
            throw new IllegalArgumentException("El teléfono principal es obligatorio");
        }
        Validador.validarTelefono(administrador.getSTelefono());
        Validador.validarContrasena(administrador.getContrasena());
    }


    private void validarAdministradorParaActualizar(Administrador administrador) {
        validarAdministrador(administrador);

        // Validar que tenga ID
        if (administrador.getIdAdmin() == null || administrador.getIdAdmin().trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del administrador es obligatorio para actualizar");
        }
    }


    public void cambiarContrasena(String idAdmin, String contrasenaActual, String nuevaContrasena) throws SQLException {
        if (idAdmin == null || idAdmin.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del administrador es obligatorio");
        }
        if (nuevaContrasena == null || nuevaContrasena.trim().isEmpty()) {
            throw new IllegalArgumentException("La nueva contraseña es obligatoria");
        }
        if (nuevaContrasena.length() < 6) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 6 caracteres");
        }
        if (nuevaContrasena.length() > 15) {
            throw new IllegalArgumentException("La nueva contraseña no puede tener más de 15 caracteres");
        }

        Optional<Administrador> adminOpt = administradorRepository.findById(idAdmin);
        if (adminOpt.isEmpty()) {
            throw new SQLException("No existe un administrador con el ID: " + idAdmin);
        }

        Administrador admin = adminOpt.get();

        if (!admin.getContrasena().equals(contrasenaActual)) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        admin.setContrasena(nuevaContrasena);
        administradorRepository.update(admin);
    }


    public Optional<Administrador> validarCredenciales(String correo, String contrasena) throws SQLException {
//        if (correo == null || correo.trim().isEmpty()) {
//            throw new IllegalArgumentException("El correo es obligatorio");
//        }
//        if (contrasena == null || contrasena.trim().isEmpty()) {
//            throw new IllegalArgumentException("La contraseña es obligatoria");
//        }

        // Buscar todos los administradores y filtrar por correo y contraseña
        List<Administrador> admins = administradorRepository.findAll();
        return admins.stream()
                .filter(admin -> admin.getPCorreo().equals(correo) &&
                        admin.getContrasena().equals(contrasena))
                .findFirst();
    }


    public String obtenerNombreCompleto(String id) throws SQLException {
        Optional<Administrador> adminOpt = buscarPorId(id);
        if (adminOpt.isEmpty()) {
            throw new SQLException("No existe un administrador con el ID: " + id);
        }
        return adminOpt.get().getNombreCompleto();
    }


    public int contarAdministradores() throws SQLException {
        return administradorRepository.findAll().size();
    }
}