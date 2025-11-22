package grupo.proyecto_aula_carpethome.services;

import grupo.proyecto_aula_carpethome.config.DatabaseConfig;
import grupo.proyecto_aula_carpethome.config.OracleDatabaseConnection;
import grupo.proyecto_aula_carpethome.repositories.*;

public class ServiceFactory {
    private static final DatabaseConfig config = DatabaseConfig.builder()
            .host("localhost")
            .port("1521")
            .service("xepdb1")
            .user("U_ADMIN_CARPET")
            .password("ADMIN")
            .build();

    private static final OracleDatabaseConnection dbConnection =
            new OracleDatabaseConnection(config);

    // Solo instancias para dependencias circulares
    private static ProyectoService proyectoServiceInstance;
    private static PrendaService prendaServiceInstance;

    // Servicios SIN dependencias circulares (creación directa)
    public static AdministradorService getAdministradorService() {
        var adminRepo = new AdministradorRepositoryImpl(dbConnection);
        return new AdministradorService(adminRepo);
    }

    public static EmpleadoService getEmpleadoService() {
        var empleadoRepo = new EmpleadoRepositoryImpl(dbConnection);
        return new EmpleadoService(empleadoRepo);
    }

    public static ClienteService getClienteService() {
        var clienteRepo = new ClientesRepositoryImpl(dbConnection);
        return new ClienteService(clienteRepo);
    }

    public static EtapaService getEtapaService() {
        var etapaRepo = new EtapasRepositoryImpl(dbConnection);
        return new EtapaService(etapaRepo);
    }

    // GastoService tiene dependencia simple (NO circular)
    public static GastoService getGastoService() {
        var gastoRepo = new GastosRepositoryImpl(dbConnection);
        return new GastoService(gastoRepo, getPrendaService()); // ← Dependencia simple
    }

    public static HistEtapaService getHistEtapaService() {
        var histEtapaRepo = new HistEtapaRepositoryImpl(dbConnection);
        return new HistEtapaService(histEtapaRepo);
    }

    public static MedidaService getMedidaService() {
        var medidaRepo = new MedidaRepositoryImpl(dbConnection);
        return new MedidaService(medidaRepo);
    }

    // SOLO estos dos necesitan manejo especial por dependencia circular
    public static PrendaService getPrendaService() {
        if (prendaServiceInstance == null) {
            var prendaRepo = new PrendaRepositoryImpl(dbConnection);
            prendaServiceInstance = new PrendaService(prendaRepo);
            prendaServiceInstance.setProyectoService(getProyectoService()); // ← Setter aquí también
        }
        return prendaServiceInstance;
    }

    public static ProyectoService getProyectoService() {
        if (proyectoServiceInstance == null) {
            var proyectoRepo = new ProyectoRepositoryImpl(dbConnection);
            proyectoServiceInstance = new ProyectoService(proyectoRepo );
            proyectoServiceInstance.setPrendaService(getPrendaService()); // ← Setter
        }
        return proyectoServiceInstance;
    }

    public static OracleDatabaseConnection getDatabaseConnection() {
        return dbConnection;
    }
}