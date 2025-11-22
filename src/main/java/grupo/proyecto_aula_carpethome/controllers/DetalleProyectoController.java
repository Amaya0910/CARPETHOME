package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.config.ServiceFactory;
import grupo.proyecto_aula_carpethome.entities.Cliente;
import grupo.proyecto_aula_carpethome.entities.Proyecto;
import grupo.proyecto_aula_carpethome.services.ClienteService;
import grupo.proyecto_aula_carpethome.services.ProyectoService;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

public class DetalleProyectoController {

    @FXML private Label lblTitulo;
    @FXML private Label lblIdProyecto;

    @FXML private TextField txtNombre;
    @FXML private TextField txtCedulaCliente;
    @FXML private TextField txtNombreCliente;
    @FXML private TextField txtTelefonoCliente;
    @FXML private ComboBox<String> comboTipoProduccion;
    @FXML private ComboBox<String> comboEstado;
    @FXML private DatePicker dateFechaInicio;
    @FXML private DatePicker dateFechaEntregaEstimada;
    @FXML private DatePicker dateFechaEntregaReal;
    @FXML private TextField txtCostoEstimado;

    @FXML private Button btnHistorialEtapas;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private Button btnCerrar;

    @FXML private VBox errorContainer;
    @FXML private Label errorLabel;

    private ProyectoService proyectoService;
    private ClienteService clienteService;
    private GestionProyectosController parentController;
    private Proyecto proyectoActual;

