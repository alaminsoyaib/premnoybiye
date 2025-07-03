package com.bytebender.premnoybiye;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;

public class App extends Application {
    private static Scene scene;

    private static Parent loadFXML(String fxml) throws IOException {
        return new FXMLLoader(App.class.getResource(fxml + ".fxml")).load();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("splash-screen"), 1000, 600);
        stage.setScene(scene);
        stage.setTitle("Prem Noy Biye");
        stage.show();

        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        stage.getIcons().add(new Image(App.class.getResourceAsStream("/com/bytebender/premnoybiye/img/Main-Logo.png")));

        PauseTransition delay = new PauseTransition(Duration.seconds(6.0));
        delay.setOnFinished(event -> {
            try {
                setRoot("loginsignupchoice");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        delay.play();
    }

    public static void main(String[] args) {
        launch();
    }
}