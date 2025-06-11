package com.bytebender.premnoybiye;

import java.io.IOException;

import com.bytebender.premnoybiye.DBConnection.FirebaseRestClient;
import com.bytebender.premnoybiye.DBConnection.userInfo;
import com.google.gson.JsonObject;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class AuthController {
    static userInfo CurrentUser;

    @FXML
    private TextField emailTextField;
    @FXML
    private TextField nameTextField;
    @FXML
    private TextField passwordTextField;

    @FXML
    private void switchToLogin() throws IOException {
        App.setRoot("login");
    }

    @FXML
    private void switchToSignUp() throws IOException {
        App.setRoot("signup");
    }

    @FXML // temporary method to develop stepper
    private void switchToStepper() throws IOException {
        String name = nameTextField.getText();
        String email = emailTextField.getText();
        String password = passwordTextField.getText();
        try {
            // Authenticate via Firebase Auth REST
            JsonObject authResp = FirebaseRestClient.signUp(email, password);
            String idToken = authResp.get("idToken").getAsString();
            String localId = authResp.get("localId").getAsString();
            // Create user object
            CurrentUser = new userInfo(name, email, password, "", "", "", "", "", "", "", "", "", "", "", "");
            // Push userInfo to Realtime Database under /users/{localId}
            FirebaseRestClient.setData("users/" + localId, CurrentUser, idToken);
            // Retrieve it back
            JsonObject dbData = FirebaseRestClient.getData("users/" + localId, idToken);
            System.out.println("Retrieved user data: " + dbData);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        App.setRoot("stepper");
    }

}
