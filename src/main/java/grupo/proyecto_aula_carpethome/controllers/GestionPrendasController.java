package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.entities.Etapa;
import grupo.proyecto_aula_carpethome.entities.HistEtapa;
import grupo.proyecto_aula_carpethome.entities.Prenda;
import grupo.proyecto_aula_carpethome.entities.Proyecto;
import grupo.proyecto_aula_carpethome.services.EtapaService;
import grupo.proyecto_aula_carpethome.services.HistEtapaService;
import grupo.proyecto_aula_carpethome.services.PrendaService;
import grupo.proyecto_aula_carpethome.services.ServiceFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class GestionPrendasController {

    @FXML private Label lblProyecto;
    @FXML private VBox contenedorPrendas;
    @FXML private VBox placeholderPrendas;
    @FXML private Button btnAgregarPrenda;
    @FXML private Button btnCerrar;

    private PrendaService prendaService;
    private Proyecto proyectoActual;
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    @FXML
    public void initialize() {
        System.out.println("GestionPrendasController inicializado");
        prendaService = ServiceFactory.getPrendaService();
    }

    public void cargarProyecto(Proyecto proyecto) {
        this.proyectoActual = proyecto;
        lblProyecto.setText("Proyecto: " + proyecto.getIdProyecto() + " - " + proyecto.getNombreProyecto());
        cargarPrendas();
    }

    public void cargarPrendas() {
        try {
            List<Prenda> prendas = prendaService.listarTodos().stream()
                    .filter(p -> p.getIdProyecto().equals(proyectoActual.getIdProyecto()))
                    .toList();

            contenedorPrendas.getChildren().clear();

            if (prendas.isEmpty()) {
                contenedorPrendas.getChildren().add(placeholderPrendas);
            } else {
                prendas.forEach(prenda -> {
                    VBox card = crearTarjetaPrenda(prenda);
                    contenedorPrendas.getChildren().add(card);
                });
            }

            System.out.println("Prendas cargadas: " + prendas.size());

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al cargar prendas", e.getMessage());
        }
    }

    /**
     * Abre el modal para cambiar la etapa de una prenda
     */
    private void cambiarEtapaPrenda(Prenda prenda) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/grupo/proyecto_aula_carpethome/CambiarEtapa.fxml")
            );
            Parent root = loader.load();

            CambiarEtapaController controller = loader.getController();
            controller.setParentController(this);
            controller.cargarPrenda(prenda);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initStyle(StageStyle.DECORATED);
            modalStage.setTitle("Cambiar Etapa - " + prenda.getNombrePrenda());

            Scene scene = new Scene(root);
            modalStage.setScene(scene);

            modalStage.setMinWidth(600);
            modalStage.setMinHeight(600);
            modalStage.setResizable(false);

            modalStage.centerOnScreen();
            modalStage.showAndWait();

            cargarPrendas();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudo abrir el formulario para cambiar etapa: " + e.getMessage());
        }
    }

    /**
     * ✅ NUEVO: Abre el modal para ver el historial de etapas de una prenda
     */
    private void verHistorialPrenda(Prenda prenda) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/grupo/proyecto_aula_carpethome/HistorialEtapas.fxml")
            );
            Parent root = loader.load();

            HistorialEtapasController controller = loader.getController();
            controller.cargarPrenda(prenda);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initStyle(StageStyle.DECORATED);
            modalStage.setTitle("Historial de Etapas - " + prenda.getNombrePrenda());

            Scene scene = new Scene(root);
            modalStage.setScene(scene);

            modalStage.setMinWidth(750);
            modalStage.setMinHeight(650);
            modalStage.setResizable(true);

            modalStage.centerOnScreen();
            modalStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudo abrir el historial de etapas: " + e.getMessage());
        }
    }

    private VBox crearTarjetaPrenda(Prenda prenda) {
        VBox card = new VBox(12);
        card.setStyle(
                "-fx-background-color: #F5F5F5;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.08), 8, 0, 0, 2);"
        );

        // Header con nombre y acciones
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblNombre = new Label(prenda.getNombrePrenda());
        lblNombre.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-text-fill: #181716;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;"
        );
        HBox.setHgrow(lblNombre, Priority.ALWAYS);

        // Botones de acción
        HBox acciones = new HBox(8);
        acciones.setAlignment(Pos.CENTER_RIGHT);

        Button btnHistorial = crearBotonAccion("📜", () -> verHistorialPrenda(prenda));
        btnHistorial.setTooltip(new Tooltip("Ver Historial de Etapas"));

        Button btnCambiarEtapa = crearBotonAccion("📋", () -> cambiarEtapaPrenda(prenda));
        btnCambiarEtapa.setTooltip(new Tooltip("Cambiar Etapa"));

        Button btnEditar = crearBotonAccion("✏️", () -> editarPrenda(prenda));
        btnEditar.setTooltip(new Tooltip("Editar Prenda"));

        Button btnEliminar = crearBotonAccion("🗑️", () -> eliminarPrenda(prenda));
        btnEliminar.setTooltip(new Tooltip("Eliminar Prenda"));

        acciones.getChildren().addAll(btnHistorial, btnCambiarEtapa, btnEditar, btnEliminar);
        header.getChildren().addAll(lblNombre, acciones);

        // Descripción
        Label lblDescripcion = new Label(prenda.getDescripcionPrenda());
        lblDescripcion.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: #61564A;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;" +
                        "-fx-wrap-text: true;"
        );
        lblDescripcion.setWrapText(true);

        // Separador
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #E4DFD7;");

        // Información de costos
        HBox costos = new HBox(30);
        costos.setAlignment(Pos.CENTER_LEFT);

        VBox costoMateriales = crearInfoCosto("Costo Materiales", prenda.getCostoMateriales());
        VBox costoTotal = crearInfoCosto("Costo Total Est.", prenda.getCostoTotalEstimado());

        costos.getChildren().addAll(costoMateriales, costoTotal);

        card.getChildren().addAll(header, lblDescripcion, separator, costos);

        // Cargar y mostrar etapa actual
        try {
            HistEtapaService histEtapaService = ServiceFactory.getHistEtapaService();
            Optional<HistEtapa> etapaActualOpt = histEtapaService.obtenerEtapaActual(prenda.getIdPrenda());

            if (etapaActualOpt.isPresent()) {
                EtapaService etapaService = ServiceFactory.getEtapaService();
                Optional<Etapa> etapaOpt = etapaService.buscarPorId(etapaActualOpt.get().getIdEtapa());

                if (etapaOpt.isPresent()) {
                    Label badgeEtapa = new Label("📍 " + etapaOpt.get().getNombreEtapa());
                    badgeEtapa.setStyle(
                            "-fx-background-color: #BBDEFB;" +
                                    "-fx-text-fill: #1565C0;" +
                                    "-fx-font-size: 11px;" +
                                    "-fx-font-weight: 700;" +
                                    "-fx-padding: 4 10;" +
                                    "-fx-background-radius: 8;"
                    );
                    header.getChildren().add(1, badgeEtapa);
                }
            } else {
                Label sinEtapa = new Label("Sin etapa");
                sinEtapa.setStyle(
                        "-fx-background-color: #FFE082;" +
                                "-fx-text-fill: #F57F17;" +
                                "-fx-font-size: 11px;" +
                                "-fx-font-weight: 700;" +
                                "-fx-padding: 4 10;" +
                                "-fx-background-radius: 8;"
                );
                header.getChildren().add(1, sinEtapa);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return card;
    }

    private VBox crearInfoCosto(String titulo, double monto) {
        VBox container = new VBox(5);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle(
                "-fx-font-size: 11px;" +
                        "-fx-text-fill: #A59B8F;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;"
        );

        Label lblMonto = new Label(currencyFormat.format(monto));
        lblMonto.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: 600;" +
                        "-fx-text-fill: #181716;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;"
        );

        container.getChildren().addAll(lblTitulo, lblMonto);
        return container;
    }

    private Button crearBotonAccion(String icono, Runnable accion) {
        Button btn = new Button(icono);
        btn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 16px;" +
                        "-fx-padding: 5;"
        );
        btn.setOnAction(e -> accion.run());

        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle() + "-fx-background-color: rgba(165, 155, 143, 0.2);"));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace("-fx-background-color: rgba(165, 155, 143, 0.2);", "")));

        return btn;
    }

    @FXML
    private void handleAgregarPrenda() {
        System.out.println("Abriendo modal para agregar prenda");
        abrirModalPrenda(null);
    }

    private void editarPrenda(Prenda prenda) {
        System.out.println("Editar prenda: " + prenda.getIdPrenda());
        abrirModalPrenda(prenda);
    }

    private void abrirModalPrenda(Prenda prenda) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/grupo/proyecto_aula_carpethome/FormularioPrenda.fxml"));
            Parent root = loader.load();

            FormularioPrendaController controller = loader.getController();
            controller.setParentController(this);
            controller.setProyecto(proyectoActual);

            if (prenda != null) {
                controller.cargarPrenda(prenda);
            }

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initStyle(StageStyle.TRANSPARENT);
            modalStage.setTitle(prenda == null ? "Nueva Prenda" : "Editar Prenda");

            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
            overlay.getChildren().add(root);
            overlay.setPadding(new Insets(40));

            Scene scene = new Scene(overlay);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            modalStage.setScene(scene);

            modalStage.setMaxWidth(900);
            modalStage.setMaxHeight(800);
            modalStage.setResizable(false);

            modalStage.centerOnScreen();
            modalStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudo abrir el formulario de prenda");
        }
    }

    private void eliminarPrenda(Prenda prenda) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Estás seguro de eliminar esta prenda?");
        alert.setContentText(prenda.getNombrePrenda());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    prendaService.eliminarPrenda(prenda.getIdPrenda());
                    cargarPrendas();
                    mostrarExito("Prenda eliminada correctamente");
                } catch (SQLException e) {
                    e.printStackTrace();
                    mostrarError("Error al eliminar", e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleCerrar() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleAgregarHover(MouseEvent event) {
        btnAgregarPrenda.setStyle(btnAgregarPrenda.getStyle() + "-fx-background-color: #4a433e;");
    }

    @FXML
    private void handleAgregarExit(MouseEvent event) {
        btnAgregarPrenda.setStyle(btnAgregarPrenda.getStyle().replace("-fx-background-color: #4a433e;", "-fx-background-color: #61564A;"));
    }

    @FXML
    private void handleCerrarHover(MouseEvent event) {
        ((Button)event.getSource()).setStyle(((Button)event.getSource()).getStyle() + "-fx-background-color: rgba(165, 155, 143, 0.1);");
    }

    @FXML
    private void handleCerrarExit(MouseEvent event) {
        ((Button)event.getSource()).setStyle(((Button)event.getSource()).getStyle().replace("-fx-background-color: rgba(165, 155, 143, 0.1);", "-fx-background-color: transparent;"));
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.show();
    }
}