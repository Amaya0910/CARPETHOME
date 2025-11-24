package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.entities.Medida;
import grupo.proyecto_aula_carpethome.services.MedidaService;
import grupo.proyecto_aula_carpethome.services.ServiceFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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

public class GestionMedidasEstandarController {

    @FXML private VBox medidasContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField searchField;
    @FXML private Button btnAgregarMedida;
    @FXML private Label lblContador;
    @FXML private VBox emptyStateContainer;

    private MedidaService medidaService;
    private ObservableList<Medida> medidasList;
    private FilteredList<Medida> filteredData;

    @FXML
    public void initialize() {
        medidaService = ServiceFactory.getMedidaService();
        configurarBusqueda();
        cargarMedidas();
    }

    private void configurarBusqueda() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (filteredData != null) {
                filteredData.setPredicate(medida -> {
                    if (newVal == null || newVal.isEmpty()) return true;
                    String search = newVal.toLowerCase();
                    return medida.getNombreMedida().toLowerCase().contains(search);
                });
                actualizarVista();
            }
        });
    }

    public void cargarMedidas() {
        try {
            medidasList = FXCollections.observableArrayList(medidaService.listarMedidasEstandar());
            filteredData = new FilteredList<>(medidasList, p -> true);
            actualizarVista();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al cargar medidas", e.getMessage());
        }
    }

    private void actualizarVista() {
        medidasContainer.getChildren().clear();

        if (filteredData.isEmpty()) {
            scrollPane.setVisible(false);
            scrollPane.setManaged(false);
            emptyStateContainer.setVisible(true);
            emptyStateContainer.setManaged(true);
            lblContador.setText("0 medidas");
            return;
        }

        scrollPane.setVisible(true);
        scrollPane.setManaged(true);
        emptyStateContainer.setVisible(false);
        emptyStateContainer.setManaged(false);

        for (Medida medida : filteredData) {
            VBox tarjeta = crearTarjetaMedida(medida);
            medidasContainer.getChildren().add(tarjeta);
        }

        lblContador.setText(filteredData.size() + (filteredData.size() == 1 ? " medida" : " medidas"));
    }

    private VBox crearTarjetaMedida(Medida medida) {
        VBox tarjeta = new VBox(20);
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

        // Encabezado de la tarjeta
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label idBadge = new Label(medida.getIdMedida());
        idBadge.setStyle(
                "-fx-background-color: #F5F5F5;" +
                        "-fx-text-fill: #61564A;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-padding: 6 12;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;"
        );

        Label nombre = new Label(medida.getNombreMedida());
        nombre.setStyle(
                "-fx-font-size: 24px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-text-fill: #181716;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label("ESTÁNDAR");
        badge.setStyle(
                "-fx-background-color: #E3F2FD;" +
                        "-fx-text-fill: #1565C0;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-padding: 6 12;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;"
        );

        header.getChildren().addAll(idBadge, nombre, spacer, badge);

        // Separator
        Separator sep1 = new Separator();
        sep1.setStyle("-fx-background-color: #E0E0E0;");

        // Sección de Medidas del Torso
        VBox torsoSection = crearSeccionMedidas("👕 Medidas del Torso",
                new String[]{"Contorno Busto", "Contorno Cintura", "Contorno Cadera",
                        "Altura Busto", "Separación Busto", "Radio Busto", "Bajo Busto"},
                new double[]{medida.getCBusto(), medida.getCCintura(), medida.getCCadera(),
                        medida.getAlturaBusto(), medida.getSeparacionBusto(),
                        medida.getRadioBusto(), medida.getBajoBusto()}
        );

        // Separator
        Separator sep2 = new Separator();
        sep2.setStyle("-fx-background-color: #E0E0E0;");

        // Sección de Medidas de Largo
        VBox largoSection = crearSeccionMedidas("📏 Medidas de Largo",
                new String[]{"Largo Falda", "Largo Cadera", "Largo Vestido",
                        "Largo Pantalón", "Largo Manga"},
                new double[]{medida.getLargoFalda(), medida.getLargoCadera(),
                        medida.getLargoVestido(), medida.getLargoPantalon(),
                        medida.getLargoManga()}
        );

        // Separator
        Separator sep3 = new Separator();
        sep3.setStyle("-fx-background-color: #E0E0E0;");

        // Botones de acción
        HBox acciones = new HBox(10);
        acciones.setAlignment(Pos.CENTER_RIGHT);

        Button btnEditar = new Button("✏️ Editar");
        btnEditar.setStyle(
                "-fx-background-color: #E4DFD7;" +
                        "-fx-text-fill: #61564A;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: 600;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;"
        );
        btnEditar.setOnAction(e -> editarMedida(medida));

        Button btnEliminar = new Button("🗑️ Eliminar");
        btnEliminar.setStyle(
                "-fx-background-color: #FFE5E5;" +
                        "-fx-text-fill: #D32F2F;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: 600;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;"
        );
        btnEliminar.setOnAction(e -> eliminarMedida(medida));

        acciones.getChildren().addAll(btnEditar, btnEliminar);

        tarjeta.getChildren().addAll(header, sep1, torsoSection, sep2, largoSection, sep3, acciones);

        return tarjeta;
    }

    private VBox crearSeccionMedidas(String titulo, String[] labels, double[] valores) {
        VBox seccion = new VBox(12);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle(
                "-fx-font-size: 15px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-text-fill: #61564A;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;"
        );

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);

        int col = 0, row = 0;
        for (int i = 0; i < labels.length; i++) {
            if (valores[i] > 0) {  // Solo mostrar medidas con valor
                HBox item = new HBox(8);
                item.setAlignment(Pos.CENTER_LEFT);

                Label lblNombre = new Label(labels[i] + ":");
                lblNombre.setStyle(
                        "-fx-font-size: 13px;" +
                                "-fx-text-fill: #A59B8F;" +
                                "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;"
                );

                Label lblValor = new Label(String.format("%.2f cm", valores[i]));
                lblValor.setStyle(
                        "-fx-font-size: 13px;" +
                                "-fx-font-weight: 600;" +
                                "-fx-text-fill: #181716;" +
                                "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;"
                );

                item.getChildren().addAll(lblNombre, lblValor);
                grid.add(item, col, row);

                col++;
                if (col >= 3) {
                    col = 0;
                    row++;
                }
            }
        }

        seccion.getChildren().addAll(lblTitulo, grid);
        return seccion;
    }

    @FXML
    private void handleAgregarMedida() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/grupo/proyecto_aula_carpethome/CrearMedidaEstandar.fxml")
            );
            Parent root = loader.load();

            CrearMedidaEstandarController controller = loader.getController();
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

    private void editarMedida(Medida medida) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/grupo/proyecto_aula_carpethome/EditarMedidaEstandar.fxml")
            );
            Parent root = loader.load();

            EditarMedidaEstandarController controller = loader.getController();
            controller.setGestionController(this);
            controller.setMedida(medida);

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

    private void eliminarMedida(Medida medida) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Eliminar esta medida estándar?");
        alert.setContentText("Medida: " + medida.getNombreMedida() + "\n\n" +
                "⚠️ Esta acción no se puede deshacer.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    medidaService.eliminarMedida(medida.getIdMedida());
                    mostrarExito("Medida eliminada exitosamente");
                    cargarMedidas();
                } catch (SQLException e) {
                    e.printStackTrace();
                    mostrarError("Error al eliminar", e.getMessage());
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