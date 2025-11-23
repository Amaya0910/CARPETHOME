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
        currentSelectedButton = btnUsers;
        setButtonSelected(btnUsers, true);
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
        loadView("GestionProyectos.fxml");
    }

    @FXML
    private void handleClientsClick() {
        System.out.println("Gestión de Cliente seleccionada");
        selectButton(btnClients);
        loadView("GestionClientes.fxml");
    }

    @FXML
    private void handleUsersClick() {
        System.out.println("Gestión de Usuarios seleccionada");
        selectButton(btnUsers);
        loadView("GestionUsuarios.fxml");
    }

    @FXML
    private void handleStatisticsClick() {
        System.out.println("Estadísticas seleccionada");
        selectButton(btnStatistics);
        loadView("ChatBot.fxml");
    }

    @FXML
    private void handleSettingsClick() {
        System.out.println("Settings seleccionado");
        // loadView("Settings.fxml");
    }

    @FXML
    private void handleLogoutClick() {
        System.out.println("Cerrando sesión...");
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/grupo/proyecto_aula_carpethome/Login.fxml")
            );
            Parent root = loader.load();

            javafx.stage.Stage stage = (javafx.stage.Stage) contentArea.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar el login: " + e.getMessage());
        }
    }

    // ============================================
    // ANIMACIONES HOVER
    // ============================================

    @FXML
    private void handleMenuButtonHover(MouseEvent event) {
        Button button = (Button) event.getSource();

        if (button == currentSelectedButton) {
            return;
        }

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), button);
        scaleTransition.setToX(1.03);
        scaleTransition.setToY(1.03);
        scaleTransition.play();

        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(200), button);
        translateTransition.setToX(5);
        translateTransition.play();

        String hoverStyle = button.getStyle() +
                "-fx-background-color: rgba(184, 175, 160, 0.5);";
        button.setStyle(hoverStyle);
    }

    @FXML
    private void handleMenuButtonExit(MouseEvent event) {
        Button button = (Button) event.getSource();

        if (button == currentSelectedButton) {
            return;
        }

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), button);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        scaleTransition.play();

        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(200), button);
        translateTransition.setToX(0);
        translateTransition.play();

        setButtonSelected(button, false);
    }

    // ============================================
    // GESTIÓN DE SELECCIÓN
    // ============================================

    private void selectButton(Button button) {
        if (currentSelectedButton != null) {
            setButtonSelected(currentSelectedButton, false);
        }

        currentSelectedButton = button;
        setButtonSelected(button, true);

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
        ScaleTransition press = new ScaleTransition(Duration.millis(100), button);
        press.setToX(0.95);
        press.setToY(0.95);

        ScaleTransition release = new ScaleTransition(Duration.millis(100), button);
        release.setToX(1.0);
        release.setToY(1.0);

        press.setOnFinished(e -> release.play());
        press.play();
    }

    // ============================================
    // CARGAR VISTAS
    // ============================================

    private void loadView(String fxmlFile) {
        try {
            System.out.println("Cargando vista: " + fxmlFile);

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/grupo/proyecto_aula_carpethome/" + fxmlFile)
            );
            Parent view = loader.load();

            System.out.println("Vista cargada exitosamente: " + fxmlFile);

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
            System.err.println("❌ Error al cargar la vista: " + fxmlFile);
            System.err.println("Detalles del error: " + e.getMessage());
            e.printStackTrace();

            // Mostrar un mensaje de error en el contentArea
            mostrarError("No se pudo cargar la vista: " + fxmlFile);
        }
    }

    private void mostrarError(String mensaje) {
        javafx.scene.layout.VBox errorView = new javafx.scene.layout.VBox(20);
        errorView.setAlignment(javafx.geometry.Pos.CENTER);
        errorView.setStyle("-fx-padding: 60; -fx-background-color: #E4DFD7;");

        Label lblError = new Label("⚠️ Error");
        lblError.setStyle(
                "-fx-font-size: 48px;"
        );

        Label lblMensaje = new Label(mensaje);
        lblMensaje.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-text-fill: #F44336;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;" +
                        "-fx-text-alignment: center;"
        );
        lblMensaje.setWrapText(true);

        errorView.getChildren().addAll(lblError, lblMensaje);

        contentArea.getChildren().clear();
        contentArea.getChildren().add(errorView);
    }
}