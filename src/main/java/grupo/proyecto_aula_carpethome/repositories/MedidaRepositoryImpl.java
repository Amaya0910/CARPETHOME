package grupo.proyecto_aula_carpethome.repositories;

import grupo.proyecto_aula_carpethome.config.OracleDatabaseConnection;
import grupo.proyecto_aula_carpethome.entities.Medida;
import lombok.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class MedidaRepositoryImpl implements MedidaRepository{
    private final OracleDatabaseConnection dbConnection;

    private Medida mapResultSetToMedida(ResultSet rs) throws SQLException {
        return Medida.builder()
                .idMedida(rs.getString("id_medida"))
                .nombreMedida(rs.getString("nombre_medida"))
                .tipoMedida(rs.getString("tipo_medida"))
                .cBusto(rs.getDouble("c_busto"))
                .cCintura(rs.getDouble("c_cintura"))
                .cCadera(rs.getDouble("c_cadera"))
                .alturaBusto(rs.getDouble("altura_busto"))
                .separacionBusto(rs.getDouble("separacion_busto"))
                .radioBusto(rs.getDouble("radio_busto"))
                .bajoBusto(rs.getDouble("bajo_busto"))
                .largoFalda(rs.getDouble("largo_falda"))
                .largoCadera(rs.getDouble("largo_cadera"))
                .largoVestido(rs.getDouble("largo_vestido"))
                .largoPantalon(rs.getDouble("largo_pantalon"))
                .largoManga(rs.getDouble("largo_manga"))
                .build();
    }



    @Override
    public Medida save(Medida entity) throws SQLException {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad medida no puede ser null");
        }

        String sql = "{CALL PKG_MEDIDAS.sp_crear_medida(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";

        try (Connection conn = dbConnection.connect();
             CallableStatement stmt = conn.prepareCall(sql)) {
            if (entity.getNombreMedida() != null && !entity.getNombreMedida().trim().isEmpty()) {
                stmt.setString(1, entity.getNombreMedida());
            } else {
                stmt.setNull(1, Types.VARCHAR);
            }

            if (entity.getTipoMedida() != null && !entity.getTipoMedida().trim().isEmpty()) {
                stmt.setString(2, entity.getTipoMedida());
            } else {
                stmt.setNull(2, Types.VARCHAR);
            }

            if (entity.getCBusto() != 0.0) {
                stmt.setDouble(3, entity.getCBusto());
            } else {
                stmt.setNull(3, Types.NUMERIC);
            }

            if (entity.getCCintura() != 0.0) {
                stmt.setDouble(4, entity.getCCintura());
            } else {
                stmt.setNull(4, Types.NUMERIC);
            }

            if (entity.getCCadera() != 0.0) {
                stmt.setDouble(5, entity.getCCadera());
            } else {
                stmt.setNull(5, Types.NUMERIC);
            }

            if (entity.getAlturaBusto() != 0.0) {
                stmt.setDouble(6, entity.getAlturaBusto());
            } else {
                stmt.setNull(6, Types.NUMERIC);
            }

            if (entity.getSeparacionBusto() != 0.0) {
                stmt.setDouble(7, entity.getSeparacionBusto());
            } else {
                stmt.setNull(7, Types.NUMERIC);
            }

            if (entity.getRadioBusto() != 0.0) {
                stmt.setDouble(8, entity.getRadioBusto());
            } else {
                stmt.setNull(8, Types.NUMERIC);
            }

            if (entity.getBajoBusto() != 0.0) {
                stmt.setDouble(9, entity.getBajoBusto());
            } else {
                stmt.setNull(9, Types.NUMERIC);
            }

            if (entity.getLargoFalda() != 0.0) {
                stmt.setDouble(10, entity.getLargoFalda());
            } else {
                stmt.setNull(10, Types.NUMERIC);
            }

            if (entity.getLargoCadera() != 0.0) {
                stmt.setDouble(11, entity.getLargoCadera());
            } else {
                stmt.setNull(11, Types.NUMERIC);
            }

            if (entity.getLargoVestido() != 0.0) {
                stmt.setDouble(12, entity.getLargoVestido());
            } else {
                stmt.setNull(12, Types.NUMERIC);
            }

            if (entity.getLargoPantalon() != 0.0) {
                stmt.setDouble(13, entity.getLargoPantalon());
            } else {
                stmt.setNull(13, Types.NUMERIC);
            }

            if (entity.getLargoManga() != 0.0) {
                stmt.setDouble(14, entity.getLargoManga());
            } else {
                stmt.setNull(14, Types.NUMERIC);
            }

            stmt.registerOutParameter(15, Types.VARCHAR);

            stmt.execute();

            String idGenerado = stmt.getString(15);
            entity.setIdMedida(idGenerado);

            System.out.println(" Medida guardada con ID: " + idGenerado);
            return entity;

        } catch (SQLException e) {
            System.err.println("  Error al guardar medida: " + e.getMessage());
            System.err.println("  Código de error: " + e.getErrorCode());
            System.err.println("  Estado SQL: " + e.getSQLState());
            throw e;
        }
    }

    @Override
    public Optional<Medida> findById(String s) throws SQLException {
        String sql = """
                SELECT id_medida, nombre_medida, tipo_medida, c_busto,c_cintura,c_cadera,Altura_busto,separacion_busto,radio_busto,bajo_busto,largo_falda,largo_cadera,largo_vestido,largo_pantalon,largo_manga
                FROM MEDIDAS
                WHERE id_medida = ?
                """;
        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, s);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMedida(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Medida> findAll() throws SQLException {
        List<Medida> medidas = new ArrayList<>();
        String sql = """
            SELECT id_medida, nombre_medida, tipo_medida, c_busto,c_cintura,c_cadera,Altura_busto,separacion_busto,radio_busto,bajo_busto,largo_falda,largo_cadera,largo_vestido,largo_pantalon,largo_manga
            FROM medidas
            """;

        try (Connection conn = dbConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                medidas.add(mapResultSetToMedida(rs));
            }
        }

        return medidas;
    }

    @Override
    public void update(Medida entity) throws SQLException {
        // Verificar que tenga ID
        if (entity.getIdMedida() == null || entity.getIdMedida().trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la medida es obligatorio para actualizar");
        }

        // ✅ 14 parámetros (sin tipo_medida)
        String sql = "{CALL PKG_MEDIDAS.sp_actualizar_medida(?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";

        try (Connection conn = dbConnection.connect();
             CallableStatement stmt = conn.prepareCall(sql)) {

            System.out.println("=== Actualizando Medida ===");
            System.out.println("ID: " + entity.getIdMedida());
            System.out.println("Nombre: " + entity.getNombreMedida());

            // PARÁMETRO 1: ID de la medida
            stmt.setString(1, entity.getIdMedida());

            // PARÁMETRO 2: Nombre
            if (entity.getNombreMedida() != null && !entity.getNombreMedida().trim().isEmpty()) {
                stmt.setString(2, entity.getNombreMedida());
            } else {
                stmt.setNull(2, Types.VARCHAR);
            }

            // PARÁMETROS 3-14: Medidas (sin tipo_medida)
            setDoubleOrNull(stmt, 3, entity.getCBusto());
            setDoubleOrNull(stmt, 4, entity.getCCintura());
            setDoubleOrNull(stmt, 5, entity.getCCadera());
            setDoubleOrNull(stmt, 6, entity.getAlturaBusto());
            setDoubleOrNull(stmt, 7, entity.getSeparacionBusto());
            setDoubleOrNull(stmt, 8, entity.getRadioBusto());
            setDoubleOrNull(stmt, 9, entity.getBajoBusto());
            setDoubleOrNull(stmt, 10, entity.getLargoFalda());
            setDoubleOrNull(stmt, 11, entity.getLargoCadera());
            setDoubleOrNull(stmt, 12, entity.getLargoVestido());
            setDoubleOrNull(stmt, 13, entity.getLargoPantalon());
            setDoubleOrNull(stmt, 14, entity.getLargoManga());

            stmt.execute();

            System.out.println("✓ Medida actualizada: " + entity.getIdMedida() + " - " + entity.getNombreMedida());

        } catch (SQLException e) {
            System.err.println("✗ Error al actualizar medida: " + e.getMessage());
            System.err.println("✗ ID que se intentó actualizar: " + entity.getIdMedida());
            e.printStackTrace();
            throw e;
        }
    }

    // Método auxiliar
    private void setDoubleOrNull(CallableStatement stmt, int index, double value) throws SQLException {
        if (value > 0) {
            stmt.setDouble(index, value);
        } else {
            stmt.setNull(index, Types.NUMERIC);
        }
    }

    @Override
    public void delete(String s) throws SQLException {
        String sql = """
                DELETE FROM MEDIDAS WHERE  id_medida = ?
        """;
        try (Connection conn = dbConnection.connect();
        PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, s);
            stmt.executeUpdate();
        }

    }


    @Override
public List<Medida> findByTipoMedida(String tipo) throws SQLException {
    List<Medida> medidas = new ArrayList<>();
    String sql = "SELECT id_medida, nombre_medida, tipo_medida, c_busto,c_cintura,c_cadera,Altura_busto,separacion_busto,radio_busto,bajo_busto,largo_falda,largo_cadera,largo_vestido,largo_pantalon,largo_manga " +
            "FROM MEDIDAS " +
            "WHERE tipo_medida = ?";

    try (Connection conn = dbConnection.connect();
         PreparedStatement stmt = conn.prepareStatement(sql)){

        stmt.setString(1, tipo);

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                medidas.add(mapResultSetToMedida(rs));
            }
        }
    }

    return medidas;
    }



}