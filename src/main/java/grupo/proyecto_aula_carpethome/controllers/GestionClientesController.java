package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.entities.Cliente;
import grupo.proyecto_aula_carpethome.services.ClienteService;
import grupo.proyecto_aula_carpethome.services.ServiceFactory;
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

public class GestionClientesController {

    @FXML private VBox tarjetasContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filtroEstado;
    @FXML private Button btnAgregarCliente;
    @FXML private Button btnAnterior;
    @FXML private Button btnSiguiente;
    @FXML private Label lblPaginacion;
    @FXML private Label lblContador;
    @FXML private VBox contenedorPrincipal;
    @FXML private VBox emptyStateContainer;

    private ObservableList<Cliente> clientesList;
    private FilteredList<Cliente> filteredData;
    private ClienteService clienteService;

    // Paginación
    private static final int ITEMS_POR_PAGINA = 6;
    private int paginaActual = 0;
    private int totalPaginas = 0;

    @FXML
    public void initialize() {
        System.out.println("GestionClientesController inicializado");

        // Inicializar servicio
        clienteService = ServiceFactory.getClienteService();

        // Configurar filtros
        configurarFiltros();

        // Configurar scroll
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Cargar datos
        cargarClientes();
    }

    // ============================================
    // CARGAR CLIENTES
    // ============================================

    public void cargarClientes() {
        try {
            clientesList = FXCollections.observableArrayList(clienteService.listarTodos());
            filteredData = new FilteredList<>(clientesList, p -> true);

            System.out.println("Clientes cargados: " + clientesList.size());

            paginaActual = 0;
            actualizarVista();
            actualizarContador();

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al cargar clientes", e.getMessage());
        }
    }

    // ============================================
    // CONFIGURAR FILTROS
    // ============================================

