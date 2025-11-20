package grupo.proyecto_aula_carpethome.services;

import grupo.proyecto_aula_carpethome.Utilidades.Validador;
import grupo.proyecto_aula_carpethome.entities.HistEtapa;
import grupo.proyecto_aula_carpethome.repositories.HistEtapaRepository;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class HistEtapaService {
    private final HistEtapaRepository histEtapaRepository;


    private void validarHistEtapa(HistEtapa he) {
        if (he == null) {
            throw new IllegalArgumentException("El historial de etapa no puede ser nulo");
        }
        Validador.validarTexto(he.getIdProyecto(),"Id del proyecto: ", 10, true);
        Validador.validarTexto(he.getIdEtapa(),"Id del etapa: ", 10, true);
        if (he.getFechaInicio() == null) {
            throw new IllegalArgumentException("La fecha de inicio es obligatoria");
        }
        if (he.getFechaFinal() != null) {
            Validador.validarRangoFechas(he.getFechaInicio(), he.getFechaFinal());
        }
        if (he.getObservaciones() != null && !he.getObservaciones().trim().isEmpty()) {
            Validador.validarTexto(he.getObservaciones(), "Observaciones", 150, false);
        }
    }

    private void validarHistEtapaParaActualizar(HistEtapa he) {
        validarHistEtapa(he);
        if (he.getIdProyecto() == null || he.getIdProyecto().trim().isEmpty() ||
                he.getIdEtapa() == null || he.getIdEtapa().trim().isEmpty()) {
            throw new IllegalArgumentException("Tanto idProyecto como idEtapa son obligatorios para actualizar");
        }
    }

    public HistEtapa registrarHistEtapa(HistEtapa histEtapa) throws SQLException {
        validarHistEtapa(histEtapa);
        return histEtapaRepository.save(histEtapa);
    }

    public Optional<HistEtapa> buscarPorId(String idCompuesto) throws SQLException {
        if (idCompuesto == null || !idCompuesto.contains("-"))
            throw new IllegalArgumentException("El id debe tener el formato 'idProyecto-idEtapa'");
        return histEtapaRepository.findById(idCompuesto);
    }

    public List<HistEtapa> listarTodos() throws SQLException {
        return histEtapaRepository.findAll();
    }

    public void actualizarHistEtapa(HistEtapa histEtapa) throws SQLException {
        validarHistEtapaParaActualizar(histEtapa);
        histEtapaRepository.update(histEtapa);
    }

    public void eliminarHistEtapa(String idCompuesto) throws SQLException {
        if (idCompuesto == null || !idCompuesto.contains("-")) {
            throw new IllegalArgumentException("El id debe tener el formato 'idProyecto-idEtapa'");
        }
        buscarPorId(idCompuesto).orElseThrow(
                () -> new SQLException("No existe un historial con el id: " + idCompuesto)
        );
        histEtapaRepository.delete(idCompuesto);
    }

    public boolean existeRegistro(String idCompuesto) throws SQLException {
        if (idCompuesto == null || !idCompuesto.contains("-")) {
            return false;
        }
        return histEtapaRepository.findById(idCompuesto).isPresent();
    }


    public List<HistEtapa> listarPorProyecto(String idProyecto) throws SQLException {
        return listarTodos().stream()
                .filter(h -> h.getIdProyecto().equalsIgnoreCase(idProyecto))
                .toList();
    }


    public List<HistEtapa> listarPorEtapa(String idEtapa) throws SQLException {
        return listarTodos().stream()
                .filter(h -> h.getIdEtapa().equalsIgnoreCase(idEtapa))
                .toList();
    }



    public int contarHistEtapas() throws SQLException {
        return histEtapaRepository.findAll().size();
    }

    public Optional<HistEtapa> buscarPorProyectoYEtapa(String idProyecto, String idEtapa) throws SQLException {
        if (idProyecto == null || idEtapa == null) {
            throw new IllegalArgumentException("Proyecto y Etapa obligatorios");
        }
        String compuesto = idProyecto + "-" + idEtapa;
        return buscarPorId(compuesto);
    }
}