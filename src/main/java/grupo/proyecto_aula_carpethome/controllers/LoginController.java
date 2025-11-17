package grupo.proyecto_aula_carpethome.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

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

    // Método initialize (se ejecuta automáticamente al cargar el FXML)
    @FXML
    public void initialize() {
        System.out.println("LoginController inicializado correctamente");

        // Ocultar el mensaje de error inicialmente
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    // ============================================
    // MÉTODO DEL BOTÓN LOGIN (VACÍO)
    // ============================================

    @FXML
    private void clickAcceder() {
        // Aquí va tu lógica de login
        System.out.println("Botón de login presionado");

        // Ejemplo de cómo obtener los datos:
        String username = usernameField.getText();
        String password = passwordField.getText();

        System.out.println("Usuario: " + username);
        System.out.println("Password: " + password);
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