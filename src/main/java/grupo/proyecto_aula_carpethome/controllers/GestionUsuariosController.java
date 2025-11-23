package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.entities.Administrador;
import grupo.proyecto_aula_carpethome.entities.Empleado;
import grupo.proyecto_aula_carpethome.services.AdministradorService;
import grupo.proyecto_aula_carpethome.services.EmpleadoService;
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
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;

public class GestionUsuariosController {

    @FXML private TabPane tabPane;
    @FXML private Tab tabAdministradores;
    @FXML private Tab tabEmpleados;

    // Administradores
    @FXML private VBox tarjetasAdminsContainer;
    @FXML private ScrollPane scrollAdmins;
    @FXML private TextField searchAdmins;
    @FXML private Button btnAgregarAdmin;
    @FXML private Label lblContadorAdmins;
    @FXML private VBox emptyStateAdmins;

    // Empleados
    @FXML private VBox tarjetasEmpleadosContainer;
    @FXML private ScrollPane scrollEmpleados;
    @FXML private TextField searchEmpleados;
    @FXML private ComboBox<String> filtroCargoEmpleados;
    @FXML private Button btnAgregarEmpleado;
    @FXML private Label lblContadorEmpleados;
    @FXML private VBox emptyStateEmpleados;

    private ObservableList<Administrador> adminsList;
    private FilteredList<Administrador> filteredAdmins;
    private AdministradorService administradorService;

    private ObservableList<Empleado> empleadosList;
    private FilteredList<Empleado> filteredEmpleados;
    private EmpleadoService empleadoService;

    @FXML
    public void initialize() {
        System.out.println("GestionUsuariosController inicializado");

        // Inicializar servicios
        administradorService = ServiceFactory.getAdministradorService();
        empleadoService = ServiceFactory.getEmpleadoService();

        // Configurar scroll
        configurarScroll();

        // Configurar filtros
        configurarFiltros();

        // Cargar datos
        cargarAdministradores();
        cargarEmpleados();
    }

    // ============================================
    // CONFIGURACIÓN INICIAL
    // ============================================

    private void configurarScroll() {
        scrollAdmins.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollAdmins.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollAdmins.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        scrollEmpleados.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollEmpleados.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollEmpleados.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    }

    private void configurarFiltros() {
        // Filtro de búsqueda para Administradores
        searchAdmins.textProperty().addListener((obs, oldVal, newVal) -> {
            aplicarFiltrosAdmins();
        });

        // Filtro de búsqueda para Empleados
        searchEmpleados.textProperty().addListener((obs, oldVal, newVal) -> {
            aplicarFiltrosEmpleados();
        });

        // ComboBox de cargo para Empleados
        filtroCargoEmpleados.setItems(FXCollections.observableArrayList(
                "Todos", "Diseñador", "Sastre", "Auxiliar", "Supervisor"
        ));
        filtroCargoEmpleados.setValue("Todos");

        filtroCargoEmpleados.valueProperty().addListener((obs, oldVal, newVal) -> {
            aplicarFiltrosEmpleados();
        });
    }

    // ============================================
    // CARGAR ADMINISTRADORES
    // ============================================

    public void cargarAdministradores() {
        try {
            adminsList = FXCollections.observableArrayList(administradorService.listarTodos());
            filteredAdmins = new FilteredList<>(adminsList, p -> true);

            System.out.println("Administradores cargados: " + adminsList.size());

            actualizarVistaAdministradores();
            actualizarContadorAdmins();

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al cargar administradores", e.getMessage());
        }
    }

    private void actualizarVistaAdministradores() {
        tarjetasAdminsContainer.getChildren().clear();

        if (filteredAdmins.isEmpty()) {
            scrollAdmins.setVisible(false);
            scrollAdmins.setManaged(false);
            emptyStateAdmins.setVisible(true);
            emptyStateAdmins.setManaged(true);
            return;
        }

        scrollAdmins.setVisible(true);
        scrollAdmins.setManaged(true);
        emptyStateAdmins.setVisible(false);
        emptyStateAdmins.setManaged(false);

        for (int i = 0; i < filteredAdmins.size(); i++) {
            Administrador admin = filteredAdmins.get(i);
            HBox tarjeta = crearTarjetaAdministrador(admin);
            tarjetasAdminsContainer.getChildren().add(tarjeta);
            animarEntrada(tarjeta, i);
        }
    }

