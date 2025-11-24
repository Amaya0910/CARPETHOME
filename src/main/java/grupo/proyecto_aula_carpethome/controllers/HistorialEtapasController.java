package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.Utilidades.ImagenUtil;
import grupo.proyecto_aula_carpethome.entities.Empleado;
import grupo.proyecto_aula_carpethome.entities.Etapa;
import grupo.proyecto_aula_carpethome.entities.HistEtapa;
import grupo.proyecto_aula_carpethome.entities.Prenda;
import grupo.proyecto_aula_carpethome.services.EmpleadoService;
import grupo.proyecto_aula_carpethome.services.EtapaService;
import grupo.proyecto_aula_carpethome.services.HistEtapaService;
import grupo.proyecto_aula_carpethome.services.ServiceFactory;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

public class HistorialEtapasController {

    @FXML private Label lblPrenda;
    @FXML private VBox timelineContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private Button btnCerrar;
    @FXML private VBox lblSinHistorial; // ✅ Cambiado de Label a VBox

    private Prenda prendaActual;
    private HistEtapaService histEtapaService = ServiceFactory.getHistEtapaService();
    private EtapaService etapaService = ServiceFactory.getEtapaService();
    private EmpleadoService empleadoService = ServiceFactory.getEmpleadoService();

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @FXML
    public void initialize() {
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        timelineContainer.setSpacing(0);
    }

    public void cargarPrenda(Prenda prenda) {
        this.prendaActual = prenda;
        lblPrenda.setText("Historial de: " + prenda.getNombrePrenda());
        cargarHistorial();
    }

    private void cargarHistorial() {
        try {
            List<HistEtapa> historial = histEtapaService.obtenerHistorialCompleto(prendaActual.getIdPrenda());

            timelineContainer.getChildren().clear();

            if (historial.isEmpty()) {
                lblSinHistorial.setVisible(true);
                lblSinHistorial.setManaged(true);
                return;
            }

            lblSinHistorial.setVisible(false);
            lblSinHistorial.setManaged(false);

            // Crear tarjetas de timeline en orden inverso (más reciente primero)
            for (int i = historial.size() - 1; i >= 0; i--) {
                HistEtapa histEtapa = historial.get(i);
                boolean esActivo = histEtapa.getFechaFinal() == null;
                boolean esUltimo = (i == 0);

                VBox tarjeta = crearTarjetaTimeline(histEtapa, esActivo, esUltimo);
                timelineContainer.getChildren().add(tarjeta);
            }

            System.out.println("✓ Historial cargado: " + historial.size() + " etapas");

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al cargar historial", e.getMessage());
        }
    }

    private VBox crearTarjetaTimeline(HistEtapa histEtapa, boolean esActivo, boolean esUltimo) {
        VBox container = new VBox();
        container.setSpacing(0);

        // Contenedor principal horizontal (línea de tiempo + tarjeta)
        HBox mainHBox = new HBox(15);
        mainHBox.setAlignment(Pos.TOP_LEFT);

        // ===== COLUMNA IZQUIERDA: Línea de Tiempo =====
        VBox timelineColumn = new VBox();
        timelineColumn.setAlignment(Pos.TOP_CENTER);
        timelineColumn.setPrefWidth(40);
        timelineColumn.setMinWidth(40);
        timelineColumn.setMaxWidth(40);

        // Círculo indicador
        Circle circle = new Circle(12);
        if (esActivo) {
            circle.setFill(Color.web("#4CAF50")); // Verde para activo
            circle.setStroke(Color.web("#2E7D32"));
        } else {
            circle.setFill(Color.web("#9E9E9E")); // Gris para completado
            circle.setStroke(Color.web("#757575"));
        }
        circle.setStrokeWidth(3);

        timelineColumn.getChildren().add(circle);

        // Línea vertical conectora (si no es el último)
        if (!esUltimo) {
            Line linea = new Line(0, 0, 0, 120);
            linea.setStroke(Color.web("#E0E0E0"));
            linea.setStrokeWidth(2);
            VBox.setMargin(linea, new Insets(5, 0, 0, 0));
            timelineColumn.getChildren().add(linea);
        }

        // ===== COLUMNA DERECHA: Contenido de la Tarjeta =====
        VBox cardContent = crearContenidoTarjeta(histEtapa, esActivo);
        HBox.setHgrow(cardContent, Priority.ALWAYS);

        mainHBox.getChildren().addAll(timelineColumn, cardContent);
        container.getChildren().add(mainHBox);

        return container;
    }

    private VBox crearContenidoTarjeta(HistEtapa histEtapa, boolean esActivo) {
        VBox card = new VBox(12);
        card.setStyle(
                "-fx-background-color: " + (esActivo ? "#E8F5E9" : "#F5F5F5") + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 8, 0, 0, 2);"
        );
        VBox.setMargin(card, new Insets(0, 0, 15, 0));

        try {
            // Obtener información de la etapa
            Optional<Etapa> etapaOpt = etapaService.buscarPorId(histEtapa.getIdEtapa());
            String nombreEtapa = etapaOpt.map(Etapa::getNombreEtapa).orElse("Etapa Desconocida");
            String descripcionEtapa = etapaOpt.map(Etapa::getDescripcionEtapa).orElse("");

            // ===== HEADER =====
            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);

