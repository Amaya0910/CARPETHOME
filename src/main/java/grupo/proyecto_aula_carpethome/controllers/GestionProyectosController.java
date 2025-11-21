package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.config.ServiceFactory;
import grupo.proyecto_aula_carpethome.entities.Proyecto;
import grupo.proyecto_aula_carpethome.services.ProyectoService;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GestionProyectosController {

    @FXML private TableView<Proyecto> tablaProyectos;
    @FXML private TableColumn<Proyecto, String> colId;
    @FXML private TableColumn<Proyecto, String> colNombre;
    @FXML private TableColumn<Proyecto, String> colEstado;
    @FXML private TableColumn<Proyecto, String> colEtapa;
    @FXML private TableColumn<Proyecto, String> colTipoProduccion;
    @FXML private TableColumn<Proyecto, String> colFechaInicio;
    @FXML private TableColumn<Proyecto, String> colEntregaEstimada;
    @FXML private TableColumn<Proyecto, String> colEntregaReal;
    @FXML private TableColumn<Proyecto, String> colCostoEstimado;
    @FXML private TableColumn<Proyecto, String> colCliente;
    @FXML private TableColumn<Proyecto, Void> colAcciones;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> estadoFilter;
    @FXML private Button btnAgregarProyecto;

    private ObservableList<Proyecto> proyectosList;
    private FilteredList<Proyecto> filteredData;
    private ProyectoService proyectoService;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    @FXML
    public void initialize() {
        System.out.println("GestionProyectosController inicializado");

        // Inicializar el servicio
        proyectoService = ServiceFactory.getProyectoService();

        // Configurar las columnas
        configurarColumnas();

        // Configurar filtros
        configurarFiltros();

        // Cargar datos
        cargarProyectos();

        // Aplicar estilos a la tabla
        aplicarEstilosTabla();
    }

    // ============================================
    // CONFIGURACIÓN DE COLUMNAS
    // ============================================

    private void configurarColumnas() {
        // ID
        colId.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getIdProyecto()));

        // NOMBRE
        colNombre.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNombreProyecto()));

        // ESTADO - Con badge de color
        colEstado.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getEstado()));
        colEstado.setCellFactory(column -> new TableCell<Proyecto, String>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = crearBadgeEstado(estado);
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        // ETAPA - Placeholder por ahora
        colEtapa.setCellValueFactory(data ->
                new SimpleStringProperty("-")); // Agregar cuando tengas este campo

        // TIPO PRODUCCIÓN
        colTipoProduccion.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getTipoProduccion()));

        // FECHA INICIO
        colFechaInicio.setCellValueFactory(data ->
                new SimpleStringProperty(formatearFecha(data.getValue().getFechaInicio())));

        // ENTREGA ESTIMADA
        colEntregaEstimada.setCellValueFactory(data ->
                new SimpleStringProperty(formatearFecha(data.getValue().getFechaEntregaEstimada())));

        // ENTREGA REAL
        colEntregaReal.setCellValueFactory(data ->
                new SimpleStringProperty(formatearFecha(data.getValue().getFechaEntregaReal())));

        // COSTO ESTIMADO
        colCostoEstimado.setCellValueFactory(data ->
                new SimpleStringProperty(formatearMoneda(data.getValue().getCostoEstimado())));

        // CLIENTE - Placeholder (necesitarás hacer join con tabla clientes)
        colCliente.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getIdCliente())); // Cambiar por nombre del cliente

        // ACCIONES - Botones de acción
        colAcciones.setCellFactory(column -> new TableCell<Proyecto, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox acciones = crearBotonesAccion(getTableRow().getItem());
                    setGraphic(acciones);
                }
            }
        });
    }

    // ============================================
    // CARGAR PROYECTOS DESDE BD
    // ============================================

    public void cargarProyectos() {
        try {
            List<Proyecto> proyectos = proyectoService.listarTodos();
            proyectosList = FXCollections.observableArrayList(proyectos);
            filteredData = new FilteredList<>(proyectosList, p -> true);
            tablaProyectos.setItems(filteredData);

            System.out.println("Proyectos cargados: " + proyectos.size());

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al cargar proyectos", e.getMessage());
        }
    }

    // ============================================
    // CONFIGURAR FILTROS
    // ============================================

    private void configurarFiltros() {
        // ComboBox de estados
        estadoFilter.setItems(FXCollections.observableArrayList(
                "Todos", "Completado", "En Progreso", "Pendiente", "Cancelado"
        ));
        estadoFilter.setValue("Todos");

        // Listener para el filtro de estado
        estadoFilter.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());

        // Listener para el campo de búsqueda
        searchField.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
    }

    private void aplicarFiltros() {
        filteredData.setPredicate(proyecto -> {
            // Filtro de búsqueda
            String searchText = searchField.getText().toLowerCase().trim();
            boolean matchesSearch = true;

            if (!searchText.isEmpty()) {
                matchesSearch = proyecto.getIdProyecto().toLowerCase().contains(searchText) ||
                        proyecto.getNombreProyecto().toLowerCase().contains(searchText) ||
                        proyecto.getIdCliente().toLowerCase().contains(searchText);
            }

            // Filtro de estado
            String estadoSeleccionado = estadoFilter.getValue();
            boolean matchesEstado = true;

            if (estadoSeleccionado != null && !estadoSeleccionado.equals("Todos")) {
                matchesEstado = proyecto.getEstado().equals(estadoSeleccionado);
            }

            return matchesSearch && matchesEstado;
        });
    }

    // ============================================
    // CREAR ELEMENTOS VISUALES
    // ============================================

    private Label crearBadgeEstado(String estado) {
        Label badge = new Label(estado);
        badge.setStyle(
                "-fx-background-radius: 12;" +
                        "-fx-padding: 4 12;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: 600;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;"
        );

        // Colores según el estado
        switch (estado) {
            case "Completado":
                badge.setStyle(badge.getStyle() +
                        "-fx-background-color: #C8E6C9;" +
                        "-fx-text-fill: #2E7D32;");
                break;
            case "En Progreso":
                badge.setStyle(badge.getStyle() +
                        "-fx-background-color: #BBDEFB;" +
                        "-fx-text-fill: #1565C0;");
                break;
            case "Pendiente":
                badge.setStyle(badge.getStyle() +
                        "-fx-background-color: #FFF9C4;" +
                        "-fx-text-fill: #F57F17;");
                break;
            case "Cancelado":
                badge.setStyle(badge.getStyle() +
                        "-fx-background-color: #E0E0E0;" +
                        "-fx-text-fill: #616161;");
                break;
            default:
                badge.setStyle(badge.getStyle() +
                        "-fx-background-color: #F5F5F5;" +
                        "-fx-text-fill: #9E9E9E;");
        }

        return badge;
    }

    private HBox crearBotonesAccion(Proyecto proyecto) {
        if (proyecto == null) return new HBox();

        HBox container = new HBox(8);
        container.setAlignment(Pos.CENTER);

        // Botón Ver
        Button btnVer = new Button("👁");
        btnVer.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 16px;" +
                        "-fx-padding: 5;"
        );
        btnVer.setOnAction(e -> verProyecto(proyecto));

        // Botón Editar
        Button btnEditar = new Button("✏️");
        btnEditar.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 16px;" +
                        "-fx-padding: 5;"
        );
        btnEditar.setOnAction(e -> editarProyecto(proyecto));

        // Botón Eliminar
        Button btnEliminar = new Button("🗑️");
        btnEliminar.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 16px;" +
                        "-fx-padding: 5;"
        );
        btnEliminar.setOnAction(e -> eliminarProyecto(proyecto));

        container.getChildren().addAll(btnVer, btnEditar, btnEliminar);
        return container;
    }

    // ============================================
    // MÉTODOS DE ACCIÓN
    // ============================================

    @FXML
    private void handleAgregarProyecto() {
        System.out.println("Abriendo modal para agregar proyecto");
        try {
            // Cargar el FXML del modal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/grupo/proyecto_aula_carpethome/RegistrarProyecto.fxml"));
            Parent root = loader.load();

            // Obtener el controlador y pasar la referencia
            RegistrarProyectoController modalController = loader.getController();
            modalController.setParentController(this);

            // Crear el Stage del modal
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initStyle(StageStyle.TRANSPARENT); // ← CAMBIADO A TRANSPARENT
            modalStage.setTitle("Nuevo Proyecto");

            // Crear la escena con fondo semi-transparente
            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);"); // Fondo oscuro
            overlay.getChildren().add(root);
            overlay.setPadding(new javafx.geometry.Insets(40));

            Scene scene = new Scene(overlay);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT); // Hacer la escena transparente
            modalStage.setScene(scene);

            // Centrar el modal en la pantalla
            modalStage.centerOnScreen();

            // Mostrar el modal
            modalStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudo abrir el formulario de proyecto: " + e.getMessage());
        }
    }

    private void verProyecto(Proyecto proyecto) {
        System.out.println("Ver proyecto: " + proyecto.getIdProyecto());
        // TODO: Mostrar detalles del proyecto
    }

    private void editarProyecto(Proyecto proyecto) {
        System.out.println("Editar proyecto: " + proyecto.getIdProyecto());
        // TODO: Abrir modal de edición
    }

    private void eliminarProyecto(Proyecto proyecto) {
        System.out.println("Eliminar proyecto: " + proyecto.getIdProyecto());

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Estás seguro de eliminar este proyecto?");
        alert.setContentText(proyecto.getNombreProyecto());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // TODO: Llamar al servicio para eliminar
                    // proyectoService.eliminar(proyecto.getIdProyecto());
                    cargarProyectos(); // Recargar la tabla
                    mostrarExito("Proyecto eliminado correctamente");
                } catch (Exception e) {
                    e.printStackTrace();
                    mostrarError("Error al eliminar", e.getMessage());
                }
            }
        });
    }

    // ============================================
    // EFECTOS HOVER
    // ============================================

    @FXML
    private void handleButtonHover(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle(button.getStyle() + "-fx-background-color: #4a433e;");
    }

    @FXML
    private void handleButtonExit(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle(button.getStyle().replace("-fx-background-color: #4a433e;",
                "-fx-background-color: #61564A;"));
    }

    // ============================================
    // UTILIDADES
    // ============================================

    private String formatearFecha(Date fecha) {
        if (fecha == null) return "-";
        return dateFormat.format(fecha);
    }

    private String formatearMoneda(double monto) {
        return currencyFormat.format(monto);
    }

    private void aplicarEstilosTabla() {
        tablaProyectos.setStyle(
                "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;" +
                        "-fx-font-size: 13px;"
        );
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
