package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.entities.Cliente;
import grupo.proyecto_aula_carpethome.services.ClienteService;
import grupo.proyecto_aula_carpethome.services.ServiceFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;

public class RegistrarClienteController {

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

    @FXML
    public void initialize() {
        clienteService = ServiceFactory.getClienteService();
        configurarValidaciones();
    }

    private void configurarValidaciones() {
        // Solo números en cédula
        txtCedula.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtCedula.setText(oldVal);
            }
        });

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

    @FXML
    private void handleGuardar() {
        try {
            // Validar campos obligatorios
            if (!validarCampos()) {
                return;
            }

            // Verificar si ya existe un cliente con esa cédula
            if (clienteService.existePorCedula(txtCedula.getText().trim())) {
                mostrarAdvertencia("Ya existe un cliente registrado con la cédula: " + txtCedula.getText());
                txtCedula.requestFocus();
                return;
            }

            // Crear el cliente
            Cliente cliente = Cliente.builder()
                    .cedula(txtCedula.getText().trim())
                    .pNombre(txtPrimerNombre.getText().trim())
                    .sNombre(txtSegundoNombre.getText().trim().isEmpty() ? null : txtSegundoNombre.getText().trim())
                    .pApellido(txtPrimerApellido.getText().trim())
                    .sApellido(txtSegundoApellido.getText().trim().isEmpty() ? null : txtSegundoApellido.getText().trim())
                    .pTelefono(Long.parseLong(txtTelefonoPrincipal.getText().trim()))
                    .sTelefono(txtTelefonoSecundario.getText().trim().isEmpty() ? null : Long.parseLong(txtTelefonoSecundario.getText().trim()))
                    .pCorreo(txtCorreoPrincipal.getText().trim().toLowerCase())
                    .sCorreo(txtCorreoSecundario.getText().trim().isEmpty() ? null : txtCorreoSecundario.getText().trim().toLowerCase())
                    .build();

            // Guardar en la base de datos
            Cliente clienteGuardado = clienteService.registrarCliente(cliente);

            // Mostrar éxito
            mostrarExito("Cliente registrado exitosamente con ID: " + clienteGuardado.getIdCliente());

            // Cerrar ventana
            cerrarVentana();

        } catch (NumberFormatException e) {
            mostrarError("Error de formato", "Los teléfonos deben ser números válidos");
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al guardar", e.getMessage());
        } catch (IllegalArgumentException e) {
            mostrarError("Error de validación", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error inesperado", e.getMessage());
        }
    }

    private boolean validarCampos() {
        // Cédula
        if (txtCedula.getText().trim().isEmpty()) {
            mostrarAdvertencia("La cédula es obligatoria");
            txtCedula.requestFocus();
            return false;
        }

        if (txtCedula.getText().trim().length() < 6 || txtCedula.getText().trim().length() > 10) {
            mostrarAdvertencia("La cédula debe tener entre 6 y 10 dígitos");
            txtCedula.requestFocus();
            return false;
        }

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