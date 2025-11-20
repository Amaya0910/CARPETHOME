package grupo.proyecto_aula_carpethome.services;

import grupo.proyecto_aula_carpethome.Utilidades.Validador;
import grupo.proyecto_aula_carpethome.entities.Gasto;
import grupo.proyecto_aula_carpethome.repositories.GastosRepository;
import grupo.proyecto_aula_carpethome.services.PrendaService; // Suponiendo que ya lo tienes implementado
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class GastoService {
    private final GastosRepository gastosRepository;
    private final PrendaService prendaService;

    private void validarGasto(Gasto gasto) {
        if (gasto == null)
            throw new IllegalArgumentException("El gasto no puede ser nulo");
        Validador.validarTexto(gasto.getNombreGasto(), "Nombre del gasto: ", 15, true);
        Validador.validarTexto(gasto.getIdPrenda(), "Id de la prenda: ", 15, true);
        if (gasto.getPrecio() < 0)
            throw new IllegalArgumentException("El precio del gasto no puede ser negativo");
        if (gasto.getDescripcionGasto() != null)
            Validador.validarTexto(gasto.getDescripcionGasto(), "Descripción del gasto: ", 150, false);
    }

    private void validarGastoParaActualizar(Gasto gasto) {
        validarGasto(gasto);
        if (gasto.getIdGasto() == null || gasto.getIdGasto().trim().isEmpty())
            throw new IllegalArgumentException("El ID de gasto es obligatorio para actualizar");
    }

    public Gasto registrarGasto(Gasto gasto) throws SQLException {
        validarGasto(gasto);
        Gasto savedGasto = gastosRepository.save(gasto);
        // Recalcular costos de la prenda asociada después de registrar un gasto
        prendaService.recalcularCostos(gasto.getIdPrenda());
        return savedGasto;
    }

    public Optional<Gasto> buscarPorId(String id) throws SQLException {
        if (id == null || id.trim().isEmpty())
            throw new IllegalArgumentException("El ID de gasto no puede ser nulo o vacío");
        return gastosRepository.findById(id);
    }

    public List<Gasto> listarTodos() throws SQLException {
        return gastosRepository.findAll();
    }

    public void actualizarGasto(Gasto gasto) throws SQLException {
        validarGastoParaActualizar(gasto);
        gastosRepository.update(gasto);
        // Recalcular costos de la prenda asociada después de actualizar un gasto
        prendaService.recalcularCostos(gasto.getIdPrenda());
    }

    public void eliminarGasto(String id) throws SQLException {
        if (id == null || id.trim().isEmpty())
            throw new IllegalArgumentException("El ID de gasto no puede ser nulo o vacío");
        Optional<Gasto> gastoOpt = gastosRepository.findById(id);
        if (gastoOpt.isEmpty())
            throw new SQLException("No existe un gasto con el ID: " + id);
        String idPrenda = gastoOpt.get().getIdPrenda();
        gastosRepository.delete(id);
        // Recalcular costos de la prenda asociada después de eliminar un gasto
        prendaService.recalcularCostos(idPrenda);
    }

    public double totalGastosPorPrenda(String idPrenda) throws SQLException {
        if (idPrenda == null || idPrenda.trim().isEmpty())
            throw new IllegalArgumentException("El idPrenda no puede ser nulo o vacío");
        return gastosRepository.TotalGastos(idPrenda);
    }

    public List<Gasto> listarPorPrenda(String idPrenda) throws SQLException {
        if (idPrenda == null || idPrenda.trim().isEmpty())
            throw new IllegalArgumentException("El idPrenda no puede ser nulo o vacío");
        return gastosRepository.findByPrenda(idPrenda);
    }

    public int contarGastos() throws SQLException {
        return gastosRepository.findAll().size();
    }


}