package grupo.proyecto_aula_carpethome.repositories;

import grupo.proyecto_aula_carpethome.config.OracleDatabaseConnection;
import grupo.proyecto_aula_carpethome.entities.Gasto;
import lombok.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class GastosRepositoryImpl implements GastosRepository {
    private final OracleDatabaseConnection dbConnection;

    private Gasto mapResultSetToGastos(ResultSet rs) throws SQLException {
        return Gasto.builder()
                .idGasto(rs.getString("id_gasto"))
                .nombreGasto(rs.getString("nombre_gasto"))
                .idPrenda(rs.getString("id_prenda"))
                .precio(rs.getDouble("gasto"))
                .descripcionGasto(rs.getString("descripcion"))
                .build();
    }


    @Override
    public Gasto save(Gasto entity) throws SQLException {
        String sql = "{CALL PKG_GESTION_GASTOS.sp_registrar_gasto(?,?,?,?,?)}";

        try(Connection conn = dbConnection.connect();
            CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, entity.getNombreGasto());
            stmt.setString(2, entity.getIdPrenda());
            stmt.setDouble(3, entity.getPrecio());
            stmt.setString(4, entity.getDescripcionGasto());
            stmt.registerOutParameter(5, Types.VARCHAR);
            stmt.execute();

            String idGenerado = stmt.getString(3);
            entity.setIdGasto(idGenerado);

            System.out.println("Gasto guardado con ID: " + idGenerado);



            return entity;
        } catch (SQLException e) {
            System.err.println("Error al guardar gasto: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Optional<Gasto> findById(String id) throws SQLException {
        String sql = """
                SELECT * FROM gastos WHERE id_gasto = ?;
                """;

        try(Connection conn = dbConnection.connect();
        PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, id);

            try (ResultSet rs = stmt.executeQuery()){
                if (rs.next()) {
                    return Optional.of(mapResultSetToGastos(rs));
                }
            }

        }
        return Optional.empty();
    }

    @Override
    public List<Gasto> findAll() throws SQLException {
        List<Gasto> gastos = new ArrayList<>();
        String sql = """
                SELECT * FROM gastos;
        """;
        try (Connection conn = dbConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                gastos.add(mapResultSetToGastos(rs));
            }
        }
        return gastos;
    }

    @Override
    public void update(Gasto entity) throws SQLException {
        String sql = "{CALL PKG_GESTION_GASTOS.sp_actualizar_gasto(?,?,?,?)}";


        try(Connection conn = dbConnection.connect();
        CallableStatement stmt = conn.prepareCall(sql)){
            stmt.setString(1, entity.getIdGasto());
            stmt.setString(2, entity.getNombreGasto());
            stmt.setDouble(3, entity.getPrecio());
            stmt.setString(4, entity.getDescripcionGasto());
            stmt.execute();

        }catch (SQLException e){
            System.err.println("Error al actualizar gasto: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void delete(String s) throws SQLException {
        String sql = "{CALL PKG_GESTION_GASTOS.sp_eliminar_gasto(?)}";

        try(Connection conn = dbConnection.connect();
        CallableStatement stmt = conn.prepareCall(sql)){
            stmt.setString(1, s);
            stmt.execute();
        } catch (SQLException e){
            System.err.println("Error al eliminar gasto: " + e.getMessage());
            throw e;
        }

    }

    @Override
    public double TotalGastos(String id) throws SQLException {
        String sql = "{ ? = call PKG_GESTION_GASTOS.fn_total_gastos_prendas(?) }";

        try (Connection conn = dbConnection.connect();
             CallableStatement stmt = conn.prepareCall(sql)) {

            // Este es el parametro de salida "lo que devuelve la funcion"
            stmt.registerOutParameter(1, Types.NUMERIC);
            stmt.setString(2, id);

            stmt.execute();

            return stmt.getDouble(1);

        }
    }


    @Override
    public List<Gasto> findByPrenda(String idPrenda) throws SQLException {
        List<Gasto> gastos = new ArrayList<>();
        String sql = "SELECT * FROM GASTOS WHERE id_prenda = ? ORDER BY nombre_gasto";

        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idPrenda);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    gastos.add(mapResultSetToGastos(rs));
                }
            }

        }

        return gastos;
    }



}
