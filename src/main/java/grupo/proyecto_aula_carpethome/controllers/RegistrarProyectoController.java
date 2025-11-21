package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.config.ServiceFactory;
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

public class RegistrarProyectoController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtCedulaCliente;
    @FXML private ComboBox<String> comboTipoProduccion;
    @FXML private DatePicker dateFechaInicio;
    @FXML private DatePicker dateFechaEntregaEstimada;
    @FXML private TextField txtCostoEstimado;

    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private Button btnCerrar;

    @FXML private VBox errorContainer;
    @FXML private Label errorLabel;

    private ProyectoService proyectoService;
    private GestionProyectosController parentController;
    private ClienteService clienteService;

    @FXML
    public void initialize() {
        System.out.println("ModalProyectoController inicializado");

        // Inicializar servicio
        proyectoService = ServiceFactory.getProyectoService();
        clienteService = ServiceFactory.getClienteService();

        // Configurar ComboBox de Tipo de Producción
        comboTipoProduccion.setItems(FXCollections.observableArrayList(
                "A Medida",
                "Por Lote",
                "Reparación"
        ));

        // Configurar fecha de inicio con la fecha actual por defecto
        dateFechaInicio.setValue(LocalDate.now());

        // Validación de números en costo estimado
        txtCostoEstimado.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                txtCostoEstimado.setText(oldVal);
            }
        });

        // Validación de números en cédula
        txtCedulaCliente.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtCedulaCliente.setText(oldVal);
            }
        });

        // Ocultar error al modificar campos
        txtNombre.textProperty().addListener((obs, oldVal, newVal) -> ocultarError());
        txtCedulaCliente.textProperty().addListener((obs, oldVal, newVal) -> ocultarError());
        comboTipoProduccion.valueProperty().addListener((obs, oldVal, newVal) -> ocultarError());
        dateFechaInicio.valueProperty().addListener((obs, oldVal, newVal) -> ocultarError());
        dateFechaEntregaEstimada.valueProperty().addListener((obs, oldVal, newVal) -> ocultarError());
        txtCostoEstimado.textProperty().addListener((obs, oldVal, newVal) -> ocultarError());
    }

    // ============================================
    // MÉTODO PARA GUARDAR PROYECTO
    // ============================================

    @FXML
    private void handleGuardar() {
        System.out.println("Intentando guardar proyecto...");

        // Deshabilitar botón para evitar múltiples clics
        btnGuardar.setDisable(true);

        try {
            // 1. Validar campos vacíos
            if (!validarCampos()) {
                btnGuardar.setDisable(false);
                return;
            }

            // 2. Crear objeto Proyecto
            Proyecto nuevoProyecto = construirProyecto();

            // 3. Registrar en la base de datos
            Proyecto proyectoGuardado = proyectoService.registrarProyecto(nuevoProyecto);

            System.out.println("Proyecto guardado exitosamente: " + proyectoGuardado.getIdProyecto());

            // 4. Recargar tabla en la vista padre
            if (parentController != null) {
                parentController.cargarProyectos();
            }

            // 5. Mostrar mensaje de éxito
            mostrarExito("Proyecto creado exitosamente");

            // 6. Cerrar el modal después de un breve delay
            cerrarModalConDelay();

        } catch (IllegalArgumentException e) {
            // Errores de validación del servicio
            System.err.println("Error de validación: " + e.getMessage());
            mostrarError(e.getMessage());
            btnGuardar.setDisable(false);

        } catch (SQLException e) {
            // Errores de base de datos
            e.printStackTrace();
            mostrarError("Error al guardar el proyecto en la base de datos");
            btnGuardar.setDisable(false);

        } catch (Exception e) {
            // Otros errores
            e.printStackTrace();
            mostrarError("Error inesperado: " + e.getMessage());
            btnGuardar.setDisable(false);
        }
    }

    // ============================================
    // VALIDACIÓN DE CAMPOS
    // ============================================

    private boolean validarCampos() {
        // Nombre del proyecto
        if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
            mostrarError("El nombre del proyecto es obligatorio");
            txtNombre.requestFocus();
            return false;
        }

        if (txtNombre.getText().trim().length() > 100) {
            mostrarError("El nombre del proyecto no puede exceder 100 caracteres");
            txtNombre.requestFocus();
            return false;
        }

        // Cédula del cliente
        if (txtCedulaCliente.getText() == null || txtCedulaCliente.getText().trim().isEmpty()) {
            mostrarError("La cédula del cliente es obligatoria");
            txtCedulaCliente.requestFocus();
            return false;
        }

        // Tipo de producción
        if (comboTipoProduccion.getValue() == null) {
            mostrarError("Debe seleccionar un tipo de producción");
            comboTipoProduccion.requestFocus();
            return false;
        }

        // Fecha de inicio
        if (dateFechaInicio.getValue() == null) {
            mostrarError("La fecha de inicio es obligatoria");
            dateFechaInicio.requestFocus();
            return false;
        }

        // Fecha de entrega estimada
        if (dateFechaEntregaEstimada.getValue() == null) {
            mostrarError("La fecha de entrega estimada es obligatoria");
            dateFechaEntregaEstimada.requestFocus();
            return false;
        }

        // Validar que la fecha de entrega sea posterior a la de inicio
        if (dateFechaEntregaEstimada.getValue().isBefore(dateFechaInicio.getValue())) {
            mostrarError("La fecha de entrega debe ser posterior a la fecha de inicio");
            dateFechaEntregaEstimada.requestFocus();
            return false;
        }

        // Costo estimado
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
    // CONSTRUIR OBJETO PROYECTO
    // ============================================

    private Proyecto construirProyecto() {
        Proyecto proyecto = new Proyecto();

        // El ID se genera automáticamente en la BD
        proyecto.setIdProyecto(null);

        // Datos del formulario
        proyecto.setNombreProyecto(txtNombre.getText().trim());
        try {
            proyecto.setIdCliente(clienteService.obtenerIdClientePorCedula(
                    txtCedulaCliente.getText().trim()
            ));
        } catch (SQLException e) {
            // Aquí puedes mostrar un alert o manejar el error como quieras
            e.printStackTrace();
            return null; // o lanza una excepción personalizada
        }
        proyecto.setTipoProduccion(comboTipoProduccion.getValue().toUpperCase());
        proyecto.setEstado("Pendiente"); // Estado inicial

        // Convertir LocalDate a Date
        proyecto.setFechaInicio(convertirLocalDateADate(dateFechaInicio.getValue()));
        proyecto.setFechaEntregaEstimada(convertirLocalDateADate(dateFechaEntregaEstimada.getValue()));
        proyecto.setFechaEntregaReal(null); // No se establece al crear

        // Costo
        proyecto.setCostoEstimado(Double.parseDouble(txtCostoEstimado.getText().trim()));

        return proyecto;
    }

    // ============================================
    // MÉTODO PARA CANCELAR
    // ============================================

    @FXML
    private void handleCancelar() {
        // Confirmar si hay datos en el formulario
        if (hayDatosEnFormulario()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar cancelación");
            alert.setHeaderText("¿Descartar cambios?");
            alert.setContentText("Los datos ingresados se perderán.");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    cerrarModal();
                }
            });
        } else {
            cerrarModal();
        }
    }

    private boolean hayDatosEnFormulario() {
        return (txtNombre.getText() != null && !txtNombre.getText().trim().isEmpty()) ||
                (txtCedulaCliente.getText() != null && !txtCedulaCliente.getText().trim().isEmpty()) ||
                (comboTipoProduccion.getValue() != null) ||
                (txtCostoEstimado.getText() != null && !txtCostoEstimado.getText().trim().isEmpty());
    }

    // ============================================
    // MANEJO DE ERRORES Y MENSAJES
    // ============================================

    private void mostrarError(String mensaje) {
        errorLabel.setText(mensaje);
        errorContainer.setVisible(true);
        errorContainer.setManaged(true);

        // Animación de aparición
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

    private Date convertirLocalDateADate(LocalDate localDate) {
        if (localDate == null) return null;
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
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
}