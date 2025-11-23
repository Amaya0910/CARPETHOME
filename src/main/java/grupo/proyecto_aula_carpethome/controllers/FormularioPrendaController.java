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
import java.util.Optional;
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

    private void configurarValidaciones() {
        // Solo números decimales en costo total
        txtCostoTotal.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                txtCostoTotal.setText(oldVal);
            }
        });

        // Solo números decimales en campos de medidas
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

        // Configurar el ComboBox
        comboMedidasEstandar.setConverter(new StringConverter<Medida>() {
            @Override
            public String toString(Medida medida) {
                if (medida == null) return "";
                return String.format("%s (Busto: %.0f, Cintura: %.0f, Cadera: %.0f)",
                        medida.getNombreMedida(),
                        medida.getCBusto(),
                        medida.getCCintura(),
                        medida.getCCadera()
                );
            }

            @Override
            public Medida fromString(String string) {
                return null;
            }
        });
    }

    private void cargarMedidasEstandar() {
        try {
            // ✅ Obtener medidas estándar y crear lista MUTABLE
            List<Medida> medidasEstandar = new java.util.ArrayList<>(
                    medidaService.listarMedidasEstandar()
            );

            // Ordenar las tallas
            medidasEstandar.sort((m1, m2) -> {
                String[] orden = {"XXS", "XS", "S", "M", "L", "XL", "XXL"};
                int idx1 = java.util.Arrays.asList(orden).indexOf(m1.getNombreMedida());
                int idx2 = java.util.Arrays.asList(orden).indexOf(m2.getNombreMedida());
                return Integer.compare(idx1, idx2);
            });

            comboMedidasEstandar.getItems().setAll(medidasEstandar);

            System.out.println("✓ Medidas estándar cargadas: " + medidasEstandar.size());

            if (medidasEstandar.isEmpty()) {
                System.out.println("⚠️ No hay medidas estándar, cambiando a personalizada");
                radioMedidaPersonalizada.setSelected(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al cargar medidas estándar: " + e.getMessage());
            radioMedidaPersonalizada.setSelected(true);
        }
    }

    // ==================== MÉTODOS PÚBLICOS ====================

    public void setParentController(GestionPrendasController controller) {
        this.parentController = controller;
    }

    public void setProyecto(Proyecto proyecto) {
        this.proyectoActual = proyecto;
        lblTitulo.setText("Nueva Prenda - " + proyecto.getNombreProyecto());
        btnGuardar.setText("Guardar Prenda");
        limpiarFormulario();
    }

    public void cargarPrenda(Prenda prenda) {
        this.prendaEditar = prenda;
        lblTitulo.setText("Editar Prenda - " + proyectoActual.getNombreProyecto());
        btnGuardar.setText("Guardar Cambios");
        cargarDatosPrenda(prenda);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // ==================== CARGAR DATOS ====================

    private void cargarDatosPrenda(Prenda prenda) {
        System.out.println("=== Cargando prenda para editar ===");
        System.out.println("ID Prenda: " + prenda.getIdPrenda());
        System.out.println("ID Medida: " + prenda.getIdMedida());

        // Datos básicos
        txtNombre.setText(prenda.getNombrePrenda());
        txtDescripcion.setText(prenda.getDescripcionPrenda());
        txtCostoTotal.setText(String.valueOf(prenda.getCostoTotalEstimado()));

        // Cargar medida
        if (prenda.getIdMedida() != null && !prenda.getIdMedida().isEmpty()) {
            try {
                Optional<Medida> medidaOpt = medidaService.buscarPorId(prenda.getIdMedida());

                if (medidaOpt.isPresent()) {
                    Medida medida = medidaOpt.get();
                    System.out.println("Medida encontrada: " + medida.getNombreMedida());
                    System.out.println("Tipo: " + medida.getTipoMedida());

                    // ✅ Usar equalsIgnoreCase para comparar tipos
                    if (MedidaService.TIPO_ESTANDAR.equalsIgnoreCase(medida.getTipoMedida())) {
                        System.out.println("→ Es medida ESTÁNDAR");

                        // Seleccionar radio button PRIMERO
                        radioMedidaEstandar.setSelected(true);

                        // Buscar en el ComboBox
                        for (Medida m : comboMedidasEstandar.getItems()) {
                            if (m.getIdMedida().equals(medida.getIdMedida())) {
                                comboMedidasEstandar.setValue(m);
                                System.out.println("✓ Medida seleccionada en combo");
                                break;
                            }
                        }

                    } else if (MedidaService.TIPO_PERSONALIZADA.equalsIgnoreCase(medida.getTipoMedida())) {
                        System.out.println("→ Es medida PERSONALIZADA");

                        // Seleccionar radio button
                        radioMedidaPersonalizada.setSelected(true);

                        // Cargar datos en formulario
                        cargarDatosMedida(medida);
                        System.out.println("✓ Datos de medida cargados");
                    }
                } else {
                    System.out.println("⚠️ Medida no encontrada");
                    mostrarError("Advertencia: No se encontró la medida asociada");
                    radioMedidaPersonalizada.setSelected(true);
                }

            } catch (SQLException e) {
                e.printStackTrace();
                mostrarError("Error al cargar medida: " + e.getMessage());
                radioMedidaPersonalizada.setSelected(true);
            }
        } else {
            System.out.println("→ Prenda sin medida asociada");
            radioMedidaPersonalizada.setSelected(true);
        }
    }

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

    private boolean validarFormulario() {
        StringBuilder errores = new StringBuilder();

        if (txtNombre.getText().trim().isEmpty()) {
            errores.append("• El nombre de la prenda es obligatorio\n");
        }

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

    @FXML
    private void handleGuardar() {
        if (!validarFormulario()) {
            return;
        }

        ocultarError();
        btnGuardar.setDisable(true);

        try {
            Medida medida = obtenerMedida();
            Prenda prenda = construirPrenda(medida);

            if (prendaEditar == null) {
                // MODO AGREGAR
                prendaService.registrarPrenda(prenda);
                mostrarExito("Prenda registrada exitosamente");
            } else {
                // MODO EDITAR
                prenda.setIdPrenda(prendaEditar.getIdPrenda());

                // ✅ PRIMERO actualizar la prenda
                prendaService.actualizarPrenda(prenda);

                // ✅ DESPUÉS actualizar la medida (si es personalizada y cambió)
                if (radioMedidaPersonalizada.isSelected() &&
                        medida.getIdMedida() != null) {

                    System.out.println("=== Actualizando medida después de la prenda ===");
                    medidaService.actualizarMedida(medida);
                }

                mostrarExito("Prenda actualizada exitosamente");
            }

            if (parentController != null) {
                parentController.cargarPrendas();
            }

            cerrarModal();

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al guardar: " + e.getMessage());
            btnGuardar.setDisable(false);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error inesperado: " + e.getMessage());
            btnGuardar.setDisable(false);
        }
    }

    private Medida obtenerMedida() throws SQLException {
        if (radioMedidaEstandar.isSelected()) {
            return comboMedidasEstandar.getValue();
        } else {
            Medida nuevaMedida = construirMedida();

            // ✅ Si estamos editando y la medida era personalizada
            if (prendaEditar != null && prendaEditar.getIdMedida() != null) {
                Optional<Medida> medidaAnteriorOpt = medidaService.buscarPorId(prendaEditar.getIdMedida());

                if (medidaAnteriorOpt.isPresent() &&
                        MedidaService.TIPO_PERSONALIZADA.equalsIgnoreCase(
                                medidaAnteriorOpt.get().getTipoMedida())) {

                    // ✅ Asignar el ID de la medida existente
                    nuevaMedida.setIdMedida(prendaEditar.getIdMedida());

                    System.out.println("=== Medida personalizada a actualizar ===");
                    System.out.println("ID: " + nuevaMedida.getIdMedida());
                    System.out.println("Nombre: " + nuevaMedida.getNombreMedida());

                    // ⚠️ NO actualizar aquí, retornar para actualizar DESPUÉS
                    return nuevaMedida;
                }
            }

            // Crear nueva medida
            System.out.println("=== Creando nueva medida personalizada ===");
            return medidaService.registrarMedida(nuevaMedida);
        }
    }

    private Medida construirMedida() {
        Medida medida = new Medida();
        medida.setNombreMedida(txtNombreMedida.getText().trim());
        medida.setTipoMedida(MedidaService.TIPO_PERSONALIZADA); // ✅ Usar constante

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

    private Prenda construirPrenda(Medida medida) {
        Prenda prenda = new Prenda();
        prenda.setNombrePrenda(txtNombre.getText().trim());
        prenda.setDescripcionPrenda(txtDescripcion.getText().trim());
        prenda.setCostoTotalEstimado(Double.parseDouble(txtCostoTotal.getText().trim()));
        prenda.setCostoMateriales(0.0);
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