    @FXML
    public void initialize() {
        System.out.println("DetalleProyectoController inicializado");

        // Inicializar servicios
        proyectoService = ServiceFactory.getProyectoService();
        clienteService = ServiceFactory.getClienteService();

        // Configurar ComboBox de Tipo de Producción
        comboTipoProduccion.setItems(FXCollections.observableArrayList(
                "A Medida",
                "Por Lote",
                "Reparación"
        ));

        // Configurar ComboBox de Estado
        comboEstado.setItems(FXCollections.observableArrayList(
                "Pendiente",
                "En Progreso",
                "Completado",
                "Cancelado"
        ));

        // Validación de números en costo estimado
        txtCostoEstimado.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                txtCostoEstimado.setText(oldVal);
            }
        });

        // Ocultar error al modificar campos
        txtNombre.textProperty().addListener((obs, oldVal, newVal) -> ocultarError());
        comboTipoProduccion.valueProperty().addListener((obs, oldVal, newVal) -> ocultarError());
        comboEstado.valueProperty().addListener((obs, oldVal, newVal) -> ocultarError());
        txtCostoEstimado.textProperty().addListener((obs, oldVal, newVal) -> ocultarError());
    }

    // ============================================
    // CARGAR DATOS DEL PROYECTO
    // ============================================

    public void cargarProyecto(Proyecto proyecto) {
        this.proyectoActual = proyecto;

        if (proyecto == null) {
            mostrarError("Error: Proyecto no válido");
            return;
        }

        try {
            // Actualizar título
            lblIdProyecto.setText(proyecto.getIdProyecto());

            // Datos generales
            txtNombre.setText(proyecto.getNombreProyecto());
            comboTipoProduccion.setValue(proyecto.getTipoProduccion());
            comboEstado.setValue(proyecto.getEstado());

            // Fechas
            dateFechaInicio.setValue(convertirDateALocalDate(proyecto.getFechaInicio()));
            dateFechaEntregaEstimada.setValue(convertirDateALocalDate(proyecto.getFechaEntregaEstimada()));
            dateFechaEntregaReal.setValue(convertirDateALocalDate(proyecto.getFechaEntregaReal()));

            // Costo
            txtCostoEstimado.setText(String.valueOf(proyecto.getCostoEstimado()));

            // Cargar datos del cliente
            cargarDatosCliente(proyecto.getIdCliente());

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error al cargar los datos del proyecto");
        }
    }

    private void cargarDatosCliente(String idCliente) {
        try {
            Optional<Cliente> clienteOpt = clienteService.buscarPorId(idCliente);

            if (clienteOpt.isPresent()) {
                Cliente cliente = clienteOpt.get();
                txtCedulaCliente.setText(cliente.getCedula());
                txtNombreCliente.setText(cliente.getPNombre() + " " + cliente.getPApellido());
                txtTelefonoCliente.setText(String.valueOf(cliente.getPTelefono()));
            } else {
                txtCedulaCliente.setText(idCliente);
                txtNombreCliente.setText("Cliente no encontrado");
                txtTelefonoCliente.setText("-");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            txtCedulaCliente.setText(idCliente);
            txtNombreCliente.setText("Error al cargar cliente");
            txtTelefonoCliente.setText("-");
        }
    }

    // ============================================
    // GUARDAR CAMBIOS
    // ============================================

    @FXML
    private void handleGuardar() {
        System.out.println("Guardando cambios del proyecto...");

        btnGuardar.setDisable(true);

        try {
            // Validar campos
            if (!validarCampos()) {
                btnGuardar.setDisable(false);
                return;
            }

            // Actualizar el objeto proyecto
            actualizarProyecto();

            // Guardar en la base de datos
            proyectoService.actualizarProyecto(proyectoActual);

            System.out.println("Proyecto actualizado exitosamente");

            // Mostrar mensaje de éxito
            mostrarExito("Cambios guardados exitosamente");

            // Cerrar el modal
            cerrarModal();

            // Recargar tabla DESPUÉS de cerrar el modal
            if (parentController != null) {
                parentController.cargarProyectos();
            }

        } catch (IllegalArgumentException e) {
            System.err.println("Error de validación: " + e.getMessage());
            mostrarError(e.getMessage());
            btnGuardar.setDisable(false);

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al actualizar el proyecto en la base de datos");
            btnGuardar.setDisable(false);

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error inesperado: " + e.getMessage());
            btnGuardar.setDisable(false);
        }
    }

    private void actualizarProyecto() {
        proyectoActual.setNombreProyecto(txtNombre.getText().trim());
        proyectoActual.setTipoProduccion(comboTipoProduccion.getValue());
        proyectoActual.setEstado(comboEstado.getValue());
        proyectoActual.setCostoEstimado(Double.parseDouble(txtCostoEstimado.getText().trim()));

        // Las fechas de solo lectura no se actualizan
        // El cliente no se cambia
    }

    // ============================================
    // VALIDACIÓN
    // ============================================

    private boolean validarCampos() {
        if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
            mostrarError("El nombre del proyecto es obligatorio");
            txtNombre.requestFocus();
            return false;
        }

        if (comboTipoProduccion.getValue() == null) {
            mostrarError("Debe seleccionar un tipo de producción");
            comboTipoProduccion.requestFocus();
            return false;
        }

        if (comboEstado.getValue() == null) {
            mostrarError("Debe seleccionar un estado");
            comboEstado.requestFocus();
            return false;
        }

        if (txtCostoEstimado.getText() == null || txtCostoEstimado.getText().trim().isEmpty()) {
            mostrarError("El costo estimado es obligatorio");
            txtCostoEstimado.requestFocus();
            return false;
        }

        try {
            double costo = Double.parseDouble(txtCostoEstimado.getText().trim());
            if (costo <= 0) {
                mostrarError("El costo estimado debe ser mayor a cero");
                txtCostoEstimado.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarError("El costo estimado debe ser un número válido");
            txtCostoEstimado.requestFocus();
            return false;
        }

        return true;
    }

    // ============================================
    // HISTORIAL DE ETAPAS
    // ============================================

    @FXML
    private void handleHistorialEtapas() {
        System.out.println("Abriendo historial de etapas del proyecto: " + proyectoActual.getIdProyecto());
        // TODO: Abrir modal de historial de etapas
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Historial de Etapas");
        alert.setHeaderText("Funcionalidad en desarrollo");
        alert.setContentText("El historial de etapas se implementará próximamente.");
        alert.show();
    }

    // ============================================
    // CANCELAR
    // ============================================

    @FXML
    private void handleCancelar() {
        if (hayCambios()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar cancelación");
            alert.setHeaderText("¿Descartar cambios?");
            alert.setContentText("Los cambios no guardados se perderán.");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    cerrarModal();
                }
            });
        } else {
            cerrarModal();
        }
    }

    private boolean hayCambios() {
        if (proyectoActual == null) return false;

        // Validar que los campos no estén vacíos antes de comparar
        if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) return false;
        if (comboTipoProduccion.getValue() == null) return false;
        if (comboEstado.getValue() == null) return false;
        if (txtCostoEstimado.getText() == null || txtCostoEstimado.getText().trim().isEmpty()) return false;

        try {
            return !txtNombre.getText().trim().equals(proyectoActual.getNombreProyecto()) ||
                    !comboTipoProduccion.getValue().equals(proyectoActual.getTipoProduccion()) ||
                    !comboEstado.getValue().equals(proyectoActual.getEstado()) ||
                    Double.parseDouble(txtCostoEstimado.getText().trim()) != proyectoActual.getCostoEstimado();
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // ============================================
    // MANEJO DE ERRORES Y MENSAJES
    // ============================================

    private void mostrarError(String mensaje) {
        errorLabel.setText(mensaje);
        errorContainer.setVisible(true);
        errorContainer.setManaged(true);

        errorContainer.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), errorContainer);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void ocultarError() {
        if (errorContainer.isVisible()) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), errorContainer);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                errorContainer.setVisible(false);
                errorContainer.setManaged(false);
            });
            fadeOut.play();
        }
    }

    private void mostrarExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.show();
    }

    // ============================================
    // UTILIDADES
    // ============================================

    private LocalDate convertirDateALocalDate(Date fecha) {
        if (fecha == null) return null;
        // Convertir java.sql.Date o java.util.Date a LocalDate
        if (fecha instanceof java.sql.Date) {
            return ((java.sql.Date) fecha).toLocalDate();
        }
        return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void cerrarModal() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    private void cerrarModalConDelay() {
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(e -> cerrarModal());
        delay.play();
    }

    public void setParentController(GestionProyectosController controller) {
        this.parentController = controller;
    }

    // ============================================
    // EFECTOS HOVER DE BOTONES
    // ============================================

    @FXML
    private void handleGuardarHover(MouseEvent event) {
        if (!btnGuardar.isDisabled()) {
            btnGuardar.setStyle(btnGuardar.getStyle() + "-fx-background-color: #4a433e;");
        }
    }

    @FXML
    private void handleGuardarExit(MouseEvent event) {
        if (!btnGuardar.isDisabled()) {
            btnGuardar.setStyle(btnGuardar.getStyle().replace("-fx-background-color: #4a433e;",
                    "-fx-background-color: #61564A;"));
        }
    }

    @FXML
    private void handleCancelarHover(MouseEvent event) {
        btnCancelar.setStyle(btnCancelar.getStyle() + "-fx-background-color: rgba(165, 155, 143, 0.1);");
    }

    @FXML
    private void handleCancelarExit(MouseEvent event) {
        btnCancelar.setStyle(btnCancelar.getStyle().replace("-fx-background-color: rgba(165, 155, 143, 0.1);",
                "-fx-background-color: transparent;"));
    }

    @FXML
    private void handleHistorialHover(MouseEvent event) {
        btnHistorialEtapas.setStyle(btnHistorialEtapas.getStyle() + "-fx-background-color: #8a8179;");
    }

    @FXML
    private void handleHistorialExit(MouseEvent event) {
        btnHistorialEtapas.setStyle(btnHistorialEtapas.getStyle().replace("-fx-background-color: #8a8179;",
                "-fx-background-color: #A59B8F;"));
    }
}