package grupo.proyecto_aula_carpethome.repositories;

import grupo.proyecto_aula_carpethome.config.OracleDatabaseConnection;
import grupo.proyecto_aula_carpethome.entities.Administrador;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdministradorRepositoryImpl implements Repository<Administrador, String> {
    private final OracleDatabaseConnection dbConnection;

    public AdministradorRepositoryImpl(OracleDatabaseConnection connection) {
        this.dbConnection = connection;
    }

    @Override
    public Administrador save(Administrador entity) throws SQLException {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Insertar en USUARIOS - NOTA: Nombres de columnas con guiones bajos
            var sqlUsuario = """
                    INSERT INTO USUARIOS 
                    (id_usuario, p_nombre, s_nombre, p_apellido, s_apellido, 
                     correo, contrasena, telefono) 
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """;

            try (PreparedStatement stmt = conn.prepareStatement(sqlUsuario)) {
                // Usamos los getters en camelCase que genera Lombok
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

            // 2. Insertar en ADMINISTRADOR
            var sqlAdmin = "INSERT INTO ADMINISTRADOR (id_usuario) VALUES (?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlAdmin)) {
                stmt.setString(1, entity.getIdUsuario());
                stmt.executeUpdate();
            }

            conn.commit();
            System.out.println("Administrador guardado: " + entity.getNombreCompleto());
            return entity;

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw new SQLException("Error al guardar administrador: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
            }
        }
    }

    @Override
    public Optional<Administrador> findById(String id) throws SQLException {
        // Nombres de columnas con guiones bajos en SQL
        var sql = """
                SELECT u.id_usuario, u.p_nombre, u.s_nombre, u.p_apellido, 
                       u.s_apellido, u.correo, u.contrasena, u.telefono
                FROM USUARIOS u 
                INNER JOIN ADMINISTRADOR a ON u.id_usuario = a.id_usuario 
                WHERE u.id_usuario = ?
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAdministrador(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Administrador> findAll() throws SQLException {
        var administradores = new ArrayList<Administrador>();
        var sql = """
                SELECT u.id_usuario, u.p_nombre, u.s_nombre, u.p_apellido, 
                       u.s_apellido, u.correo, u.contrasena, u.telefono
                FROM USUARIOS u 
                INNER JOIN ADMINISTRADOR a ON u.id_usuario = a.id_usuario 
                ORDER BY u.id_usuario
                """;

        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                administradores.add(mapResultSetToAdministrador(rs));
            }
        }

        return administradores;
    }

    @Override
    public void update(Administrador entity) throws SQLException {
        var sql = """
                UPDATE USUARIOS 
                SET p_nombre=?, s_nombre=?, p_apellido=?, s_apellido=?, 
                    correo=?, contrasena=?, telefono=? 
                WHERE id_usuario=?
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getPNombre());
            stmt.setString(2, entity.getSNombre());
            stmt.setString(3, entity.getPApellido());
            stmt.setString(4, entity.getSApellido());
            stmt.setString(5, entity.getCorreo());
            stmt.setString(6, entity.getContrasena());
            stmt.setLong(7, entity.getTelefono());
            stmt.setString(8, entity.getIdUsuario());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("No se encontró el administrador con ID: " + entity.getIdUsuario());
            }

            System.out.println("✅ Administrador actualizado: " + entity.getNombreCompleto());
        }
    }

    @Override
    public void delete(String id) throws SQLException {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Eliminar de ADMINISTRADOR
            var sqlAdmin = "DELETE FROM ADMINISTRADOR WHERE id_usuario = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlAdmin)) {
                stmt.setString(1, id);
                stmt.executeUpdate();
            }

            // 2. Eliminar de USUARIOS
            var sqlUsuario = "DELETE FROM USUARIOS WHERE id_usuario = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlUsuario)) {
                stmt.setString(1, id);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected == 0) {
                    throw new SQLException("No se encontró el usuario con ID: " + id);
                }
            }

            conn.commit();
            System.out.println("✅ Administrador eliminado con ID: " + id);

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw new SQLException("Error al eliminar administrador: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Mapea un ResultSet a un objeto Administrador
     * IMPORTANTE: Los nombres de las columnas deben coincidir con la BD (snake_case)
     */
    private Administrador mapResultSetToAdministrador(ResultSet rs) throws SQLException {
        return Administrador.builder()
                .idUsuario(rs.getString("id_usuario"))      // BD: id_usuario
                .pNombre(rs.getString("p_nombre"))          // BD: p_nombre
                .sNombre(rs.getString("s_nombre"))          // BD: s_nombre
                .pApellido(rs.getString("p_apellido"))      // BD: p_apellido
                .sApellido(rs.getString("s_apellido"))      // BD: s_apellido
                .correo(rs.getString("correo"))             // BD: correo
                .contrasena(rs.getString("contrasena"))     // BD: contrasena
                .telefono(rs.getLong("telefono"))           // BD: telefono
                .build();
    }
}