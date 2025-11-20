package grupo.proyecto_aula_carpethome.entities;

public class UsuarioLogueado {

    private String nombreCompleto;
    private String rol;

    public UsuarioLogueado(String nombreCompleto, String rol) {
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getRol() {
        return rol;
    }
}

