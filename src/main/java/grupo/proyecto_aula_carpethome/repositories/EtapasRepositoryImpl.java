package grupo.proyecto_aula_carpethome.repositories;

import grupo.proyecto_aula_carpethome.config.OracleDatabaseConnection;
import grupo.proyecto_aula_carpethome.entities.Etapa;
import lombok.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class EtapasRepositoryImpl implements EtapasRepository {
    private final OracleDatabaseConnection dbConnection;


    private Etapa mapResultSetToEtapas(ResultSet rs) throws SQLException {
        return Etapa.builder()
                .idEtapa(rs.getString("id_etapa"))
                .nombreEtapa(rs.getString("nombre_etapa"))
                .descripcionEtapa(rs.getString("descripcion_etapa"))

                .build();
    }


    @Override
    public Etapa save(Etapa entity) throws SQLException {

        String sql = "{CALL PKG_ETAPAS.sp_crear_etapa(?,?,?)}";

        try (Connection conn = dbConnection.connect();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, entity.getNombreEtapa());
            stmt.setString(2, entity.getDescripcionEtapa());

            stmt.registerOutParameter(3, Types.VARCHAR);

            stmt.execute();

            String idGenerado = stmt.getString(3);
            entity.setIdEtapa(idGenerado);

            System.out.println("Etapa guardada con ID: " + idGenerado);
            return entity;

        } catch (SQLException e) {
            System.err.println("Error al guardar etapa: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Optional<Etapa> findById(String id) throws SQLException {
        String sql = """
                SELECT * FROM etapas WHERE id_etapa = ?;
                """;


        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEtapas(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Etapa> findAll() throws SQLException {
        List<Etapa> etapas = new ArrayList<>();
        String sql = """
                SELECT  * FROM etapas;
                """;
        try (Connection conn = dbConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                etapas.add(mapResultSetToEtapas(rs));
            }
        }
        return etapas;
    }

    @Override
    public void update(Etapa entity) throws SQLException {
        String sql = "{CALL PKG_ETAPAS.sp_actualizar_etapa(?,?,?)} ";

        try(Connection conn = dbConnection.connect();
        CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, entity.getIdEtapa());
            stmt.setString(2, entity.getNombreEtapa());
            stmt.setString(3, entity.getDescripcionEtapa());
            stmt.execute();
        }catch (SQLException e){
            System.err.println("Error al actualizar etapa: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void delete(String id) throws SQLException {
        String sql = "{ CALL PKG_ETAPAS.sp_eliminar_etapa(?) }";

        try (Connection conn = dbConnection.connect();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, id);
            stmt.execute();

        } catch (SQLException e) {
            int errorCode = e.getErrorCode();
            if (errorCode == 20021) {
                throw new SQLException("La etapa no existe.");
            } else if (errorCode == 20022) {
                throw new SQLException("No se puede eliminar: la etapa está siendo usada en proyectos.");
            } else {
                throw e;
            }
        }
    }
}

