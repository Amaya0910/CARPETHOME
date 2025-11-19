package grupo.proyecto_aula_carpethome.repositories;

import grupo.proyecto_aula_carpethome.config.OracleDatabaseConnection;
import grupo.proyecto_aula_carpethome.entities.Proyecto;
import lombok.*;


import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ProyectoRepositoryImpl implements ProyectoRepository{
    private final OracleDatabaseConnection dbConnection;

    private Proyecto mapResultSetToProyecto(ResultSet rs) throws SQLException {
        return Proyecto.builder()
                .idProyecto(rs.getString("id_proyecto"))
                .nombreProyecto(rs.getString("nombre_proyecto"))
                .tipoProduccion(rs.getString("tipo_produccion"))
                .fechaInicio(rs.getDate("fecha_inicio"))
                .fechaEntregaEstimada(rs.getDate("fecha_entrega_estimada"))
                .fechaEntregaReal(rs.getDate("fecha_entrega_real"))
                .estado(rs.getString("estado"))
                .costoEstimado(rs.getDouble("costo_estimado"))
                .idCliente(rs.getString("id_cliente"))

                .build();
    }

    @Override
    public Proyecto save(Proyecto entity) throws SQLException {
        String sql ="{CALL PKG_PROYECTOS.sp_crear_proyecto(?,?,?,?,?,?,?,?,?)}";
        String sqlFinalizar  = "{CALL PKG_PROYECTOS.sp_finalizar_proyecto(?,?)}";
        Date fecha_real;

        if(entity.getFechaEntregaReal() != null){
            fecha_real = entity.getFechaEntregaReal();
        }else{fecha_real = null;}


        try (Connection conn = dbConnection.connect()) {

            conn.setAutoCommit(false);

            try (CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setString(1, entity.getNombreProyecto());
            stmt.setString(2, entity.getTipoProduccion());
            stmt.setDate(3, new java.sql.Date(entity.getFechaInicio().getTime()));
            stmt.setDate(4, new java.sql.Date(entity.getFechaEntregaEstimada().getTime()));
            stmt.setDate(5, new java.sql.Date(fecha_real.getTime()));
            stmt.setString(6, entity.getEstado());
            stmt.setDouble(7, entity.getCostoEstimado());
            stmt.setString(8, entity.getIdCliente());

            stmt.execute();

            String idGenerado = stmt.getString(9);
            entity.setIdProyecto(idGenerado);

            System.out.println("Proyecto guardado con ID: "+ idGenerado);

                if (entity.getFechaEntregaReal() != null) {
                    try (CallableStatement stmt2 = conn.prepareCall(sqlFinalizar)) {
                        stmt2.setString(1, entity.getIdProyecto());
                        stmt2.setDate(2, new java.sql.Date(entity.getFechaEntregaReal().getTime()));
                        stmt2.execute();
                    }
                }

                conn.commit();
                return entity;
        } catch (SQLException e) {
            System.err.println("Error al guardar proyecto"+ e.getMessage());
            throw e;
        }

        }
    }

    @Override
    public Optional<Proyecto> findById(String s) throws SQLException {
        String sql = """
                SELECT *
                FROM proyectos
                WHERE id_proyecto = ?;""";
        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, s);
            try (ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    return Optional.of(mapResultSetToProyecto(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Proyecto> findAll() throws SQLException {
        List<Proyecto> proyectos = new ArrayList<>();

        String sql = """
                SELECT *
                FROM  proyectos;
                """;
        try (Connection conn = dbConnection.connect();
        Statement  stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)){
            while(rs.next()){
                proyectos.add(mapResultSetToProyecto(rs));
            }
        }
        return proyectos;
    }

    @Override
    public void update(Proyecto entity) throws SQLException {

        String sqlActualizar = "{CALL PKG_PROYECTOS.sp_actualizar_proyecto(?,?,?,?,?,?,?, ?, ?)}";
        String sqlFinalizar  = "{CALL PKG_PROYECTOS.sp_finalizar_proyecto(?,?)}";

        try (Connection conn = dbConnection.connect()) {

            conn.setAutoCommit(false);

            try (CallableStatement stmt = conn.prepareCall(sqlActualizar)) {

                stmt.setString(1, entity.getIdProyecto());
                stmt.setString(2, entity.getNombreProyecto());
                stmt.setString(3, entity.getTipoProduccion());
                stmt.setDate(4, new java.sql.Date(entity.getFechaInicio().getTime()));
                stmt.setDate(5, new java.sql.Date(entity.getFechaEntregaEstimada().getTime()));

                // fecha entrega real puede ser null
                if (entity.getFechaEntregaReal() != null) {
                    stmt.setDate(6, new java.sql.Date(entity.getFechaEntregaReal().getTime()));
                } else {
                    stmt.setNull(6, Types.DATE);
                }

                stmt.setString(7, entity.getEstado());
                stmt.setDouble(8, entity.getCostoEstimado());
                stmt.setString(9, entity.getIdCliente());

                stmt.execute();
            }

            // Si el proyecto ya se finalizó llamar procedure finalización
            if (entity.getFechaEntregaReal() != null) {
                try (CallableStatement stmt2 = conn.prepareCall(sqlFinalizar)) {
                    stmt2.setString(1, entity.getIdProyecto());
                    stmt2.setDate(2, new java.sql.Date(entity.getFechaEntregaReal().getTime()));
                    stmt2.execute();
                }
            }

            conn.commit();

        } catch (SQLException e) {
            throw new SQLException("Error al actualizar proyecto: " + e.getMessage(), e);
        }
    }


    @Override
    public void delete(String s) throws SQLException {
        String sql = """
                DELETE FROM proyectos
                WHERE id_proyecto = ?;""";

        try (Connection conn = dbConnection.connect();
        CallableStatement stmt = conn.prepareCall(sql)){
            stmt.setString(1, s);
            stmt.execute();
        }catch (SQLException e){
            throw new SQLException("Error al eliminar proyecto: " + e.getMessage(), e);
        }
    }
}