    private HBox crearTarjetaAdministrador(Administrador admin) {
        HBox tarjeta = new HBox(20);
        tarjeta.setAlignment(Pos.CENTER_LEFT);
        tarjeta.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.08), 10, 0, 0, 3);" +
                        "-fx-cursor: hand;"
        );

        // Avatar
        StackPane avatar = crearAvatar(admin.getPNombre(), admin.getPApellido(), "#667eea");

        // Información
        VBox infoVBox = new VBox(8);
        infoVBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoVBox, Priority.ALWAYS);

        HBox nombreRow = new HBox(12);
        nombreRow.setAlignment(Pos.CENTER_LEFT);

        Label nombre = new Label(admin.getNombreCompleto());
        nombre.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-text-fill: #181716;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;"
        );

        Label rolBadge = new Label("ADMINISTRADOR");
        rolBadge.setStyle(
                "-fx-background-color: #FFF3E0;" +
                        "-fx-text-fill: #E65100;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;" +
                        "-fx-padding: 4 10;" +
                        "-fx-background-radius: 8;"
        );

        nombreRow.getChildren().addAll(nombre, rolBadge);

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(25);
        infoGrid.setVgap(5);

        agregarInfoItem(infoGrid, 0, 0, "📋", admin.getCedula());
        agregarInfoItem(infoGrid, 1, 0, "📱", String.valueOf(admin.getPTelefono()));
        agregarInfoItem(infoGrid, 2, 0, "📧", admin.getPCorreo());

        infoVBox.getChildren().addAll(nombreRow, infoGrid);

        // Botones de acción
        VBox accionesVBox = new VBox(8);
        accionesVBox.setAlignment(Pos.CENTER);

        Button btnEditar = crearBotonAccion("✏️", "#4CAF50");
        btnEditar.setOnAction(e -> editarAdministrador(admin));
        btnEditar.setTooltip(new Tooltip("Editar administrador"));

        Button btnEliminar = crearBotonAccion("🗑️", "#F44336");
        btnEliminar.setOnAction(e -> eliminarAdministrador(admin));
        btnEliminar.setTooltip(new Tooltip("Eliminar administrador"));

        accionesVBox.getChildren().addAll(btnEditar, btnEliminar);

        tarjeta.getChildren().addAll(avatar, infoVBox, accionesVBox);

        return tarjeta;
    }

    private void aplicarFiltrosAdmins() {
        filteredAdmins.setPredicate(admin -> {
            String searchText = searchAdmins.getText().toLowerCase().trim();

            if (searchText.isEmpty()) {
                return true;
            }

            return admin.getNombreCompleto().toLowerCase().contains(searchText) ||
                    admin.getCedula().toLowerCase().contains(searchText) ||
                    admin.getPCorreo().toLowerCase().contains(searchText);
        });

        actualizarVistaAdministradores();
        actualizarContadorAdmins();
    }

    private void actualizarContadorAdmins() {
        int total = filteredAdmins.size();
        lblContadorAdmins.setText(total == 1 ? "1 administrador" : total + " administradores");
    }

    // ============================================
    // CARGAR EMPLEADOS
    // ============================================

    public void cargarEmpleados() {
        try {
            empleadosList = FXCollections.observableArrayList(empleadoService.listarTodos());
            filteredEmpleados = new FilteredList<>(empleadosList, p -> true);

            System.out.println("Empleados cargados: " + empleadosList.size());

            actualizarVistaEmpleados();
            actualizarContadorEmpleados();

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al cargar empleados", e.getMessage());
        }
    }

    private void actualizarVistaEmpleados() {
        tarjetasEmpleadosContainer.getChildren().clear();

        if (filteredEmpleados.isEmpty()) {
            scrollEmpleados.setVisible(false);
            scrollEmpleados.setManaged(false);
            emptyStateEmpleados.setVisible(true);
            emptyStateEmpleados.setManaged(true);
            return;
        }

        scrollEmpleados.setVisible(true);
        scrollEmpleados.setManaged(true);
        emptyStateEmpleados.setVisible(false);
        emptyStateEmpleados.setManaged(false);

        for (int i = 0; i < filteredEmpleados.size(); i++) {
            Empleado empleado = filteredEmpleados.get(i);
            HBox tarjeta = crearTarjetaEmpleado(empleado);
            tarjetasEmpleadosContainer.getChildren().add(tarjeta);
            animarEntrada(tarjeta, i);
        }
    }

    private HBox crearTarjetaEmpleado(Empleado empleado) {
        HBox tarjeta = new HBox(20);
        tarjeta.setAlignment(Pos.CENTER_LEFT);
        tarjeta.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.08), 10, 0, 0, 3);" +
                        "-fx-cursor: hand;"
        );

        // Avatar
        StackPane avatar = crearAvatar(empleado.getPNombre(), empleado.getPApellido(), "#4CAF50");

        // Información
        VBox infoVBox = new VBox(8);
        infoVBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoVBox, Priority.ALWAYS);

        HBox nombreRow = new HBox(12);
        nombreRow.setAlignment(Pos.CENTER_LEFT);

        Label nombre = new Label(empleado.getNombreCompleto());
        nombre.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-text-fill: #181716;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;"
        );

        Label cargoBadge = new Label(empleado.getCargo());
        cargoBadge.setStyle(
                "-fx-background-color: #E8F5E9;" +
                        "-fx-text-fill: #2E7D32;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;" +
                        "-fx-padding: 4 10;" +
                        "-fx-background-radius: 8;"
        );

        nombreRow.getChildren().addAll(nombre, cargoBadge);

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(25);
        infoGrid.setVgap(5);

        agregarInfoItem(infoGrid, 0, 0, "📋", empleado.getCedula());
        agregarInfoItem(infoGrid, 1, 0, "📱", String.valueOf(empleado.getPTelefono()));
        agregarInfoItem(infoGrid, 2, 0, "📧", empleado.getPCorreo());

        infoVBox.getChildren().addAll(nombreRow, infoGrid);

        // Botones de acción
        VBox accionesVBox = new VBox(8);
        accionesVBox.setAlignment(Pos.CENTER);

        Button btnEditar = crearBotonAccion("✏️", "#4CAF50");
        btnEditar.setOnAction(e -> editarEmpleado(empleado));
        btnEditar.setTooltip(new Tooltip("Editar empleado"));

        Button btnEliminar = crearBotonAccion("🗑️", "#F44336");
        btnEliminar.setOnAction(e -> eliminarEmpleado(empleado));
        btnEliminar.setTooltip(new Tooltip("Eliminar empleado"));

        accionesVBox.getChildren().addAll(btnEditar, btnEliminar);

        tarjeta.getChildren().addAll(avatar, infoVBox, accionesVBox);

        return tarjeta;
    }

    private void aplicarFiltrosEmpleados() {
        filteredEmpleados.setPredicate(empleado -> {
            String searchText = searchEmpleados.getText().toLowerCase().trim();
            boolean matchesSearch = true;

            if (!searchText.isEmpty()) {
                matchesSearch = empleado.getNombreCompleto().toLowerCase().contains(searchText) ||
                        empleado.getCedula().toLowerCase().contains(searchText) ||
                        empleado.getPCorreo().toLowerCase().contains(searchText);
            }

            String cargoSeleccionado = filtroCargoEmpleados.getValue();
            boolean matchesCargo = true;

            if (cargoSeleccionado != null && !cargoSeleccionado.equals("Todos")) {
                matchesCargo = empleado.getCargo().equalsIgnoreCase(cargoSeleccionado);
            }

            return matchesSearch && matchesCargo;
        });

        actualizarVistaEmpleados();
        actualizarContadorEmpleados();
    }

    private void actualizarContadorEmpleados() {
        int total = filteredEmpleados.size();
        lblContadorEmpleados.setText(total == 1 ? "1 empleado" : total + " empleados");
    }

    // ============================================
    // COMPONENTES AUXILIARES
    // ============================================

    private StackPane crearAvatar(String primerNombre, String primerApellido, String color) {
        StackPane avatar = new StackPane();
        avatar.setPrefSize(60, 60);
        avatar.setMinSize(60, 60);
        avatar.setMaxSize(60, 60);
        avatar.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-background-radius: 50%;"
        );

        String iniciales = "";
        if (primerNombre != null && !primerNombre.isEmpty()) {
            iniciales += primerNombre.charAt(0);
        }
        if (primerApellido != null && !primerApellido.isEmpty()) {
            iniciales += primerApellido.charAt(0);
        }

        Label lblIniciales = new Label(iniciales.toUpperCase());
        lblIniciales.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;"
        );

        avatar.getChildren().add(lblIniciales);
        return avatar;
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
    // ACCIONES
    // ============================================

    @FXML
    private void handleAgregarAdmin() {
        abrirModal("/grupo/proyecto_aula_carpethome/RegistrarAdministrador.fxml", "Nuevo Administrador");
    }

    @FXML
    private void handleAgregarEmpleado() {
        abrirModal("/grupo/proyecto_aula_carpethome/RegistrarEmpleado.fxml", "Nuevo Empleado");
    }

    private void editarAdministrador(Administrador admin) {
        // TODO: Implementar modal de edición
        System.out.println("Editar administrador: " + admin.getIdAdmin());
    }

    private void editarEmpleado(Empleado empleado) {
        // TODO: Implementar modal de edición
        System.out.println("Editar empleado: " + empleado.getIdEmpleado());
    }

    private void eliminarAdministrador(Administrador admin) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Estás seguro de eliminar este administrador?");
        alert.setContentText(admin.getNombreCompleto());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    administradorService.eliminarAdministrador(admin.getIdAdmin());
                    mostrarExito("Administrador eliminado correctamente");
                    cargarAdministradores();
                } catch (SQLException e) {
                    e.printStackTrace();
                    mostrarError("Error al eliminar", e.getMessage());
                }
            }
        });
    }

    private void eliminarEmpleado(Empleado empleado) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Estás seguro de eliminar este empleado?");
        alert.setContentText(empleado.getNombreCompleto());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    empleadoService.eliminarEmpleado(empleado.getIdEmpleado());
                    mostrarExito("Empleado eliminado correctamente");
                    cargarEmpleados();
                } catch (SQLException e) {
                    e.printStackTrace();
                    mostrarError("Error al eliminar", e.getMessage());
                }
            }
        });
    }

    private void abrirModal(String fxmlPath, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();

            // Pasar referencia del controlador padre
            if (controller instanceof RegistrarAdministradorController) {
                ((RegistrarAdministradorController) controller).setParentController(this);
            } else if (controller instanceof RegistrarEmpleadoController) {
                ((RegistrarEmpleadoController) controller).setParentController(this);
            }

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

            // Recargar listas después de cerrar el modal
            cargarAdministradores();
            cargarEmpleados();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudo abrir el formulario: " + e.getMessage());
        }
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