package com.bytebender.premnoybiye;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.util.Duration;
import com.bytebender.premnoybiye.DBConnection.FirebaseRestClient;
import com.google.gson.JsonObject;
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

        // scene = new Scene(loadFXML("splash-screen"), 1000, 600); // main code
        scene = new Scene(loadFXML("loginsignupchoice"), 1000, 600); // demo code for signup ,sidebar
        // Stepper, profile, signup, loginsignupchoice, login, sidebar, splash-screen,
        // DemoMiniSide
        stage.setScene(scene);
        stage.setTitle("Prem Noy Biye");
        stage.show();

        String css = this.getClass().getResource("application.css").toExternalForm();
        scene.getStylesheets().add(css);

        Image icon = new Image(App.class.getResourceAsStream("/com/bytebender/premnoybiye/img/Main-Logo.png"));
        stage.getIcons().add(icon); // Demo: store sample data in Realtime Database
        new Thread(() -> {
            try {
                System.out.println("Starting Firebase demo test...");

                // Sign up a demo user (or replace with existing credentials)
                System.out.println("Attempting to sign up demo user...");
                JsonObject authResp = FirebaseRestClient.signUp("demo@demo.com", "demo123");
                System.out.println("Initial auth response: " + authResp);

                // If user already exists, fallback to signIn
                if (authResp.has("error")) {
                    System.err.println("SignUp error, trying signIn: " + authResp);
                    authResp = FirebaseRestClient.signIn("demo@demo.com", "demo123");
                    System.out.println("SignIn response: " + authResp);
                }

                if (authResp.has("idToken")) {
                    String idToken = authResp.get("idToken").getAsString();
                    System.out.println("Got idToken: " + idToken.substring(0, 20) + "...");

                    // Write demo node
                    JsonObject demoData = new JsonObject();
                    demoData.addProperty("message", "Hello from JavaFX demo");
                    demoData.addProperty("timestamp", System.currentTimeMillis());

                    System.out.println("Writing demo data...");
                    JsonObject writeResp = FirebaseRestClient.setData("demoTest", demoData, idToken);
                    System.out.println("Demo write response: " + writeResp);
                    System.out.println("Demo data saved to /demoTest");
                } else {
                    System.err.println("Failed to get idToken from auth response: " + authResp);
                }
            } catch (Exception e) {
                System.err.println("Exception in demo thread:");
                e.printStackTrace();
            }
        }).start();

        // // Triggering delay to switch Splash Screen
        // PauseTransition delay = new PauseTransition(Duration.seconds(1));
        // // default timing 6second decided
        // delay.play();
        // delay.setOnFinished(event -> { // Code to execute after the delay
        // try {
        // App.setRoot("loginsignupchoice");
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
        // });
    }

    public static void main(String[] args) {
        launch();
    }
}