package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.HelloApplication;
import grupo.proyecto_aula_carpethome.services.ServiceFactory;
import grupo.proyecto_aula_carpethome.entities.UsuarioLogueado;
import grupo.proyecto_aula_carpethome.services.AdministradorService;
import grupo.proyecto_aula_carpethome.services.EmpleadoService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;


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
import javafx.scene.control.Control;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.animation.*;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    // Referencias a los elementos del FXML
    @FXML private VBox mainContainer;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button togglePasswordBtn;
    @FXML private SVGPath eyeIcon;
    @FXML private SVGPath lockIcon;
    @FXML private SVGPath threadDecoration1;
    @FXML private SVGPath threadDecoration2;
    @FXML private Label errorLabel;
    @FXML private HBox errorContainer;

    // Estado de visibilidad de contraseña
    private boolean isPasswordVisible = false;

    // Íconos SVG para el toggle de password
    private static final String EYE_VISIBLE = "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z";
    private static final String EYE_HIDDEN = "M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zM2 4.27l2.28 2.28.46.46A11.804 11.804 0 0 0 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 4.27zM7.53 9.8l1.55 1.55c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3 .22 0 .44-.03.65-.08l1.55 1.55c-.67.33-1.41.53-2.2.53-2.76 0-5-2.24-5-5 0-.79.2-1.53.53-2.2zm4.31-.78 3.15 3.15.02-.16c0-1.66-1.34-3-3-3l-.17.01z";

    @FXML
    public void initialize() {
        // Animación de entrada inicial
        playEntranceAnimation();

        // Animación de hilos decorativos
        animateThreadDecorations();

        // Configurar efectos hover en campos
        setupInputEffects();

        // Configurar efectos hover en botones
        setupButtonHoverEffects();

        // Configurar toggle de password
        setupPasswordToggle();

        // Focus inicial en username
        usernameField.requestFocus();
    }

    // ============================================
    // MÉTODO PRINCIPAL DE LOGIN
    // ============================================

    @FXML
    private void clickAcceder() {
        hideError();

        // 1. Capturar los datos
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // 2. Validar que no estén vacíos
        if (username.isEmpty() || password.isEmpty()) {
            showError("Por favor, completa todos los campos");
            return;
        }

        // Mostrar loading
        showLoadingButton();

        // 3. Intentar validar como Administrador
        try {
            AdministradorService adminService = ServiceFactory.getAdministradorService();
            var administrador = adminService.validarCredenciales(username, password);

            if (administrador != null && administrador.isPresent()) {
                // Es un administrador válido
                UsuarioLogueado usuario = new UsuarioLogueado(administrador.get().getIdAdmin(),
                        administrador.get().getNombreCompleto(),
                        "ADMINISTRADOR",
                        administrador.get().getCedula()
                );
                try {
                    navigateToMainView(usuario);
                } catch (IOException e) {
                    e.printStackTrace();
                    hideLoadingButton();
                    showError("Error al cargar el menú");
                }
                return;
            }

            // 4. Si no es admin, intentar validar como Empleado
            EmpleadoService empleadoService = ServiceFactory.getEmpleadoService();
            var empleado = empleadoService.validarCredenciales(username, password);

            if (empleado != null && empleado.isPresent()) {
                // Es un empleado válido
                UsuarioLogueado usuario = new UsuarioLogueado(
                        empleado.get().getIdEmpleado(),
                        empleado.get().getNombreCompleto(),
                        "EMPLEADO",
                        empleado.get().getCedula()
                );
                try {
                    navigateToMainView(usuario);
                } catch (IOException e) {
                    e.printStackTrace();
                    hideLoadingButton();
                    showError("Error al cargar el menú");
                }
                return;
            }

            // 5. Si no es ni admin ni empleado, mostrar error
            hideLoadingButton();
            showError("Usuario o contraseña incorrectos");
            passwordField.clear();
            passwordField.requestFocus();

        } catch (SQLException e) {
            e.printStackTrace();
            hideLoadingButton();
            showError("Error de conexión con la base de datos");
        }
    }

    // ============================================
    // ANIMACIONES
    // ============================================

    /**
     * Animación de entrada del contenedor principal
     */
    private void playEntranceAnimation() {
        if (mainContainer == null) return;

        mainContainer.setOpacity(0);
        mainContainer.setTranslateY(30);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), mainContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideUp = new TranslateTransition(Duration.millis(600), mainContainer);
        slideUp.setFromY(30);
        slideUp.setToY(0);

        ParallelTransition entrance = new ParallelTransition(fadeIn, slideUp);
        entrance.setInterpolator(Interpolator.EASE_OUT);
        entrance.play();
    }

    /**
     * Animación de hilos decorativos
     */
    private void animateThreadDecorations() {
        if (threadDecoration1 != null) {
            Timeline wave1 = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(threadDecoration1.translateYProperty(), 0)),
                    new KeyFrame(Duration.seconds(3), new KeyValue(threadDecoration1.translateYProperty(), -10)),
                    new KeyFrame(Duration.seconds(6), new KeyValue(threadDecoration1.translateYProperty(), 0))
            );
            wave1.setCycleCount(Timeline.INDEFINITE);
            wave1.play();
        }

        if (threadDecoration2 != null) {
            Timeline wave2 = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(threadDecoration2.translateYProperty(), 0)),
                    new KeyFrame(Duration.seconds(4), new KeyValue(threadDecoration2.translateYProperty(), 10)),
                    new KeyFrame(Duration.seconds(8), new KeyValue(threadDecoration2.translateYProperty(), 0))
            );
            wave2.setCycleCount(Timeline.INDEFINITE);
            wave2.play();
        }
    }

    /**
     * Configurar efectos visuales en los campos de entrada
     */
    private void setupInputEffects() {
        // Efecto de brillo al hacer focus
        addFocusGlowEffect(usernameField);
        addFocusGlowEffect(passwordField);

        // Animación del ícono de candado
        if (lockIcon != null) {
            RotateTransition rotate = new RotateTransition(Duration.millis(500), lockIcon);
            rotate.setByAngle(10);
            rotate.setCycleCount(4);
            rotate.setAutoReverse(true);
            rotate.setDelay(Duration.millis(300));
            rotate.play();
        }
    }

    /**
     * Configurar efectos hover en botones
     */
    private void setupButtonHoverEffects() {
        // Hover effect en botón de login
        if (loginButton != null) {
            loginButton.setOnMouseEntered(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(200), loginButton);
                scale.setToX(1.02);
                scale.setToY(1.02);
                scale.play();

                loginButton.setStyle(
                        "-fx-background-color: linear-gradient(to bottom, #61564A, #4a433e);" +
                                "-fx-background-radius: 12;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 16 0;" +
                                "-fx-min-height: 55px;" +
                                "-fx-effect: dropshadow(gaussian, rgba(24, 23, 22, 0.5), 20, 0, 0, 7);"
                );
            });

            loginButton.setOnMouseExited(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(200), loginButton);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.play();

                loginButton.setStyle(
                        "-fx-background-color: linear-gradient(to bottom, #181716, #2a2827);" +
                                "-fx-background-radius: 12;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 16 0;" +
                                "-fx-min-height: 55px;" +
                                "-fx-effect: dropshadow(gaussian, rgba(24, 23, 22, 0.4), 15, 0, 0, 5);"
                );
            });

            loginButton.setOnMousePressed(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(100), loginButton);
                scale.setToX(0.98);
                scale.setToY(0.98);
                scale.play();
            });

            loginButton.setOnMouseReleased(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(100), loginButton);
                scale.setToX(1.02);
                scale.setToY(1.02);
                scale.play();
            });
        }

        // Hover effect en botón toggle password
        if (togglePasswordBtn != null) {
            String normalStyle =
                    "-fx-background-color: transparent;" +
                            "-fx-border-width: 0;" +
                            "-fx-background-radius: 0 12 12 0;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 16 15 16 10;" +
                            "-fx-min-height: 55px;" +
                            "-fx-min-width: 50;";

            String hoverStyle =
                    "-fx-background-color: rgba(165, 155, 143, 0.15);" +
                            "-fx-border-width: 0;" +
                            "-fx-background-radius: 0 12 12 0;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 16 15 16 10;" +
                            "-fx-min-height: 55px;" +
                            "-fx-min-width: 50;";

            String pressedStyle =
                    "-fx-background-color: rgba(165, 155, 143, 0.25);" +
                            "-fx-border-width: 0;" +
                            "-fx-background-radius: 0 12 12 0;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 16 15 16 10;" +
                            "-fx-min-height: 55px;" +
                            "-fx-min-width: 50;";

            togglePasswordBtn.setOnMouseEntered(e -> togglePasswordBtn.setStyle(hoverStyle));
            togglePasswordBtn.setOnMouseExited(e -> togglePasswordBtn.setStyle(normalStyle));
            togglePasswordBtn.setOnMousePressed(e -> togglePasswordBtn.setStyle(pressedStyle));
            togglePasswordBtn.setOnMouseReleased(e -> togglePasswordBtn.setStyle(hoverStyle));
        }
    }

    /**
     * Añade efecto de brillo al hacer focus en un campo
     */
    private void addFocusGlowEffect(Control field) {
        DropShadow glow = new DropShadow();
        glow.setColor(Color.rgb(97, 86, 74, 0.4));
        glow.setRadius(15);
        glow.setSpread(0.3);

        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // Animación de glow al recibir focus
                Timeline glowAnimation = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(glow.radiusProperty(), 0)),
                        new KeyFrame(Duration.millis(300), new KeyValue(glow.radiusProperty(), 15))
                );
                field.setEffect(glow);
                glowAnimation.play();
            } else {
                field.setEffect(null);
            }
        });
    }

    /**
     * Animación de shake para errores
     */
    private void shakeAnimation(VBox container) {
        if (container == null) return;

        TranslateTransition shake = new TranslateTransition(Duration.millis(50), container);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }

    // ============================================
    // TOGGLE PASSWORD
    // ============================================

    /**
     * Configurar funcionalidad de toggle de password
     */
    private void setupPasswordToggle() {
        if (togglePasswordBtn != null) {
            togglePasswordBtn.setOnAction(e -> togglePasswordVisibility());
        }
    }

    /**
     * Alternar visibilidad de la contraseña
     */
    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;

        // Cambiar ícono
        if (eyeIcon != null) {
            eyeIcon.setContent(isPasswordVisible ? EYE_HIDDEN : EYE_VISIBLE);
        }

        // Animación del botón
        if (togglePasswordBtn != null) {
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), togglePasswordBtn);
            scale.setToX(0.9);
            scale.setToY(0.9);
            scale.setCycleCount(2);
            scale.setAutoReverse(true);
            scale.play();
        }

        // Nota: Para mostrar/ocultar realmente la contraseña necesitarías
        // alternar entre PasswordField y TextField dinámicamente
    }

    // ============================================
    // MANEJO DE ERRORES
    // ============================================

    /**
     * Mostrar mensaje de error con animación
     */
    private void showError(String message) {
        if (errorLabel == null || errorContainer == null) return;

        errorLabel.setText(message);
        errorContainer.setManaged(true);
        errorContainer.setVisible(true);
        errorContainer.setOpacity(0);
        errorContainer.setTranslateY(-10);

        // Animación de entrada del error
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), errorContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideDown = new TranslateTransition(Duration.millis(300), errorContainer);
        slideDown.setFromY(-10);
        slideDown.setToY(0);

        ParallelTransition errorAnimation = new ParallelTransition(fadeIn, slideDown);
        errorAnimation.play();

        // Shake animation en el contenedor
        shakeAnimation(mainContainer);
    }

    /**
     * Ocultar mensaje de error
     */
    private void hideError() {
        if (errorContainer == null || !errorContainer.isVisible()) return;

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), errorContainer);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            errorContainer.setManaged(false);
            errorContainer.setVisible(false);
        });
        fadeOut.play();
    }

    // ============================================
    // LOADING STATE
    // ============================================

    /**
     * Mostrar estado de carga en el botón
     */
    private void showLoadingButton() {
        if (loginButton == null) return;

        loginButton.setDisable(true);

        // Cambiar texto del Label dentro del HBox
        if (loginButton.getGraphic() instanceof HBox) {
            HBox graphic = (HBox) loginButton.getGraphic();
            if (graphic.getChildren().get(0) instanceof Label) {
                Label label = (Label) graphic.getChildren().get(0);
                label.setText("CARGANDO...");
            }
        }

        // Animación de pulsación
        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(loginButton.scaleXProperty(), 1),
                        new KeyValue(loginButton.scaleYProperty(), 1)),
                new KeyFrame(Duration.millis(500),
                        new KeyValue(loginButton.scaleXProperty(), 0.98),
                        new KeyValue(loginButton.scaleYProperty(), 0.98)),
                new KeyFrame(Duration.millis(1000),
                        new KeyValue(loginButton.scaleXProperty(), 1),
                        new KeyValue(loginButton.scaleYProperty(), 1))
        );
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.play();
        loginButton.setUserData(pulse);
    }

    /**
     * Ocultar estado de carga del botón
     */
    private void hideLoadingButton() {
        if (loginButton == null) return;

        loginButton.setDisable(false);

        // Restaurar texto
        if (loginButton.getGraphic() instanceof HBox) {
            HBox graphic = (HBox) loginButton.getGraphic();
            if (graphic.getChildren().get(0) instanceof Label) {
                Label label = (Label) graphic.getChildren().get(0);
                label.setText("ACCEDER");
            }
        }

        // Detener animación
        if (loginButton.getUserData() instanceof Timeline) {
            ((Timeline) loginButton.getUserData()).stop();
        }

        loginButton.setScaleX(1);
        loginButton.setScaleY(1);
    }

    // ============================================
    // NAVEGACIÓN
    // ============================================

    /**
     * Navegar a la vista principal después del login exitoso
     */
    private void navigateToMainView(UsuarioLogueado usuario) throws IOException {
        // Animación de salida antes de cambiar de vista
        if (mainContainer != null) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(400), mainContainer);
            fadeOut.setToValue(0);

            ScaleTransition shrink = new ScaleTransition(Duration.millis(400), mainContainer);
            shrink.setToX(0.9);
            shrink.setToY(0.9);

            ParallelTransition exit = new ParallelTransition(fadeOut, shrink);
            exit.setOnFinished(e -> {
                try {
                    HelloApplication.loadMenu(usuario);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            exit.play();
        } else {
            // Si no hay animación, cargar directamente
            HelloApplication.loadMenu(usuario);
        }
    }
}