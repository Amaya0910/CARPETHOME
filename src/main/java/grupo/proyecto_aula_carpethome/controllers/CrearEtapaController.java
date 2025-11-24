package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.entities.Etapa;
import grupo.proyecto_aula_carpethome.services.EtapaService;
import grupo.proyecto_aula_carpethome.services.ServiceFactory;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class CrearEtapaController {

    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private Button btnCerrar;
    @FXML private Button btnGuardar;

    private EtapaService etapaService;
    private GestionEtapasController gestionController;

    @FXML
    public void initialize() {
        etapaService = ServiceFactory.getEtapaService();
        configurarValidaciones();
    }

    private void configurarValidaciones() {
        // Validación en tiempo real del nombre
        txtNombre.textProperty().addListener((obs, oldVal, newVal) -> {
            validarFormulario();
        });

        // Validación en tiempo real de la descripción
        txtDescripcion.textProperty().addListener((obs, oldVal, newVal) -> {
            validarFormulario();
        });
    }

    private void validarFormulario() {
        boolean nombreValido = txtNombre.getText() != null && !txtNombre.getText().trim().isEmpty();
        boolean descripcionValida = txtDescripcion.getText() != null && !txtDescripcion.getText().trim().isEmpty();

        btnGuardar.setDisable(!(nombreValido && descripcionValida));
    }

    @FXML
    private void handleGuardar() {
        String nombre = txtNombre.getText().trim();
        String descripcion = txtDescripcion.getText().trim();

        // Validación adicional
        if (nombre.isEmpty() || descripcion.isEmpty()) {
            mostrarError("Campos incompletos", "Por favor complete todos los campos obligatorios.");
            return;
        }

        try {
            Etapa nuevaEtapa = Etapa.builder()
                    .nombreEtapa(nombre)
                    .descripcionEtapa(descripcion)
                    .build();

            etapaService.registrarEtapa(nuevaEtapa);

            mostrarExito("¡Etapa creada exitosamente!");

            // Recargar la lista en el controlador principal
            if (gestionController != null) {
                gestionController.cargarEtapas();
            }

            cerrarVentana();

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al crear etapa",
                    "No se pudo crear la etapa: " + e.getMessage());
        }
    }

    @FXML
    private void handleCerrar() {
        // Confirmar si hay cambios sin guardar
        if (!txtNombre.getText().trim().isEmpty() || !txtDescripcion.getText().trim().isEmpty()) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar");
            confirmacion.setHeaderText("¿Descartar cambios?");
            confirmacion.setContentText("Los datos ingresados se perderán.");

            confirmacion.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    cerrarVentana();
                }
            });
        } else {
            cerrarVentana();
        }
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }

    public void setGestionController(GestionEtapasController controller) {
        this.gestionController = controller;
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