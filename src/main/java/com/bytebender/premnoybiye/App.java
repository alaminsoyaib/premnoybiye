package com.bytebender.premnoybiye;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
/*  JavaFX App */

public class App extends Application {

    private static Scene scene;

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    static void setRoot(String fxml) throws IOException {

        scene.setRoot(loadFXML(fxml));
    }

    @Override
    public void start(Stage stage) throws IOException {

        scene = new Scene(loadFXML("primary"), 1000, 600);
        stage.setScene(scene);
        stage.setTitle("Prem Noy Biye");
        stage.show();

        String css = this.getClass().getResource("application.css").toExternalForm();
        scene.getStylesheets().add(css);

        Image icon = new Image(App.class.getResourceAsStream("/com/bytebender/premnoybiye/img/Main-Logo.png"));
        stage.getIcons().add(icon);
    }

    public static void main(String[] args) {
        launch();
    }
}