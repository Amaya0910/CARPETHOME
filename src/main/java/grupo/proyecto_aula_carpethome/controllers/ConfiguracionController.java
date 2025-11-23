package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.entities.Administrador;
import grupo.proyecto_aula_carpethome.entities.Empleado;
import grupo.proyecto_aula_carpethome.entities.Persona;
import grupo.proyecto_aula_carpethome.entities.UsuarioLogueado;
import grupo.proyecto_aula_carpethome.services.AdministradorService;
import grupo.proyecto_aula_carpethome.services.EmpleadoService;
import grupo.proyecto_aula_carpethome.services.ServiceFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.sql.SQLException;

public class ConfiguracionController {

    // ==================== SERVICIOS ====================
    private final AdministradorService administradorService = ServiceFactory.getAdministradorService();
    private final EmpleadoService empleadoService = ServiceFactory.getEmpleadoService();

    // ==================== COMPONENTES FXML ====================
    @FXML private Label lblRol;
    @FXML private Label lblUltimaModificacion;
    @FXML private VBox messageContainer;
    @FXML private Label messageLabel;

    // Información Personal
    @FXML private TextField txtCedula;
    @FXML private TextField txtPrimerNombre;
    @FXML private TextField txtSegundoNombre;
    @FXML private TextField txtPrimerApellido;
    @FXML private TextField txtSegundoApellido;
    @FXML private TextField txtCorreoPrincipal;
    @FXML private TextField txtCorreoSecundario;
    @FXML private TextField txtTelefonoPrincipal;
    @FXML private TextField txtTelefonoSecundario;
    @FXML private VBox cargoContainer;
    @FXML private TextField txtCargo;

    // Botones
    @FXML private Button btnCambiarContrasena;
    @FXML private Button btnCancelar;
    @FXML private Button btnGuardar;

    // ==================== VARIABLES DE INSTANCIA ====================
    private UsuarioLogueado usuarioLogueado;
    private Persona personaOriginal; // Para restaurar si cancela
    private boolean esAdministrador;

    // ==================== INICIALIZACIÓN ====================
    @FXML
    private void initialize() {
        configurarValidaciones();
        ocultarMensaje();
    }

