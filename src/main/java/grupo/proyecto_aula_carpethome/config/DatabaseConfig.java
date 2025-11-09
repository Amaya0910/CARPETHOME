package grupo.proyecto_aula_carpethome.config;

public record DatabaseConfig(
        String host,
        String port,
        String service,
        String user,
        String password
) {
    // Constructor compacto con validación
    public DatabaseConfig {
        if (host == null || host.isBlank())
            throw new IllegalArgumentException("Host no puede estar vacío");
        if (port == null || port.isBlank())
            throw new IllegalArgumentException("Port no puede estar vacío");
        if (service == null || service.isBlank())
            throw new IllegalArgumentException("Service no puede estar vacío");
        if (user == null || user.isBlank())
            throw new IllegalArgumentException("User no puede estar vacío");
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Password no puede estar vacío");
    }

    public String getJdbcUrl() {
        return "jdbc:oracle:thin:@%s:%s/%s".formatted(host, port, service);
    }
}