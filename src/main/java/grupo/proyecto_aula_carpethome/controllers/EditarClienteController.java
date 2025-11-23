package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.entities.Cliente;
import grupo.proyecto_aula_carpethome.services.ClienteService;
import grupo.proyecto_aula_carpethome.services.ServiceFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;

public class EditarClienteController {

    @FXML private Label lblIdCliente;
    @FXML private TextField txtCedula;
    @FXML private TextField txtPrimerNombre;
    @FXML private TextField txtSegundoNombre;
    @FXML private TextField txtPrimerApellido;
    @FXML private TextField txtSegundoApellido;
    @FXML private TextField txtTelefonoPrincipal;
    @FXML private TextField txtTelefonoSecundario;
    @FXML private TextField txtCorreoPrincipal;
    @FXML private TextField txtCorreoSecundario;
    @FXML private Button btnGuardar;
    @FXML private Button btnCerrar;

    private ClienteService clienteService;
    private GestionClientesController parentController;
    private Cliente clienteActual;

    @FXML
    public void initialize() {
        clienteService = ServiceFactory.getClienteService();
        configurarValidaciones();
    }

    private void configurarValidaciones() {
        // Solo números en teléfonos
        txtTelefonoPrincipal.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtTelefonoPrincipal.setText(oldVal);
            }
        });

        txtTelefonoSecundario.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtTelefonoSecundario.setText(oldVal);
            }
        });

        // Solo letras y espacios en nombres
        configurarValidacionNombre(txtPrimerNombre);
        configurarValidacionNombre(txtSegundoNombre);
        configurarValidacionNombre(txtPrimerApellido);
        configurarValidacionNombre(txtSegundoApellido);
    }

    private void configurarValidacionNombre(TextField field) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]*")) {
                field.setText(oldVal);
            }
        });
    }

    public void setParentController(GestionClientesController parent) {
        this.parentController = parent;
    }

    public void cargarCliente(Cliente cliente) {
        this.clienteActual = cliente;

        // Cargar datos en los campos
        lblIdCliente.setText("ID: " + cliente.getIdCliente());
        txtCedula.setText(cliente.getCedula());
        txtPrimerNombre.setText(cliente.getPNombre());
        txtSegundoNombre.setText(cliente.getSNombre() != null ? cliente.getSNombre() : "");
        txtPrimerApellido.setText(cliente.getPApellido());
        txtSegundoApellido.setText(cliente.getSApellido() != null ? cliente.getSApellido() : "");
        txtTelefonoPrincipal.setText(String.valueOf(cliente.getPTelefono()));
        txtTelefonoSecundario.setText(cliente.getSTelefono() != null ? String.valueOf(cliente.getSTelefono()) : "");
        txtCorreoPrincipal.setText(cliente.getPCorreo());
        txtCorreoSecundario.setText(cliente.getSCorreo() != null ? cliente.getSCorreo() : "");
    }

    @FXML
    private void handleGuardar() {
        try {
            // Validar campos obligatorios
            if (!validarCampos()) {
                return;
            }

            // Actualizar el cliente con los nuevos valores
            clienteActual.setPNombre(txtPrimerNombre.getText().trim());
            clienteActual.setSNombre(txtSegundoNombre.getText().trim().isEmpty() ? null : txtSegundoNombre.getText().trim());
            clienteActual.setPApellido(txtPrimerApellido.getText().trim());
            clienteActual.setSApellido(txtSegundoApellido.getText().trim().isEmpty() ? null : txtSegundoApellido.getText().trim());
            clienteActual.setPTelefono(Long.parseLong(txtTelefonoPrincipal.getText().trim()));
            clienteActual.setSTelefono(txtTelefonoSecundario.getText().trim().isEmpty() ? null : Long.parseLong(txtTelefonoSecundario.getText().trim()));
            clienteActual.setPCorreo(txtCorreoPrincipal.getText().trim().toLowerCase());
            clienteActual.setSCorreo(txtCorreoSecundario.getText().trim().isEmpty() ? null : txtCorreoSecundario.getText().trim().toLowerCase());

            // Guardar en la base de datos
            clienteService.actualizarCliente(clienteActual);

            // Mostrar éxito y cerrar
            mostrarExito("Cliente actualizado correctamente");
            cerrarVentana();

        } catch (NumberFormatException e) {
            mostrarError("Error de formato", "Los teléfonos deben ser números válidos");
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al actualizar", e.getMessage());
        } catch (IllegalArgumentException e) {
            mostrarError("Error de validación", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error inesperado", e.getMessage());
        }
    }

    private boolean validarCampos() {
        // Primer nombre
        if (txtPrimerNombre.getText().trim().isEmpty()) {
            mostrarAdvertencia("El primer nombre es obligatorio");
            txtPrimerNombre.requestFocus();
            return false;
        }

        if (txtPrimerNombre.getText().trim().length() < 2) {
            mostrarAdvertencia("El primer nombre debe tener al menos 2 caracteres");
            txtPrimerNombre.requestFocus();
            return false;
        }

        // Primer apellido
        if (txtPrimerApellido.getText().trim().isEmpty()) {
            mostrarAdvertencia("El primer apellido es obligatorio");
            txtPrimerApellido.requestFocus();
            return false;
        }

        if (txtPrimerApellido.getText().trim().length() < 2) {
            mostrarAdvertencia("El primer apellido debe tener al menos 2 caracteres");
            txtPrimerApellido.requestFocus();
            return false;
        }

        // Teléfono principal
        if (txtTelefonoPrincipal.getText().trim().isEmpty()) {
            mostrarAdvertencia("El teléfono principal es obligatorio");
            txtTelefonoPrincipal.requestFocus();
            return false;
        }

        if (txtTelefonoPrincipal.getText().trim().length() != 10) {
            mostrarAdvertencia("El teléfono debe tener 10 dígitos");
            txtTelefonoPrincipal.requestFocus();
            return false;
        }

        // Teléfono secundario (si está lleno)
        if (!txtTelefonoSecundario.getText().trim().isEmpty()) {
            if (txtTelefonoSecundario.getText().trim().length() != 10) {
                mostrarAdvertencia("El teléfono secundario debe tener 10 dígitos");
                txtTelefonoSecundario.requestFocus();
                return false;
            }
        }

        // Correo principal
        if (txtCorreoPrincipal.getText().trim().isEmpty()) {
            mostrarAdvertencia("El correo principal es obligatorio");
            txtCorreoPrincipal.requestFocus();
            return false;
        }

        if (!validarFormatoCorreo(txtCorreoPrincipal.getText().trim())) {
            mostrarAdvertencia("El formato del correo principal no es válido");
            txtCorreoPrincipal.requestFocus();
            return false;
        }

        // Correo secundario (si está lleno)
        if (!txtCorreoSecundario.getText().trim().isEmpty()) {
            if (!validarFormatoCorreo(txtCorreoSecundario.getText().trim())) {
                mostrarAdvertencia("El formato del correo secundario no es válido");
                txtCorreoSecundario.requestFocus();
                return false;
            }
        }

        return true;
    }

    private boolean validarFormatoCorreo(String correo) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return correo.matches(regex);
    }

    @FXML
    private void handleCerrar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }

    // ============================================
    // MENSAJES
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

    private void mostrarAdvertencia(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertencia");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}