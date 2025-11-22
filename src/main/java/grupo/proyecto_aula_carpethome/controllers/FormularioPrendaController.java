package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.entities.Medida;
import grupo.proyecto_aula_carpethome.entities.Prenda;
import grupo.proyecto_aula_carpethome.entities.Proyecto;
import grupo.proyecto_aula_carpethome.services.MedidaService;
import grupo.proyecto_aula_carpethome.services.PrendaService;
import grupo.proyecto_aula_carpethome.services.ServiceFactory;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

public class FormularioPrendaController {

    // ==================== SERVICIOS ====================
    private final PrendaService prendaService = ServiceFactory.getPrendaService();
    private final MedidaService medidaService = ServiceFactory.getMedidaService();

    // ==================== COMPONENTES FXML ====================
    @FXML private Label lblTitulo;
    @FXML private Label errorLabel;
    @FXML private VBox errorContainer;

    // Datos de la Prenda
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtCostoTotal;

    // Opciones de Medida
    @FXML private RadioButton radioMedidaEstandar;
    @FXML private RadioButton radioMedidaPersonalizada;
    @FXML private ToggleGroup grupoMedidas;
    @FXML private ComboBox<Medida> comboMedidasEstandar;
    @FXML private VBox formularioMedidas;

    // Formulario de Medidas Personalizadas
    @FXML private TextField txtNombreMedida;
    @FXML private TextField txtCBusto;
    @FXML private TextField txtCCintura;
    @FXML private TextField txtCCadera;
    @FXML private TextField txtAlturaBusto;
    @FXML private TextField txtSeparacionBusto;
    @FXML private TextField txtRadioBusto;
    @FXML private TextField txtBajoBusto;
    @FXML private TextField txtLargoFalda;
    @FXML private TextField txtLargoCadera;
    @FXML private TextField txtLargoVestido;
    @FXML private TextField txtLargoPantalon;
    @FXML private TextField txtLargoManga;

    // Botones
    @FXML private Button btnCancelar;
    @FXML private Button btnGuardar;
    @FXML private Button btnCerrar;

    // ==================== VARIABLES DE INSTANCIA ====================
    private Proyecto proyectoActual;
    private Prenda prendaEditar; // null = modo agregar, != null = modo editar
    private Consumer<Boolean> onGuardar;
    private Stage stage;
    private GestionPrendasController parentController;

    // ==================== INICIALIZACIÓN ====================
    @FXML
    private void initialize() {
        configurarValidaciones();
        configurarOpcionesMedidas();
        cargarMedidasEstandar();
    }

