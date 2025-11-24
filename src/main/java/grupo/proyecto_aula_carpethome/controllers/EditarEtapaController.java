package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.entities.Etapa;
import grupo.proyecto_aula_carpethome.services.EtapaService;
import grupo.proyecto_aula_carpethome.services.ServiceFactory;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class EditarEtapaController {

    @FXML private Label lblIdEtapa;
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private Button btnCerrar;
    @FXML private Button btnGuardar;

    private EtapaService etapaService;
    private GestionEtapasController gestionController;
    private Etapa etapaActual;
    private String nombreOriginal;
    private String descripcionOriginal;

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
        boolean hubocambios = hayCambios();

        btnGuardar.setDisable(!(nombreValido && descripcionValida && hubocambios));
    }

    private boolean hayCambios() {
        if (nombreOriginal == null || descripcionOriginal == null) {
            return false;
        }

        String nombreActual = txtNombre.getText().trim();
        String descripcionActual = txtDescripcion.getText().trim();

        return !nombreActual.equals(nombreOriginal) || !descripcionActual.equals(descripcionOriginal);
    }

    public void setEtapa(Etapa etapa) {
        this.etapaActual = etapa;
        lblIdEtapa.setText("ID: " + etapa.getIdEtapa());
        txtNombre.setText(etapa.getNombreEtapa());
        txtDescripcion.setText(etapa.getDescripcionEtapa());

        // Guardar valores originales para detectar cambios
        nombreOriginal = etapa.getNombreEtapa();
        descripcionOriginal = etapa.getDescripcionEtapa();

        validarFormulario();
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

        // Confirmar cambios importantes
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Cambios");
        confirmacion.setHeaderText("¿Guardar los cambios realizados?");
        confirmacion.setContentText("Esta acción afectará a todas las prendas que tengan asignada esta etapa.");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    etapaActual.setNombreEtapa(nombre);
                    etapaActual.setDescripcionEtapa(descripcion);

                    etapaService.actualizarEtapa(etapaActual);

                    mostrarExito("¡Etapa actualizada exitosamente!");

                    // Recargar la lista en el controlador principal
                    if (gestionController != null) {
                        gestionController.cargarEtapas();
                    }

                    cerrarVentana();

                } catch (SQLException e) {
                    e.printStackTrace();
                    mostrarError("Error al actualizar etapa",
                            "No se pudo actualizar la etapa: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleCerrar() {
        // Confirmar si hay cambios sin guardar
        if (hayCambios()) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar");
            confirmacion.setHeaderText("¿Descartar cambios?");
            confirmacion.setContentText("Los cambios realizados se perderán.");

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