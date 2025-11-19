package grupo.proyecto_aula_carpethome.repositories;

import grupo.proyecto_aula_carpethome.Utilidades.Validador;
import grupo.proyecto_aula_carpethome.config.OracleDatabaseConnection;
import grupo.proyecto_aula_carpethome.entities.Empleado;
import lombok.RequiredArgsConstructor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class EmpleadoRepositoryImpl implements EmpleadoRepository {
    private final OracleDatabaseConnection dbConnection;


    private Empleado mapResultSetToEmpleado(ResultSet rs) throws SQLException {
        return Empleado.builder()
                .cedula(rs.getString("cedula"))
                .pNombre(rs.getString("p_nombre"))
                .sNombre(rs.getString("s_nombre"))
                .pApellido(rs.getString("p_apellido"))
                .sApellido(rs.getString("s_apellido"))
                .pCorreo(rs.getString("p_correo"))
                .sCorreo(rs.getString("s_correo"))
                .pTelefono(rs.getLong("p_telefono"))
                .sTelefono(rs.getObject("s_telefono") != null ? rs.getLong("s_telefono") : null)
                .idEmpleado(rs.getString("id_empleado"))
                .cargo(rs.getString("cargo"))
                .contrasena(rs.getString("contrasena"))
                .build();
    }

    @Override
    public Empleado save(Empleado entity) throws SQLException {
        String sql = "{CALL PKG_GESTION_PERSONAS.sp_registrar_empleado(?,?,?,?,?,?,?,?,?,?,?,?)}";

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

            stmt.setString(10, entity.getCargo());
            stmt.setString(11, entity.getContrasena());

            // Parámetro OUT
            stmt.registerOutParameter(12, Types.VARCHAR);

            stmt.execute();

            String idGenerado = stmt.getString(12);
            entity.setIdEmpleado(idGenerado);

            System.out.println("Empleado guardado con ID: " + idGenerado + " - Cargo: " + entity.getCargo());
            return entity;

        } catch (SQLException e) {
            System.err.println("Error al guardar empleado: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Optional<Empleado> findById(String id) throws SQLException {
        String sql = """
                SELECT p.cedula, p.p_nombre,p.s_nombre, p.p_apellido, p.s_apellido, p.p_correo, p.s_correo, p.p_telefono, p.s_telefono, e.id_empleado, e.cargo, e.contrasena
                FROM PERSONAS p
                INNER JOIN EMPLEADOS e ON p.cedula = e.cedula
                WHERE e.id_empleado = ?
                """;

        try (Connection conn = dbConnection.connect();
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
    public List<Empleado> findAll() throws SQLException {
        List<Empleado> empleados = new ArrayList<>();
        String sql = """
                SELECT p.cedula, p.p_nombre,p.s_nombre, p.p_apellido, p.s_apellido, p.p_correo, p.s_correo, p.p_telefono, p.s_telefono, e.id_empleado
                FROM PERSONAS p
                INNER JOIN EMPLEADOS e ON p.cedula = e.cedula
                ORDER BY e.id_empleado
                """;

        try (Connection conn = dbConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                empleados.add(mapResultSetToEmpleado(rs));
            }
        }

        return empleados;
    }

    @Override
    public void update(Empleado entity) throws SQLException {
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

            // 2. Actualizar EMPLEADOS (cargo y contraseña)
            String sqlEmpleado = """
                    UPDATE EMPLEADOS 
                    SET cargo=?, contrasena=? 
                    WHERE id_empleado=?
                    """;

            try (PreparedStatement stmt = conn.prepareStatement(sqlEmpleado)) {
                stmt.setString(1, entity.getCargo());

                Validador.validarContrasena(entity.getContrasena());
                stmt.setString(2, entity.getContrasena());
                stmt.setString(3, entity.getIdEmpleado());

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("No se encontró el empleado con ID: " + entity.getIdEmpleado());
                }
            }

            conn.commit();
            System.out.println("Empleado actualizado: " + entity.getNombreCompleto() + " - Cargo: " + entity.getCargo());

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw new SQLException("Error al actualizar empleado: " + e.getMessage(), e);
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
            String sqlGetCedula = "SELECT cedula FROM EMPLEADOS WHERE id_empleado = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlGetCedula)) {
                stmt.setString(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        cedula = rs.getString("cedula");
                    }
                }
            }

            if (cedula == null) {
                throw new SQLException("No se encontró el empleado con ID: " + id);
            }

            // Eliminar de EMPLEADOS
            String sqlEmpleado = "DELETE FROM EMPLEADOS WHERE id_empleado = ?";
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
            System.out.println("Empleado eliminado con ID: " + id);

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw new SQLException("Error al eliminar empleado: " + e.getMessage(), e);
        } finally {
            if (conn != null) conn.setAutoCommit(true);
        }
    }


    @Override
    public List<Empleado> findByCargo(String cargo) throws SQLException {
        List<Empleado> empleados = new ArrayList<>();
        String sql = """
                SELECT p.cedula, p.p_nombre,p.s_nombre, p.p_apellido, p.s_apellido, p.p_correo, p.s_correo, p.p_telefono, p.s_telefono, e.id_empleado, e.cargo, e.contrasena
                FROM PERSONAS p
                INNER JOIN EMPLEADOS e ON p.cedula = e.cedula
                WHERE UPPER(e.cargo) = UPPER(?)
                ORDER BY p.p_nombre
                """;

        try (Connection conn = dbConnection.connect();
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


    @Override
    public Optional<Empleado> findByCedula(String cedula) throws SQLException {
        String sql = """
                SELECT p.cedula, p.p_nombre,p.s_nombre, p.p_apellido, p.s_apellido, p.p_correo, p.s_correo, p.p_telefono, p.s_telefono, e.id_empleado, e.cargo
                FROM PERSONAS p
                INNER JOIN EMPLEADOS e ON p.cedula = e.cedula
                WHERE p.cedula = ?
                """;

        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cedula);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEmpleado(rs));
                }
            }
        }
        return Optional.empty();
    }


    @Override
    public int countByCargo(String cargo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM EMPLEADOS WHERE UPPER(cargo) = UPPER(?)";

        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cargo);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    @Override
    public List<String> getAllCargos() throws SQLException {
        List<String> cargos = new ArrayList<>();
        String sql = "SELECT DISTINCT cargo FROM EMPLEADOS ORDER BY cargo";

        try (Connection conn = dbConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                cargos.add(rs.getString("cargo"));
            }
        }

        return cargos;
    }



}