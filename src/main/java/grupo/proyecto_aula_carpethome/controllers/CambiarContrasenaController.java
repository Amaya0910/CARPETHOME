package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.Utilidades.PasswordUtils;
import grupo.proyecto_aula_carpethome.entities.Administrador;
import grupo.proyecto_aula_carpethome.entities.Empleado;
import grupo.proyecto_aula_carpethome.entities.UsuarioLogueado;
import grupo.proyecto_aula_carpethome.services.AdministradorService;
import grupo.proyecto_aula_carpethome.services.EmpleadoService;

import grupo.proyecto_aula_carpethome.services.ServiceFactory;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

public class CambiarContrasenaController {
    private final AdministradorService administradorService = ServiceFactory.getAdministradorService();
    private final EmpleadoService empleadoService = ServiceFactory.getEmpleadoService();

    @FXML private VBox errorContainer;
    @FXML private Label errorLabel;
    @FXML private PasswordField txtContrasenaActual;
    @FXML private PasswordField txtNuevaContrasena;
    @FXML private PasswordField txtConfirmarContrasena;
    @FXML private Button btnCancelar;
    @FXML private Button btnCambiar;
    @FXML private Button btnCerrar;

    private UsuarioLogueado usuarioLogueado;
    private boolean esAdministrador;
    private Stage stage;

    @FXML
    private void initialize() {
        ocultarError();
    }

    /**
     * Inicializa el modal con los datos del usuario
     */
    public void inicializar(UsuarioLogueado usuario, boolean esAdmin) {
        this.usuarioLogueado = usuario;
        this.esAdministrador = esAdmin;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private boolean validarFormulario() {
        StringBuilder errores = new StringBuilder();

        // Validar contraseña actual
        if (txtContrasenaActual.getText().trim().isEmpty()) {
            errores.append("• La contraseña actual es obligatoria\n");
        }

        // Validar nueva contraseña
        if (txtNuevaContrasena.getText().trim().isEmpty()) {
            errores.append("• La nueva contraseña es obligatoria\n");
        } else if (txtNuevaContrasena.getText().trim().length() < 6) {
            errores.append("• La nueva contraseña debe tener al menos 6 caracteres\n");
        }

        // Validar confirmación
        if (txtConfirmarContrasena.getText().trim().isEmpty()) {
            errores.append("• Debe confirmar la nueva contraseña\n");
        } else if (!txtNuevaContrasena.getText().equals(txtConfirmarContrasena.getText())) {
            errores.append("• Las contraseñas no coinciden\n");
        }

        // Validar que la nueva contraseña sea diferente a la actual
        if (!txtContrasenaActual.getText().trim().isEmpty() &&
                !txtNuevaContrasena.getText().trim().isEmpty() &&
                txtContrasenaActual.getText().equals(txtNuevaContrasena.getText())) {
            errores.append("• La nueva contraseña debe ser diferente a la actual\n");
        }

        if (errores.length() > 0) {
            mostrarError(errores.toString());
            return false;
        }

        return true;
    }


    private boolean validarContrasenaActual() throws SQLException {
        String contrasenaActual = txtContrasenaActual.getText().trim();
        String contrasenaAlmacenada = null;

        // Obtener la contraseña almacenada según el tipo de usuario
        if (esAdministrador) {
            var adminOpt = administradorService.buscarPorId(usuarioLogueado.getId());
            if (adminOpt.isPresent()) {
                contrasenaAlmacenada = adminOpt.get().getContrasena();
            }
        } else {
            var empleadoOpt = empleadoService.buscarPorId(usuarioLogueado.getId());
            if (empleadoOpt.isPresent()) {
                contrasenaAlmacenada = empleadoOpt.get().getContrasena();
            }
        }

        if (contrasenaAlmacenada == null) {
            mostrarError("No se pudo verificar la contraseña actual");
            return false;
        }

        // Comparar contraseñas
        String contrasenaActualEncriptada = PasswordUtils.hashPassword(contrasenaActual);

        if (!contrasenaActual.equals(contrasenaAlmacenada)) {
            mostrarError("La contraseña actual es incorrecta");
            return false;
        }

        return true;
    }



    @FXML
    private void handleCambiar() {
        if (!validarFormulario()) {
            return;
        }

        btnCambiar.setDisable(true);
        ocultarError();

        try {
            // Validar contraseña actual
            if (!validarContrasenaActual()) {
                btnCambiar.setDisable(false);
                return;
            }

            // Encriptar nueva contraseña
            String nuevaContrasenaEncriptada = txtNuevaContrasena.getText().trim();

            // Actualizar según el tipo de usuario
            if (esAdministrador) {
                var adminOpt = administradorService.buscarPorId(usuarioLogueado.getId());
                if (adminOpt.isPresent()) {
                    Administrador admin = adminOpt.get();
                    admin.setContrasena(nuevaContrasenaEncriptada);
                    administradorService.actualizarAdministrador(admin);
                }
            } else {
                var empleadoOpt = empleadoService.buscarPorId(usuarioLogueado.getId());
                if (empleadoOpt.isPresent()) {
                    Empleado empleado = empleadoOpt.get();
                    empleado.setContrasena(nuevaContrasenaEncriptada);
                    empleadoService.actualizarEmpleado(empleado);
                }
            }

            // Mostrar mensaje de éxito y cerrar
            mostrarExito("✓ Contraseña cambiada exitosamente");

            // Cerrar después de 1.5 segundos
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(this::cerrarModal);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al cambiar la contraseña: " + e.getMessage());
            btnCambiar.setDisable(false);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error inesperado: " + e.getMessage());
            btnCambiar.setDisable(false);
        }
    }

    /**
     * Cancela el cambio de contraseña
     */
    @FXML
    private void handleCancelar() {
        // Si hay cambios, confirmar
        if (!txtContrasenaActual.getText().isEmpty() ||
                !txtNuevaContrasena.getText().isEmpty() ||
                !txtConfirmarContrasena.getText().isEmpty()) {

            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar Cancelación");
            confirmacion.setHeaderText("¿Cancelar cambio de contraseña?");
            confirmacion.setContentText("Los datos ingresados se perderán.");

            if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                cerrarModal();
            }
        } else {
            cerrarModal();
        }
    }

