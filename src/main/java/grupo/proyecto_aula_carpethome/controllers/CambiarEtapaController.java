package grupo.proyecto_aula_carpethome.controllers;

import grupo.proyecto_aula_carpethome.HelloApplication;
import grupo.proyecto_aula_carpethome.Utilidades.ImagenUtil;
import grupo.proyecto_aula_carpethome.entities.*;
import grupo.proyecto_aula_carpethome.services.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class CambiarEtapaController {

    @FXML private Label lblPrenda;
    @FXML private Label lblEtapaActual;
    @FXML private Label lblDescripcionEtapaActual;
    @FXML private ImageView imgEtapaActual;
    @FXML private Label lblDescripcionImagenActual;
    @FXML private VBox containerEtapaActual;

    @FXML private ComboBox<Etapa> comboNuevaEtapa;
    @FXML private Label lblDescripcionNuevaEtapa;

    @FXML private TextArea txtObservaciones;

    @FXML private Button btnSeleccionarImagen;
    @FXML private ImageView imgPreview;
    @FXML private Label lblNombreArchivo;
    @FXML private TextField txtDescripcionImagen;
    @FXML private Button btnQuitarImagen;

    @FXML private Button btnConfirmar;
    @FXML private Button btnCerrar;

    private Prenda prendaActual;
    private HistEtapa etapaActualHist;
    private GestionPrendasController parentController;
    private File imagenSeleccionada;

    private EtapaService etapaService = ServiceFactory.getEtapaService();
    private HistEtapaService histEtapaService = ServiceFactory.getHistEtapaService();

    @FXML
    public void initialize() {
        configurarComboBox();
        configurarImagenPreview();
        btnQuitarImagen.setVisible(false);
    }

    private void configurarComboBox() {
        comboNuevaEtapa.setConverter(new StringConverter<Etapa>() {
            @Override
            public String toString(Etapa etapa) {
                return etapa == null ? "" : etapa.getNombreEtapa();
            }

            @Override
            public Etapa fromString(String string) {
                return null;
            }
        });

        // Listener para actualizar descripción al seleccionar etapa
        comboNuevaEtapa.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                lblDescripcionNuevaEtapa.setText(newVal.getDescripcionEtapa());
            } else {
                lblDescripcionNuevaEtapa.setText("");
            }
        });
    }

    private void configurarImagenPreview() {
        // Configurar tamaño fijo para preview
        imgPreview.setFitWidth(200);
        imgPreview.setFitHeight(200);
        imgPreview.setPreserveRatio(true);
        imgPreview.setSmooth(true);

        // Configurar imagen actual
        imgEtapaActual.setFitWidth(150);
        imgEtapaActual.setFitHeight(150);
        imgEtapaActual.setPreserveRatio(true);
        imgEtapaActual.setSmooth(true);
    }

    public void setParentController(GestionPrendasController controller) {
        this.parentController = controller;
    }

    public void cargarPrenda(Prenda prenda) {
        this.prendaActual = prenda;
        lblPrenda.setText("Prenda: " + prenda.getNombrePrenda());

        try {
            // Cargar etapa actual
            var etapaActualOpt = histEtapaService.obtenerEtapaActual(prenda.getIdPrenda());

            if (etapaActualOpt.isPresent()) {
                etapaActualHist = etapaActualOpt.get();
                var etapaOpt = etapaService.buscarPorId(etapaActualHist.getIdEtapa());

                if (etapaOpt.isPresent()) {
                    Etapa etapaActual = etapaOpt.get();
                    lblEtapaActual.setText(etapaActual.getNombreEtapa());
                    lblDescripcionEtapaActual.setText(etapaActual.getDescripcionEtapa());

                    // Cargar imagen de etapa actual si existe
                    if (etapaActualHist.getUrlImagen() != null && !etapaActualHist.getUrlImagen().trim().isEmpty()) {
                        Image imagen = ImagenUtil.cargarImagen(etapaActualHist.getUrlImagen());
                        if (imagen != null) {
                            imgEtapaActual.setImage(imagen);
                            lblDescripcionImagenActual.setText(
                                    etapaActualHist.getDescripcionImagen() != null
                                            ? etapaActualHist.getDescripcionImagen()
                                            : "Sin descripción"
                            );
                        } else {
                            mostrarImagenPlaceholder(imgEtapaActual);
                            lblDescripcionImagenActual.setText("Imagen no disponible");
                        }
                    } else {
                        mostrarImagenPlaceholder(imgEtapaActual);
                        lblDescripcionImagenActual.setText("Sin imagen de referencia");
                    }
                } else {
                    lblEtapaActual.setText("Sin etapa");
                    containerEtapaActual.setVisible(false);
                }
            } else {
                lblEtapaActual.setText("Sin etapa asignada");
                containerEtapaActual.setVisible(false);
            }

            // Cargar todas las etapas disponibles
            List<Etapa> etapas = etapaService.listarTodas();
            comboNuevaEtapa.getItems().setAll(etapas);

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al cargar etapas", e.getMessage());
        }
    }

    @FXML
    private void handleSeleccionarImagen() {
        try {
            File archivo = ImagenUtil.seleccionarImagen(btnSeleccionarImagen.getScene().getWindow());

            if (archivo != null) {
                // Validar imagen
                ImagenUtil.validarImagen(archivo);

                // Guardar referencia
                imagenSeleccionada = archivo;

                // Mostrar preview
                Image imagen = new Image(archivo.toURI().toString());
                imgPreview.setImage(imagen);

                // Mostrar nombre del archivo
                lblNombreArchivo.setText(archivo.getName() +
                        String.format(" (%.2f MB)", ImagenUtil.obtenerTamanoMB(archivo)));
                lblNombreArchivo.setStyle("-fx-text-fill: #2e7d32;");

                // Mostrar botón para quitar imagen
                btnQuitarImagen.setVisible(true);

                System.out.println("✓ Imagen seleccionada: " + archivo.getName());
            }

        } catch (IllegalArgumentException e) {
            mostrarAdvertencia(e.getMessage());
            System.err.println("✗ Imagen inválida: " + e.getMessage());
        } catch (Exception e) {
            mostrarError("Error al cargar imagen", e.getMessage());
        }
    }

    @FXML
    private void handleQuitarImagen() {
        imagenSeleccionada = null;
        imgPreview.setImage(null);
        lblNombreArchivo.setText("No se ha seleccionado ninguna imagen");
        lblNombreArchivo.setStyle("-fx-text-fill: #757575;");
        txtDescripcionImagen.clear();
        btnQuitarImagen.setVisible(false);
    }

    @FXML
    private void handleConfirmar() {
        // Validar selección de etapa
        if (comboNuevaEtapa.getValue() == null) {
            mostrarAdvertencia("Debe seleccionar una etapa");
            return;
        }

        // Validar observaciones (obligatorias al cambiar etapa)
        if (etapaActualHist != null && (txtObservaciones.getText() == null || txtObservaciones.getText().trim().isEmpty())) {
            mostrarAdvertencia("Debe ingresar observaciones sobre el cierre de la etapa actual");
            return;
        }

        try {
            // Obtener usuario logueado
            UsuarioLogueado usuarioLogueado = HelloApplication.getUser();

            if (usuarioLogueado == null) {
                mostrarAdvertencia("Debe iniciar sesión para realizar esta acción");
                return;
            }

            if (!"Empleado".equalsIgnoreCase(usuarioLogueado.getRol())) {
                mostrarAdvertencia("Solo los empleados pueden cambiar etapas de prendas");
                return;
            }

            String idEtapaActual = etapaActualHist != null ? etapaActualHist.getIdEtapa() : null;
            String idEtapaNueva = comboNuevaEtapa.getValue().getIdEtapa();
            String observaciones = txtObservaciones.getText().trim();
            String idEmpleado = usuarioLogueado.getId();

            // Cambiar etapa
            histEtapaService.cambiarEtapa(
                    prendaActual.getIdPrenda(),
                    idEtapaActual,
                    idEtapaNueva,
                    observaciones,
                    idEmpleado
            );

            // Si se seleccionó una imagen, guardarla y actualizar registro
            if (imagenSeleccionada != null) {
                try {
                    // Guardar imagen físicamente
                    String rutaImagen = ImagenUtil.guardarImagen(
                            imagenSeleccionada,
                            prendaActual.getIdPrenda(),
                            idEtapaNueva
                    );

                    // Actualizar registro con URL de imagen
                    String descripcionImagen = txtDescripcionImagen.getText().trim();
                    histEtapaService.actualizarImagenEtapa(
                            prendaActual.getIdPrenda(),
                            idEtapaNueva,
                            rutaImagen,
                            descripcionImagen.isEmpty() ? null : descripcionImagen
                    );

                    System.out.println("✓ Imagen guardada y asociada a la etapa");

                } catch (IOException e) {
                    System.err.println("✗ Error al guardar imagen: " + e.getMessage());
                    mostrarAdvertencia("Etapa cambiada, pero no se pudo guardar la imagen: " + e.getMessage());
                }
            }

            mostrarExito("Etapa cambiada exitosamente");

            // Actualizar vista padre
            if (parentController != null) {
                parentController.cargarPrendas();
            }

            cerrarVentana();

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al cambiar etapa", e.getMessage());
        }
    }

    @FXML
    private void handleCerrar() {
        cerrarVentana();
    }

    private void mostrarImagenPlaceholder(ImageView imageView) {
        // Aquí podrías cargar una imagen placeholder por defecto
        imageView.setImage(null);
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