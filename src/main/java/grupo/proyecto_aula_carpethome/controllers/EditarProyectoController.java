package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.entities.Proyecto;
import grupo.proyecto_aula_carpethome.services.ProyectoService;
import grupo.proyecto_aula_carpethome.services.ServiceFactory;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class EditarProyectoController {

    @FXML private Label lblIdProyecto;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<String> cmbTipoProduccion;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private DatePicker dpFechaInicio;
    @FXML private DatePicker dpEntregaEstimada;
    @FXML private DatePicker dpEntregaReal;
    @FXML private TextField txtCostoEstimado;
    @FXML private TextField txtCliente;
    @FXML private Button btnGuardar;
    @FXML private Button btnCerrar;

    private ProyectoService proyectoService;
    private GestionProyectosController parentController;
    private Proyecto proyectoActual;

    @FXML
    public void initialize() {
        System.out.println("EditarProyectoController inicializado");
        proyectoService = ServiceFactory.getProyectoService();
        configurarComboBoxes();
        configurarValidaciones();
    }

    // ============================================
    // CONFIGURACIÓN INICIAL
    // ============================================

    private void configurarComboBoxes() {
        // Tipos de Producción
        cmbTipoProduccion.setItems(FXCollections.observableArrayList(
                "En Serie",
                "Personalizado",
                "A Medida",
                "Prototipo",
                "Colección Especial"
        ));

        // Estados
        cmbEstado.setItems(FXCollections.observableArrayList(
                "Pendiente",
                "En Progreso",
                "Completado",
                "Cancelado"
        ));
    }

    private void configurarValidaciones() {
        // Validación numérica para costo estimado
        txtCostoEstimado.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                txtCostoEstimado.setText(oldVal);
            }
        });

        // Validación de fechas
        dpEntregaEstimada.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dpFechaInicio.getValue() != null) {
                if (newVal.isBefore(dpFechaInicio.getValue())) {
                    mostrarError("Error de validación",
                            "La fecha de entrega real no puede ser anterior a la fecha de inicio");
                    dpEntregaReal.setValue(oldVal);
                }
            }
        });
    }

    // ============================================
    // SETTERS PÚBLICOS
    // ============================================

    public void setParentController(GestionProyectosController controller) {
        this.parentController = controller;
    }

    // ============================================
    // CARGAR DATOS DEL PROYECTO
    // ============================================

    public void cargarProyecto(Proyecto proyecto) {
        if (proyecto == null) {
            mostrarError("Error", "No se pudo cargar el proyecto");
            cerrar();
            return;
        }

        this.proyectoActual = proyecto;

        // Cargar datos básicos
        lblIdProyecto.setText("ID: " + proyecto.getIdProyecto());
        txtNombre.setText(proyecto.getNombreProyecto());
        cmbTipoProduccion.setValue(proyecto.getTipoProduccion());
        cmbEstado.setValue(proyecto.getEstado());

        // Cargar fechas
        if (proyecto.getFechaInicio() != null) {
            dpFechaInicio.setValue(convertirDateALocalDate(proyecto.getFechaInicio()));
        }
        if (proyecto.getFechaEntregaEstimada() != null) {
            dpEntregaEstimada.setValue(convertirDateALocalDate(proyecto.getFechaEntregaEstimada()));
        }
        if (proyecto.getFechaEntregaReal() != null) {
            dpEntregaReal.setValue(convertirDateALocalDate(proyecto.getFechaEntregaReal()));
        }

        // Cargar información financiera y cliente
        txtCostoEstimado.setText(String.valueOf(proyecto.getCostoEstimado()));
        txtCliente.setText(proyecto.getIdCliente());

        // Deshabilitar campos no editables
        dpFechaInicio.setDisable(true);
        txtCliente.setDisable(true);

        System.out.println("Proyecto cargado para edición: " + proyecto.getIdProyecto());
    }

    // ============================================
    // VALIDACIÓN Y GUARDADO
    // ============================================

    @FXML
    private void handleGuardar() {
        System.out.println("Intentando guardar cambios del proyecto");

        if (!validarFormulario()) {
            return;
        }

        try {
            // Actualizar datos del proyecto
            actualizarDatosProyecto();

            // Guardar en la base de datos
            proyectoService.actualizarProyecto(proyectoActual);

            System.out.println("✓ Proyecto actualizado exitosamente");
            mostrarExito("El proyecto se ha actualizado correctamente");

            // Cerrar modal
            cerrar();

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al actualizar",
                    "No se pudo actualizar el proyecto: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error inesperado",
                    "Ocurrió un error inesperado: " + e.getMessage());
        }
    }

    private boolean validarFormulario() {
        StringBuilder errores = new StringBuilder();

        // Validar nombre
        if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
            errores.append("• El nombre del proyecto es obligatorio\n");
        }

        // Validar tipo de producción
        if (cmbTipoProduccion.getValue() == null) {
            errores.append("• Debe seleccionar un tipo de producción\n");
        }

        // Validar estado
        if (cmbEstado.getValue() == null) {
            errores.append("• Debe seleccionar un estado\n");
        }

        // Validar fecha de entrega estimada
        if (dpEntregaEstimada.getValue() == null) {
            errores.append("• La fecha de entrega estimada es obligatoria\n");
        }

        // Validar costo estimado
        if (txtCostoEstimado.getText() == null || txtCostoEstimado.getText().trim().isEmpty()) {
            errores.append("• El costo estimado es obligatorio\n");
        } else {
            try {
                double costo = Double.parseDouble(txtCostoEstimado.getText());
                if (costo < 0) {
                    errores.append("• El costo estimado no puede ser negativo\n");
                }
            } catch (NumberFormatException e) {
                errores.append("• El costo estimado debe ser un número válido\n");
            }
        }

        // Validar coherencia de fechas
        if (dpEntregaEstimada.getValue() != null && dpFechaInicio.getValue() != null) {
            if (dpEntregaEstimada.getValue().isBefore(dpFechaInicio.getValue())) {
                errores.append("• La fecha de entrega estimada debe ser posterior a la fecha de inicio\n");
            }
        }

        if (dpEntregaReal.getValue() != null && dpFechaInicio.getValue() != null) {
            if (dpEntregaReal.getValue().isBefore(dpFechaInicio.getValue())) {
                errores.append("• La fecha de entrega real debe ser posterior a la fecha de inicio\n");
            }
        }

        // Validar estado completado con fecha real
        if ("Completado".equals(cmbEstado.getValue()) && dpEntregaReal.getValue() == null) {
            errores.append("• Si el estado es 'Completado', debe especificar la fecha de entrega real\n");
        }

        if (errores.length() > 0) {
            mostrarError("Errores de validación", errores.toString());
            return false;
        }

        return true;
    }

    private void actualizarDatosProyecto() {
        proyectoActual.setNombreProyecto(txtNombre.getText().trim());
        proyectoActual.setTipoProduccion(cmbTipoProduccion.getValue());
        proyectoActual.setEstado(cmbEstado.getValue());

        // Actualizar fechas (fecha inicio no se modifica)
        proyectoActual.setFechaEntregaEstimada(
                convertirLocalDateADate(dpEntregaEstimada.getValue())
        );

        if (dpEntregaReal.getValue() != null) {
            proyectoActual.setFechaEntregaReal(
                    convertirLocalDateADate(dpEntregaReal.getValue())
            );
        } else {
            proyectoActual.setFechaEntregaReal(null);
        }

        // Actualizar costo estimado
        try {
            double costo = Double.parseDouble(txtCostoEstimado.getText());
            proyectoActual.setCostoEstimado(costo);
        } catch (NumberFormatException e) {
            proyectoActual.setCostoEstimado(0.0);
        }

        // El cliente no se modifica
    }

    // ============================================
    // CONVERSIÓN DE FECHAS
    // ============================================

    private LocalDate convertirDateALocalDate(Date fecha) {
        return fecha.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private Date convertirLocalDateADate(LocalDate localDate) {
        if (localDate == null) return null;
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    // ============================================
    // CERRAR MODAL
    // ============================================

    @FXML
    private void handleCerrar() {
        cerrar();
    }

    private void cerrar() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
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

    private void mostrarAdvertencia(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}