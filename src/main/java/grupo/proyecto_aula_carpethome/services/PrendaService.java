package grupo.proyecto_aula_carpethome.services;

import grupo.proyecto_aula_carpethome.Utilidades.Validador;
import grupo.proyecto_aula_carpethome.entities.Prenda;
import grupo.proyecto_aula_carpethome.repositories.PrendaRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class PrendaService {
    private final PrendaRepository prendaRepository;
    // Setter para inyectar la dependencia
    @Setter
    private ProyectoService proyectoService; // ← Quitar final y agregar setter

    private void validarPrenda(Prenda prenda) {
        if (prenda == null)
            throw new IllegalArgumentException("La prenda no puede ser nula");
        Validador.validarTexto(prenda.getNombrePrenda(), "Nombre de la prenda: ", 15, true);
        Validador.validarTexto(prenda.getDescripcionPrenda(), "Descripción de la prenda: ", 15, false);
        if (prenda.getIdProyecto() == null || prenda.getIdProyecto().trim().isEmpty())
            throw new IllegalArgumentException("El ID del proyecto es obligatorio");

    }

    private void validarPrendaParaActualizar(Prenda prenda) {
        validarPrenda(prenda);
        if (prenda.getIdPrenda() == null || prenda.getIdPrenda().trim().isEmpty())
            throw new IllegalArgumentException("El ID de la prenda es obligatorio para actualizar");
    }


    public Prenda registrarPrenda(Prenda prenda) throws SQLException {
        validarPrenda(prenda);
        Prenda resultado = prendaRepository.save(prenda);
        proyectoService.recalcularCostoEstimadoProyecto(prenda.getIdProyecto());
        return resultado;
    }

    public Optional<Prenda> buscarPorId(String id) throws SQLException {
        if (id == null || id.trim().isEmpty())
            throw new IllegalArgumentException("El ID de la prenda no puede ser nulo o vacío");
        return prendaRepository.findById(id);
    }

    public List<Prenda> listarTodos() throws SQLException {
        return prendaRepository.findAll();
    }

    public void actualizarPrenda(Prenda prenda) throws SQLException {
        validarPrendaParaActualizar(prenda);
        prendaRepository.update(prenda);
        proyectoService.recalcularCostoEstimadoProyecto(prenda.getIdProyecto());
    }

    public void eliminarPrenda(String id) throws SQLException {
        if (id == null || id.trim().isEmpty())
            throw new IllegalArgumentException("El ID no puede ser nulo o vacío");
        Optional<Prenda> prendaOpt = prendaRepository.findById(id);
        if (prendaOpt.isEmpty())
            throw new SQLException("No existe una prenda con el ID: " + id);
        Prenda prenda = prendaOpt.get();
        prendaRepository.delete(id);
        proyectoService.recalcularCostoEstimadoProyecto(prenda.getIdProyecto());
    }

    public boolean existePorId(String id) throws SQLException {
        if (id == null || id.trim().isEmpty()) return false;
        return prendaRepository.findById(id).isPresent();
    }

    public int contarPrendas() throws SQLException {
        return prendaRepository.findAll().size();
    }


    public Optional<Prenda> buscarPorNombre(String nombre) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty())
            throw new IllegalArgumentException("El nombre de la prenda no puede ser nulo o vacío");
        return listarTodos().stream()
                .filter(p -> nombre.equalsIgnoreCase(p.getNombrePrenda()))
                .findFirst();
    }


    public List<Prenda> listarPorProyecto(String idProyecto) throws SQLException {
        if (idProyecto == null || idProyecto.trim().isEmpty())
            throw new IllegalArgumentException("El idProyecto no puede ser nulo o vacío");
        return listarTodos().stream()
                .filter(p -> idProyecto.equalsIgnoreCase(p.getIdProyecto()))
                .toList();
    }


    public List<Prenda> listarPorMedida(String idMedida) throws SQLException {
        if (idMedida == null || idMedida.trim().isEmpty())
            throw new IllegalArgumentException("El idMedida no puede ser nulo o vacío");
        return listarTodos().stream()
                .filter(p -> idMedida.equalsIgnoreCase(p.getIdMedida()))
                .toList();
    }

    public Prenda recalcularCostos(String idPrenda) throws SQLException {
        if (idPrenda == null || idPrenda.trim().isEmpty())
            throw new IllegalArgumentException("El idPrenda no puede ser nulo o vacío");
        return prendaRepository.recalcularCostos(idPrenda);
    }


}