    private void configurarFiltros() {
        // ComboBox de estado (para futuras mejoras)
        filtroEstado.setItems(FXCollections.observableArrayList(
                "Todos", "Con proyectos activos", "Sin proyectos"
        ));
        filtroEstado.setValue("Todos");

        // Listener para filtros
        filtroEstado.valueProperty().addListener((obs, oldVal, newVal) -> {
            aplicarFiltros();
            paginaActual = 0;
            actualizarVista();
        });

        // Listener para búsqueda con debounce
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            aplicarFiltros();
            paginaActual = 0;
            actualizarVista();
        });
    }

    private void aplicarFiltros() {
        filteredData.setPredicate(cliente -> {
            // Filtro de búsqueda
            String searchText = searchField.getText().toLowerCase().trim();
            boolean matchesSearch = true;

            if (!searchText.isEmpty()) {
                matchesSearch =
                        cliente.getNombreCompleto().toLowerCase().contains(searchText) ||
                                cliente.getCedula().toLowerCase().contains(searchText) ||
                                cliente.getPCorreo().toLowerCase().contains(searchText) ||
                                (cliente.getSCorreo() != null && cliente.getSCorreo().toLowerCase().contains(searchText)) ||
                                cliente.getIdCliente().toLowerCase().contains(searchText);
            }

            // Filtro de estado
            String estadoSeleccionado = filtroEstado.getValue();
            boolean matchesEstado = true;

            if (estadoSeleccionado != null && !estadoSeleccionado.equals("Todos")) {
                try {
                    if (estadoSeleccionado.equals("Con proyectos activos")) {
                        int proyectosActivos = clienteService.obtenerEstadisticasCliente(cliente.getIdCliente()).getProyectosActivos();
                        matchesEstado = proyectosActivos > 0;
                    } else if (estadoSeleccionado.equals("Sin proyectos")) {
                        int proyectosTotales = clienteService.obtenerEstadisticasCliente(cliente.getIdCliente()).getProyectosTotales();
                        matchesEstado = proyectosTotales == 0;
                    }
                } catch (Exception e) {
                    System.err.println("Error al filtrar clientes: " + e.getMessage());
                    matchesEstado = true;
                }
            }

            return matchesSearch && matchesEstado;
        });

        actualizarContador();
    }

    // ============================================
    // ACTUALIZAR VISTA
    // ============================================

    private void actualizarVista() {
        tarjetasContainer.getChildren().clear();

        if (filteredData.isEmpty()) {
            // Mostrar estado vacío
            contenedorPrincipal.setVisible(false);
            contenedorPrincipal.setManaged(false);
            emptyStateContainer.setVisible(true);
            emptyStateContainer.setManaged(true);
            return;
        }

        // Mostrar contenido
        contenedorPrincipal.setVisible(true);
        contenedorPrincipal.setManaged(true);
        emptyStateContainer.setVisible(false);
        emptyStateContainer.setManaged(false);

        // Calcular paginación
        totalPaginas = (int) Math.ceil((double) filteredData.size() / ITEMS_POR_PAGINA);
        int inicio = paginaActual * ITEMS_POR_PAGINA;
        int fin = Math.min(inicio + ITEMS_POR_PAGINA, filteredData.size());

        // Crear tarjetas
        for (int i = inicio; i < fin; i++) {
            Cliente cliente = filteredData.get(i);
            HBox tarjeta = crearTarjetaCliente(cliente);
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

    private void actualizarContador() {
        int total = filteredData.size();
        lblContador.setText(total == 1 ? "1 cliente" : total + " clientes");
    }

    // ============================================
    // CREAR TARJETA DE CLIENTE
    // ============================================

    private HBox crearTarjetaCliente(Cliente cliente) {
        HBox tarjeta = new HBox(20);
        tarjeta.setAlignment(Pos.CENTER_LEFT);
        tarjeta.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 20;" +
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

        // Avatar (círculo con iniciales)
        StackPane avatar = crearAvatar(cliente);

        // Información principal
        VBox infoVBox = new VBox(8);
        infoVBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoVBox, Priority.ALWAYS);

        // Nombre y ID
        HBox nombreRow = new HBox(12);
        nombreRow.setAlignment(Pos.CENTER_LEFT);

        Label nombre = new Label(cliente.getNombreCompleto());
        nombre.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-text-fill: #181716;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;"
        );

        Label idBadge = new Label(cliente.getIdCliente());
        idBadge.setStyle(
                "-fx-background-color: #F5F5F5;" +
                        "-fx-text-fill: #61564A;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;" +
                        "-fx-padding: 4 10;" +
                        "-fx-background-radius: 8;"
        );

        nombreRow.getChildren().addAll(nombre, idBadge);

        // Grid de información
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(25);
        infoGrid.setVgap(5);

        // Cédula
        agregarInfoItem(infoGrid, 0, 0, "📋", cliente.getCedula());

        // Teléfono
        String telefono = String.format("%,d", cliente.getPTelefono());
        agregarInfoItem(infoGrid, 1, 0, "📱", telefono);

        // Correo
        agregarInfoItem(infoGrid, 2, 0, "📧", cliente.getPCorreo());

        infoVBox.getChildren().addAll(nombreRow, infoGrid);

        // Botones de acción
        VBox accionesVBox = new VBox(8);
        accionesVBox.setAlignment(Pos.CENTER);

        Button btnVer = crearBotonAccion("👁", "#61564A");
        btnVer.setOnAction(e -> verCliente(cliente));
        btnVer.setTooltip(new Tooltip("Ver detalles"));

        Button btnEditar = crearBotonAccion("✏️", "#4CAF50");
        btnEditar.setOnAction(e -> editarCliente(cliente));
        btnEditar.setTooltip(new Tooltip("Editar cliente"));

        Button btnEliminar = crearBotonAccion("🗑️", "#F44336");
        btnEliminar.setOnAction(e -> eliminarCliente(cliente));
        btnEliminar.setTooltip(new Tooltip("Eliminar cliente"));

        accionesVBox.getChildren().addAll(btnVer, btnEditar, btnEliminar);

        tarjeta.getChildren().addAll(avatar, infoVBox, accionesVBox);

        return tarjeta;
    }

    private StackPane crearAvatar(Cliente cliente) {
        StackPane avatar = new StackPane();
        avatar.setPrefSize(60, 60);
        avatar.setMinSize(60, 60);
        avatar.setMaxSize(60, 60);
        avatar.setStyle(
                "-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%);" +
                        "-fx-background-radius: 50%;"
        );

        // Iniciales
        String iniciales = obtenerIniciales(cliente);
        Label lblIniciales = new Label(iniciales);
        lblIniciales.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;"
        );

        avatar.getChildren().add(lblIniciales);
        return avatar;
    }

    private String obtenerIniciales(Cliente cliente) {
        String iniciales = "";
        if (cliente.getPNombre() != null && !cliente.getPNombre().isEmpty()) {
            iniciales += cliente.getPNombre().charAt(0);
        }
        if (cliente.getPApellido() != null && !cliente.getPApellido().isEmpty()) {
            iniciales += cliente.getPApellido().charAt(0);
        }
        return iniciales.toUpperCase();
    }

    private void agregarInfoItem(GridPane grid, int col, int row, String icono, String valor) {
        HBox item = new HBox(6);
        item.setAlignment(Pos.CENTER_LEFT);

        Label lblIcono = new Label(icono);
        lblIcono.setStyle("-fx-font-size: 14px;");

        Label lblValor = new Label(valor);
        lblValor.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: #61564A;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;"
        );

        item.getChildren().addAll(lblIcono, lblValor);
        grid.add(item, col, row);
    }

    private Button crearBotonAccion(String texto, String color) {
        Button btn = new Button(texto);
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-min-width: 45;" +
                        "-fx-min-height: 45;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 6, 0, 0, 2);"
        );

        // Efecto hover
        btn.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), btn);
            st.setToX(1.1);
            st.setToY(1.1);
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

    // ============================================
    // ANIMACIONES
    // ============================================

    private void animarEntrada(HBox tarjeta, int index) {
        tarjeta.setOpacity(0);
        tarjeta.setTranslateX(-20);

        FadeTransition fade = new FadeTransition(Duration.millis(300), tarjeta);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(index * 50));

        javafx.animation.TranslateTransition translate =
                new javafx.animation.TranslateTransition(Duration.millis(300), tarjeta);
        translate.setFromX(-20);
        translate.setToX(0);
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
            scrollPane.setVvalue(0);
            actualizarVista();
        }
    }

    @FXML
    private void handleSiguiente() {
        if (paginaActual < totalPaginas - 1) {
            paginaActual++;
            scrollPane.setVvalue(0);
            actualizarVista();
        }
    }

    // ============================================
    // ACCIONES
    // ============================================

    @FXML
    private void handleAgregarCliente() {
        abrirModal("/grupo/proyecto_aula_carpethome/RegistrarCliente.fxml", "Nuevo Cliente", null);
    }

    private void verCliente(Cliente cliente) {
        abrirModal("/grupo/proyecto_aula_carpethome/DetalleCliente.fxml", "Detalle del Cliente", cliente);
    }

    private void editarCliente(Cliente cliente) {
        abrirModal("/grupo/proyecto_aula_carpethome/EditarCliente.fxml", "Editar Cliente", cliente);
    }

    private void eliminarCliente(Cliente cliente) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("⚠️ Confirmar eliminación");
        alert.setHeaderText("¿Estás seguro de eliminar este cliente?");
        alert.setContentText(
                "Cliente: " + cliente.getNombreCompleto() + "\n" +
                        "ID: " + cliente.getIdCliente() + "\n" +
                        "Cédula: " + cliente.getCedula() + "\n\n" +
                        "⚠️ Esta acción no se puede deshacer."
        );

        ButtonType btnEliminar = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnEliminar, btnCancelar);

        alert.showAndWait().ifPresent(response -> {
            if (response == btnEliminar) {
                try {
                    clienteService.eliminarCliente(cliente.getIdCliente());
                    mostrarExito("Cliente eliminado correctamente");
                    cargarClientes();
                } catch (SQLException e) {
                    e.printStackTrace();
                    String mensaje = e.getMessage();
                    if (mensaje.contains("constraint") || mensaje.contains("ORA-02292")) {
                        mostrarError("No se puede eliminar",
                                "Este cliente tiene proyectos asociados.\n" +
                                        "Elimine primero los proyectos relacionados.");
                    } else {
                        mostrarError("Error al eliminar", mensaje);
                    }
                }
            }
        });
    }

    // ============================================
    // MODAL HELPER
    // ============================================

    private void abrirModal(String fxmlPath, String titulo, Cliente cliente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();

            // Pasar el controlador padre y el cliente
            if (controller instanceof RegistrarClienteController) {
                ((RegistrarClienteController) controller).setParentController(this);
            } else if (controller instanceof DetalleClienteController) {
                DetalleClienteController detalle = (DetalleClienteController) controller;
                detalle.setParentController(this);
                detalle.cargarCliente(cliente);
            } else if (controller instanceof EditarClienteController) {
                EditarClienteController editar = (EditarClienteController) controller;
                editar.setParentController(this);
                editar.cargarCliente(cliente);
            }

            // Crear stage
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initStyle(StageStyle.TRANSPARENT);
            modalStage.setTitle(titulo);

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
            cargarClientes();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudo abrir el formulario: " + e.getMessage());
        }
    }

    // ============================================
    // EFECTOS HOVER
    // ============================================

    @FXML
    private void handleAgregarHover(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle(button.getStyle().replace("#61564A", "#4a433e"));
    }

    @FXML
    private void handleAgregarExit(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle(button.getStyle().replace("#4a433e", "#61564A"));
    }

    // ============================================
    // UTILIDADES
    // ============================================

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