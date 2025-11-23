package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.config.DatabaseConfig;
import grupo.proyecto_aula_carpethome.config.OracleDatabaseConnection;
import grupo.proyecto_aula_carpethome.controllers.ClaudeAIService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import oracle.ucp.jdbc.ConnectionConnectionPool;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;



/**
 * Controller JavaFX para la interfaz de consultas a la IA
 * Maneja la interacción del usuario con el asistente virtual
 */
public class ChatBotController implements Initializable {

    @FXML
    private VBox chatContainer;

    @FXML
    private ScrollPane scrollChat;

    @FXML
    private TextField txtPregunta;

    @FXML
    private Button btnEnviar;

    private ClaudeAIService claudeService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar el scroll automático cuando se agreguen mensajes
        chatContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            scrollChat.setVvalue(1.0);
        });

        // Configurar Enter para enviar
        txtPregunta.setOnAction(event -> enviarPregunta(null));

        // Inicializar servicio de IA
        try {
            // Crear configuración de base de datos
            var config = DatabaseConfig.builder()
                    .host("localhost")
                    .port("1521")
                    .service("xepdb1")
                    .user("U_ADMIN_CARPET")
                    .password("ADMIN")
                    .build();

            // Crear conexión
            var dbConnection = new OracleDatabaseConnection(config);

            // Obtener Connection de JDBC
            Connection conn = dbConnection.connect();

            // Inicializar servicio de Claude AI
            claudeService = new ClaudeAIService(conn);

        } catch (SQLException e) {
            mostrarError("Error al conectar con la base de datos: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            mostrarError("Error inesperado al inicializar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Maneja el evento de enviar una pregunta
     */
    @FXML
    private void enviarPregunta(ActionEvent event) {
        String pregunta = txtPregunta.getText().trim();

        if (pregunta.isEmpty()) {
            return;
        }

        // Mostrar mensaje del usuario
        agregarMensajeUsuario(pregunta);

        // Limpiar campo de texto
        txtPregunta.clear();

        // Deshabilitar entrada mientras procesa
        deshabilitarEntrada(true);

        // Procesar en segundo plano para no bloquear la UI
        new Thread(() -> {
            try {
                String respuesta = claudeService.procesarConsulta(pregunta);

                // Actualizar UI en el hilo de JavaFX
                Platform.runLater(() -> {
                    agregarMensajeIA(respuesta);
                    deshabilitarEntrada(false);
                });

            } catch (SQLException e) {
                Platform.runLater(() -> {
                    mostrarError("Error al consultar la base de datos: " + e.getMessage());
                    deshabilitarEntrada(false);
                });
                e.printStackTrace();

            } catch (Exception e) {
                Platform.runLater(() -> {
                    mostrarError("Error inesperado: " + e.getMessage());
                    deshabilitarEntrada(false);
                });
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Maneja el clic en los botones de ejemplo
     */
    @FXML
    private void usarEjemplo(ActionEvent event) {
        Button btn = (Button) event.getSource();
        txtPregunta.setText(btn.getText());
        txtPregunta.requestFocus();
    }

    /**
     * Agrega un mensaje del usuario al chat
     */
    private void agregarMensajeUsuario(String texto) {
        HBox contenedor = new HBox();
        contenedor.setAlignment(Pos.CENTER_RIGHT);
        contenedor.setPadding(new Insets(0, 0, 0, 0));

        VBox mensajeBox = new VBox();
        mensajeBox.setMaxWidth(650);
        mensajeBox.setStyle(
                "-fx-background-color: #667eea; " +
                        "-fx-background-radius: 12; " +
                        "-fx-padding: 12 16 12 16;"
        );

        Label label = new Label(texto);
        label.setWrapText(true);
        label.setFont(Font.font(14));
        label.setStyle("-fx-text-fill: white;");

        mensajeBox.getChildren().add(label);
        contenedor.getChildren().add(mensajeBox);

        chatContainer.getChildren().add(contenedor);
    }

    /**
     * Agrega un mensaje de la IA al chat
     */
    private void agregarMensajeIA(String texto) {
        HBox contenedor = new HBox();
        contenedor.setAlignment(Pos.CENTER_LEFT);
        contenedor.setPadding(new Insets(0, 0, 0, 0));

        VBox mensajeBox = new VBox();
        mensajeBox.setMaxWidth(650);
        mensajeBox.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: #e0e0e0; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12; " +
                        "-fx-padding: 12 16 12 16;"
        );

        Label label = new Label(texto);
        label.setWrapText(true);
        label.setFont(Font.font(14));
        label.setStyle("-fx-text-fill: #333;");

        mensajeBox.getChildren().add(label);
        contenedor.getChildren().add(mensajeBox);

        chatContainer.getChildren().add(contenedor);
    }

    /**
     * Muestra un mensaje de error en el chat
     */
    private void mostrarError(String mensaje) {
        HBox contenedor = new HBox();
        contenedor.setAlignment(Pos.CENTER_LEFT);
        contenedor.setPadding(new Insets(0, 0, 0, 0));

        VBox mensajeBox = new VBox();
        mensajeBox.setMaxWidth(650);
        mensajeBox.setStyle(
                "-fx-background-color: #fee; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: #fcc; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12; " +
                        "-fx-padding: 12 16 12 16;"
        );

        Label label = new Label("❌ " + mensaje);
        label.setWrapText(true);
        label.setFont(Font.font(14));
        label.setStyle("-fx-text-fill: #c33;");

        mensajeBox.getChildren().add(label);
        contenedor.getChildren().add(mensajeBox);

        chatContainer.getChildren().add(contenedor);
    }

    /**
     * Habilita o deshabilita la entrada mientras procesa
     */
    private void deshabilitarEntrada(boolean deshabilitar) {
        txtPregunta.setDisable(deshabilitar);
        btnEnviar.setDisable(deshabilitar);

        if (deshabilitar) {
            btnEnviar.setText("Procesando...");
        } else {
            btnEnviar.setText("Preguntar");
            txtPregunta.requestFocus();
        }
    }
}
