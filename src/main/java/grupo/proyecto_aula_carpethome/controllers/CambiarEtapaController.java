package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.HelloApplication;
import grupo.proyecto_aula_carpethome.entities.*;
import grupo.proyecto_aula_carpethome.services.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.List;

public class CambiarEtapaController {

    @FXML private Label lblPrenda;
    @FXML private Label lblEtapaActual;
    @FXML private ComboBox<Etapa> comboNuevaEtapa;
    @FXML private TextArea txtObservaciones;
    @FXML private Button btnConfirmar;
    @FXML private Button btnCerrar;

    private Prenda prendaActual;
    private HistEtapa etapaActualHist;
    private GestionPrendasController parentController;

    private EtapaService etapaService = ServiceFactory.getEtapaService();
    private HistEtapaService histEtapaService = ServiceFactory.getHistEtapaService();

    @FXML
    public void initialize() {
        configurarComboBox();
    }

    private void configurarComboBox() {
        comboNuevaEtapa.setConverter(new StringConverter<Etapa>() {
            @Override
            public String toString(Etapa etapa) {
                return etapa == null ? "" : etapa.getNombreEtapa();
            }

            @Override
            public Etapa fromString(String string) {
                return null;
            }
        });
    }

    public void setParentController(GestionPrendasController controller) {
        this.parentController = controller;
    }

    public void cargarPrenda(Prenda prenda) {
        this.prendaActual = prenda;
        lblPrenda.setText("Prenda: " + prenda.getNombrePrenda());

        try {
            // Cargar etapa actual
            var etapaActualOpt = histEtapaService.obtenerEtapaActual(prenda.getIdPrenda());

            if (etapaActualOpt.isPresent()) {
                etapaActualHist = etapaActualOpt.get();
                var etapaOpt = etapaService.buscarPorId(etapaActualHist.getIdEtapa());
                lblEtapaActual.setText(etapaOpt.map(Etapa::getNombreEtapa).orElse("Sin etapa"));
            } else {
                lblEtapaActual.setText("Sin etapa asignada");
            }

            // Cargar todas las etapas disponibles
            List<Etapa> etapas = etapaService.listarTodas();
            comboNuevaEtapa.getItems().setAll(etapas);

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al cargar etapas", e.getMessage());
        }
    }

    @FXML
    private void handleConfirmar() {
        if (comboNuevaEtapa.getValue() == null) {
            mostrarAdvertencia("Debe seleccionar una etapa");
            return;
        }

        try {
            // Obtener el usuario logueado
            UsuarioLogueado usuarioLogueado = HelloApplication.getUser();

            // Verificar que hay un usuario logueado
            if (usuarioLogueado == null) {
                mostrarAdvertencia("Debe iniciar sesión para realizar esta acción");
                return;
            }

            // Verificar que el usuario es un Empleado (no Administrador)
            if (!"Empleado".equalsIgnoreCase(usuarioLogueado.getRol())) {
                mostrarAdvertencia("Solo los empleados pueden cambiar etapas de prendas");
                return;
            }

            // Preparar datos para el cambio de etapa
            String idEtapaActual = etapaActualHist != null ? etapaActualHist.getIdEtapa() : null;
            String idEtapaNueva = comboNuevaEtapa.getValue().getIdEtapa();
            String observaciones = txtObservaciones.getText().trim();

            // Usar el ID del usuario logueado (que en este caso es el idEmpleado)
            String idEmpleado = usuarioLogueado.getId();

            // Cambiar etapa
            histEtapaService.cambiarEtapa(
                    prendaActual.getIdPrenda(),
                    idEtapaActual,
                    idEtapaNueva,
                    observaciones,
                    idEmpleado
            );

            mostrarExito("Etapa cambiada exitosamente");

            // Actualizar la vista del controlador padre
            if (parentController != null) {
                parentController.cargarPrendas();
            }

            cerrarVentana();

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al cambiar etapa", e.getMessage());
        }
    }

    @FXML
    private void handleCerrar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
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

    private void mostrarAdvertencia(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertencia");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}