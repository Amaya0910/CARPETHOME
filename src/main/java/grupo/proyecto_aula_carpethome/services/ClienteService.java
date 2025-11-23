package grupo.proyecto_aula_carpethome.services;

import grupo.proyecto_aula_carpethome.Utilidades.Validador;
import grupo.proyecto_aula_carpethome.entities.Cliente;
import grupo.proyecto_aula_carpethome.repositories.ClientesRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ClienteService {
    private final ClientesRepository clientesRepository;

    public String obtenerIdClientePorCedula(String cedula) throws SQLException {
        Validador.validarCedula(cedula);

        Optional<String> idOpt = clientesRepository.findIdClienteByCedula(cedula);

        if (idOpt.isEmpty()) {
            throw new SQLException("No existe un cliente registrado con la cédula: " + cedula);
        }

        return idOpt.get();
    }

    public Cliente registrarCliente(Cliente cliente) throws SQLException {
        validarCliente(cliente);

        // MEJORA: Verificar que no exista ya un cliente con esa cédula
        if (existePorCedula(cliente.getCedula())) {
            throw new SQLException("Ya existe un cliente registrado con la cédula: " + cliente.getCedula());
        }

        return clientesRepository.save(cliente);
    }

    public Optional<Cliente> buscarPorId(String id) throws SQLException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID no puede ser nulo o vacío");
        }
        return clientesRepository.findById(id);
    }

    public Optional<Cliente> buscarPorCedula(String cedula) throws SQLException {
        Validador.validarCedula(cedula);
        return clientesRepository.findByCedula(cedula);
    }

    public List<Cliente> listarTodos() throws SQLException {
        return clientesRepository.findAll();
    }

    public void actualizarCliente(Cliente cliente) throws SQLException {
        validarClienteParaActualizar(cliente);

        // MEJORA: Verificar que el cliente existe
        Optional<Cliente> clienteExistente = buscarPorId(cliente.getIdCliente());
        if (clienteExistente.isEmpty()) {
            throw new SQLException("No existe un cliente con el ID: " + cliente.getIdCliente());
        }

        // MEJORA: Si cambió la cédula, verificar que no esté en uso
        if (!clienteExistente.get().getCedula().equals(cliente.getCedula())) {
            if (existePorCedula(cliente.getCedula())) {
                throw new SQLException("Ya existe un cliente con la cédula: " + cliente.getCedula());
            }
        }

        clientesRepository.update(cliente);
    }

    public void eliminarCliente(String id) throws SQLException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID no puede ser nulo o vacío");
        }

        // Verificar que existe antes de eliminar
        Optional<Cliente> cliente = clientesRepository.findById(id);
        if (cliente.isEmpty()) {
            throw new SQLException("No existe un cliente con el ID: " + id);
        }

        clientesRepository.delete(id);
    }

    public boolean existePorCedula(String cedula) throws SQLException {
        if (cedula == null || cedula.trim().isEmpty()) {
            return false;
        }
        return clientesRepository.findByCedula(cedula).isPresent();
    }

    public boolean existePorId(String id) throws SQLException {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        return clientesRepository.findById(id).isPresent();
    }

    // MEJORA: Búsqueda más flexible
    public List<Cliente> buscarPorNombre(String nombre) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de búsqueda no puede estar vacío");
        }

        List<Cliente> todosLosClientes = clientesRepository.findAll();
        String nombreBusqueda = nombre.toLowerCase().trim();

        return todosLosClientes.stream()
                .filter(cliente -> {
                    String nombreCompleto = cliente.getNombreCompleto().toLowerCase();
                    return nombreCompleto.contains(nombreBusqueda);
                })
                .collect(Collectors.toList());
    }

    // MEJORA: Búsqueda combinada (nombre, cédula o correo)
    public List<Cliente> buscarPorTexto(String texto) throws SQLException {
        if (texto == null || texto.trim().isEmpty()) {
            return listarTodos();
        }

        List<Cliente> todosLosClientes = clientesRepository.findAll();
        String textoBusqueda = texto.toLowerCase().trim();

        return todosLosClientes.stream()
                .filter(cliente -> {
                    String nombreCompleto = cliente.getNombreCompleto().toLowerCase();
                    String cedula = cliente.getCedula().toLowerCase();
                    String correo = cliente.getPCorreo().toLowerCase();
                    String correo2 = cliente.getSCorreo() != null ? cliente.getSCorreo().toLowerCase() : "";

                    return nombreCompleto.contains(textoBusqueda) ||
                            cedula.contains(textoBusqueda) ||
                            correo.contains(textoBusqueda) ||
                            correo2.contains(textoBusqueda);
                })
                .collect(Collectors.toList());
    }

    public Optional<Cliente> buscarPorCorreo(String correo) throws SQLException {
        Validador.validarCorreo(correo);

        List<Cliente> clientes = clientesRepository.findAll();
        return clientes.stream()
                .filter(cliente -> cliente.getPCorreo().equalsIgnoreCase(correo) ||
                        (cliente.getSCorreo() != null && cliente.getSCorreo().equalsIgnoreCase(correo)))
                .findFirst();
    }

    public String obtenerNombreCompleto(String id) throws SQLException {
        Optional<Cliente> clienteOpt = buscarPorId(id);
        if (clienteOpt.isEmpty()) {
            throw new SQLException("No existe un cliente con el ID: " + id);
        }
        return clienteOpt.get().getNombreCompleto();
    }

    public int contarClientes() throws SQLException {
        return clientesRepository.findAll().size();
    }

    // MEJORA: Contar clientes activos (los que tienen proyectos activos)
    public int contarClientesActivos() throws SQLException {
        // TODO: Implementar cuando tengas la relación con proyectos
        return contarClientes();
    }

    public List<Cliente> listarOrdenadosPorNombre() throws SQLException {
        List<Cliente> clientes = clientesRepository.findAll();
        return clientes.stream()
                .sorted((c1, c2) -> c1.getNombreCompleto().compareToIgnoreCase(c2.getNombreCompleto()))
                .collect(Collectors.toList());
    }

    public String obtenerResumenCliente(String id) throws SQLException {
        Optional<Cliente> clienteOpt = buscarPorId(id);
        if (clienteOpt.isEmpty()) {
            throw new SQLException("No existe un cliente con el ID: " + id);
        }

        Cliente cliente = clienteOpt.get();
        return String.format(
                "ID: %s | Nombre: %s | Cédula: %s | Teléfono: %d | Email: %s",
                cliente.getIdCliente(),
                cliente.getNombreCompleto(),
                cliente.getCedula(),
                cliente.getPTelefono(),
                cliente.getPCorreo()
        );
    }

    public void actualizarCorreo(String id, String nuevoCorreo) throws SQLException {
        Validador.validarCorreo(nuevoCorreo);

        Optional<Cliente> clienteOpt = buscarPorId(id);
        if (clienteOpt.isEmpty()) {
            throw new SQLException("No existe un cliente con el ID: " + id);
        }

        Cliente cliente = clienteOpt.get();
        cliente.setPCorreo(nuevoCorreo);
        clientesRepository.update(cliente);
    }

    public void actualizarTelefono(String id, Long nuevoTelefono) throws SQLException {
        Validador.validarTelefono(nuevoTelefono);

        Optional<Cliente> clienteOpt = buscarPorId(id);
        if (clienteOpt.isEmpty()) {
            throw new SQLException("No existe un cliente con el ID: " + id);
        }

        Cliente cliente = clienteOpt.get();
        cliente.setPTelefono(nuevoTelefono);
        clientesRepository.update(cliente);
    }

    private void validarCliente(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }

        Validador.validarCedula(cliente.getCedula());

        Validador.validarNombre(cliente.getPNombre(), "Primer nombre");
        Validador.validarNombre(cliente.getPApellido(), "Primer apellido");

        if (cliente.getSNombre() != null && !cliente.getSNombre().trim().isEmpty()) {
            Validador.validarNombre(cliente.getSNombre(), "Segundo nombre");
        }
        if (cliente.getSApellido() != null && !cliente.getSApellido().trim().isEmpty()) {
            Validador.validarNombre(cliente.getSApellido(), "Segundo apellido");
        }

        if (cliente.getPCorreo() == null || cliente.getPCorreo().trim().isEmpty()) {
            throw new IllegalArgumentException("El correo principal es obligatorio");
        }
        Validador.validarCorreo(cliente.getPCorreo());

        if (cliente.getSCorreo() != null && !cliente.getSCorreo().trim().isEmpty()) {
            Validador.validarCorreo(cliente.getSCorreo());
        }

        if (cliente.getPTelefono() == null) {
            throw new IllegalArgumentException("El teléfono principal es obligatorio");
        }
        Validador.validarTelefono(cliente.getPTelefono());

        if (cliente.getSTelefono() != null) {
            Validador.validarTelefono(cliente.getSTelefono());
        }
    }

    private void validarClienteParaActualizar(Cliente cliente) {
        validarCliente(cliente);

        if (cliente.getIdCliente() == null || cliente.getIdCliente().trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del cliente es obligatorio para actualizar");
        }
    }

    public EstadisticasCliente obtenerEstadisticasCliente(String idCliente) throws SQLException {
        if (idCliente == null || idCliente.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del cliente no puede ser nulo o vacío");
        }

        int total = clientesRepository.contarProyectosPorCliente(idCliente);
        int activos = clientesRepository.contarProyectosActivosPorCliente(idCliente);
        int completados = clientesRepository.contarProyectosCompletadosPorCliente(idCliente);

        return new EstadisticasCliente(total, activos, completados);
    }

    public List<Cliente> listarClientesConProyectosActivos() throws SQLException {
        return clientesRepository.findClientesConProyectosActivos();
    }

    public List<Cliente> listarClientesSinProyectos() throws SQLException {
        return clientesRepository.findClientesSinProyectos();
    }

    // Clase interna
    public static class EstadisticasCliente {
        private final int proyectosTotales;
        private final int proyectosActivos;
        private final int proyectosCompletados;

        public EstadisticasCliente(int proyectosTotales, int proyectosActivos, int proyectosCompletados) {
            this.proyectosTotales = proyectosTotales;
            this.proyectosActivos = proyectosActivos;
            this.proyectosCompletados = proyectosCompletados;
        }

        public int getProyectosTotales() { return proyectosTotales; }
        public int getProyectosActivos() { return proyectosActivos; }
        public int getProyectosCompletados() { return proyectosCompletados; }
    }


}