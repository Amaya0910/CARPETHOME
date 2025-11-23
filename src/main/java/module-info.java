module grupo.proyecto_aula_carpethome {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires static lombok;
    requires ojdbc8;
    requires ucp;
    requires com.google.gson;
    requires okhttp3;


    opens grupo.proyecto_aula_carpethome.controllers to javafx.fxml;
    opens grupo.proyecto_aula_carpethome to javafx.fxml;
    exports grupo.proyecto_aula_carpethome;
    exports grupo.proyecto_aula_carpethome.Pruebas;
    opens grupo.proyecto_aula_carpethome.Pruebas to javafx.fxml;
    opens grupo.proyecto_aula_carpethome.services to javafx.fxml;
}