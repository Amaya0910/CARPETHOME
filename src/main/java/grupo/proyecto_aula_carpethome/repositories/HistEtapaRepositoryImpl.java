package grupo.proyecto_aula_carpethome.repositories;

import grupo.proyecto_aula_carpethome.config.OracleDatabaseConnection;
import grupo.proyecto_aula_carpethome.entities.HistEtapa;
import lombok.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class HistEtapaRepositoryImpl implements HistEtapaRepository {
    private final OracleDatabaseConnection dbConnection;

    private HistEtapa mapResultSetToHistEtapa(ResultSet rs) throws SQLException {
        return HistEtapa.builder()
                .idProyecto(rs.getString("id_proyecto"))
                .idEtapa(rs.getString("id_etapa"))
                .fechaInicio(rs.getDate("fecha_inicio"))
                .fechaFinal(rs.getDate("fecha_final"))
                .observaciones(rs.getString("observaciones"))
                .build();
    }

    @Override
    public HistEtapa save(HistEtapa entity) throws SQLException {
        String sql = "{CALL PKG_HISTORIAL_ETAPAS.sp_iniciar_etapa_proyecto(?, ?, ?, ?)}";

        try (Connection conn = dbConnection.connect();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, entity.getIdProyecto());
            stmt.setString(2, entity.getIdEtapa());
            stmt.setDate(3, new java.sql.Date(entity.getFechaInicio().getTime()));

            if (entity.getObservaciones() != null) {
                stmt.setString(4, entity.getObservaciones());
            } else {
                stmt.setNull(4, Types.VARCHAR);
            }

            stmt.execute();

            System.out.println("✓ Historial guardado entre: " +
                    entity.getIdProyecto() + " y " + entity.getIdEtapa());
            return entity;

        } catch (SQLException e) {
            System.err.println("✗ Error al guardar historial de etapas: " + e.getMessage());
            System.err.println("  Código: " + e.getErrorCode());

            switch (e.getErrorCode()) {
                case 20031:
                    throw new SQLException("El proyecto no existe", e);
                case 20021:
                    throw new SQLException("La etapa no existe", e);
                case 20040:
                    throw new SQLException("Esta etapa ya fue iniciada para este proyecto", e);
                default:
                    throw e;
            }
        }
    }

    @Override
    public Optional<HistEtapa> findById(String s) throws SQLException {
        String[] parts = s.split("-");
        String idProyecto = parts[0].trim();
        String idEtapa = parts[1].trim();


        String sql = "SELECT * FROM HIST_ETAPA WHERE id_proyecto = ? AND id_etapa = ?";

        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idProyecto);
            stmt.setString(2, idEtapa);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToHistEtapa(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("✗ Error al buscar historial: " + e.getMessage());
            throw e;
        }

        return Optional.empty();
    }

    @Override
    public List<HistEtapa> findAll() throws SQLException {
        List<HistEtapa> histEtapas = new ArrayList<>();
        String sql = "SELECT * FROM HIST_ETAPA ORDER BY id_proyecto, id_etapa";

        try (Connection conn = dbConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                histEtapas.add(mapResultSetToHistEtapa(rs));
            }

            System.out.println("✓ Se encontraron " + histEtapas.size() + " registros");

        } catch (SQLException e) {
            System.err.println("✗ Error al obtener todos los historiales: " + e.getMessage());
            throw e;
        }

        return histEtapas;
    }

    @Override
    public void update(HistEtapa entity) throws SQLException {

        String sql = "{CALL PKG_HISTORIAL_ETAPAS.sp_actualizar_hist_etapa(?, ?, ?, ?, ?)}";

        try (Connection conn = dbConnection.connect();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, entity.getIdProyecto());
            stmt.setString(2, entity.getIdEtapa());
            stmt.setDate(3, new java.sql.Date(entity.getFechaInicio().getTime()));

            if (entity.getFechaFinal() != null) {
                stmt.setDate(4, new java.sql.Date(entity.getFechaFinal().getTime()));
            } else {
                stmt.setNull(4, Types.DATE);
            }

            if (entity.getObservaciones() != null && !entity.getObservaciones().trim().isEmpty()) {
                stmt.setString(5, entity.getObservaciones());
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }

            stmt.execute();

            System.out.println("  Historial de etapa actualizado");
            System.out.println("  Proyecto: " + entity.getIdProyecto());
            System.out.println("  Etapa: " + entity.getIdEtapa());

        } catch (SQLException e) {
            System.err.println("✗ Error al actualizar historial de etapa: " + e.getMessage());
            System.err.println("  Código: " + e.getErrorCode());

            if (e.getErrorCode() == 20042) {
                throw new SQLException("No existe registro de historial para esa etapa y proyecto", e);
            } else if (e.getErrorCode() == 20043) {
                throw new SQLException("La fecha final no puede ser menor que la fecha de inicio", e);
            }

            throw e;
        }
    }

    @Override
    public void delete(String s) throws SQLException {

        String[] parts = s.split("-");
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "El ID debe tener el formato 'idProyecto-idEtapa'. Ejemplo: 'PRY001-ETA001'"
            );
        }

        String idProyecto = parts[0].trim();
        String idEtapa = parts[1].trim();


        String sql = "{CALL PKG_HISTORIAL_ETAPAS.sp_eliminar_hist_etapa(?, ?)}";

        try (Connection conn = dbConnection.connect();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, idProyecto);
            stmt.setString(2, idEtapa);

            stmt.execute();

            System.out.println("  Historial de etapa eliminado exitosamente");
            System.out.println("  Proyecto: " + idProyecto);
            System.out.println("  Etapa: " + idEtapa);

        } catch (SQLException e) {
            System.err.println("  Error al eliminar historial: " + e.getMessage());
            System.err.println("  Código: " + e.getErrorCode());

            if (e.getErrorCode() == 20044) {
                throw new SQLException(
                        "No existe registro de historial para el proyecto '" +
                                idProyecto + "' y la etapa '" + idEtapa + "'", e
                );
            }

            throw e;
        }
    }
}