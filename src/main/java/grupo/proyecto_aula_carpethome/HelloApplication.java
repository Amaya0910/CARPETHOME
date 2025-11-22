package grupo.proyecto_aula_carpethome;

import grupo.proyecto_aula_carpethome.controllers.MenuController;
import grupo.proyecto_aula_carpethome.entities.UsuarioLogueado;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

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

        // Centrar la ventana en la pantalla
        stage.centerOnScreen();

        stage.show();
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
        scene.setRoot(loadFXML(fxml));
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
        user_logged = null;
        try {
            setRoot("Login", 600, 700);
            setTitle("CarpetHome - Iniciar Sesión");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cerrar sesión: " + e.getMessage());
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
        FXMLLoader fxmlLoader = new FXMLLoader(
                HelloApplication.class.getResource(fxml + ".fxml")
        );
        return fxmlLoader.load();
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
            controller.setUserInfo(usuario.getNombreCompleto(), usuario.getRol());
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
        System.out.println("Aplicación cerrada");
        // Aquí puedes agregar lógica de limpieza si es necesaria
        // Por ejemplo: cerrar conexiones, guardar configuraciones, etc.
    }
}