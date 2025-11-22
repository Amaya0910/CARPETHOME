package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.HelloApplication;
import grupo.proyecto_aula_carpethome.services.ServiceFactory;
import grupo.proyecto_aula_carpethome.entities.UsuarioLogueado;
import grupo.proyecto_aula_carpethome.services.AdministradorService;
import grupo.proyecto_aula_carpethome.services.EmpleadoService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    // Referencias a los elementos del FXML
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button togglePasswordBtn;

    @FXML
    private ImageView eyeIcon;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    @FXML
    public void initialize() {
        System.out.println("LoginController inicializado correctamente");

        // Ocultar el mensaje de error inicialmente
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    @FXML
    private void clickAcceder() throws SQLException {
        // 1. Capturar los datos
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // 2. Validar que no estén vacíos
        if (username.isEmpty() || password.isEmpty()) {
            mostrarError("Por favor, completa todos los campos");
            return;
        }


        // 3. Intentar validar como Administrador
        AdministradorService adminService = ServiceFactory.getAdministradorService();
        var administrador = adminService.validarCredenciales(username, password);

        if (administrador != null && administrador.isPresent()) {
            // Es un administrador válido
            UsuarioLogueado usuario = new UsuarioLogueado(
                    administrador.get().getNombreCompleto(),
                    "Administrador"
            );
            try {
                HelloApplication.loadMenu(usuario);
            } catch (IOException e) {
                e.printStackTrace();
                mostrarError("Error al cargar el menú");
            }
            return;
        }

        // 4. Si no es admin, intentar validar como Empleado
        EmpleadoService empleadoService = ServiceFactory.getEmpleadoService();
        var empleado = empleadoService.validarCredenciales(username, password);

        if (empleado != null && empleado.isPresent()) {
            // Es un empleado válido
            UsuarioLogueado usuario = new UsuarioLogueado(
                    empleado.get().getNombreCompleto(),
                    "Administrador"
            );
            try {
                HelloApplication.loadMenu(usuario);
            } catch (IOException e) {
                e.printStackTrace();
                mostrarError("Error al cargar el menú");
            }
            return;
        }


        // 5. Si no es ni admin ni empleado, mostrar error
        System.out.println("Credenciales incorrectas");
        mostrarError("Usuario o contraseña incorrectos");
        passwordField.clear();
        passwordField.requestFocus();

    }

    private void mostrarError(String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    // ============================================
    // EFECTOS HOVER DEL BOTÓN
    // ============================================

    @FXML
    private void handleMouseEnter(MouseEvent event) {
        loginButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #61564A, #4a433e);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: 600;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;" +
                        "-fx-background-radius: 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 16 0;" +
                        "-fx-min-height: 55px;" +
                        "-fx-letter-spacing: 1.5px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(24, 23, 22, 0.5), 20, 0, 0, 7);" +
                        "-fx-scale-x: 1.02;" +
                        "-fx-scale-y: 1.02;"
        );
    }

    @FXML
    private void handleMouseExit(MouseEvent event) {
        loginButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #181716, #2a2827);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: 600;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;" +
                        "-fx-background-radius: 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 16 0;" +
                        "-fx-min-height: 55px;" +
                        "-fx-letter-spacing: 1.5px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(24, 23, 22, 0.4), 15, 0, 0, 5);"
        );
    }
}