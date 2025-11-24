package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.entities.UsuarioLogueado;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class MenuController {

    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Button btnProjects;
    @FXML private Button btnEtapas;
    @FXML private Button btnMedidas;  // ✨ NUEVO
    @FXML private Button btnClients;
    @FXML private Button btnUsers;
    @FXML private Button btnStatistics;
    @FXML private Button btnSettings;
    @FXML private Button btnLogout;
    @FXML private StackPane contentArea;

    private Button currentSelectedButton = null;
    private UsuarioLogueado usuarioLogueado;

    @FXML
    public void initialize() {
        System.out.println("MenuController inicializado");
        currentSelectedButton = btnUsers;
        setButtonSelected(btnUsers, true);
    }

    // ============================================
    // MÉTODOS PARA ESTABLECER INFO DEL USUARIO
    // ============================================

    public void setUserInfo(UsuarioLogueado usuario) {
        this.usuarioLogueado = usuario;
        lblUserName.setText(usuario.getNombreCompleto());
        lblUserRole.setText(usuario.getRol());

        System.out.println("=== Usuario configurado en MenuController ===");
        System.out.println("ID: " + usuario.getId());
        System.out.println("Nombre: " + usuario.getNombreCompleto());
        System.out.println("Rol: " + usuario.getRol());

        // Ocultar Gestión de Usuarios si no es admin
        if (!"Administrador".equalsIgnoreCase(usuario.getRol())) {
            btnUsers.setVisible(false);
            btnUsers.setManaged(false);
        }
    }

    @Deprecated
    public void setUserInfo(String userName, String userRole) {
        lblUserName.setText(userName);
        lblUserRole.setText(userRole);

        if (!"ADMINISTRADOR".equals(userRole) && !"Administrador".equalsIgnoreCase(userRole)) {
            btnUsers.setVisible(false);
            btnUsers.setManaged(false);
        }

        System.err.println("⚠️ ADVERTENCIA: Usando método deprecated. Usa setUserInfo(UsuarioLogueado) en su lugar.");
    }

    // ============================================
    // MANEJADORES DE CLICKS
    // ============================================

    @FXML
    private void handleProjectsClick() {
        System.out.println("Gestión de Proyectos seleccionada");
        selectButton(btnProjects);
        loadView("GestionProyectos.fxml");
    }

    // ✨ NUEVO: Handler para Gestión de Etapas
    @FXML
    private void handleEtapasClick() {
        System.out.println("Gestión de Etapas seleccionada");
        selectButton(btnEtapas);
        loadView("GestionEtapas.fxml");
    }

    // ✨ NUEVO: Handler para Medidas Estándar
    @FXML
    private void handleMedidasClick() {
        System.out.println("Medidas Estándar seleccionada");
        selectButton(btnMedidas);
        loadView("GestionMedidasEstandar.fxml");
    }

    @FXML
    private void handleClientsClick() {
        System.out.println("Gestión de Clientes seleccionada");
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
        selectButton(btnSettings);

        if (usuarioLogueado == null) {
            System.err.println("❌ ERROR: No hay usuario logueado");
            mostrarError("Error: No hay información del usuario. Por favor, inicia sesión nuevamente.");
            return;
        }

        if (usuarioLogueado.getId() == null || usuarioLogueado.getId().isEmpty()) {
            System.err.println("❌ ERROR: El ID del usuario es nulo");
            mostrarError("Error: Información del usuario incompleta. Por favor, inicia sesión nuevamente.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/grupo/proyecto_aula_carpethome/Configuracion.fxml"));
            Parent root = loader.load();

            ConfiguracionController controller = loader.getController();
            controller.cargarDatosUsuario(usuarioLogueado);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(150), contentArea);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            fadeOut.setOnFinished(e -> {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(150), contentArea);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });

            fadeOut.play();
            System.out.println("✓ Vista de Configuración cargada exitosamente");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("❌ Error al cargar Configuracion.fxml: " + e.getMessage());
            mostrarError("Error al cargar la configuración: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error inesperado: " + e.getMessage());
            mostrarError("Error inesperado: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogoutClick() {
        System.out.println("Cerrando sesión...");
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/grupo/proyecto_aula_carpethome/Login.fxml")
            );
            Parent root = loader.load();

            Stage stage = (Stage) contentArea.getScene().getWindow();
            Scene scene = new Scene(root);
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

        String hoverStyle = button.getStyle() + "-fx-background-color: rgba(184, 175, 160, 0.5);";
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
            mostrarError("No se pudo cargar la vista: " + fxmlFile);
        }
    }

    private void mostrarError(String mensaje) {
        VBox errorView = new VBox(20);
        errorView.setAlignment(Pos.CENTER);
        errorView.setStyle("-fx-padding: 60; -fx-background-color: #E4DFD7;");

        Label lblError = new Label("⚠️ Error");
        lblError.setStyle("-fx-font-size: 48px;");

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