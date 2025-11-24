package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.entities.Medida;
import grupo.proyecto_aula_carpethome.services.MedidaService;
import grupo.proyecto_aula_carpethome.services.ServiceFactory;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class EditarMedidaEstandarController {

    @FXML private Label lblIdMedida;
    @FXML private TextField txtNombre;
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
    @FXML private Button btnCerrar;
    @FXML private Button btnGuardar;

    private MedidaService medidaService;
    private GestionMedidasEstandarController gestionController;
    private Medida medidaActual;

    @FXML
    public void initialize() {
        medidaService = ServiceFactory.getMedidaService();
        configurarValidacionesNumericas();
    }

    private void configurarValidacionesNumericas() {
        TextField[] camposNumericos = {
                txtCBusto, txtCCintura, txtCCadera, txtAlturaBusto,
                txtSeparacionBusto, txtRadioBusto, txtBajoBusto,
                txtLargoFalda, txtLargoCadera, txtLargoVestido,
                txtLargoPantalon, txtLargoManga
        };

        for (TextField campo : camposNumericos) {
            campo.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("\\d*\\.?\\d*")) {
                    campo.setText(oldVal);
                }
            });
        }
    }

    public void setMedida(Medida medida) {
        this.medidaActual = medida;
        lblIdMedida.setText("ID: " + medida.getIdMedida());
        txtNombre.setText(medida.getNombreMedida());

        // Cargar todas las medidas
        setDoubleValue(txtCBusto, medida.getCBusto());
        setDoubleValue(txtCCintura, medida.getCCintura());
        setDoubleValue(txtCCadera, medida.getCCadera());
        setDoubleValue(txtAlturaBusto, medida.getAlturaBusto());
        setDoubleValue(txtSeparacionBusto, medida.getSeparacionBusto());
        setDoubleValue(txtRadioBusto, medida.getRadioBusto());
        setDoubleValue(txtBajoBusto, medida.getBajoBusto());
        setDoubleValue(txtLargoFalda, medida.getLargoFalda());
        setDoubleValue(txtLargoCadera, medida.getLargoCadera());
        setDoubleValue(txtLargoVestido, medida.getLargoVestido());
        setDoubleValue(txtLargoPantalon, medida.getLargoPantalon());
        setDoubleValue(txtLargoManga, medida.getLargoManga());
    }

    private void setDoubleValue(TextField field, double value) {
        if (value > 0) {
            field.setText(String.format("%.2f", value));
        } else {
            field.setText("");
        }
    }

    @FXML
    private void handleGuardar() {
        String nombre = txtNombre.getText().trim();

        if (nombre.isEmpty()) {
            mostrarError("Campo incompleto", "El nombre de la medida es obligatorio.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Cambios");
        confirmacion.setHeaderText("¿Guardar los cambios realizados?");
        confirmacion.setContentText("Se actualizarán todas las medidas de la talla " + nombre);

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    medidaActual.setNombreMedida(nombre);
                    medidaActual.setCBusto(parseDouble(txtCBusto.getText()));
                    medidaActual.setCCintura(parseDouble(txtCCintura.getText()));
                    medidaActual.setCCadera(parseDouble(txtCCadera.getText()));
                    medidaActual.setAlturaBusto(parseDouble(txtAlturaBusto.getText()));
                    medidaActual.setSeparacionBusto(parseDouble(txtSeparacionBusto.getText()));
                    medidaActual.setRadioBusto(parseDouble(txtRadioBusto.getText()));
                    medidaActual.setBajoBusto(parseDouble(txtBajoBusto.getText()));
                    medidaActual.setLargoFalda(parseDouble(txtLargoFalda.getText()));
                    medidaActual.setLargoCadera(parseDouble(txtLargoCadera.getText()));
                    medidaActual.setLargoVestido(parseDouble(txtLargoVestido.getText()));
                    medidaActual.setLargoPantalon(parseDouble(txtLargoPantalon.getText()));
                    medidaActual.setLargoManga(parseDouble(txtLargoManga.getText()));

                    medidaService.actualizarMedida(medidaActual);

                    mostrarExito("¡Medida estándar actualizada exitosamente!");

                    if (gestionController != null) {
                        gestionController.cargarMedidas();
                    }

                    cerrarVentana();

                } catch (SQLException e) {
                    e.printStackTrace();
                    mostrarError("Error al actualizar medida",
                            "No se pudo actualizar la medida: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    mostrarError("Validación", e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleCerrar() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText("¿Cerrar sin guardar?");
        confirmacion.setContentText("Los cambios realizados se perderán.");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                cerrarVentana();
            }
        });
    }

    private double parseDouble(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }

    public void setGestionController(GestionMedidasEstandarController controller) {
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