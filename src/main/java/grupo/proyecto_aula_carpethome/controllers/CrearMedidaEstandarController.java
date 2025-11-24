package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.entities.Medida;
import grupo.proyecto_aula_carpethome.services.MedidaService;
import grupo.proyecto_aula_carpethome.services.ServiceFactory;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class CrearMedidaEstandarController {

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

    @FXML
    public void initialize() {
        medidaService = ServiceFactory.getMedidaService();
        configurarValidacionesNumericas();
        configurarValidaciones();
    }

    private void configurarValidacionesNumericas() {
        // Hacer que todos los campos numéricos solo acepten números
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

    private void configurarValidaciones() {
        txtNombre.textProperty().addListener((obs, oldVal, newVal) -> {
            validarFormulario();
        });
    }

    private void validarFormulario() {
        boolean nombreValido = txtNombre.getText() != null && !txtNombre.getText().trim().isEmpty();
        btnGuardar.setDisable(!nombreValido);
    }

    @FXML
    private void handleGuardar() {
        String nombre = txtNombre.getText().trim();

        if (nombre.isEmpty()) {
            mostrarError("Campo incompleto", "El nombre de la medida es obligatorio.");
            return;
        }

        try {
            Medida nuevaMedida = Medida.builder()
                    .nombreMedida(nombre)
                    .tipoMedida(MedidaService.TIPO_ESTANDAR)
                    .cBusto(parseDouble(txtCBusto.getText()))
                    .cCintura(parseDouble(txtCCintura.getText()))
                    .cCadera(parseDouble(txtCCadera.getText()))
                    .alturaBusto(parseDouble(txtAlturaBusto.getText()))
                    .separacionBusto(parseDouble(txtSeparacionBusto.getText()))
                    .radioBusto(parseDouble(txtRadioBusto.getText()))
                    .bajoBusto(parseDouble(txtBajoBusto.getText()))
                    .largoFalda(parseDouble(txtLargoFalda.getText()))
                    .largoCadera(parseDouble(txtLargoCadera.getText()))
                    .largoVestido(parseDouble(txtLargoVestido.getText()))
                    .largoPantalon(parseDouble(txtLargoPantalon.getText()))
                    .largoManga(parseDouble(txtLargoManga.getText()))
                    .build();

            medidaService.registrarMedida(nuevaMedida);

            mostrarExito("¡Medida estándar creada exitosamente!");

            if (gestionController != null) {
                gestionController.cargarMedidas();
            }

            cerrarVentana();

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al crear medida",
                    "No se pudo crear la medida: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            mostrarError("Validación", e.getMessage());
        }
    }

    @FXML
    private void handleCerrar() {
        if (!txtNombre.getText().trim().isEmpty() || hayCamposConValor()) {
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

    private boolean hayCamposConValor() {
        TextField[] campos = {
                txtCBusto, txtCCintura, txtCCadera, txtAlturaBusto,
                txtSeparacionBusto, txtRadioBusto, txtBajoBusto,
                txtLargoFalda, txtLargoCadera, txtLargoVestido,
                txtLargoPantalon, txtLargoManga
        };

        for (TextField campo : campos) {
            if (!campo.getText().trim().isEmpty()) {
                return true;
            }
        }
        return false;
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