            Label lblNombreEtapa = new Label(nombreEtapa);
            lblNombreEtapa.setStyle(
                    "-fx-font-family: 'Poppins', 'Segoe UI', Arial; " +
                            "-fx-font-size: 18px; " +
                            "-fx-font-weight: 700; " +
                            "-fx-text-fill: #181716;"
            );
            HBox.setHgrow(lblNombreEtapa, Priority.ALWAYS);

            // Badge de estado
            Label badgeEstado = new Label(esActivo ? "● EN PROCESO" : "✓ COMPLETADO");
            badgeEstado.setStyle(
                    "-fx-background-color: " + (esActivo ? "#4CAF50" : "#9E9E9E") + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 10px;" +
                            "-fx-font-weight: 700;" +
                            "-fx-padding: 4 10;" +
                            "-fx-background-radius: 8;"
            );

            header.getChildren().addAll(lblNombreEtapa, badgeEstado);

            // ===== DESCRIPCIÓN DE LA ETAPA =====
            if (!descripcionEtapa.isEmpty()) {
                Label lblDescripcion = new Label(descripcionEtapa);
                lblDescripcion.setStyle(
                        "-fx-font-size: 12px; " +
                                "-fx-text-fill: #61564A; " +
                                "-fx-wrap-text: true;"
                );
                lblDescripcion.setWrapText(true);
                card.getChildren().add(lblDescripcion);
            }

            // ===== IMAGEN (si existe) =====
            if (histEtapa.getUrlImagen() != null && !histEtapa.getUrlImagen().trim().isEmpty()) {
                VBox imageContainer = new VBox(8);
                imageContainer.setAlignment(Pos.CENTER);

                Image imagen = ImagenUtil.cargarImagen(histEtapa.getUrlImagen());
                if (imagen != null) {
                    ImageView imageView = new ImageView(imagen);
                    imageView.setFitWidth(300);
                    imageView.setFitHeight(200);
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);
                    imageView.setStyle(
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);" +
                                    "-fx-background-radius: 8;"
                    );

                    // Descripción de la imagen
                    if (histEtapa.getDescripcionImagen() != null && !histEtapa.getDescripcionImagen().trim().isEmpty()) {
                        Label lblDescImg = new Label("📷 " + histEtapa.getDescripcionImagen());
                        lblDescImg.setStyle(
                                "-fx-font-size: 11px; " +
                                        "-fx-text-fill: #757575; " +
                                        "-fx-font-style: italic; " +
                                        "-fx-wrap-text: true;"
                        );
                        lblDescImg.setWrapText(true);
                        lblDescImg.setMaxWidth(300);
                        imageContainer.getChildren().addAll(imageView, lblDescImg);
                    } else {
                        imageContainer.getChildren().add(imageView);
                    }

                    card.getChildren().add(imageContainer);
                }
            }

            // ===== SEPARADOR =====
            Separator separator = new Separator();
            separator.setStyle("-fx-background-color: " + (esActivo ? "#C8E6C9" : "#E0E0E0") + ";");

            // ===== INFORMACIÓN DE FECHAS Y EMPLEADO =====
            VBox infoBox = new VBox(8);

            // Fechas
            String textoFechas = esActivo
                    ? "Inicio: " + dateFormat.format(histEtapa.getFechaInicio())
                    : dateFormat.format(histEtapa.getFechaInicio()) + " - " + dateFormat.format(histEtapa.getFechaFinal());

            Label lblFechas = new Label("📅 " + textoFechas);
            lblFechas.setStyle(
                    "-fx-font-size: 12px; " +
                            "-fx-font-weight: 600; " +
                            "-fx-text-fill: #181716;"
            );

            // Empleado
            String nombreEmpleado = "Desconocido";
            try {
                Optional<Empleado> empOpt = empleadoService.buscarPorId(histEtapa.getIdEmpleado());
                if (empOpt.isPresent()) {
                    Empleado emp = empOpt.get();
                    nombreEmpleado = emp.getNombreCompleto();
                }
            } catch (SQLException e) {
                System.err.println("Error al buscar empleado: " + e.getMessage());
            }

            Label lblEmpleado = new Label("👤 Empleado: " + nombreEmpleado);
            lblEmpleado.setStyle(
                    "-fx-font-size: 11px; " +
                            "-fx-text-fill: #61564A;"
            );

            infoBox.getChildren().addAll(lblFechas, lblEmpleado);

            // Observaciones
            if (histEtapa.getObservaciones() != null && !histEtapa.getObservaciones().trim().isEmpty()) {
                Label lblObsTitle = new Label("📝 Observaciones:");
                lblObsTitle.setStyle(
                        "-fx-font-size: 11px; " +
                                "-fx-font-weight: 600; " +
                                "-fx-text-fill: #61564A;"
                );

                Label lblObs = new Label(histEtapa.getObservaciones());
                lblObs.setStyle(
                        "-fx-font-size: 11px; " +
                                "-fx-text-fill: #757575; " +
                                "-fx-wrap-text: true;"
                );
                lblObs.setWrapText(true);

                infoBox.getChildren().addAll(lblObsTitle, lblObs);
            } else if (!esActivo) {
                Label lblSinObs = new Label("Sin observaciones finales");
                lblSinObs.setStyle(
                        "-fx-font-size: 11px; " +
                                "-fx-text-fill: #9E9E9E; " +
                                "-fx-font-style: italic;"
                );
                infoBox.getChildren().add(lblSinObs);
            }

            card.getChildren().addAll(header, separator, infoBox);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return card;
    }

    @FXML
    private void handleCerrar() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}