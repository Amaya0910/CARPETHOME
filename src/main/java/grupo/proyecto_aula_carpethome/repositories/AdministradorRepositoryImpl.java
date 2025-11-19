package grupo.proyecto_aula_carpethome.repositories;

import grupo.proyecto_aula_carpethome.config.OracleDatabaseConnection;
import grupo.proyecto_aula_carpethome.entities.Administrador;

import lombok.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;



@RequiredArgsConstructor
public class AdministradorRepositoryImpl implements AdministradorRepository{
    private final OracleDatabaseConnection dbConnection;


    private Administrador mapResultSetToAdministrador(ResultSet rs) throws SQLException {
        return Administrador.builder()
                .cedula(rs.getString("cedula"))
                .pNombre(rs.getString("p_nombre"))
                .sNombre(rs.getString("s_nombre"))
                .pApellido(rs.getString("p_apellido"))
                .sApellido(rs.getString("s_apellido"))
                .pCorreo(rs.getString("p_correo"))
                .sCorreo(rs.getString("s_correo"))
                .pTelefono(rs.getLong("p_telefono"))
                .sTelefono(rs.getObject("s_telefono") != null ? rs.getLong("s_telefono") : null)
                .idAdmin(rs.getString("id_admin"))
                .contrasena(rs.getString("contrasena"))
                .build();
    }


    @Override
    public Administrador save(Administrador entity) throws SQLException {
        String sql = "{CALL PKG_GESTION_PERSONAS.sp_registrar_administrador(?,?,?,?,?,?,?,?,?,?,?)}";

        try (Connection conn = dbConnection.connect();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, entity.getCedula());
            stmt.setString(2, entity.getPNombre());

            if (entity.getSNombre() != null && !entity.getSNombre().isEmpty()) {
                stmt.setString(3, entity.getSNombre());
            } else {
                stmt.setNull(3, Types.VARCHAR);
            }

            stmt.setString(4, entity.getPApellido());

            if (entity.getSApellido() != null && !entity.getSApellido().isEmpty()) {
                stmt.setString(5, entity.getSApellido());
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }

            stmt.setString(6, entity.getPCorreo());

            if (entity.getSCorreo() != null && !entity.getSCorreo().isEmpty()) {
                stmt.setString(7, entity.getSCorreo());
            } else {
                stmt.setNull(7, Types.VARCHAR);
            }

            stmt.setLong(8, entity.getPTelefono());

            if (entity.getSTelefono() != null) {
                stmt.setLong(9, entity.getSTelefono());
            } else {
                stmt.setNull(9, Types.NUMERIC);
            }

            stmt.setString(10, entity.getContrasena());

            // Parámetro OUT
            stmt.registerOutParameter(11, Types.VARCHAR);

            stmt.execute();

            String idGenerado = stmt.getString(11);
            entity.setIdAdmin(idGenerado);

            System.out.println("Administrador guardado con ID: " + idGenerado);
            return entity;

        } catch (SQLException e) {
            System.err.println("Error al guardar Administrador: " + e.getMessage());
            System.err.println("  Código de error: " + e.getErrorCode());
            System.err.println("  Estado SQL: " + e.getSQLState());
            throw e;
        }
    }



    @Override
    public Optional<Administrador> findById(String id) throws SQLException {
        String sql = """
            SELECT p.*, e.id_admin, e.contrasena
            FROM PERSONAS p
            INNER JOIN ADMINISTRADORES e ON p.cedula = e.cedula
            WHERE e.id_admin = ?
            """;

        try (Connection conn = dbConnection.connect();
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
        List<Administrador> admins = new ArrayList<>();
        String sql = """
            SELECT p.*, e.id_admin, e.contrasena
            FROM PERSONAS p
            INNER JOIN ADMINISTRADORES e ON p.cedula = e.cedula
            ORDER BY e.id_admin
            """;

        try (Connection conn = dbConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                admins.add(mapResultSetToAdministrador(rs));
            }
        }

        return admins;
    }

    @Override
    public void update(Administrador entity) throws SQLException {
        Connection conn = null;
        try {
            conn = dbConnection.connect();
            conn.setAutoCommit(false);

            String sqlPersona = """
                    UPDATE PERSONAS 
                    SET p_nombre=?, s_nombre=?, p_apellido=?, s_apellido=?, 
                        p_correo=?, s_correo=?, p_telefono=?, s_telefono=?
                    WHERE cedula=?
                    """;

            try (PreparedStatement stmt = conn.prepareStatement(sqlPersona)) {
                stmt.setString(1, entity.getPNombre());
                stmt.setString(2, entity.getSNombre());
                stmt.setString(3, entity.getPApellido());
                stmt.setString(4, entity.getSApellido());
                stmt.setString(5, entity.getPCorreo());
                stmt.setString(6, entity.getSCorreo());
                stmt.setLong(7, entity.getPTelefono());

                if (entity.getSTelefono() != null) {
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

            // 2. Actualizar ADMINISTRADORES
            String sqlEmpleado = """
                    UPDATE ADMINISTRADORES 
                    SET contrasena=? 
                    WHERE id_admin=?
                    """;

            try (PreparedStatement stmt = conn.prepareStatement(sqlEmpleado)) {
                stmt.setString(1, entity.getContrasena());
                stmt.setString(2, entity.getIdAdmin());

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("No se encontró el administrador con ID: " + entity.getIdAdmin());
                }
            }

            conn.commit();
            System.out.println("Administrador actualizado: " + entity.getNombreCompleto());

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw new SQLException("Error al actualizar administrador: " + e.getMessage(), e);
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
            String sqlGetCedula = "SELECT cedula FROM ADMINISTRADORES WHERE id_admin = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlGetCedula)) {
                stmt.setString(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        cedula = rs.getString("cedula");
                    }
                }
            }

            if (cedula == null) {
                throw new SQLException("No se encontró el administrador con ID: " + id);
            }

            // Eliminar de EMPLEADOS
            String sqlEmpleado = "DELETE FROM ADMINISTRADORES WHERE id_admin = ?";
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
            System.out.println("Administrador eliminado con ID: " + id);

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw new SQLException("Error al eliminar administrador: " + e.getMessage(), e);
        } finally {
            if (conn != null) conn.setAutoCommit(true);
        }
    }



    @Override
    public Optional<Administrador> findByCedula(String cedula) throws SQLException {
        String sql = """
            SELECT p.*, e.id_admin, e.contrasena
            FROM PERSONAS p
            INNER JOIN ADMINISTRADORES e ON p.cedula = e.cedula
            WHERE p.cedula = ?
            """;

        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cedula);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAdministrador(rs));
                }
            }
        }
        return Optional.empty();
    }



}
