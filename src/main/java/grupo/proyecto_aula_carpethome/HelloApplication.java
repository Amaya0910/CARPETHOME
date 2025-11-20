package grupo.proyecto_aula_carpethome;

import grupo.proyecto_aula_carpethome.controllers.MenuController;
import grupo.proyecto_aula_carpethome.entities.Persona;
import grupo.proyecto_aula_carpethome.entities.UsuarioLogueado;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    private static Scene scene;
    private static UsuarioLogueado user_logged;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("Login"), 600, 700);
        stage.setTitle("Aslu - Iniciar Sesión");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    // Método para cambiar de escena
    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    // Método para establecer el usuario logueado
    public static void setUser(UsuarioLogueado user) {
        HelloApplication.user_logged = user;
    }

    // Método para obtener el usuario logueado
    public static UsuarioLogueado getUser() {
        return HelloApplication.user_logged;
    }

    // Método para cargar FXML
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    // Método especial para cargar el menú con el controlador
    public static void loadMenu(UsuarioLogueado usuario) throws IOException {
        setUser(usuario);
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("Menu.fxml"));
        Parent root = loader.load();

        // Obtener el controlador y pasar el usuario
        MenuController controller = loader.getController();
        controller.setUserInfo(usuario.getNombreCompleto(), usuario.getRol());

        scene.setRoot(root);
    }

    public static void main(String[] args) {
        launch();
    }
}
