package grupo.proyecto_aula_carpethome;

import grupo.proyecto_aula_carpethome.controllers.MenuController;
import grupo.proyecto_aula_carpethome.entities.UsuarioLogueado;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class HelloApplication extends Application {

    private static Scene scene;
    private static Stage primaryStage;
    private static UsuarioLogueado user_logged;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        scene = new Scene(loadFXML("Login"), 600, 700);

        stage.setTitle("CarpetHome - Iniciar Sesión");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(600);
        stage.setMinHeight(700);

        // MEJORA: Agregar icono de la aplicación (si tienes uno)
        try {
            // Descomentar cuando tengas el icono
            // stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        } catch (Exception e) {
            System.out.println("No se pudo cargar el icono de la aplicación");
        }

        // MEJORA: Confirmar antes de cerrar
        stage.setOnCloseRequest(event -> {
            if (isUserLogged()) {
                event.consume(); // Prevenir cierre automático
                confirmarCierre();
            }
        });

        // Centrar la ventana en la pantalla
        stage.centerOnScreen();

        stage.show();

        System.out.println("✓ Aplicación iniciada correctamente");
    }

    // ============================================
    // MÉTODOS PARA CAMBIAR DE ESCENA
    // ============================================

    /**
     * Cambia la escena actual por una nueva vista FXML
     * @param fxml Nombre del archivo FXML (sin extensión)
     * @throws IOException Si no se puede cargar el archivo FXML
     */
    public static void setRoot(String fxml) throws IOException {
        Parent root = loadFXML(fxml);
        scene.setRoot(root);
        System.out.println("✓ Vista cargada: " + fxml);
    }

    /**
     * Cambia la escena y ajusta el tamaño de la ventana
     * @param fxml Nombre del archivo FXML
     * @param width Ancho de la ventana
     * @param height Alto de la ventana
     * @throws IOException Si no se puede cargar el archivo FXML
     */
    public static void setRoot(String fxml, double width, double height) throws IOException {
        scene.setRoot(loadFXML(fxml));
        if (primaryStage != null) {
            primaryStage.setWidth(width);
            primaryStage.setHeight(height);
            primaryStage.centerOnScreen();
        }
        System.out.println("✓ Vista cargada: " + fxml + " (" + width + "x" + height + ")");
    }

    /**
     * Cambia el título de la ventana principal
     * @param title Nuevo título
     */
    public static void setTitle(String title) {
        if (primaryStage != null) {
            primaryStage.setTitle(title);
        }
    }

    // ============================================
    // GESTIÓN DE USUARIO LOGUEADO
    // ============================================

    /**
     * Establece el usuario actualmente logueado
     * @param user Usuario que ha iniciado sesión
     */
    public static void setUser(UsuarioLogueado user) {
        HelloApplication.user_logged = user;
        if (user != null) {
            System.out.println("✓ Usuario establecido: " + user.getNombreCompleto() + " (" + user.getRol() + ")");
        }
    }

    /**
     * Obtiene el usuario actualmente logueado
     * @return Usuario logueado o null si no hay sesión activa
     */
    public static UsuarioLogueado getUser() {
        return HelloApplication.user_logged;
    }




    /**
     * Verifica si hay un usuario logueado
     * @return true si hay sesión activa, false en caso contrario
     */
    public static boolean isUserLogged() {
        return user_logged != null;
    }

    /**
     * Cierra la sesión del usuario actual
     */
    public static void logout() {
        if (user_logged != null) {
            System.out.println("✓ Cerrando sesión de: " + user_logged.getNombreCompleto());
            user_logged = null;
        }

        try {
            setRoot("Login", 600, 700);
            setTitle("CarpetHome - Iniciar Sesión");
            System.out.println("✓ Sesión cerrada exitosamente");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("✗ Error al cerrar sesión: " + e.getMessage());
            mostrarError("Error", "No se pudo regresar a la pantalla de inicio de sesión");
        }
    }

    /**
     * MEJORA: Cierra la sesión con confirmación
     */
    public static void logoutWithConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cerrar Sesión");
        alert.setHeaderText("¿Está seguro de cerrar sesión?");
        alert.setContentText("Perderá cualquier cambio no guardado.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            logout();
        }
    }

    // ============================================
    // CARGA DE VISTAS FXML
    // ============================================

    /**
     * Carga un archivo FXML y retorna su nodo raíz
     * @param fxml Nombre del archivo FXML (sin extensión)
     * @return Nodo raíz del FXML cargado
     * @throws IOException Si no se puede cargar el archivo
     */
    private static Parent loadFXML(String fxml) throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    HelloApplication.class.getResource(fxml + ".fxml")
            );
            return fxmlLoader.load();
        } catch (IOException e) {
            System.err.println("✗ Error al cargar FXML: " + fxml + ".fxml");
            System.err.println("✗ Ruta buscada: " + HelloApplication.class.getResource(fxml + ".fxml"));
            throw e;
        }
    }

    /**
     * Carga un archivo FXML y retorna el FXMLLoader para acceder al controlador
     * @param fxml Nombre del archivo FXML (sin extensión)
     * @return FXMLLoader con la vista cargada
     * @throws IOException Si no se puede cargar el archivo
     */
    public static FXMLLoader loadFXMLWithController(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                HelloApplication.class.getResource(fxml + ".fxml")
        );
        loader.load();
        return loader;
    }

    /**
     * MEJORA: Carga una vista con su controlador de forma genérica
     * @param fxml Nombre del archivo FXML
     * @param controllerClass Clase del controlador esperado
     * @return El controlador cargado
     * @throws IOException Si hay error al cargar
     */
    public static <T> T loadViewWithController(String fxml, Class<T> controllerClass) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                HelloApplication.class.getResource(fxml + ".fxml")
        );
        Parent root = loader.load();
        scene.setRoot(root);

        T controller = loader.getController();
        if (controller == null) {
            throw new IOException("No se pudo obtener el controlador de tipo: " + controllerClass.getSimpleName());
        }

        return controller;
    }

    // ============================================
    // MÉTODO ESPECIAL PARA CARGAR EL MENÚ
    // ============================================

    /**
     * Carga el menú principal y configura el controlador con la información del usuario
     * @param usuario Usuario que ha iniciado sesión
     * @throws IOException Si no se puede cargar el menú
     */
    public static void loadMenu(UsuarioLogueado usuario) throws IOException {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }

        setUser(usuario);

        FXMLLoader loader = new FXMLLoader(
                HelloApplication.class.getResource("Menu.fxml")
        );
        Parent root = loader.load();

        // Obtener el controlador y pasar el usuario
        MenuController controller = loader.getController();
        if (controller != null) {
            controller.setUserInfo(usuario);
        } else {
            System.err.println("⚠ Advertencia: No se pudo obtener el MenuController");
        }

        scene.setRoot(root);

        // Ajustar tamaño de ventana para el menú
        if (primaryStage != null) {
            primaryStage.setWidth(1400);
            primaryStage.setHeight(850);
            primaryStage.setMinWidth(1200);
            primaryStage.setMinHeight(700);
            primaryStage.centerOnScreen();
        }

        setTitle("CarpetHome - Sistema de Gestión");

        System.out.println("✓ Menú cargado para: " + usuario.getNombreCompleto());
    }

    // ============================================
    // MEJORAS: MÉTODOS AUXILIARES
    // ============================================

    /**
     * Obtiene el Stage principal
     * @return Stage principal
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Obtiene la Scene principal
     * @return Scene principal
     */
    public static Scene getScene() {
        return scene;
    }

    /**
     * MEJORA: Maximiza la ventana
     */
    public static void maximizeWindow() {
        if (primaryStage != null) {
            primaryStage.setMaximized(true);
        }
    }

    /**
     * MEJORA: Restaura el tamaño de la ventana
     */
    public static void restoreWindow() {
        if (primaryStage != null) {
            primaryStage.setMaximized(false);
        }
    }

    /**
     * MEJORA: Confirma antes de cerrar la aplicación
     */
    private void confirmarCierre() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cerrar Aplicación");
        alert.setHeaderText("¿Está seguro de cerrar la aplicación?");
        alert.setContentText("Se cerrará la sesión actual.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            cerrarAplicacion();
        }
    }

    /**
     * MEJORA: Cierra la aplicación de forma ordenada
     */
    public static void cerrarAplicacion() {
        System.out.println("✓ Cerrando aplicación...");

        // Cerrar sesión si hay usuario logueado
        if (isUserLogged()) {
            System.out.println("✓ Cerrando sesión de: " + user_logged.getNombreCompleto());
            user_logged = null;
        }

        // Cerrar conexiones, guardar configuraciones, etc.
        // TODO: Agregar lógica de limpieza aquí

        Platform.exit();
        System.exit(0);
    }

    /**
     * MEJORA: Muestra un mensaje de error global
     */
    public static void mostrarError(String titulo, String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }

    /**
     * MEJORA: Muestra un mensaje de éxito global
     */
    public static void mostrarExito(String titulo, String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }

    /**
     * MEJORA: Muestra un mensaje de advertencia global
     */
    public static void mostrarAdvertencia(String titulo, String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }

    // ============================================
    // MÉTODO PRINCIPAL
    // ============================================

    public static void main(String[] args) {
        launch();
    }

    // ============================================
    // MÉTODO DE CIERRE
    // ============================================

    @Override
    public void stop() {
        System.out.println("✓ Aplicación cerrada correctamente");

        // Limpiar usuario logueado
        user_logged = null;

        // Aquí puedes agregar más lógica de limpieza
        // Por ejemplo:
        // - Cerrar pools de conexiones
        // - Guardar configuraciones del usuario
        // - Cerrar archivos temporales
        // - Cancelar tareas en segundo plano
    }
}