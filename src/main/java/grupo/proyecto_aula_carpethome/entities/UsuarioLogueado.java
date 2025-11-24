package grupo.proyecto_aula_carpethome.entities;

public class UsuarioLogueado {
    private String id;              // ← NUEVO: puede ser idAdmin o idEmpleado
    private String nombreCompleto;
    private String rol;             // "Administrador" o "Empleado"
    private String cedula;          // ← NUEVO: para cargar los datos

    public UsuarioLogueado(String id, String nombreCompleto, String rol, String cedula) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
        this.cedula = cedula;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }


}