    /**
     * Configura las validaciones de campos numéricos
     */
    private void configurarValidaciones() {
        // Solo números decimales en costo total
        txtCostoTotal.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                txtCostoTotal.setText(oldVal);
            }
        });

        // Solo números decimales en todos los campos de medidas
        TextField[] camposMedidas = {
                txtCBusto, txtCCintura, txtCCadera, txtAlturaBusto,
                txtSeparacionBusto, txtRadioBusto, txtBajoBusto,
                txtLargoFalda, txtLargoCadera, txtLargoVestido,
                txtLargoPantalon, txtLargoManga
        };

        for (TextField campo : camposMedidas) {
            campo.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("\\d*\\.?\\d*")) {
                    campo.setText(oldVal);
                }
            });
        }
    }

    /**
     * Configura el comportamiento de las opciones de medida
     */
    private void configurarOpcionesMedidas() {
        // Listener para mostrar/ocultar el formulario de medidas
        grupoMedidas.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == radioMedidaEstandar) {
                formularioMedidas.setVisible(false);
                formularioMedidas.setManaged(false);
                comboMedidasEstandar.setDisable(false);
            } else if (newToggle == radioMedidaPersonalizada) {
                formularioMedidas.setVisible(true);
                formularioMedidas.setManaged(true);
                comboMedidasEstandar.setDisable(true);
                comboMedidasEstandar.setValue(null);
            }
        });

        // Configurar el ComboBox para mostrar el nombre de la medida
        comboMedidasEstandar.setConverter(new StringConverter<Medida>() {
            @Override
            public String toString(Medida medida) {
                return medida == null ? "" : medida.getNombreMedida();
            }

            @Override
            public Medida fromString(String string) {
                return null; // No se usa
            }
        });
    }

    /**
     * Carga las medidas estándar desde la BD
     */
    private void cargarMedidasEstandar() {
        try {
            List<Medida> medidasEstandar = medidaService.listarTodos().stream()
                    .filter(m -> "Estándar".equalsIgnoreCase(m.getTipoMedida()))
                    .toList();

            comboMedidasEstandar.getItems().setAll(medidasEstandar);

            // Si no hay medidas estándar, sugerir crear una personalizada
            if (medidasEstandar.isEmpty()) {
                radioMedidaPersonalizada.setSelected(true);
            }

        } catch (SQLException e) {
            mostrarError("Error al cargar medidas estándar: " + e.getMessage());
            radioMedidaPersonalizada.setSelected(true);
        }
    }

    // ==================== MÉTODOS PÚBLICOS ====================

    /**
     * Establece el controlador padre (usado por GestionPrendasController)
     */
    public void setParentController(GestionPrendasController controller) {
        this.parentController = controller;
    }

    /**
     * Establece el proyecto actual
     */
    public void setProyecto(Proyecto proyecto) {
        this.proyectoActual = proyecto;
        lblTitulo.setText("Nueva Prenda - " + proyecto.getNombreProyecto());
        btnGuardar.setText("Guardar Prenda");
        limpiarFormulario();
    }

    /**
     * Carga una prenda para editar
     */
    public void cargarPrenda(Prenda prenda) {
        this.prendaEditar = prenda;
        lblTitulo.setText("Editar Prenda - " + proyectoActual.getNombreProyecto());
        btnGuardar.setText("Guardar Cambios");
        cargarDatosPrenda(prenda);
    }

    /**
     * Inicializa el modal en modo AGREGAR
     */
    public void inicializarAgregar(Proyecto proyecto, Consumer<Boolean> callback) {
        this.proyectoActual = proyecto;
        this.prendaEditar = null;
        this.onGuardar = callback;

        lblTitulo.setText("Nueva Prenda - " + proyecto.getNombreProyecto());
        btnGuardar.setText("Guardar Prenda");

        limpiarFormulario();
    }

    /**
     * Inicializa el modal en modo EDITAR
     */
    public void inicializarEditar(Prenda prenda, Proyecto proyecto, Consumer<Boolean> callback) {
        this.proyectoActual = proyecto;
        this.prendaEditar = prenda;
        this.onGuardar = callback;

        lblTitulo.setText("Editar Prenda - " + proyecto.getNombreProyecto());
        btnGuardar.setText("Guardar Cambios");

        cargarDatosPrenda(prenda);
    }

    /**
     * Establece el Stage del modal
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // ==================== CARGAR DATOS ====================

    /**
     * Carga los datos de una prenda existente para editar
     */
    private void cargarDatosPrenda(Prenda prenda) {
        // Datos básicos de la prenda
        txtNombre.setText(prenda.getNombrePrenda());
        txtDescripcion.setText(prenda.getDescripcionPrenda());
        txtCostoTotal.setText(String.valueOf(prenda.getCostoTotalEstimado()));

        // Cargar la medida asociada
        try {
            var medidaOpt = medidaService.buscarPorId(prenda.getIdMedida());

            if (medidaOpt.isPresent()) {
                Medida medida = medidaOpt.get();

                // Verificar si es estándar o personalizada
                if ("Estándar".equalsIgnoreCase(medida.getTipoMedida())) {
                    radioMedidaEstandar.setSelected(true);
                    comboMedidasEstandar.setValue(medida);
                } else {
                    radioMedidaPersonalizada.setSelected(true);
                    cargarDatosMedida(medida);
                }
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar la medida asociada: " + e.getMessage());
        }
    }

    /**
     * Carga los datos de una medida en el formulario
     */
    private void cargarDatosMedida(Medida medida) {
        txtNombreMedida.setText(medida.getNombreMedida());
        txtCBusto.setText(formatearDecimal(medida.getCBusto()));
        txtCCintura.setText(formatearDecimal(medida.getCCintura()));
        txtCCadera.setText(formatearDecimal(medida.getCCadera()));
        txtAlturaBusto.setText(formatearDecimal(medida.getAlturaBusto()));
        txtSeparacionBusto.setText(formatearDecimal(medida.getSeparacionBusto()));
        txtRadioBusto.setText(formatearDecimal(medida.getRadioBusto()));
        txtBajoBusto.setText(formatearDecimal(medida.getBajoBusto()));
        txtLargoFalda.setText(formatearDecimal(medida.getLargoFalda()));
        txtLargoCadera.setText(formatearDecimal(medida.getLargoCadera()));
        txtLargoVestido.setText(formatearDecimal(medida.getLargoVestido()));
        txtLargoPantalon.setText(formatearDecimal(medida.getLargoPantalon()));
        txtLargoManga.setText(formatearDecimal(medida.getLargoManga()));
    }

    private String formatearDecimal(double valor) {
        return valor == 0.0 ? "" : String.valueOf(valor);
    }

    // ==================== VALIDACIONES ====================

    /**
     * Valida el formulario completo
     */
    private boolean validarFormulario() {
        StringBuilder errores = new StringBuilder();

        // Validar nombre
        if (txtNombre.getText().trim().isEmpty()) {
            errores.append("• El nombre de la prenda es obligatorio\n");
        }

        // Validar costo total
        if (txtCostoTotal.getText().trim().isEmpty()) {
            errores.append("• El costo total estimado es obligatorio\n");
        } else {
            try {
                double costo = Double.parseDouble(txtCostoTotal.getText().trim());
                if (costo <= 0) {
                    errores.append("• El costo total debe ser mayor a 0\n");
                }
            } catch (NumberFormatException e) {
                errores.append("• El costo total debe ser un número válido\n");
            }
        }

        // Validar medida
        if (radioMedidaEstandar.isSelected()) {
            if (comboMedidasEstandar.getValue() == null) {
                errores.append("• Debe seleccionar una medida estándar\n");
            }
        } else if (radioMedidaPersonalizada.isSelected()) {
            if (txtNombreMedida.getText().trim().isEmpty()) {
                errores.append("• El nombre de la medida es obligatorio\n");
            }
        }

        if (errores.length() > 0) {
            mostrarError(errores.toString());
            return false;
        }

        return true;
    }

    // ==================== ACCIONES ====================

    /**
     * Guarda la prenda (agregar o editar)
     */
    @FXML
    private void handleGuardar() {
        if (!validarFormulario()) {
            return;
        }

        ocultarError();
        btnGuardar.setDisable(true);

        try {
            // 1. Obtener o crear la medida
            Medida medida = obtenerMedida();

            // 2. Crear o actualizar la prenda
            Prenda prenda = construirPrenda(medida);

            // 3. Guardar en la BD
            if (prendaEditar == null) {
                // MODO AGREGAR
                prendaService.registrarPrenda(prenda);
                mostrarExito("Prenda registrada exitosamente");
            } else {
                // MODO EDITAR
                prenda.setIdPrenda(prendaEditar.getIdPrenda());
                prendaService.actualizarPrenda(prenda);
                mostrarExito("Prenda actualizada exitosamente");
            }

            // 4. Notificar al callback y cerrar
            if (onGuardar != null) {
                onGuardar.accept(true);
            }

            // Si hay un parentController, notificarlo también
            if (parentController != null) {
                parentController.cargarPrendas();
            }

            cerrarModal();

        } catch (SQLException e) {
            mostrarError("Error al guardar la prenda: " + e.getMessage());
            btnGuardar.setDisable(false);
        } catch (Exception e) {
            mostrarError("Error inesperado: " + e.getMessage());
            btnGuardar.setDisable(false);
        }
    }

    /**
     * Obtiene o crea la medida según la opción seleccionada
     */
    private Medida obtenerMedida() throws SQLException {
        if (radioMedidaEstandar.isSelected()) {
            // Usar medida estándar existente
            return comboMedidasEstandar.getValue();
        } else {
            // Crear nueva medida personalizada
            Medida nuevaMedida = construirMedida();

            // Si estamos editando y la medida anterior era personalizada, actualizarla
            if (prendaEditar != null) {
                var medidaAnteriorOpt = medidaService.buscarPorId(prendaEditar.getIdMedida());
                if (medidaAnteriorOpt.isPresent() &&
                        "Personalizada".equalsIgnoreCase(medidaAnteriorOpt.get().getTipoMedida())) {
                    nuevaMedida.setIdMedida(prendaEditar.getIdMedida());
                    medidaService.actualizarMedida(nuevaMedida);
                    return nuevaMedida;
                }
            }

            // Registrar nueva medida
            return medidaService.registrarMedida(nuevaMedida);
        }
    }

    /**
     * Construye el objeto Medida desde el formulario
     */
    private Medida construirMedida() {
        Medida medida = new Medida();
        medida.setNombreMedida(txtNombreMedida.getText().trim());
        medida.setTipoMedida("Personalizada");

        medida.setCBusto(parsearDecimal(txtCBusto.getText()));
        medida.setCCintura(parsearDecimal(txtCCintura.getText()));
        medida.setCCadera(parsearDecimal(txtCCadera.getText()));
        medida.setAlturaBusto(parsearDecimal(txtAlturaBusto.getText()));
        medida.setSeparacionBusto(parsearDecimal(txtSeparacionBusto.getText()));
        medida.setRadioBusto(parsearDecimal(txtRadioBusto.getText()));
        medida.setBajoBusto(parsearDecimal(txtBajoBusto.getText()));
        medida.setLargoFalda(parsearDecimal(txtLargoFalda.getText()));
        medida.setLargoCadera(parsearDecimal(txtLargoCadera.getText()));
        medida.setLargoVestido(parsearDecimal(txtLargoVestido.getText()));
        medida.setLargoPantalon(parsearDecimal(txtLargoPantalon.getText()));
        medida.setLargoManga(parsearDecimal(txtLargoManga.getText()));

        return medida;
    }

    /**
     * Construye el objeto Prenda desde el formulario
     */
    private Prenda construirPrenda(Medida medida) {
        Prenda prenda = new Prenda();
        prenda.setNombrePrenda(txtNombre.getText().trim());
        prenda.setDescripcionPrenda(txtDescripcion.getText().trim());
        prenda.setCostoTotalEstimado(Double.parseDouble(txtCostoTotal.getText().trim()));
        prenda.setCostoMateriales(0.0); // Se actualiza automáticamente
        prenda.setIdProyecto(proyectoActual.getIdProyecto());
        prenda.setIdMedida(medida.getIdMedida());

        return prenda;
    }

    private double parsearDecimal(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(texto.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Cancela la operación
     */
    @FXML
    private void handleCancelar() {
        if (confirmarCancelacion()) {
            cerrarModal();
        }
    }

    private boolean confirmarCancelacion() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Cancelación");
        alert.setHeaderText("¿Está seguro de cancelar?");
        alert.setContentText("Los cambios no guardados se perderán.");

        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    // ==================== UI HELPERS ====================

    private void mostrarError(String mensaje) {
        errorLabel.setText(mensaje);
        errorContainer.setVisible(true);
        errorContainer.setManaged(true);
    }

    private void ocultarError() {
        errorContainer.setVisible(false);
        errorContainer.setManaged(false);
    }

    private void mostrarExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void limpiarFormulario() {
        txtNombre.clear();
        txtDescripcion.clear();
        txtCostoTotal.clear();

        radioMedidaEstandar.setSelected(true);
        comboMedidasEstandar.setValue(null);

        txtNombreMedida.clear();
        txtCBusto.clear();
        txtCCintura.clear();
        txtCCadera.clear();
        txtAlturaBusto.clear();
        txtSeparacionBusto.clear();
        txtRadioBusto.clear();
        txtBajoBusto.clear();
        txtLargoFalda.clear();
        txtLargoCadera.clear();
        txtLargoVestido.clear();
        txtLargoPantalon.clear();
        txtLargoManga.clear();

        ocultarError();
    }

    private void cerrarModal() {
        if (stage != null) {
            stage.close();
        } else {
            // Buscar el stage desde cualquier nodo
            Stage currentStage = (Stage) btnCerrar.getScene().getWindow();
            if (currentStage != null) {
                currentStage.close();
            }
        }
    }

    // ==================== EFECTOS HOVER ====================

    @FXML
    private void handleCancelarHover() {
        btnCancelar.setStyle(btnCancelar.getStyle() + "-fx-background-color: #F5F5F5;");
    }

    @FXML
    private void handleCancelarExit() {
        btnCancelar.setStyle(btnCancelar.getStyle().replace("-fx-background-color: #F5F5F5;", ""));
    }

    @FXML
    private void handleGuardarHover() {
        btnGuardar.setStyle(btnGuardar.getStyle() + "-fx-background-color: #4A4037;");
    }

    @FXML
    private void handleGuardarExit() {
        btnGuardar.setStyle(btnGuardar.getStyle().replace("-fx-background-color: #4A4037;", ""));
    }
}