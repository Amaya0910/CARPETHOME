package grupo.proyecto_aula_carpethome.repositories;

import grupo.proyecto_aula_carpethome.Utilidades.Validador;
import grupo.proyecto_aula_carpethome.config.OracleDatabaseConnection;
import grupo.proyecto_aula_carpethome.entities.Cliente;
import lombok.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ClientesRepositoryImpl implements ClientesRepository {
    private final OracleDatabaseConnection dbConnection;

    private Cliente mapResultSetToCliente(ResultSet rs) throws SQLException {
        return Cliente.builder()
                .cedula(rs.getString("cedula"))
                .pNombre(rs.getString("p_nombre"))
                .sNombre(rs.getString("s_nombre"))
                .pApellido(rs.getString("p_apellido"))
                .sApellido(rs.getString("s_apellido"))
                .pCorreo(rs.getString("p_correo"))
                .sCorreo(rs.getString("s_correo"))
                .pTelefono(rs.getLong("p_telefono"))
                .sTelefono(rs.getObject("s_telefono") != null ? rs.getLong("s_telefono") : null)
                .idCliente(rs.getString("id_cliente"))
                .build();
    }

    @Override
    public Optional<String> findIdClienteByCedula(String cedula) throws SQLException {
        String sql = "SELECT id_cliente FROM CLIENTES WHERE cedula = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cedula);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getString("id_cliente"));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Cliente> findByCedula(String cedula) throws SQLException {
        String sql = """
                SELECT p.cedula, p.p_nombre,p.s_nombre, p.p_apellido, p.s_apellido, p.p_correo, p.s_correo, p.p_telefono, p.s_telefono, e.id_cliente
                FROM PERSONAS p
                INNER JOIN CLIENTES e ON p.cedula = e.cedula
                WHERE p.cedula = ?
                """;

        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cedula);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCliente(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Cliente save(Cliente entity) throws SQLException {
        String sql = "{CALL PKG_GESTION_PERSONAS.sp_registrar_cliente(?,?,?,?,?,?,?,?,?,?)}";

        try (Connection conn = dbConnection.connect();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, entity.getCedula());

            Validador.validarNombre(entity.getPNombre(), "Primer nombre");
            stmt.setString(2, entity.getPNombre());

            if (entity.getSNombre() != null && !entity.getSNombre().isEmpty()) {
                Validador.validarTexto(entity.getSNombre(), "Segundo nombre", 15, false );
                stmt.setString(3, entity.getSNombre());
            } else {
                stmt.setNull(3, Types.VARCHAR);
            }

            Validador.validarNombre(entity.getPApellido(), "Primer apellido");
            stmt.setString(4, entity.getPApellido());

            if (entity.getSApellido() != null && !entity.getSApellido().isEmpty()) {
                stmt.setString(5, entity.getSApellido());
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }

            Validador.validarCorreo(entity.getPCorreo());
            stmt.setString(6, entity.getPCorreo());

            if (entity.getSCorreo() != null && !entity.getSCorreo().isEmpty()) {
                Validador.validarCorreo(entity.getSCorreo());
                stmt.setString(7, entity.getSCorreo());
            } else {
                stmt.setNull(7, Types.VARCHAR);
            }

            Validador.validarTelefono(entity.getPTelefono());
            stmt.setLong(8, entity.getPTelefono());

            if (entity.getSTelefono() != null) {
                Validador.validarTelefono(entity.getSTelefono());
                stmt.setLong(9, entity.getSTelefono());
            } else {
                stmt.setNull(9, Types.NUMERIC);
            }

            stmt.registerOutParameter(10, Types.VARCHAR);

            stmt.execute();

            String idGenerado = stmt.getString(10);
            entity.setIdCliente(idGenerado);

            System.out.println("Cliente guardado exitosamente con ID: " + idGenerado);
            return entity;

        } catch (SQLException e) {
            System.err.println("Error al guardar Cliente: " + e.getMessage());
            System.err.println("  Código de error: " + e.getErrorCode());
            System.err.println("  Estado SQL: " + e.getSQLState());
            throw e;
        }
    }

    @Override
    public Optional<Cliente> findById(String id) throws SQLException {
        String sql = """
                SELECT p.cedula, p.p_nombre,p.s_nombre, p.p_apellido, p.s_apellido, p.p_correo, p.s_correo, p.p_telefono, p.s_telefono, e.id_cliente
                FROM PERSONAS p
                INNER JOIN CLIENTES e ON p.cedula = e.cedula
                WHERE e.id_cliente = ?
                """;

        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCliente(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Cliente> findAll() throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        String sql = """
                SELECT p.cedula, p.p_nombre,p.s_nombre, p.p_apellido, p.s_apellido, p.p_correo, p.s_correo, p.p_telefono, p.s_telefono, e.id_cliente
                FROM PERSONAS p
                INNER JOIN CLIENTES e ON p.cedula = e.cedula
                ORDER BY e.id_cliente
                """;

        try (Connection conn = dbConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                clientes.add(mapResultSetToCliente(rs));
            }
        }

        return clientes;
    }

    @Override
    public void update(Cliente entity) throws SQLException {
        Connection conn = null;
        try {
            conn = dbConnection.connect();
            conn.setAutoCommit(false);

            // 1. Actualizar PERSONAS
            String sqlPersona = """
                    UPDATE PERSONAS 
                    SET p_nombre=?, s_nombre=?, p_apellido=?, s_apellido=?, 
                        p_correo=?, s_correo=?, p_telefono=?, s_telefono=?
                    WHERE cedula=?
                    """;

            try (PreparedStatement stmt = conn.prepareStatement(sqlPersona)) {
                Validador.validarNombre(entity.getPNombre(), "Primer nombre");
                stmt.setString(1, entity.getPNombre());

                if (entity.getSNombre() != null && !entity.getSNombre().isEmpty()) {
                    Validador.validarTexto(entity.getSNombre(), "Segundo nombre", 15, false );
                    stmt.setString(2, entity.getSNombre());
                } else {
                    stmt.setNull(2, Types.VARCHAR);
                }

                Validador.validarNombre(entity.getPApellido(), "Primer apellido");
                stmt.setString(3, entity.getPApellido());

                if (entity.getSApellido() != null && !entity.getSApellido().isEmpty()) {
                    stmt.setString(4, entity.getSApellido());
                } else {
                    stmt.setNull(4, Types.VARCHAR);
                }

                Validador.validarCorreo(entity.getPCorreo());
                stmt.setString(5, entity.getPCorreo());

                if (entity.getSCorreo() != null && !entity.getSCorreo().isEmpty()) {
                    Validador.validarCorreo(entity.getSCorreo());
                    stmt.setString(6, entity.getSCorreo());
                } else {
                    stmt.setNull(6, Types.VARCHAR);
                }

                Validador.validarTelefono(entity.getPTelefono());
                stmt.setLong(7, entity.getPTelefono());

                if (entity.getSTelefono() != null) {
                    Validador.validarTelefono(entity.getSTelefono());
                    stmt.setLong(8, entity.getSTelefono());
                } else {
                    stmt.setNull(8, Types.NUMERIC);
                }

                stmt.setString(9, entity.getCedula());

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("No se encontró la persona con cédula: " + entity.getCedula());
                }
            }


            conn.commit();
            System.out.println("Cliente actualizado: " + entity.getNombreCompleto());

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw new SQLException("Error al actualizar cliente: " + e.getMessage(), e);
        } finally {
            if (conn != null) conn.setAutoCommit(true);
        }
    }

    @Override
    public void delete(String id) throws SQLException {
        Connection conn = null;
        try {
            conn = dbConnection.connect();
            conn.setAutoCommit(false);

            String cedula = null;
            String sqlGetCedula = "SELECT cedula FROM CLIENTES WHERE id_cliente = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlGetCedula)) {
                stmt.setString(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        cedula = rs.getString("cedula");
                    }
                }
            }

            if (cedula == null) {
                throw new SQLException("No se encontró el cliente con ID: " + id);
            }

            // Eliminar de EMPLEADOS
            String sqlEmpleado = "DELETE FROM CLIENTES WHERE id_cliente = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlEmpleado)) {
                stmt.setString(1, id);
                stmt.executeUpdate();
            }

            // Eliminar de PERSONAS
            String sqlPersona = "DELETE FROM PERSONAS WHERE cedula = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlPersona)) {
                stmt.setString(1, cedula);
                stmt.executeUpdate();
            }

            conn.commit();
            System.out.println("Cliente eliminado con ID: " + id);

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw new SQLException("Error al eliminar cliente: " + e.getMessage(), e);
        } finally {
            if (conn != null) conn.setAutoCommit(true);
        }
    }

    @Override
    public int contarProyectosPorCliente(String idCliente) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM PROYECTOS WHERE id_cliente = ?";

        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idCliente);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    @Override
    public int contarProyectosActivosPorCliente(String idCliente) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM PROYECTOS WHERE id_cliente = ? AND estado = 'En Progreso'";

        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idCliente);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    @Override
    public int contarProyectosCompletadosPorCliente(String idCliente) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM PROYECTOS WHERE id_cliente = ? AND estado = 'Completado'";

        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idCliente);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    @Override
    public List<Cliente> findClientesConProyectosActivos() throws SQLException {
        List<Cliente> clientes = new ArrayList<>();

        String sql = """
            SELECT DISTINCT p.cedula, p.p_nombre, p.s_nombre, p.p_apellido, p.s_apellido, 
                   p.p_correo, p.s_correo, p.p_telefono, p.s_telefono, c.id_cliente
            FROM PERSONAS p
            INNER JOIN CLIENTES c ON p.cedula = c.cedula
            INNER JOIN PROYECTOS pr ON c.id_cliente = pr.id_cliente
            WHERE pr.estado = 'En Pogreso'
            ORDER BY c.id_cliente
            """;

        try (Connection conn = dbConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                clientes.add(mapResultSetToCliente(rs));
            }
        }

        return clientes;
    }

    @Override
    public List<Cliente> findClientesSinProyectos() throws SQLException {
        List<Cliente> clientes = new ArrayList<>();

        String sql = """
            SELECT p.cedula, p.p_nombre, p.s_nombre, p.p_apellido, p.s_apellido, 
                   p.p_correo, p.s_correo, p.p_telefono, p.s_telefono, c.id_cliente
            FROM PERSONAS p
            INNER JOIN CLIENTES c ON p.cedula = c.cedula
            WHERE NOT EXISTS (
                SELECT 1 FROM PROYECTOS pr WHERE pr.id_cliente = c.id_cliente
            )
            ORDER BY c.id_cliente
            """;

        try (Connection conn = dbConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                clientes.add(mapResultSetToCliente(rs));
            }
        }

        return clientes;
    }


}
