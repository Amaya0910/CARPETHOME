package grupo.proyecto_aula_carpethome.repositories;

import grupo.proyecto_aula_carpethome.Utilidades.Validador;
import grupo.proyecto_aula_carpethome.config.OracleDatabaseConnection;
import grupo.proyecto_aula_carpethome.entities.Proyecto;
import lombok.*;


import java.sql.*;
import java.util.ArrayList;
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
        String sql = "{CALL PKG_PROYECTOS.sp_crear_proyecto(?,?,?,?,?,?,?,?)}";  // ← 8 parámetros
        String sqlFinalizar = "{CALL PKG_PROYECTOS.sp_finalizar_proyecto(?,?)}";

        try (Connection conn = dbConnection.connect()) {
            conn.setAutoCommit(false);

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                // Parámetro 1: nombre_proyecto
                Validador.validarTexto(entity.getNombreProyecto(), "Nombre del proyecto", 15, true);
                stmt.setString(1, entity.getNombreProyecto());

                // Parámetro 2: tipo_produccion
                Validador.validarTexto(entity.getTipoProduccion(), "Tipo de producción", 15, true);
                stmt.setString(2, entity.getTipoProduccion());

                // Parámetro 3: fecha_inicio
                stmt.setDate(3, new java.sql.Date(entity.getFechaInicio().getTime()));

                // Parámetro 4: fecha_entrega_estimada
                Validador.validarRangoFechas(entity.getFechaInicio(), entity.getFechaEntregaEstimada());
                stmt.setDate(4, new java.sql.Date(entity.getFechaEntregaEstimada().getTime()));

                // Parámetro 5: estado
                Validador.validarTexto(entity.getEstado(), "Estado del proyecto", 15, true);
                stmt.setString(5, entity.getEstado());

                // Parámetro 6: costo_estimado
                if (entity.getCostoEstimado() != 0.0) {
                    stmt.setDouble(6, entity.getCostoEstimado());
                } else {
                    stmt.setNull(6, Types.NUMERIC);
                }

                // Parámetro 7: id_cliente
                Validador.validarTexto(entity.getIdCliente(), "Cliente del proyecto", 10, true);
                stmt.setString(7, entity.getIdCliente());

                // Parámetro 8: id_proyecto OUT
                stmt.registerOutParameter(8, Types.VARCHAR);

                // Ejecutar
                stmt.execute();

                // Obtener el ID generado
                String idGenerado = stmt.getString(8);
                entity.setIdProyecto(idGenerado);

                System.out.println("✓ Proyecto guardado con ID: " + idGenerado);

                // Si ya tiene fecha de entrega real, finalizar el proyecto
                if (entity.getFechaEntregaReal() != null) {
                    try (CallableStatement stmt2 = conn.prepareCall(sqlFinalizar)) {
                        stmt2.setString(1, idGenerado);
                        Validador.validarRangoFechas(entity.getFechaInicio(), entity.getFechaEntregaReal());
                        stmt2.setDate(2, new java.sql.Date(entity.getFechaEntregaReal().getTime()));
                        stmt2.execute();
                        System.out.println("✓ Proyecto finalizado");
                    }
                }

                conn.commit();
                return entity;

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("✗ Error al guardar proyecto: " + e.getMessage());
                throw e;
            }
        }
    }

    @Override
    public Optional<Proyecto> findById(String s) throws SQLException {
        String sql = """
                SELECT id_proyecto, nombre_proyecto, tipo_produccion, fecha_inicio,fecha_entrega_estimada, fecha_entrega_real, estado,costo_estimado, id_cliente
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
                SELECT id_proyecto, nombre_proyecto, tipo_produccion, fecha_inicio,fecha_entrega_estimada, fecha_entrega_real, estado,costo_estimado, id_cliente
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
        String sqlActualizar = "{CALL PKG_PROYECTOS.sp_actualizar_proyecto(?,?,?,?,?,?,?,?)}"; // 8 params
        String sqlFinalizar = "{CALL PKG_PROYECTOS.sp_finalizar_proyecto(?,?)}";

        try (Connection conn = dbConnection.connect()) {
            conn.setAutoCommit(false);

            try (CallableStatement stmt = conn.prepareCall(sqlActualizar)) {
                // Parámetro 1: id_proyecto
                stmt.setString(1, entity.getIdProyecto());

                // Parámetro 2: nombre_proyecto
                Validador.validarTexto(entity.getNombreProyecto(), "Nombre del proyecto", 15, true);
                stmt.setString(2, entity.getNombreProyecto());

                // Parámetro 3: tipo_produccion
                Validador.validarTexto(entity.getTipoProduccion(), "Tipo de producción", 15, true);
                stmt.setString(3, entity.getTipoProduccion());

                // Parámetro 4: fecha_inicio (NUEVO)
                if (entity.getFechaInicio() != null) {
                    stmt.setDate(4, new java.sql.Date(entity.getFechaInicio().getTime()));
                } else {
                    stmt.setNull(4, Types.DATE);
                }

                // Parámetro 5: fecha_entrega_estimada
                Validador.validarRangoFechas(entity.getFechaInicio(), entity.getFechaEntregaEstimada());
                stmt.setDate(5, new java.sql.Date(entity.getFechaEntregaEstimada().getTime()));

                // Parámetro 6: fecha_entrega_real (puede ser null)
                if (entity.getFechaEntregaReal() != null) {
                    Validador.validarRangoFechas(entity.getFechaInicio(), entity.getFechaEntregaReal());
                    stmt.setDate(6, new java.sql.Date(entity.getFechaEntregaReal().getTime()));
                } else {
                    stmt.setNull(6, Types.DATE);
                }

                // Parámetro 7: estado
                Validador.validarTexto(entity.getEstado(), "Estado del proyecto", 15, true);
                stmt.setString(7, entity.getEstado());

                // Parámetro 8: costo_estimado
                if (entity.getCostoEstimado() != 0.0) {
                    stmt.setDouble(8, entity.getCostoEstimado());
                } else {
                    stmt.setNull(8, Types.NUMERIC);
                }

                stmt.execute();
                System.out.println("✓ Proyecto actualizado");
            }

            // Si el proyecto se finalizó, llamar al procedimiento de finalización
            if (entity.getFechaEntregaReal() != null && "FINALIZADO".equals(entity.getEstado())) {
                try (CallableStatement stmt2 = conn.prepareCall(sqlFinalizar)) {
                    stmt2.setString(1, entity.getIdProyecto());
                    stmt2.setDate(2, new java.sql.Date(entity.getFechaEntregaReal().getTime()));
                    stmt2.execute();
                    System.out.println(" Proyecto marcado como finalizado");
                }
            }

            conn.commit();

        } catch (SQLException e) {
            System.err.println(" Error al actualizar proyecto: " + e.getMessage());
            throw new SQLException("Error al actualizar proyecto: " + e.getMessage(), e);
        }
    }


    @Override
    public void delete(String s) throws SQLException {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del proyecto no puede ser nulo o vacío");
        }

        String sql = "DELETE FROM PROYECTOS WHERE id_proyecto = ?";

        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, s);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se encontró el proyecto con ID: " + s);
            }

            System.out.println("✓ Proyecto eliminado: " + s);

        } catch (SQLException e) {
            System.err.println("✗ Error al eliminar proyecto: " + e.getMessage());
            throw new SQLException("Error al eliminar proyecto: " + e.getMessage(), e);
        }
    }
}


