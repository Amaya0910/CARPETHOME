package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.entities.Etapa;
import grupo.proyecto_aula_carpethome.services.EtapaService;
import grupo.proyecto_aula_carpethome.services.ServiceFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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

public class GestionEtapasController {

    @FXML private GridPane etapasGrid;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField searchField;
    @FXML private Button btnAgregarEtapa;
    @FXML private Label lblContador;
    @FXML private VBox emptyStateContainer;

    private EtapaService etapaService;
    private ObservableList<Etapa> etapasList;
    private FilteredList<Etapa> filteredData;

    @FXML
    public void initialize() {
        etapaService = ServiceFactory.getEtapaService();
        configurarBusqueda();
        cargarEtapas();
    }

    private void configurarBusqueda() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (filteredData != null) {
                filteredData.setPredicate(etapa -> {
                    if (newVal == null || newVal.isEmpty()) return true;
                    String search = newVal.toLowerCase();
                    return etapa.getNombreEtapa().toLowerCase().contains(search) ||
                            etapa.getDescripcionEtapa().toLowerCase().contains(search);
                });
                actualizarVista();
            }
        });
    }

    public void cargarEtapas() {
        try {
            etapasList = FXCollections.observableArrayList(etapaService.listarTodas());
            filteredData = new FilteredList<>(etapasList, p -> true);
            actualizarVista();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al cargar etapas", e.getMessage());
        }
    }

    private void actualizarVista() {
        etapasGrid.getChildren().clear();

        if (filteredData.isEmpty()) {
            scrollPane.setVisible(false);
            scrollPane.setManaged(false);
            emptyStateContainer.setVisible(true);
            emptyStateContainer.setManaged(true);
            lblContador.setText("0 etapas");
            return;
        }

        scrollPane.setVisible(true);
        scrollPane.setManaged(true);
        emptyStateContainer.setVisible(false);
        emptyStateContainer.setManaged(false);

        int col = 0, row = 0;
        for (Etapa etapa : filteredData) {
            VBox tarjeta = crearTarjetaEtapa(etapa);
            etapasGrid.add(tarjeta, col, row);

            col++;
            if (col >= 3) {  // 3 columnas
                col = 0;
                row++;
            }
        }

        lblContador.setText(filteredData.size() + (filteredData.size() == 1 ? " etapa" : " etapas"));
    }

    private VBox crearTarjetaEtapa(Etapa etapa) {
        VBox tarjeta = new VBox(15);
        tarjeta.setPrefWidth(280);
        tarjeta.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 24;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.08), 10, 0, 0, 3);"
        );

        // Efectos hover
        tarjeta.setOnMouseEntered(e -> {
            tarjeta.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 16;" +
                            "-fx-padding: 24;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.15), 15, 0, 0, 5);" +
                            "-fx-translate-y: -2;"
            );
        });

        tarjeta.setOnMouseExited(e -> {
            tarjeta.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 16;" +
                            "-fx-padding: 24;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.08), 10, 0, 0, 3);"
            );
        });

        // ID Badge
        Label idBadge = new Label(etapa.getIdEtapa());
        idBadge.setStyle(
                "-fx-background-color: #F5F5F5;" +
                        "-fx-text-fill: #61564A;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-padding: 6 12;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;"
        );

        // Nombre
        Label nombre = new Label(etapa.getNombreEtapa());
        nombre.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-text-fill: #181716;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;"
        );
        nombre.setWrapText(true);

        // Descripción
        Label descripcion = new Label(etapa.getDescripcionEtapa());
        descripcion.setWrapText(true);
        descripcion.setMaxHeight(60);
        descripcion.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: #61564A;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;"
        );

        // Separator
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #E0E0E0;");

        // Botones
        HBox acciones = new HBox(8);
        acciones.setAlignment(Pos.CENTER);

        Button btnEditar = new Button("✏️ Editar");
        btnEditar.setStyle(
                "-fx-background-color: #E4DFD7;" +
                        "-fx-text-fill: #61564A;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: 600;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10 18;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;"
        );
        btnEditar.setOnAction(e -> editarEtapa(etapa));

        Button btnEliminar = new Button("🗑️");
        btnEliminar.setStyle(
                "-fx-background-color: #FFE5E5;" +
                        "-fx-text-fill: #D32F2F;" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10 14;" +
                        "-fx-cursor: hand;"
        );
        btnEliminar.setOnAction(e -> eliminarEtapa(etapa));

        acciones.getChildren().addAll(btnEditar, btnEliminar);

        tarjeta.getChildren().addAll(idBadge, nombre, descripcion, sep, acciones);

        return tarjeta;
    }

    @FXML
    private void handleAgregarEtapa() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/grupo/proyecto_aula_carpethome/CrearEtapa.fxml")
            );
            Parent root = loader.load();

            CrearEtapaController controller = loader.getController();
            controller.setGestionController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudo abrir el formulario de creación.");
        }
    }

    private void editarEtapa(Etapa etapa) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/grupo/proyecto_aula_carpethome/EditarEtapa.fxml")
            );
            Parent root = loader.load();

            EditarEtapaController controller = loader.getController();
            controller.setGestionController(this);
            controller.setEtapa(etapa);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudo abrir el formulario de edición.");
        }
    }

    private void eliminarEtapa(Etapa etapa) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Eliminar esta etapa?");
        alert.setContentText("Etapa: " + etapa.getNombreEtapa() + "\n\n" +
                "⚠️ Esta acción no se puede deshacer y podría afectar a las prendas asociadas.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    etapaService.eliminarEtapa(etapa.getIdEtapa());
                    mostrarExito("Etapa eliminada exitosamente");
                    cargarEtapas();
                } catch (SQLException e) {
                    e.printStackTrace();
                    mostrarError("Error al eliminar",
                            "No se pudo eliminar la etapa. Puede que esté siendo utilizada en prendas activas.");
                }
            }
        });
    }

    @FXML
    private void handleAgregarHover(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle(btn.getStyle().replace("#61564A", "#4a433e"));
    }

    @FXML
    private void handleAgregarExit(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle(btn.getStyle().replace("#4a433e", "#61564A"));
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
        alert.showAndWait();
    }
}