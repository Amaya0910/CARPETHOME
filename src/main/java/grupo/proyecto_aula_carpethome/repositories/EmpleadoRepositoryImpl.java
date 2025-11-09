package grupo.proyecto_aula_carpethome.repositories;

import grupo.proyecto_aula_carpethome.config.OracleDatabaseConnection;
import grupo.proyecto_aula_carpethome.entities.Empleados;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmpleadoRepositoryImpl implements Repository<Empleados, String> {
    private final OracleDatabaseConnection dbConnection;

    public EmpleadoRepositoryImpl(OracleDatabaseConnection connection) {
        this.dbConnection = connection;
    }

    @Override
    public Empleados save(Empleados entity) throws SQLException {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Insertar en USUARIOS
            var sqlUsuario = """
                    INSERT INTO USUARIOS 
                    (id_usuario, p_nombre, s_nombre, p_apellido, s_apellido, 
                     correo, contrasena, telefono) 
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """;

            try (PreparedStatement stmt = conn.prepareStatement(sqlUsuario)) {
                stmt.setString(1, entity.getIdUsuario());
                stmt.setString(2, entity.getPNombre());
                stmt.setString(3, entity.getSNombre());
                stmt.setString(4, entity.getPApellido());
                stmt.setString(5, entity.getSApellido());
                stmt.setString(6, entity.getCorreo());
                stmt.setString(7, entity.getContrasena());
                stmt.setLong(8, entity.getTelefono());
                stmt.executeUpdate();
            }

            // 2. Insertar en EMPLEADOS
            var sqlEmpleado = "INSERT INTO EMPLEADOS (id_usuario, cargo) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlEmpleado)) {
                stmt.setString(1, entity.getIdUsuario());
                stmt.setString(2, entity.getCargo());
                stmt.executeUpdate();
            }

            conn.commit();
            System.out.println("Empleado guardado: " + entity.getNombreCompleto() + " - Cargo: " + entity.getCargo());
            return entity;

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw new SQLException("Error al guardar empleado: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
            }
        }
    }

    @Override
    public Optional<Empleados> findById(String id) throws SQLException {
        var sql = """
                SELECT u.id_usuario, u.p_nombre, u.s_nombre, u.p_apellido, 
                       u.s_apellido, u.correo, u.contrasena, u.telefono, e.cargo
                FROM USUARIOS u 
                INNER JOIN EMPLEADOS e ON u.id_usuario = e.id_usuario 
                WHERE u.id_usuario = ?
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEmpleado(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Empleados> findAll() throws SQLException {
        var empleados = new ArrayList<Empleados>();
        var sql = """
                SELECT u.id_usuario, u.p_nombre, u.s_nombre, u.p_apellido, 
                       u.s_apellido, u.correo, u.contrasena, u.telefono, e.cargo
                FROM USUARIOS u 
                INNER JOIN EMPLEADOS e ON u.id_usuario = e.id_usuario 
                ORDER BY u.id_usuario
                """;

        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                empleados.add(mapResultSetToEmpleado(rs));
            }
        }

        return empleados;
    }

    @Override
    public void update(Empleados entity) throws SQLException {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Actualizar USUARIOS
            var sqlUsuario = """
                    UPDATE USUARIOS 
                    SET p_nombre=?, s_nombre=?, p_apellido=?, s_apellido=?, 
                        correo=?, contrasena=?, telefono=? 
                    WHERE id_usuario=?
                    """;

            try (PreparedStatement stmt = conn.prepareStatement(sqlUsuario)) {
                stmt.setString(1, entity.getPNombre());
                stmt.setString(2, entity.getSNombre());
                stmt.setString(3, entity.getPApellido());
                stmt.setString(4, entity.getSApellido());
                stmt.setString(5, entity.getCorreo());
                stmt.setString(6, entity.getContrasena());
                stmt.setLong(7, entity.getTelefono());
                stmt.setString(8, entity.getIdUsuario());
                stmt.executeUpdate();
            }

            // 2. Actualizar EMPLEADOS
            var sqlEmpleado = "UPDATE EMPLEADOS SET cargo=? WHERE id_usuario=?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlEmpleado)) {
                stmt.setString(1, entity.getCargo());
                stmt.setString(2, entity.getIdUsuario());
                stmt.executeUpdate();
            }

            conn.commit();
            System.out.println("✅ Empleado actualizado: " + entity.getNombreCompleto());

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw new SQLException("Error al actualizar empleado: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
            }
        }
    }

    @Override
    public void delete(String id) throws SQLException {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Eliminar de EMPLEADOS
            var sqlEmpleado = "DELETE FROM EMPLEADOS WHERE id_usuario = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlEmpleado)) {
                stmt.setString(1, id);
                stmt.executeUpdate();
            }

            // 2. Eliminar de USUARIOS
            var sqlUsuario = "DELETE FROM USUARIOS WHERE id_usuario = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlUsuario)) {
                stmt.setString(1, id);
                stmt.executeUpdate();
            }

            conn.commit();
            System.out.println("✅ Empleado eliminado con ID: " + id);

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw new SQLException("Error al eliminar empleado: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<Empleados> findByCargo(String cargo) throws SQLException {
        var empleados = new ArrayList<Empleados>();
        var sql = """
                SELECT u.id_usuario, u.p_nombre, u.s_nombre, u.p_apellido, 
                       u.s_apellido, u.correo, u.contrasena, u.telefono, e.cargo
                FROM USUARIOS u 
                INNER JOIN EMPLEADOS e ON u.id_usuario = e.id_usuario 
                WHERE e.cargo = ?
                ORDER BY u.p_nombre
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cargo);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    empleados.add(mapResultSetToEmpleado(rs));
                }
            }
        }

        return empleados;
    }

    private Empleados mapResultSetToEmpleado(ResultSet rs) throws SQLException {
        return Empleados.builder()
                .idUsuario(rs.getString("id_usuario"))
                .pNombre(rs.getString("p_nombre"))
                .sNombre(rs.getString("s_nombre"))
                .pApellido(rs.getString("p_apellido"))
                .sApellido(rs.getString("s_apellido"))
                .correo(rs.getString("correo"))
                .contrasena(rs.getString("contrasena"))
                .telefono(rs.getLong("telefono"))
                .cargo(rs.getString("cargo"))
                .build();
    }
}