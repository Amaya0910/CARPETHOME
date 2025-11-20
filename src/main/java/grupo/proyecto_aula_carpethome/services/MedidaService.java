package grupo.proyecto_aula_carpethome.services;

import grupo.proyecto_aula_carpethome.Utilidades.Validador;
import grupo.proyecto_aula_carpethome.entities.Medida;
import grupo.proyecto_aula_carpethome.repositories.MedidaRepository;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class MedidaService {
    private final MedidaRepository medidaRepository;

    private void validarMedida(Medida medida) {
        if (medida == null) {
            throw new IllegalArgumentException("La medida no puede ser nula");
        }
        Validador.validarTexto(medida.getNombreMedida(), "Nombre de la medida: ", 15, true);
        if (medida.getTipoMedida() != null) {
            Validador.validarTexto(medida.getTipoMedida(), "Tipo de la medida: ", 15, true);
        }
    }

    private void validarMedidaParaActualizar(Medida medida) {
        validarMedida(medida);
        if (medida.getIdMedida() == null || medida.getIdMedida().trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la medida es obligatorio para actualizar");
        }
    }

    public Medida registrarMedida(Medida medida) throws SQLException {
        validarMedida(medida);
        return medidaRepository.save(medida);
    }

    public Optional<Medida> buscarPorId(String id) throws SQLException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID no puede ser nulo o vacío");
        }
        return medidaRepository.findById(id);
    }

    public List<Medida> listarTodos() throws SQLException {
        return medidaRepository.findAll();
    }

    public void actualizarMedida(Medida medida) throws SQLException {
        validarMedidaParaActualizar(medida);
        medidaRepository.update(medida);
    }

    public void eliminarMedida(String id) throws SQLException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID no puede ser nulo o vacío");
        }
        Optional<Medida> mOpt = medidaRepository.findById(id);
        if (mOpt.isEmpty()) {
            throw new SQLException("No existe una medida con el ID: " + id);
        }
        medidaRepository.delete(id);
    }

    public List<Medida> buscarPorTipoMedida(String tipo) throws SQLException {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de medida no puede ser nulo o vacío");
        }
        return medidaRepository.findByTipoMedida(tipo);
    }

    public boolean existePorId(String id) throws SQLException {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        return medidaRepository.findById(id).isPresent();
    }

    public int contarMedidas() throws SQLException {
        return medidaRepository.findAll().size();
    }



    public Optional<Medida> buscarPorNombre(String nombre) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la medida no puede ser nulo o vacío");
        }
        return listarTodos().stream()
                .filter(m -> nombre.equalsIgnoreCase(m.getNombreMedida()))
                .findFirst();
    }
}