package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.config.ServiceFactory;
import grupo.proyecto_aula_carpethome.entities.Proyecto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class GestionProyectosController implements Initializable {

    @FXML
    private TableView<Proyecto> proyectosTable;

    @FXML
    private TextField txtBuscar;

    @FXML
    private ComboBox<String> cmbEstado;

    @FXML
    private ComboBox<String> cmbTipo;

    @FXML
    private Button btnNuevoProyecto, btnPagos, btnVer, btnEditar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar comboboxes
        ObservableList<String> estados = FXCollections.observableArrayList(
                "Todos", "Activo", "Completado", "Pendiente", "Cancelado"
        );
        ObservableList<String> tipos = FXCollections.observableArrayList(
                "Todos", "A Medida", "Por Lote", "Reparación"
        );

        cmbEstado.setItems(estados);
        cmbTipo.setItems(tipos);

        cmbEstado.setValue("Todos");
        cmbTipo.setValue("Todos");

        // Cargar proyectos desde la base de datos
        cargarProyectosDesdeBD();
    }

    private void cargarProyectosDesdeBD() {
        try {
            List<Proyecto> proyectos = ServiceFactory.getProyectoService().listarTodos();
            ObservableList<Proyecto> proyectosObservableList = FXCollections.observableArrayList(proyectos);
            proyectosTable.setItems(proyectosObservableList);

            System.out.println("Proyectos cargados: " + proyectos.size());

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlertaError("Error al cargar proyectos: " + e.getMessage());
        }
    }

    private void mostrarAlertaError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Método para recargar proyectos (si necesitas actualizar)
    public void recargarProyectos() {
        cargarProyectosDesdeBD();
    }

}
