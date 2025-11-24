package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.entities.Empleado;
import grupo.proyecto_aula_carpethome.services.EmpleadoService;
import grupo.proyecto_aula_carpethome.services.ServiceFactory;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class EditarEmpleadoController {

    @FXML private TextField txtCedula;
    @FXML private TextField txtPrimerNombre;
    @FXML private TextField txtSegundoNombre;
    @FXML private TextField txtPrimerApellido;
    @FXML private TextField txtSegundoApellido;
    @FXML private TextField txtTelefonoPrincipal;
    @FXML private TextField txtTelefonoSecundario;
    @FXML private TextField txtCorreoPrincipal;
    @FXML private TextField txtCorreoSecundario;
    @FXML private ComboBox<String> comboCargo;
    @FXML private PasswordField txtContrasena;
    @FXML private PasswordField txtConfirmarContrasena;
    @FXML private Button btnGuardar;
    @FXML private Button btnCerrar;

    private EmpleadoService empleadoService;
    private GestionUsuariosController parentController;
    private Empleado empleadoActual;

    @FXML
    public void initialize() {
        empleadoService = ServiceFactory.getEmpleadoService();
        configurarValidaciones();
        cargarCargos();
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

    private void cargarCargos() {
        comboCargo.getItems().addAll("Diseñador", "Sastre", "Auxiliar", "Supervisor");
    }

    public void setParentController(GestionUsuariosController parent) {
        this.parentController = parent;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleadoActual = empleado;
        cargarDatosEmpleado();
    }

    private void cargarDatosEmpleado() {
        if (empleadoActual == null) return;

        // Deshabilitar cédula (no se puede modificar)
        txtCedula.setText(empleadoActual.getCedula());
        txtCedula.setDisable(true);
        txtCedula.setStyle(txtCedula.getStyle() + "-fx-opacity: 0.6;");

        txtPrimerNombre.setText(empleadoActual.getPNombre());
        txtSegundoNombre.setText(empleadoActual.getSNombre() != null ? empleadoActual.getSNombre() : "");
        txtPrimerApellido.setText(empleadoActual.getPApellido());
        txtSegundoApellido.setText(empleadoActual.getSApellido() != null ? empleadoActual.getSApellido() : "");
        txtTelefonoPrincipal.setText(String.valueOf(empleadoActual.getPTelefono()));
        txtTelefonoSecundario.setText(empleadoActual.getSTelefono() != null ? String.valueOf(empleadoActual.getSTelefono()) : "");
        txtCorreoPrincipal.setText(empleadoActual.getPCorreo());
        txtCorreoSecundario.setText(empleadoActual.getSCorreo() != null ? empleadoActual.getSCorreo() : "");
        comboCargo.setValue(empleadoActual.getCargo());
        txtContrasena.setText(empleadoActual.getContrasena());
        txtConfirmarContrasena.setText(empleadoActual.getContrasena());
    }

    @FXML
    private void handleGuardar() {
        try {
            if (!validarCampos()) {
                return;
            }

            if (!txtContrasena.getText().equals(txtConfirmarContrasena.getText())) {
                mostrarAdvertencia("Las contraseñas no coinciden");
                txtConfirmarContrasena.requestFocus();
                return;
            }

            // Actualizar datos del empleado
            empleadoActual.setPNombre(txtPrimerNombre.getText().trim());
            empleadoActual.setSNombre(txtSegundoNombre.getText().trim().isEmpty() ? null : txtSegundoNombre.getText().trim());
            empleadoActual.setPApellido(txtPrimerApellido.getText().trim());
            empleadoActual.setSApellido(txtSegundoApellido.getText().trim().isEmpty() ? null : txtSegundoApellido.getText().trim());
            empleadoActual.setPTelefono(Long.parseLong(txtTelefonoPrincipal.getText().trim()));
            empleadoActual.setSTelefono(txtTelefonoSecundario.getText().trim().isEmpty() ? null : Long.parseLong(txtTelefonoSecundario.getText().trim()));
            empleadoActual.setPCorreo(txtCorreoPrincipal.getText().trim().toLowerCase());
            empleadoActual.setSCorreo(txtCorreoSecundario.getText().trim().isEmpty() ? null : txtCorreoSecundario.getText().trim().toLowerCase());
            empleadoActual.setCargo(comboCargo.getValue());
            empleadoActual.setContrasena(txtContrasena.getText());

            // Guardar cambios
            empleadoService.actualizarEmpleado(empleadoActual);

            mostrarExito("Empleado actualizado exitosamente");
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

        if (!txtTelefonoSecundario.getText().trim().isEmpty()) {
            if (txtTelefonoSecundario.getText().trim().length() != 10) {
                mostrarAdvertencia("El teléfono secundario debe tener 10 dígitos");
                txtTelefonoSecundario.requestFocus();
                return false;
            }
        }

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

        if (!txtCorreoSecundario.getText().trim().isEmpty()) {
            if (!validarFormatoCorreo(txtCorreoSecundario.getText().trim())) {
                mostrarAdvertencia("El formato del correo secundario no es válido");
                txtCorreoSecundario.requestFocus();
                return false;
            }
        }

        if (comboCargo.getValue() == null) {
            mostrarAdvertencia("El cargo es obligatorio");
            comboCargo.requestFocus();
            return false;
        }

        if (txtContrasena.getText().isEmpty()) {
            mostrarAdvertencia("La contraseña es obligatoria");
            txtContrasena.requestFocus();
            return false;
        }

        if (txtContrasena.getText().length() < 6) {
            mostrarAdvertencia("La contraseña debe tener al menos 6 caracteres");
            txtContrasena.requestFocus();
            return false;
        }

        if (txtContrasena.getText().length() > 15) {
            mostrarAdvertencia("La contraseña no puede tener más de 15 caracteres");
            txtContrasena.requestFocus();
            return false;
        }

        if (txtConfirmarContrasena.getText().isEmpty()) {
            mostrarAdvertencia("Debe confirmar la contraseña");
            txtConfirmarContrasena.requestFocus();
            return false;
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