    /**
     * Configura las validaciones de campos
     */
    private void configurarValidaciones() {
        // Solo números en teléfonos
        txtTelefonoPrincipal.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtTelefonoPrincipal.setText(oldVal);
            }
        });

        txtTelefonoSecundario.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtTelefonoSecundario.setText(oldVal);
            }
        });

        // Validación de correo en tiempo real (opcional)
        txtCorreoPrincipal.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !newVal.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                txtCorreoPrincipal.setStyle(txtCorreoPrincipal.getStyle() +
                        "-fx-border-color: #FFA726; -fx-border-width: 2; -fx-border-radius: 8;");
            } else {
                txtCorreoPrincipal.setStyle(txtCorreoPrincipal.getStyle().replace(
                        "-fx-border-color: #FFA726; -fx-border-width: 2; -fx-border-radius: 8;", ""));
            }
        });
    }

    // ==================== MÉTODOS PÚBLICOS ====================

    /**
     * Carga los datos del usuario logueado
     */
    public void cargarDatosUsuario(UsuarioLogueado usuario) {
        this.usuarioLogueado = usuario;
        this.esAdministrador = "Administrador".equalsIgnoreCase(usuario.getRol());

        // Configurar badge de rol
        lblRol.setText(usuario.getRol());
        if (esAdministrador) {
            lblRol.setStyle(lblRol.getStyle() + "-fx-background-color: #C8E6C9; -fx-text-fill: #2E7D32;");
        } else {
            lblRol.setStyle(lblRol.getStyle() + "-fx-background-color: #BBDEFB; -fx-text-fill: #1565C0;");
        }

        // Cargar datos desde la BD
        cargarDatosPersona();
    }

    /**
     * Carga los datos de la persona desde la BD
     */
    private void cargarDatosPersona() {
        try {
            if (esAdministrador) {
                var adminOpt = administradorService.buscarPorId(usuarioLogueado.getId());
                if (adminOpt.isPresent()) {
                    Administrador admin = adminOpt.get();
                    personaOriginal = admin;
                    llenarFormulario(admin);
                    cargoContainer.setVisible(false);
                    cargoContainer.setManaged(false);
                }
            } else {
                var empleadoOpt = empleadoService.buscarPorId(usuarioLogueado.getId());
                if (empleadoOpt.isPresent()) {
                    Empleado empleado = empleadoOpt.get();
                    personaOriginal = empleado;
                    llenarFormulario(empleado);
                    cargoContainer.setVisible(true);
                    cargoContainer.setManaged(true);
                    txtCargo.setText(empleado.getCargo());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al cargar los datos: " + e.getMessage());
        }
    }

    /**
     * Llena el formulario con los datos de la persona
     */
    private void llenarFormulario(Persona persona) {
        txtCedula.setText(persona.getCedula());
        txtPrimerNombre.setText(persona.getPNombre());
        txtSegundoNombre.setText(persona.getSNombre());
        txtPrimerApellido.setText(persona.getPApellido());
        txtSegundoApellido.setText(persona.getSApellido());
        txtCorreoPrincipal.setText(persona.getPCorreo());
        txtCorreoSecundario.setText(persona.getSCorreo());

        if (persona.getPTelefono() != null && persona.getPTelefono() > 0) {
            txtTelefonoPrincipal.setText(String.valueOf(persona.getPTelefono()));
        }
        if (persona.getSTelefono() != null && persona.getSTelefono() > 0) {
            txtTelefonoSecundario.setText(String.valueOf(persona.getSTelefono()));
        }
    }

    // ==================== VALIDACIONES ====================

    /**
     * Valida el formulario completo
     */
    private boolean validarFormulario() {
        StringBuilder errores = new StringBuilder();

        // Validar campos obligatorios
        if (txtPrimerNombre.getText().trim().isEmpty()) {
            errores.append("• El primer nombre es obligatorio\n");
        }

        if (txtPrimerApellido.getText().trim().isEmpty()) {
            errores.append("• El primer apellido es obligatorio\n");
        }

        if (txtCorreoPrincipal.getText().trim().isEmpty()) {
            errores.append("• El correo principal es obligatorio\n");
        } else if (!validarEmail(txtCorreoPrincipal.getText().trim())) {
            errores.append("• El correo principal no es válido\n");
        }

        // Validar correo secundario si existe
        if (!txtCorreoSecundario.getText().trim().isEmpty() &&
                !validarEmail(txtCorreoSecundario.getText().trim())) {
            errores.append("• El correo secundario no es válido\n");
        }

        if (txtTelefonoPrincipal.getText().trim().isEmpty()) {
            errores.append("• El teléfono principal es obligatorio\n");
        } else if (txtTelefonoPrincipal.getText().trim().length() < 7) {
            errores.append("• El teléfono principal debe tener al menos 7 dígitos\n");
        }

        // Validar teléfono secundario si existe
        if (!txtTelefonoSecundario.getText().trim().isEmpty() &&
                txtTelefonoSecundario.getText().trim().length() < 7) {
            errores.append("• El teléfono secundario debe tener al menos 7 dígitos\n");
        }

        if (errores.length() > 0) {
            mostrarError(errores.toString());
            return false;
        }

        return true;
    }

    /**
     * Valida formato de email
     */
    private boolean validarEmail(String email) {
        return email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    // ==================== ACCIONES ====================

    /**
     * Guarda los cambios en la BD
     */
    @FXML
    private void handleGuardar() {
        if (!validarFormulario()) {
            return;
        }

        btnGuardar.setDisable(true);
        ocultarMensaje();

        try {
            if (esAdministrador) {
                Administrador admin = (Administrador) personaOriginal;
                actualizarDatosPersona(admin);
                administradorService.actualizarAdministrador(admin);
            } else {
                Empleado empleado = (Empleado) personaOriginal;
                actualizarDatosPersona(empleado);
                empleadoService.actualizarEmpleado(empleado);
            }

            mostrarExito("✓ Cambios guardados exitosamente");

            // Recargar datos actualizados
            cargarDatosPersona();

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al guardar los cambios: " + e.getMessage());
        } finally {
            btnGuardar.setDisable(false);
        }
    }

    /**
     * Actualiza los datos de la persona desde el formulario
     */
    private void actualizarDatosPersona(Persona persona) {
        persona.setPNombre(txtPrimerNombre.getText().trim());
        persona.setPNombre(txtSegundoNombre.getText().trim());
        persona.setPApellido(txtPrimerApellido.getText().trim());
        persona.setSApellido(txtSegundoApellido.getText().trim());
        persona.setPCorreo(txtCorreoPrincipal.getText().trim());
        persona.setSCorreo(txtCorreoSecundario.getText().trim());

        try {
            persona.setPTelefono(Long.parseLong(txtTelefonoPrincipal.getText().trim()));
        } catch (NumberFormatException e) {
            persona.setPTelefono(0L);
        }

        if (!txtTelefonoSecundario.getText().trim().isEmpty()) {
            try {
                persona.setSTelefono(Long.parseLong(txtTelefonoSecundario.getText().trim()));
            } catch (NumberFormatException e) {
                persona.setSTelefono(0L);
            }
        }
    }

    /**
     * Cancela los cambios y restaura valores originales
     */
    @FXML
    private void handleCancelar() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Cancelación");
        confirmacion.setHeaderText("¿Descartar cambios?");
        confirmacion.setContentText("Los cambios no guardados se perderán.");

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            cargarDatosPersona(); // Recargar datos originales
            ocultarMensaje();
        }
    }

    /**
     * Abre el modal para cambiar contraseña
     */
    @FXML
    private void handleCambiarContrasena() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/grupo/proyecto_aula_carpethome/CambiarContrasenaModal.fxml"));
            Parent root = loader.load();

            CambiarContrasenaController controller = loader.getController();
            controller.inicializar(usuarioLogueado, esAdministrador);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initStyle(StageStyle.TRANSPARENT);
            modalStage.setTitle("Cambiar Contraseña");

            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
            overlay.getChildren().add(root);
            overlay.setPadding(new Insets(40));

            Scene scene = new Scene(overlay);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            modalStage.setScene(scene);

            controller.setStage(modalStage);

            modalStage.centerOnScreen();
            modalStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("Error al abrir el modal de cambiar contraseña");
        }
    }

    // ==================== UI HELPERS ====================

    private void mostrarError(String mensaje) {
        messageLabel.setText(mensaje);
        messageLabel.setStyle(messageLabel.getStyle() +
                "-fx-background-color: rgba(211, 47, 47, 0.1); -fx-text-fill: #D32F2F;");
        messageContainer.setVisible(true);
        messageContainer.setManaged(true);
    }

    private void mostrarExito(String mensaje) {
        messageLabel.setText(mensaje);
        messageLabel.setStyle(messageLabel.getStyle() +
                "-fx-background-color: rgba(46, 125, 50, 0.1); -fx-text-fill: #2E7D32;");
        messageContainer.setVisible(true);
        messageContainer.setManaged(true);
    }

    private void ocultarMensaje() {
        messageContainer.setVisible(false);
        messageContainer.setManaged(false);
    }

    // ==================== EFECTOS HOVER ====================

    @FXML
    private void handleButtonHover() {
        btnCambiarContrasena.setStyle(btnCambiarContrasena.getStyle() +
                "-fx-background-color: #4A4037;");
    }

    @FXML
    private void handleButtonExit() {
        btnCambiarContrasena.setStyle(btnCambiarContrasena.getStyle().replace(
                "-fx-background-color: #4A4037;", "-fx-background-color: #61564A;"));
    }

    @FXML
    private void handleCancelarHover() {
        btnCancelar.setStyle(btnCancelar.getStyle() + "-fx-background-color: #F5F5F5;");
    }

    @FXML
    private void handleCancelarExit() {
        btnCancelar.setStyle(btnCancelar.getStyle().replace("-fx-background-color: #F5F5F5;", ""));
    }

    @FXML
    private void handleGuardarHover() {
        btnGuardar.setStyle(btnGuardar.getStyle() + "-fx-background-color: #4A4037;");
    }

    @FXML
    private void handleGuardarExit() {
        btnGuardar.setStyle(btnGuardar.getStyle().replace("-fx-background-color: #4A4037;", ""));
    }
}
