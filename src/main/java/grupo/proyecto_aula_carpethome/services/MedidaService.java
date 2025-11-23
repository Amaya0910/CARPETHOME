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

    // ⚠️ IMPORTANTE: Usar EXACTAMENTE como están en BD (MAYÚSCULAS)
    public static final String TIPO_ESTANDAR = "ESTANDAR";
    public static final String TIPO_PERSONALIZADA = "PERSONALIZADA";

    // ============================================
    // VALIDACIONES
    // ============================================

    private void validarMedida(Medida medida) {
        if (medida == null) {
            throw new IllegalArgumentException("La medida no puede ser nula");
        }
        Validador.validarTexto(medida.getNombreMedida(), "Nombre de la medida", 15, true);

        if (medida.getTipoMedida() != null) {
            Validador.validarTexto(medida.getTipoMedida(), "Tipo de la medida", 15, true);

            // Validar que el tipo sea correcto (case insensitive)
            if (!TIPO_ESTANDAR.equalsIgnoreCase(medida.getTipoMedida()) &&
                    !TIPO_PERSONALIZADA.equalsIgnoreCase(medida.getTipoMedida())) {
                throw new IllegalArgumentException(
                        "El tipo de medida debe ser '" + TIPO_ESTANDAR +
                                "' o '" + TIPO_PERSONALIZADA + "'"
                );
            }
        }
    }

    private void validarMedidaParaActualizar(Medida medida) {
        validarMedida(medida);
        if (medida.getIdMedida() == null || medida.getIdMedida().trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la medida es obligatorio para actualizar");
        }
    }

    private void validarMedidaEstandar(Medida medida) throws SQLException {
        if (TIPO_ESTANDAR.equalsIgnoreCase(medida.getTipoMedida())) {
            // Validar que no exista otra medida estándar con el mismo nombre
            Optional<Medida> existente = buscarPorNombre(medida.getNombreMedida());
            if (existente.isPresent() &&
                    !existente.get().getIdMedida().equals(medida.getIdMedida())) {
                throw new IllegalArgumentException(
                        "Ya existe una medida estándar con el nombre: " + medida.getNombreMedida()
                );
            }
        }
    }

    // ============================================
    // CRUD BÁSICO
    // ============================================

    public Medida registrarMedida(Medida medida) throws SQLException {
        validarMedida(medida);
        validarMedidaEstandar(medida);
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

        // No permitir cambiar el tipo de medida estándar a personalizada
        Optional<Medida> medidaActual = buscarPorId(medida.getIdMedida());
        if (medidaActual.isPresent()) {
            String tipoActual = medidaActual.get().getTipoMedida();
            String tipoNuevo = medida.getTipoMedida();

            if (TIPO_ESTANDAR.equalsIgnoreCase(tipoActual) &&
                    TIPO_PERSONALIZADA.equalsIgnoreCase(tipoNuevo)) {
                throw new IllegalArgumentException(
                        "No se puede cambiar una medida estándar a personalizada"
                );
            }
        }

        validarMedidaEstandar(medida);
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

        // Advertencia: si es estándar, considerar si debe eliminarse
        Medida medida = mOpt.get();
        if (TIPO_ESTANDAR.equalsIgnoreCase(medida.getTipoMedida())) {
            System.out.println("⚠️ Advertencia: Eliminando medida estándar: " + medida.getNombreMedida());
        }

        medidaRepository.delete(id);
    }

    // ============================================
    // MÉTODOS ESPECÍFICOS
    // ============================================

    /**
     * Obtiene todas las medidas estándar (XXS, XS, S, M, L, XL, XXL)
     */
    public List<Medida> listarMedidasEstandar() throws SQLException {
        return listarTodos().stream()
                .filter(m -> TIPO_ESTANDAR.equalsIgnoreCase(m.getTipoMedida()))
                .toList();
    }

    /**
     * Obtiene todas las medidas personalizadas
     */
    public List<Medida> listarMedidasPersonalizadas() throws SQLException {
        return listarTodos().stream()
                .filter(m -> TIPO_PERSONALIZADA.equalsIgnoreCase(m.getTipoMedida()))
                .toList();
    }

    /**
     * Verifica si una medida es estándar
     */
    public boolean esMedidaEstandar(String idMedida) throws SQLException {
        Optional<Medida> medida = buscarPorId(idMedida);
        return medida.isPresent() &&
                TIPO_ESTANDAR.equalsIgnoreCase(medida.get().getTipoMedida());
    }

    /**
     * Verifica si una medida es personalizada
     */
    public boolean esMedidaPersonalizada(String idMedida) throws SQLException {
        Optional<Medida> medida = buscarPorId(idMedida);
        return medida.isPresent() &&
                TIPO_PERSONALIZADA.equalsIgnoreCase(medida.get().getTipoMedida());
    }

    // ============================================
    // BÚSQUEDAS Y UTILIDADES
    // ============================================

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

    public int contarMedidasEstandar() throws SQLException {
        return listarMedidasEstandar().size();
    }

    public int contarMedidasPersonalizadas() throws SQLException {
        return listarMedidasPersonalizadas().size();
    }

    public Optional<Medida> buscarPorNombre(String nombre) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la medida no puede ser nulo o vacío");
        }
        return listarTodos().stream()
                .filter(m -> nombre.equalsIgnoreCase(m.getNombreMedida()))
                .findFirst();
    }

    /**
     * Valida que al menos un campo de medida tenga valor
     */
    public boolean tieneMedidasValidas(Medida medida) {
        if (medida == null) return false;

        return medida.getCBusto() > 0 ||
                medida.getCCintura() > 0 ||
                medida.getCCadera() > 0 ||
                medida.getAlturaBusto() > 0 ||
                medida.getSeparacionBusto() > 0 ||
                medida.getRadioBusto() > 0 ||
                medida.getBajoBusto() > 0 ||
                medida.getLargoFalda() > 0 ||
                medida.getLargoCadera() > 0 ||
                medida.getLargoVestido() > 0 ||
                medida.getLargoPantalon() > 0 ||
                medida.getLargoManga() > 0;
    }
}