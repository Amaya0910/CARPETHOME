package grupo.proyecto_aula_carpethome.config;


import lombok.*;

@RequiredArgsConstructor
@Data
@Builder
public class DatabaseConfig {
    private final String host;
    private final String port;
    private final String service;
    private final String user;
    private final String password;

    public String getJdbcUrl(){
        return "jdbc:oracle:thin:@" + host + ":" + port + "/" + service;
    }


}


