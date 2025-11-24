package grupo.proyecto_aula_carpethome.repositories;

import grupo.proyecto_aula_carpethome.Utilidades.Validador;
import grupo.proyecto_aula_carpethome.config.OracleDatabaseConnection;
import grupo.proyecto_aula_carpethome.entities.Prenda;
import lombok.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@RequiredArgsConstructor

public class PrendaRepositoryImpl implements PrendaRepository{
    private final OracleDatabaseConnection dbConnection;
    private static final double ganancia = 0.25;
    private Prenda mapRessultSetToPrenda(ResultSet rs) throws SQLException
    {
        return Prenda.builder()
                .idPrenda(rs.getString("id_prenda"))
                .nombrePrenda(rs.getString("nombre_prenda"))
                .descripcionPrenda(rs.getString("descripcion"))
                .costoMateriales(rs.getDouble("costo_materiales"))
                .costoTotalEstimado(rs.getDouble("costo_total_estimado"))
                .idProyecto(rs.getString("id_proyecto"))
                .idMedida(rs.getString("id_medida"))
        .build();
    }

    @Override
    public Prenda save(Prenda entity) throws SQLException {
        String sql = "{CALL PKG_PRENDAS.sp_crear_prenda(?,?,?,?,?,?,?)}";

        try (Connection conn = dbConnection.connect();
             CallableStatement stmt = conn.prepareCall(sql)) {

            // Usar los valores que vienen del formulario
            double costoMateriales = entity.getCostoMateriales();
            double costoTotalEstimado = entity.getCostoTotalEstimado();
            Validador.validarTexto(entity.getNombrePrenda(), "Nombre de la prenda: ", 15, true);
            stmt.setString(1, entity.getNombrePrenda());

            Validador.validarTexto(entity.getDescripcionPrenda(), "Descripcion de la prenda: ", 150, false);
            stmt.setString(2, entity.getDescripcionPrenda());

            stmt.setDouble(3, costoMateriales);
            stmt.setDouble(4, costoTotalEstimado);
            stmt.setString(5, entity.getIdProyecto());

            if (entity.getIdMedida() != null && !entity.getIdMedida().isBlank()) {
                stmt.setString(6, entity.getIdMedida());
            } else {
                stmt.setNull(6, Types.VARCHAR);
            }

            stmt.registerOutParameter(7, Types.VARCHAR);
            stmt.execute();

            String idGenerado = stmt.getString(7);
            entity.setIdPrenda(idGenerado);
            entity.setCostoMateriales(costoMateriales);
            entity.setCostoTotalEstimado(costoTotalEstimado);

            System.out.println("✓ Prenda guardada con ID: " + idGenerado);
            System.out.println("  Costo Total: " + costoTotalEstimado);
            return entity;

        } catch (SQLException e) {
            System.err.println("✗ Error al guardar prenda: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Optional<Prenda> findById(String s) throws SQLException {
        String sql = """
                SELECT id_prenda, nombre_prenda, descripcion, costo_materiales, costo_total_estimado, id_proyecto, id_medida
                FROM prendas
                WHERE id_prenda = ?""";
        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, s);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRessultSetToPrenda(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Prenda> findAll() throws SQLException {
        List<Prenda> prendas = new ArrayList<>();
        String sql = """
            SELECT id_prenda, nombre_prenda, descripcion, costo_materiales, costo_total_estimado, id_proyecto, id_medida
            FROM prendas
            """;
        try (Connection conn = dbConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                prendas.add(mapRessultSetToPrenda(rs));
            }
        }

        return prendas;
    }

    @Override
    public void update(Prenda entity) throws SQLException {
        String sql = "{CALL PKG_PRENDAS.sp_actualizar_prenda(?,?,?,?,?,?)}";

        try (Connection conn = dbConnection.connect();
             CallableStatement stmt = conn.prepareCall(sql)) {

            // Usar los valores que vienen del formulario
            double costoMateriales = entity.getCostoMateriales();
            double costoTotalEstimado = entity.getCostoTotalEstimado();

            // Parámetro 1: ID de la prenda
            stmt.setString(1, entity.getIdPrenda());

            // Parámetro 2: Nombre
            Validador.validarTexto(entity.getNombrePrenda(), "Nombre de la prenda: ", 15, true);
            stmt.setString(2, entity.getNombrePrenda());

            // Parámetro 3: Descripción
            Validador.validarTexto(entity.getDescripcionPrenda(), "Descripcion de la prenda: ", 150, false);
            stmt.setString(3, entity.getDescripcionPrenda());

            // Parámetro 4: Costo materiales
            stmt.setDouble(4, costoMateriales);

            // Parámetro 5: Costo total estimado
            stmt.setDouble(5, costoTotalEstimado);

            // Parámetro 6: ID de la MEDIDA (NO de la prenda) ← ✅ CORREGIDO
            if (entity.getIdMedida() != null && !entity.getIdMedida().isBlank()) {
                stmt.setString(6, entity.getIdMedida());
            } else {
                stmt.setNull(6, Types.VARCHAR);
            }

            stmt.execute();

            System.out.println("✓ Prenda actualizada con ID: " + entity.getIdPrenda());
            System.out.println("  ID Medida: " + entity.getIdMedida());
            System.out.println("  Costo Total: " + costoTotalEstimado);

        } catch (SQLException e) {
            System.err.println("✗ Error al actualizar prenda: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void delete(String s) throws SQLException {
        String sql = "{CALL PKG_PRENDAS.sp_eliminar_prenda(?)}";

        try (Connection conn = dbConnection.connect();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, s);
            stmt.execute();

        } catch (SQLException e) {
            int errorCode = e.getErrorCode();
            if (errorCode == 20021) {
                throw new SQLException("La prenda no existe.");
            } else if (errorCode == 20022) {
                throw new SQLException("No se puede eliminar: la prenda");
            } else {
                throw e;
            }
        }
    }

    @Override
    public Prenda recalcularCostos(String idPrenda) throws SQLException {
        String sqlGetGastos = "{? = CALL PKG_GESTION_GASTOS.fn_calcular_gastos_prenda(?)}";

        try (Connection conn = dbConnection.connect()) {

            // Obtener el total de gastos
            double costoMaterialesReal;
            try (CallableStatement stmt = conn.prepareCall(sqlGetGastos)) {
                stmt.registerOutParameter(1, Types.NUMERIC);
                stmt.setString(2, idPrenda);
                stmt.execute();

                costoMaterialesReal = stmt.getDouble(1);
            }

            // Calcular el costo total (+25%)
            double costoTotalEstimado = costoMaterialesReal * (1 + ganancia);

            // Actualizar la prenda con los nuevos costos
            String sqlUpdate = """
                    UPDATE PRENDAS 
                    SET costo_materiales = ?, 
                        costo_total_estimado = ?
                    WHERE id_prenda = ?
                    """;

            try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate)) {
                stmtUpdate.setDouble(1, costoMaterialesReal);
                stmtUpdate.setDouble(2, costoTotalEstimado);
                stmtUpdate.setString(3, idPrenda);

                int rowsAffected = stmtUpdate.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("No se encontró la prenda con ID: " + idPrenda);
                }
            }

            //  Obtener la prenda actualizada
            Optional<Prenda> prendaOpt = findById(idPrenda);
            if (prendaOpt.isEmpty()) {
                throw new SQLException("Error al obtener prenda actualizada");
            }

            Prenda prendaActualizada = prendaOpt.get();

            System.out.printf("""
                ♻️  Costos recalculados para: %s (ID: %s)
                   Costo materiales (suma de gastos): $%,.2f
                   Costo total estimado (+25%%): $%,.2f
                   Ganancia estimada: $%,.2f
                """,
                    prendaActualizada.getNombrePrenda(),
                    idPrenda,
                    costoMaterialesReal,
                    costoTotalEstimado,
                    costoTotalEstimado - costoMaterialesReal
            );

            return prendaActualizada;

        } catch (SQLException e) {
            System.err.println("Error al recalcular costos: " + e.getMessage());
            throw e;
        }
    }


}
