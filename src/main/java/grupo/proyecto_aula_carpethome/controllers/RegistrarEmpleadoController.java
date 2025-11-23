package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.entities.Empleado;
import grupo.proyecto_aula_carpethome.services.EmpleadoService;
import grupo.proyecto_aula_carpethome.services.ServiceFactory;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class RegistrarEmpleadoController {

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

    @FXML
    public void initialize() {
        empleadoService = ServiceFactory.getEmpleadoService();

        // Configurar ComboBox de cargos
        comboCargo.setItems(FXCollections.observableArrayList(
                "Diseñador", "Sastre", "Auxiliar", "Supervisor"
        ));

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

    public void setParentController(GestionUsuariosController parent) {
        this.parentController = parent;
    }

    @FXML
    private void handleGuardar() {
        try {
            // Validar campos
            if (!validarCampos()) {
                return;
            }

            // Verificar que las contraseñas coincidan
            if (!txtContrasena.getText().equals(txtConfirmarContrasena.getText())) {
                mostrarAdvertencia("Las contraseñas no coinciden");
                txtConfirmarContrasena.requestFocus();
                return;
            }

            // Verificar si ya existe la cédula
            if (empleadoService.existePorCedula(txtCedula.getText().trim())) {
                mostrarAdvertencia("Ya existe un usuario con la cédula: " + txtCedula.getText());
                txtCedula.requestFocus();
                return;
            }

            // Crear el empleado
            Empleado empleado = Empleado.builder()
                    .cedula(txtCedula.getText().trim())
                    .pNombre(txtPrimerNombre.getText().trim())
                    .sNombre(txtSegundoNombre.getText().trim().isEmpty() ? null : txtSegundoNombre.getText().trim())
                    .pApellido(txtPrimerApellido.getText().trim())
                    .sApellido(txtSegundoApellido.getText().trim().isEmpty() ? null : txtSegundoApellido.getText().trim())
                    .pTelefono(Long.parseLong(txtTelefonoPrincipal.getText().trim()))
                    .sTelefono(txtTelefonoSecundario.getText().trim().isEmpty() ? null : Long.parseLong(txtTelefonoSecundario.getText().trim()))
                    .pCorreo(txtCorreoPrincipal.getText().trim().toLowerCase())
                    .sCorreo(txtCorreoSecundario.getText().trim().isEmpty() ? null : txtCorreoSecundario.getText().trim().toLowerCase())
                    .cargo(comboCargo.getValue())
                    .contrasena(txtContrasena.getText())
                    .build();

            // Guardar en la base de datos
            Empleado guardado = empleadoService.registrarEmpleado(empleado);

            // Mostrar éxito
            mostrarExito("Empleado creado exitosamente con ID: " + guardado.getIdEmpleado());

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

        // Cargo
        if (comboCargo.getValue() == null || comboCargo.getValue().isEmpty()) {
            mostrarAdvertencia("Debe seleccionar un cargo");
            comboCargo.requestFocus();
            return false;
        }

        // Contraseña
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

        // Confirmar contraseña
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