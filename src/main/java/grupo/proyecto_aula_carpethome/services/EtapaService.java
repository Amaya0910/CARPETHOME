package grupo.proyecto_aula_carpethome.services;

import grupo.proyecto_aula_carpethome.Utilidades.Validador;
import grupo.proyecto_aula_carpethome.entities.Etapa;
import grupo.proyecto_aula_carpethome.repositories.EtapasRepository;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class EtapaService {
    private final EtapasRepository etapasRepository;


    private void validarEtapa(Etapa etapa) {
        if (etapa == null) {
            throw new IllegalArgumentException("La etapa no puede ser nula");
        }
        Validador.validarTexto(etapa.getNombreEtapa(), "Nombre etapa: ", 15, true);
        Validador.validarTexto(etapa.getDescripcionEtapa(), "Descripción etapa: ", 150, false);
    }

    private void validarEtapaParaActualizar(Etapa etapa) {
        validarEtapa(etapa);
        if (etapa.getIdEtapa() == null || etapa.getIdEtapa().trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la etapa es obligatorio para actualizar");
        }
    }


    public Etapa registrarEtapa(Etapa etapa) throws SQLException {
        validarEtapa(etapa);
        return etapasRepository.save(etapa);
    }

    public Optional<Etapa> buscarPorId(String id) throws SQLException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID no puede ser nulo o vacío");
        }
        return etapasRepository.findById(id);
    }

    public List<Etapa> listarTodos() throws SQLException {
        return etapasRepository.findAll();
    }

    public void actualizarEtapa(Etapa etapa) throws SQLException {
        validarEtapaParaActualizar(etapa);
        etapasRepository.update(etapa);
    }

    public void eliminarEtapa(String id) throws SQLException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID no puede ser nulo o vacío");
        }

        Optional<Etapa> etapa = etapasRepository.findById(id);
        if (etapa.isEmpty()) {
            throw new SQLException("No existe una etapa con el ID: " + id);
        }

        etapasRepository.delete(id);
    }



    public boolean existePorId(String id) throws SQLException {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        return etapasRepository.findById(id).isPresent();
    }

    public Optional<Etapa> buscarPorNombre(String nombre) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede ser nulo o vacío");
        }
        return listarTodos().stream()
                .filter(etapa -> etapa.getNombreEtapa().equalsIgnoreCase(nombre))
                .findFirst();
    }


    public int contarEtapas() throws SQLException {
        return etapasRepository.findAll().size();
    }

    public String obtenerDescripcionPorId(String id) throws SQLException {
        Optional<Etapa> etapaOpt = buscarPorId(id);
        if (etapaOpt.isEmpty()) {
            throw new SQLException("No existe una etapa con el ID: " + id);
        }
        return etapaOpt.get().getDescripcionEtapa();
    }
}