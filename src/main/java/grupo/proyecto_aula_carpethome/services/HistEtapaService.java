package grupo.proyecto_aula_carpethome.services;

import grupo.proyecto_aula_carpethome.Utilidades.Validador;
import grupo.proyecto_aula_carpethome.entities.HistEtapa;
import grupo.proyecto_aula_carpethome.repositories.HistEtapaRepository;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class HistEtapaService {
    private final HistEtapaRepository histEtapaRepository;


    private void validarHistEtapa(HistEtapa he) {
        if (he == null) {
            throw new IllegalArgumentException("El historial de etapa no puede ser nulo");
        }
        Validador.validarTexto(he.getIdPrenda(),"Id de la prenda: ", 10, true);
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
        if (he.getIdPrenda() == null || he.getIdPrenda().trim().isEmpty() ||
                he.getIdEtapa() == null || he.getIdEtapa().trim().isEmpty()) {
            throw new IllegalArgumentException("Tanto idPrenda como idEtapa son obligatorios para actualizar");
        }
    }

    public HistEtapa registrarHistEtapa(HistEtapa histEtapa) throws SQLException {
        validarHistEtapa(histEtapa);
        return histEtapaRepository.save(histEtapa);
    }




    public boolean existeRegistro(String idCompuesto) throws SQLException {
        if (idCompuesto == null || !idCompuesto.contains("-")) {
            return false;
        }
        return histEtapaRepository.findById(idCompuesto).isPresent();
    }


    public List<HistEtapa> listarPorProyecto(String idProyecto) throws SQLException {
        return listarTodos().stream()
                .filter(h -> h.getIdPrenda().equalsIgnoreCase(idProyecto))
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

    public HistEtapa iniciarEtapa(HistEtapa histEtapa) throws SQLException {
        validarHistEtapa(histEtapa);
        return histEtapaRepository.save(histEtapa);
    }

    /**
     * Cambia una prenda de etapa (cierra la actual y abre una nueva)
     */
    public void cambiarEtapa(String idPrenda, String idEtapaActual, String idEtapaNueva,
                             String observaciones, String idEmpleado) throws SQLException {

        // 1. Cerrar etapa actual (si existe y está abierta)
        if (idEtapaActual != null && !idEtapaActual.trim().isEmpty()) {
            Optional<HistEtapa> etapaActualOpt = buscarPorId(idPrenda + "-" + idEtapaActual);
            if (etapaActualOpt.isPresent()) {
                HistEtapa etapaActual = etapaActualOpt.get();
                if (etapaActual.getFechaFinal() == null) {
                    // Cerrar la etapa actual
                    etapaActual.setFechaFinal(new Date());
                    etapaActual.setIdEmpleado(idEmpleado);
                    if (observaciones != null && !observaciones.trim().isEmpty()) {
                        String obsActual = etapaActual.getObservaciones();
                        etapaActual.setObservaciones(
                                (obsActual != null ? obsActual + " | " : "") + "Finalizado: " + observaciones
                        );
                    }
                    actualizarHistEtapa(etapaActual);
                }
            }
        }

        // 2. Iniciar nueva etapa
        HistEtapa nuevaEtapa = HistEtapa.builder()
                .idPrenda(idPrenda)
                .idEtapa(idEtapaNueva)
                .fechaInicio(new Date())
                .observaciones(observaciones)
                .idEmpleado(idEmpleado)
                .build();

        iniciarEtapa(nuevaEtapa);
    }

    /**
     * Obtiene la etapa actual de una prenda (la última sin fecha final)
     */
    public Optional<HistEtapa> obtenerEtapaActual(String idPrenda) throws SQLException {
        if (idPrenda == null || idPrenda.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de prenda no puede ser nulo");
        }

        return listarPorPrenda(idPrenda).stream()
                .filter(h -> h.getFechaFinal() == null)
                .findFirst();
    }

    /**
     * Obtiene el historial completo de etapas de una prenda (ordenado por fecha)
     */
    public List<HistEtapa> obtenerHistorialCompleto(String idPrenda) throws SQLException {
        return listarPorPrenda(idPrenda).stream()
                .sorted((h1, h2) -> h1.getFechaInicio().compareTo(h2.getFechaInicio()))
                .collect(Collectors.toList());
    }

    public Optional<HistEtapa> buscarPorId(String id) throws SQLException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID no puede ser nulo o vacío");
        }
        return histEtapaRepository.findById(id);
    }

    public List<HistEtapa> listarTodos() throws SQLException {
        return histEtapaRepository.findAll();
    }

    /**
     * Lista todas las etapas de una prenda específica
     */
    public List<HistEtapa> listarPorPrenda(String idPrenda) throws SQLException {
        if (idPrenda == null || idPrenda.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de prenda no puede ser nulo");
        }
        return listarTodos().stream()
                .filter(h -> idPrenda.equals(h.getIdPrenda()))
                .collect(Collectors.toList());
    }

    /**
     * Lista todas las prendas que están en una etapa específica
     */
    public List<HistEtapa> listarPorEtapa(String idEtapa) throws SQLException {
        if (idEtapa == null || idEtapa.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de etapa no puede ser nulo");
        }
        return listarTodos().stream()
                .filter(h -> idEtapa.equals(h.getIdEtapa()))
                .collect(Collectors.toList());
    }

    /**
     * Lista todas las etapas gestionadas por un empleado
     */
    public List<HistEtapa> listarPorEmpleado(String idEmpleado) throws SQLException {
        if (idEmpleado == null || idEmpleado.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de empleado no puede ser nulo");
        }
        return listarTodos().stream()
                .filter(h -> idEmpleado.equals(h.getIdEmpleado()))
                .collect(Collectors.toList());
    }

    public void actualizarHistEtapa(HistEtapa histEtapa) throws SQLException {
        validarHistEtapa(histEtapa);
        histEtapaRepository.update(histEtapa);
    }

    public void eliminarHistEtapa(String id) throws SQLException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID no puede ser nulo o vacío");
        }
        histEtapaRepository.delete(id);
    }

    public int contarHistoriales() throws SQLException {
        return histEtapaRepository.findAll().size();
    }

    /**
     * Verifica si una prenda tiene etapas registradas
     */
    public boolean prendaTieneEtapas(String idPrenda) throws SQLException {
        return !listarPorPrenda(idPrenda).isEmpty();
    }

    /**
     * Verifica si una prenda está actualmente en una etapa específica
     */
    public boolean prendaEnEtapa(String idPrenda, String idEtapa) throws SQLException {
        Optional<HistEtapa> etapaActual = obtenerEtapaActual(idPrenda);
        return etapaActual.isPresent() && idEtapa.equals(etapaActual.get().getIdEtapa());
    }
}