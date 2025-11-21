package grupo.proyecto_aula_carpethome.services;

import grupo.proyecto_aula_carpethome.Utilidades.Validador;
import grupo.proyecto_aula_carpethome.entities.Prenda;
import grupo.proyecto_aula_carpethome.entities.Proyecto;
import grupo.proyecto_aula_carpethome.repositories.ProyectoRepository;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ProyectoService {
    private final ProyectoRepository proyectoRepository;
    private PrendaService prendaService; // ← Quitar final

    // Setter para inyectar la dependencia después
    public void setPrendaService(PrendaService prendaService) {
        this.prendaService = prendaService;
    }

    private void validarProyecto(Proyecto proyecto) {
        if (proyecto == null) throw new IllegalArgumentException("El proyecto no puede ser nulo");
        Validador.validarTexto(proyecto.getNombreProyecto(), "Nombre del proyecto: ", 15, true);
        Validador.validarTexto(proyecto.getTipoProduccion(), "Tipo de producción del proyecto: ", 15, true);
        Validador.validarTexto(proyecto.getEstado(), "Estado del proyecto: ", 15, true);
        Validador.validarRangoFechas(proyecto.getFechaInicio(), proyecto.getFechaEntregaEstimada());
        if (proyecto.getFechaEntregaReal() != null) {
            Validador.validarRangoFechas(proyecto.getFechaInicio(), proyecto.getFechaEntregaEstimada());
        }
        if (proyecto.getIdCliente() == null || proyecto.getIdCliente().trim().isEmpty()) {
            throw new IllegalArgumentException("El cliente del proyecto es obligatorio");
        }
    }

    private void validarProyectoParaActualizar(Proyecto proyecto) {
        validarProyecto(proyecto);
        if (proyecto.getIdProyecto() == null || proyecto.getIdProyecto().trim().isEmpty())
            throw new IllegalArgumentException("El ID del proyecto es obligatorio para actualizar");
    }

    public Proyecto registrarProyecto(Proyecto proyecto) throws SQLException {
        validarProyecto(proyecto);
        return proyectoRepository.save(proyecto);
    }

    public Optional<Proyecto> buscarPorId(String id) throws SQLException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de proyecto no puede ser nulo o vacío");
        }
        return proyectoRepository.findById(id);
    }

    public List<Proyecto> listarTodos() throws SQLException {
        return proyectoRepository.findAll();
    }

    public void actualizarProyecto(Proyecto proyecto) throws SQLException {
        validarProyectoParaActualizar(proyecto);
        proyectoRepository.update(proyecto);
    }

    public void eliminarProyecto(String id) throws SQLException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de proyecto no puede ser nulo o vacío");
        }
        Optional<Proyecto> p = proyectoRepository.findById(id);
        if (p.isEmpty()) {
            throw new SQLException("No existe un proyecto con el ID: " + id);
        }
        proyectoRepository.delete(id);
    }

    public boolean existePorId(String id) throws SQLException {
        if (id == null || id.trim().isEmpty()) return false;
        return proyectoRepository.findById(id).isPresent();
    }

    public Optional<Proyecto> buscarPorNombre(String nombre) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty())
            throw new IllegalArgumentException("El nombre no puede ser nulo o vacío");
        return listarTodos().stream()
                .filter(p -> nombre.equalsIgnoreCase(p.getNombreProyecto()))
                .findFirst();
    }

    public List<Proyecto> listarPorCliente(String idCliente) throws SQLException {
        if (idCliente == null || idCliente.trim().isEmpty())
            throw new IllegalArgumentException("El idCliente no puede ser nulo o vacío");
        return listarTodos().stream()
                .filter(p -> idCliente.equalsIgnoreCase(p.getIdCliente()))
                .toList();
    }

    public List<Proyecto> listarPorEstado(String estado) throws SQLException {
        if (estado == null || estado.trim().isEmpty())
            throw new IllegalArgumentException("El estado no puede ser nulo o vacío");
        return listarTodos().stream()
                .filter(p -> estado.equalsIgnoreCase(p.getEstado()))
                .toList();
    }

    public int contarProyectos() throws SQLException {
        return proyectoRepository.findAll().size();
    }

    public void recalcularCostoEstimadoProyecto(String idProyecto) throws SQLException {
        List<Prenda> prendas = prendaService.listarPorProyecto(idProyecto);
        double suma = prendas.stream()
                .mapToDouble(Prenda::getCostoTotalEstimado)
                .sum();

        Optional<Proyecto> proyectoOpt = buscarPorId(idProyecto);
        if(proyectoOpt.isPresent()) {
            Proyecto proyecto = proyectoOpt.get();
            proyecto.setCostoEstimado(suma);
            proyectoRepository.update(proyecto); // Actualizas el valor en la BD
        } else {
            throw new SQLException("No existe el proyecto con ID: " + idProyecto);
        }
    }


}