    // ==================== UI HELPERS ====================

    private void mostrarError(String mensaje) {
        errorLabel.setText(mensaje);
        errorContainer.setVisible(true);
        errorContainer.setManaged(true);
    }

    private void mostrarExito(String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setStyle(errorLabel.getStyle().replace(
                "-fx-text-fill: #D32F2F;", "-fx-text-fill: #2E7D32;"));
        errorLabel.setStyle(errorLabel.getStyle().replace(
                "rgba(211, 47, 47, 0.1)", "rgba(46, 125, 50, 0.1)"));
        errorContainer.setVisible(true);
        errorContainer.setManaged(true);
    }

    private void ocultarError() {
        errorContainer.setVisible(false);
        errorContainer.setManaged(false);
    }

    private void cerrarModal() {
        if (stage != null) {
            stage.close();
        } else {
            Stage currentStage = (Stage) btnCerrar.getScene().getWindow();
            if (currentStage != null) {
                currentStage.close();
            }
        }
    }

    // ==================== EFECTOS HOVER ====================

    @FXML
    private void handleCancelarHover() {
        btnCancelar.setStyle(btnCancelar.getStyle() + "-fx-background-color: #F5F5F5;");
    }

    @FXML
    private void handleCancelarExit() {
        btnCancelar.setStyle(btnCancelar.getStyle().replace("-fx-background-color: #F5F5F5;", ""));
    }

    @FXML
    private void handleCambiarHover() {
        btnCambiar.setStyle(btnCambiar.getStyle() + "-fx-background-color: #4A4037;");
    }

    @FXML
    private void handleCambiarExit() {
        btnCambiar.setStyle(btnCambiar.getStyle().replace("-fx-background-color: #4A4037;", ""));
    }
}