package grupo.proyecto_aula_carpethome.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;

public class MenuController {

    @FXML
    private Label lblUserName;

    @FXML
    private Label lblUserRole;

    @FXML
    private Button btnProjects;

    @FXML
    private Button btnClients;

    @FXML
    private Button btnUsers;

    @FXML
    private Button btnStatistics;

    @FXML
    private Button btnSettings;

    @FXML
    private Button btnLogout;

    @FXML
    private StackPane contentArea;

    private Button currentSelectedButton = null;

    @FXML
    public void initialize() {
        System.out.println("MenuController inicializado");

        // Establecer el botón de usuarios como seleccionado por defecto
        currentSelectedButton = btnUsers;
        setButtonSelected(btnUsers, true);

        // Aquí puedes establecer el usuario y rol dinámicamente
        // Por ejemplo, recibirlos desde el login
        setUserInfo("Juan Pérez", "Administrador");
    }

    // ============================================
    // MÉTODOS PARA ESTABLECER INFO DEL USUARIO
    // ============================================

    public void setUserInfo(String userName, String userRole) {
        lblUserName.setText(userName);
        lblUserRole.setText(userRole);
    }

    // ============================================
    // MANEJADORES DE CLICKS
    // ============================================

    @FXML
    private void handleProjectsClick() {
        System.out.println("Gestión de Proyecto seleccionada");
        selectButton(btnProjects);
        // Aquí cargarías la vista de gestión de proyectos
        // loadView("ProjectsView.fxml");
    }

    @FXML
    private void handleClientsClick() {
        System.out.println("Gestión de Cliente seleccionada");
        selectButton(btnClients);
        // loadView("ClientsView.fxml");
    }

    @FXML
    private void handleUsersClick() {
        System.out.println("Gestión de Usuarios seleccionada");
        selectButton(btnUsers);
        // loadView("UsersView.fxml");
    }

    @FXML
    private void handleStatisticsClick() {
        System.out.println("Estadísticas seleccionada");
        selectButton(btnStatistics);
        // loadView("StatisticsView.fxml");
    }

    @FXML
    private void handleSettingsClick() {
        System.out.println("Settings seleccionado");
        // loadView("SettingsView.fxml");
    }

    @FXML
    private void handleLogoutClick() {
        System.out.println("Cerrando sesión...");
        // Aquí implementarías la lógica de logout
        // Por ejemplo: volver a la pantalla de login
    }

    // ============================================
    // ANIMACIONES HOVER
    // ============================================

    @FXML
    private void handleMenuButtonHover(MouseEvent event) {
        Button button = (Button) event.getSource();

        // No animar si es el botón seleccionado actualmente
        if (button == currentSelectedButton) {
            return;
        }

        // Animación de escala sutil
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), button);
        scaleTransition.setToX(1.03);
        scaleTransition.setToY(1.03);
        scaleTransition.play();

        // Animación de desplazamiento hacia la derecha
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(200), button);
        translateTransition.setToX(5);
        translateTransition.play();

        // Cambiar color de fondo con estilo inline
        String hoverStyle = button.getStyle() +
                "-fx-background-color: rgba(184, 175, 160, 0.5);";
        button.setStyle(hoverStyle);
    }

    @FXML
    private void handleMenuButtonExit(MouseEvent event) {
        Button button = (Button) event.getSource();

        // No animar si es el botón seleccionado actualmente
        if (button == currentSelectedButton) {
            return;
        }

        // Restaurar escala
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), button);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        scaleTransition.play();

        // Restaurar posición
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(200), button);
        translateTransition.setToX(0);
        translateTransition.play();

        // Restaurar estilo original
        setButtonSelected(button, false);
    }


    // GESTIÓN DE SELECCIÓN


    private void selectButton(Button button) {
        // Deseleccionar el botón anterior
        if (currentSelectedButton != null) {
            setButtonSelected(currentSelectedButton, false);
        }

        // Seleccionar el nuevo botón
        currentSelectedButton = button;
        setButtonSelected(button, true);

        // Animación de "click"
        animateButtonClick(button);
    }

    private void setButtonSelected(Button button, boolean selected) {
        String baseStyle = "-fx-text-fill: #181716;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: " + (selected ? "600" : "500") + ";" +
                "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 14 20;" +
                "-fx-border-color: transparent;" +
                "-fx-border-width: 0;";

        if (selected) {
            button.setStyle(baseStyle + "-fx-background-color: #B8AFA0;");
        } else {
            // Estilos especiales para Settings y Logout
            if (button == btnSettings || button == btnLogout) {
                button.setStyle("-fx-background-color: transparent;" +
                        "-fx-text-fill: #61564A;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: 500;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 12 20;");
            } else {
                button.setStyle(baseStyle + "-fx-background-color: transparent;");
            }
        }
    }

    private void animateButtonClick(Button button) {
        // Animación de "presión" rápida
        ScaleTransition press = new ScaleTransition(Duration.millis(100), button);
        press.setToX(0.95);
        press.setToY(0.95);

        ScaleTransition release = new ScaleTransition(Duration.millis(100), button);
        release.setToX(1.0);
        release.setToY(1.0);

        press.setOnFinished(e -> release.play());
        press.play();
    }


    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/grupo/proyecto_aula_carpethome/" + fxmlFile));
            Parent view = loader.load();

            // Animación de fade para el cambio de vista
            FadeTransition fadeOut = new FadeTransition(Duration.millis(150), contentArea);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            fadeOut.setOnFinished(e -> {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(view);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(150), contentArea);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });

            fadeOut.play();

        } catch (IOException e) {
            System.err.println("Error al cargar la vista: " + fxmlFile);
            e.printStackTrace();
        }
    }

}
