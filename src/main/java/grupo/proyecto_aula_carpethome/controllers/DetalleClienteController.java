package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.entities.Cliente;
import grupo.proyecto_aula_carpethome.services.ClienteService;
import grupo.proyecto_aula_carpethome.services.ServiceFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.sql.SQLException;

public class DetalleClienteController {

    @FXML private StackPane avatarContainer;
    @FXML private Label lblIniciales;
    @FXML private Label lblNombreCompleto;
    @FXML private Label lblIdCliente;
    @FXML private Label lblCedula;
    @FXML private Label lblTelefonoPrincipal;
    @FXML private Label lblTelefonoSecundario;
    @FXML private Label lblCorreoPrincipal;
    @FXML private Label lblCorreoSecundario;
    @FXML private Label lblProyectosTotales;
    @FXML private Label lblProyectosActivos;
    @FXML private Label lblProyectosCompletados;
    @FXML private HBox boxTelefonoSecundario;
    @FXML private HBox boxCorreoSecundario;
    @FXML private Button btnCerrar;

    private ClienteService clienteService;
    private GestionClientesController parentController;
    private Cliente clienteActual;

    @FXML
    public void initialize() {
        clienteService = ServiceFactory.getClienteService();
    }

    public void setParentController(GestionClientesController parent) {
        this.parentController = parent;
    }

    public void cargarCliente(Cliente cliente) {
        this.clienteActual = cliente;

        // Cargar información básica
        lblNombreCompleto.setText(cliente.getNombreCompleto());
        lblIdCliente.setText("ID: " + cliente.getIdCliente());
        lblCedula.setText("CC: " + cliente.getCedula());

        // Iniciales para el avatar
        String iniciales = obtenerIniciales(cliente);
        lblIniciales.setText(iniciales);

        // Teléfonos
        lblTelefonoPrincipal.setText(formatearTelefono(cliente.getPTelefono()));

        if (cliente.getSTelefono() != null) {
            lblTelefonoSecundario.setText(formatearTelefono(cliente.getSTelefono()));
            boxTelefonoSecundario.setVisible(true);
            boxTelefonoSecundario.setManaged(true);
        } else {
            boxTelefonoSecundario.setVisible(false);
            boxTelefonoSecundario.setManaged(false);
        }

        // Correos
        lblCorreoPrincipal.setText(cliente.getPCorreo());

        if (cliente.getSCorreo() != null && !cliente.getSCorreo().isEmpty()) {
            lblCorreoSecundario.setText(cliente.getSCorreo());
            boxCorreoSecundario.setVisible(true);
            boxCorreoSecundario.setManaged(true);
        } else {
            boxCorreoSecundario.setVisible(false);
            boxCorreoSecundario.setManaged(false);
        }

        cargarEstadisticas(cliente);
    }

    private void cargarEstadisticas(Cliente cliente) {
        try {
            ClienteService.EstadisticasCliente stats =
                    clienteService.obtenerEstadisticasCliente(cliente.getIdCliente());

            lblProyectosTotales.setText(String.valueOf(stats.getProyectosTotales()));
            lblProyectosActivos.setText(String.valueOf(stats.getProyectosActivos()));
            lblProyectosCompletados.setText(String.valueOf(stats.getProyectosCompletados()));

        } catch (SQLException e) {
            System.err.println("Error al cargar estadísticas: " + e.getMessage());
            e.printStackTrace();
            lblProyectosTotales.setText("0");
            lblProyectosActivos.setText("0");
            lblProyectosCompletados.setText("0");
        }
    }

    private String obtenerIniciales(Cliente cliente) {
        String iniciales = "";
        if (cliente.getPNombre() != null && !cliente.getPNombre().isEmpty()) {
            iniciales += cliente.getPNombre().charAt(0);
        }
        if (cliente.getPApellido() != null && !cliente.getPApellido().isEmpty()) {
            iniciales += cliente.getPApellido().charAt(0);
        }
        return iniciales.toUpperCase();
    }

    private String formatearTelefono(Long telefono) {
        String tel = String.valueOf(telefono);
        if (tel.length() == 10) {
            return String.format("%s %s %s",
                    tel.substring(0, 3),
                    tel.substring(3, 6),
                    tel.substring(6));
        }
        return tel;
    }

    @FXML
    private void handleEditar() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/grupo/proyecto_aula_carpethome/EditarCliente.fxml")
            );
            Parent root = loader.load();

            EditarClienteController editarController = loader.getController();
            editarController.setParentController(parentController);
            editarController.cargarCliente(clienteActual);

            // Cerrar el modal actual
            cerrarVentana();

            // Abrir el modal de edición
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initStyle(StageStyle.TRANSPARENT);
            modalStage.setTitle("Editar Cliente");

            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
            overlay.getChildren().add(root);
            overlay.setPadding(new Insets(40));

            Scene scene = new Scene(overlay);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            modalStage.setScene(scene);

            modalStage.centerOnScreen();
            modalStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al abrir el modal de edición: " + e.getMessage());
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
}