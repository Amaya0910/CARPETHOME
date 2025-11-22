package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.services.ServiceFactory;
import grupo.proyecto_aula_carpethome.entities.Proyecto;
import grupo.proyecto_aula_carpethome.services.ProyectoService;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
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
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GestionProyectosController {

    @FXML private VBox tarjetasContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> estadoFilter;
    @FXML private Button btnAgregarProyecto;
    @FXML private Button btnAnterior;
    @FXML private Button btnSiguiente;
    @FXML private Label lblPaginacion;
    @FXML private VBox emptyStateContainer;

    private ObservableList<Proyecto> proyectosList;
    private FilteredList<Proyecto> filteredData;
    private ProyectoService proyectoService;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    // Paginación
    private static final int ITEMS_POR_PAGINA = 5;
    private int paginaActual = 0;
    private int totalPaginas = 0;

    @FXML
    public void initialize() {
        System.out.println("GestionProyectosController inicializado");

        // Inicializar el servicio
        proyectoService = ServiceFactory.getProyectoService();

        // Configurar filtros
        configurarFiltros();

        // Configurar scroll suave
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Cargar datos
        cargarProyectos();
    }

    // ============================================
    // CARGAR PROYECTOS DESDE BD
    // ============================================

    public void cargarProyectos() {
        try {
            List<Proyecto> proyectos = proyectoService.listarTodos();
            proyectosList = FXCollections.observableArrayList(proyectos);
            filteredData = new FilteredList<>(proyectosList, p -> true);

            System.out.println("Proyectos cargados: " + proyectos.size());

            paginaActual = 0;
            actualizarVista();

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
        estadoFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            aplicarFiltros();
            paginaActual = 0;
            actualizarVista();
        });

        // Listener para el campo de búsqueda
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            aplicarFiltros();
            paginaActual = 0;
            actualizarVista();
        });
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
    // ACTUALIZAR VISTA CON PAGINACIÓN
    // ============================================

    private void actualizarVista() {
        tarjetasContainer.getChildren().clear();

        if (filteredData.isEmpty()) {
            // Mostrar estado vacío
            scrollPane.setVisible(false);
            scrollPane.setManaged(false);
            emptyStateContainer.setVisible(true);
            emptyStateContainer.setManaged(true);
            btnAnterior.setVisible(false);
            btnSiguiente.setVisible(false);
            lblPaginacion.setVisible(false);
            return;
        }

        // Mostrar contenido
        scrollPane.setVisible(true);
        scrollPane.setManaged(true);
        emptyStateContainer.setVisible(false);
        emptyStateContainer.setManaged(false);

        // Calcular paginación
        totalPaginas = (int) Math.ceil((double) filteredData.size() / ITEMS_POR_PAGINA);
        int inicio = paginaActual * ITEMS_POR_PAGINA;
        int fin = Math.min(inicio + ITEMS_POR_PAGINA, filteredData.size());

        // Crear tarjetas para la página actual
        for (int i = inicio; i < fin; i++) {
            Proyecto proyecto = filteredData.get(i);
            VBox tarjeta = crearTarjetaProyecto(proyecto);
            tarjetasContainer.getChildren().add(tarjeta);

            // Animación de entrada
            animarEntrada(tarjeta, i - inicio);
        }

        // Actualizar controles de paginación
        actualizarControlesPaginacion();
    }

    private void actualizarControlesPaginacion() {
        lblPaginacion.setText(String.format("Página %d de %d", paginaActual + 1, totalPaginas));

        btnAnterior.setDisable(paginaActual == 0);
        btnSiguiente.setDisable(paginaActual >= totalPaginas - 1);

        btnAnterior.setVisible(totalPaginas > 1);
        btnSiguiente.setVisible(totalPaginas > 1);
        lblPaginacion.setVisible(totalPaginas > 1);

        // Estilos para botones deshabilitados
        if (btnAnterior.isDisabled()) {
            btnAnterior.setStyle(btnAnterior.getStyle() + "-fx-opacity: 0.5;");
        }
        if (btnSiguiente.isDisabled()) {
            btnSiguiente.setStyle(btnSiguiente.getStyle() + "-fx-opacity: 0.5;");
        }
    }

    // ============================================
    // CREAR TARJETA DE PROYECTO
    // ============================================

    private VBox crearTarjetaProyecto(Proyecto proyecto) {
        VBox tarjeta = new VBox(15);
        tarjeta.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 24;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.08), 10, 0, 0, 3);" +
                        "-fx-cursor: hand;"
        );

        // Efecto hover
        tarjeta.setOnMouseEntered(e -> {
            tarjeta.setStyle(
                    tarjeta.getStyle() +
                            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.15), 15, 0, 0, 5);" +
                            "-fx-translate-y: -2;"
            );
        });
        tarjeta.setOnMouseExited(e -> {
            tarjeta.setStyle(
                    tarjeta.getStyle().replace("-fx-translate-y: -2;", "") +
                            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.08), 10, 0, 0, 3);"
            );
        });

        // SECCIÓN SUPERIOR: ID, Nombre y Estado
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        // ID Badge
        Label idBadge = new Label(proyecto.getIdProyecto());
        idBadge.setStyle(
                "-fx-background-color: #F5F5F5;" +
                        "-fx-text-fill: #61564A;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;" +
                        "-fx-padding: 6 12;" +
                        "-fx-background-radius: 8;"
        );

        // Nombre del proyecto
        Label nombre = new Label(proyecto.getNombreProyecto());
        nombre.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-text-fill: #181716;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;"
        );
        HBox.setHgrow(nombre, Priority.ALWAYS);

        // Badge de estado
        Label estadoBadge = crearBadgeEstado(proyecto.getEstado());

        header.getChildren().addAll(idBadge, nombre, estadoBadge);

        // SECCIÓN DE INFORMACIÓN EN GRID
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(30);
        infoGrid.setVgap(15);
        infoGrid.setStyle("-fx-padding: 10 0;");

        // Columna 1
        agregarInfoItem(infoGrid, 0, 0, "📦 Tipo de Producción", proyecto.getTipoProduccion());
        agregarInfoItem(infoGrid, 0, 1, "📅 Fecha Inicio", formatearFecha(proyecto.getFechaInicio()));
        agregarInfoItem(infoGrid, 0, 2, "💰 Costo Estimado", formatearMoneda(proyecto.getCostoEstimado()));

        // Columna 2
        agregarInfoItem(infoGrid, 1, 0, "⏰ Entrega Estimada", formatearFecha(proyecto.getFechaEntregaEstimada()));
        agregarInfoItem(infoGrid, 1, 1, "✅ Entrega Real", formatearFecha(proyecto.getFechaEntregaReal()));
        agregarInfoItem(infoGrid, 1, 2, "👤 Cliente", proyecto.getIdCliente());

        // SECCIÓN DE ACCIONES
        HBox acciones = new HBox(12);
        acciones.setAlignment(Pos.CENTER_RIGHT);

        Button btnVer = crearBotonAccion("👁 Ver Detalles", "#61564A", "white");
        btnVer.setOnAction(e -> verProyecto(proyecto));

        Button btnEditar = crearBotonAccion("✏️ Editar", "#E4DFD7", "#61564A");
        btnEditar.setOnAction(e -> editarProyecto(proyecto));

        Button btnEliminar = crearBotonAccion("🗑️ Eliminar", "#FFE5E5", "#D32F2F");
        btnEliminar.setOnAction(e -> eliminarProyecto(proyecto));

        acciones.getChildren().addAll(btnVer, btnEditar, btnEliminar);

        // Separador
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #E0E0E0;");

        tarjeta.getChildren().addAll(header, separator, infoGrid, acciones);

        return tarjeta;
    }

    private void agregarInfoItem(GridPane grid, int col, int row, String etiqueta, String valor) {
        VBox item = new VBox(5);

        Label lblEtiqueta = new Label(etiqueta);
        lblEtiqueta.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-font-weight: 500;" +
                        "-fx-text-fill: #A59B8F;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;"
        );

        Label lblValor = new Label(valor != null && !valor.isEmpty() ? valor : "-");
        lblValor.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: 600;" +
                        "-fx-text-fill: #181716;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;"
        );

        item.getChildren().addAll(lblEtiqueta, lblValor);
        grid.add(item, col, row);
    }

    private Button crearBotonAccion(String texto, String bgColor, String textColor) {
        Button btn = new Button(texto);
        btn.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-text-fill: " + textColor + ";" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: 600;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10 18;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 4, 0, 0, 2);"
        );

        // Efecto hover
        btn.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), btn);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });
        btn.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), btn);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        return btn;
    }

    private Label crearBadgeEstado(String estado) {
        Label badge = new Label(estado);
        badge.setStyle(
                "-fx-background-radius: 12;" +
                        "-fx-padding: 6 14;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: 700;" +
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

    // ============================================
    // ANIMACIONES
    // ============================================

    private void animarEntrada(VBox tarjeta, int index) {
        tarjeta.setOpacity(0);
        tarjeta.setTranslateY(20);

        FadeTransition fade = new FadeTransition(Duration.millis(300), tarjeta);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(index * 50));

        javafx.animation.TranslateTransition translate =
                new javafx.animation.TranslateTransition(Duration.millis(300), tarjeta);
        translate.setFromY(20);
        translate.setToY(0);
        translate.setDelay(Duration.millis(index * 50));

        fade.play();
        translate.play();
    }

    // ============================================
    // MÉTODOS DE PAGINACIÓN
    // ============================================

    @FXML
    private void handleAnterior() {
        if (paginaActual > 0) {
            paginaActual--;
            scrollPane.setVvalue(0); // Scroll al inicio
            actualizarVista();
        }
    }

    @FXML
    private void handleSiguiente() {
        if (paginaActual < totalPaginas - 1) {
            paginaActual++;
            scrollPane.setVvalue(0); // Scroll al inicio
            actualizarVista();
        }
    }

    // ============================================
    // MÉTODOS DE ACCIÓN
    // ============================================

    @FXML
    private void handleAgregarProyecto() {
        System.out.println("Abriendo modal para agregar proyecto");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/grupo/proyecto_aula_carpethome/RegistrarProyecto.fxml"));
            Parent root = loader.load();

            RegistrarProyectoController modalController = loader.getController();
            modalController.setParentController(this);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initStyle(StageStyle.TRANSPARENT);
            modalStage.setTitle("Nuevo Proyecto");

            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
            overlay.getChildren().add(root);
            overlay.setPadding(new Insets(40));

            Scene scene = new Scene(overlay);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            modalStage.setScene(scene);

            modalStage.centerOnScreen();
            modalStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudo abrir el formulario de proyecto: " + e.getMessage());
        }
    }

    private void verProyecto(Proyecto proyecto) {
        System.out.println("Ver proyecto: " + proyecto.getIdProyecto());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/grupo/proyecto_aula_carpethome/DetalleProyecto.fxml"));
            Parent root = loader.load();

            DetalleProyectoController modalController = loader.getController();
            modalController.setParentController(this);
            modalController.cargarProyecto(proyecto);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initStyle(StageStyle.TRANSPARENT);
            modalStage.setTitle("Detalle del Proyecto");

            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
            overlay.getChildren().add(root);
            overlay.setPadding(new Insets(40));

            Scene scene = new Scene(overlay);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            modalStage.setScene(scene);

            modalStage.centerOnScreen();
            modalStage.showAndWait();

            System.out.println("Modal cerrado, recargando proyectos...");
            cargarProyectos();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudo abrir el detalle del proyecto");
        }
    }

    private void editarProyecto(Proyecto proyecto) {
        System.out.println("Editar proyecto: " + proyecto.getIdProyecto());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/grupo/proyecto_aula_carpethome/EditarProyecto.fxml"));
            Parent root = loader.load();

            EditarProyectoController modalController = loader.getController();
            modalController.setParentController(this);
            modalController.cargarProyecto(proyecto);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initStyle(StageStyle.TRANSPARENT);
            modalStage.setTitle("Editar Proyecto");

            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
            overlay.getChildren().add(root);
            overlay.setPadding(new Insets(40));

            Scene scene = new Scene(overlay);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            modalStage.setScene(scene);

            modalStage.centerOnScreen();
            modalStage.showAndWait();

            // Recargar después de cerrar
            System.out.println("Modal cerrado, recargando proyectos...");
            cargarProyectos();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudo abrir el formulario de edición");
        }
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
                    cargarProyectos();
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

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}