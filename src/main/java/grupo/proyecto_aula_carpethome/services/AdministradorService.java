package grupo.proyecto_aula_carpethome.services;

import grupo.proyecto_aula_carpethome.entities.Administrador;
import grupo.proyecto_aula_carpethome.repositories.AdministradorRepository;
import lombok.*;

import java.sql.SQLException;


@RequiredArgsConstructor
public class AdministradorService {
    private final AdministradorRepository administradorRepository;

    public void registrarAdministrador(Administrador administrador) throws SQLException {
        administradorRepository.save(administrador);
    }

    public void validarAdministrador(Administrador administrador) throws SQLException {

    }
}
