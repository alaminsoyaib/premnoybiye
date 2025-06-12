package com.bytebender.premnoybiye;

import java.io.IOException;

import com.bytebender.premnoybiye.DBConnection.userInfo;
import com.bytebender.premnoybiye.DBConnection.FirebaseConnection;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class AuthController {
    static userInfo CurrentUser;
    private FirebaseConnection firebaseConnection = new FirebaseConnection();

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

    @FXML
    private void switchToSidebar() throws IOException {
        String email = emailTextField.getText();
        String password = passwordTextField.getText();

        // Use Firebase connection to login user (similar to Component.setImage pattern)
        userInfo loggedInUser = firebaseConnection.loginUser(email, password);

        if (loggedInUser != null) {
            CurrentUser = loggedInUser;
            App.setRoot("sidebar");
        } else {
            // Handle login failure (Firebase connection already shows error alerts)
            System.err.println("Login failed. Please check your credentials and try again.");
        }
    }

    @FXML // Register user and switch to stepper
    private void switchToStepper() throws IOException {
        String name = nameTextField.getText();
        String email = emailTextField.getText();
        String password = passwordTextField.getText();

        // Use Firebase connection to register user (similar to Component.setImage
        // pattern)
        boolean registrationSuccess = firebaseConnection.registerUser(name, email, password);

        if (registrationSuccess) {
            CurrentUser = new userInfo(name, email, password, "", "", "", "", "", "", "", "", "", "", "", "");
            App.setRoot("stepper");
        } else {
            // Handle registration failure (you might want to show an error message)
            System.err.println("Registration failed. Please try again.");
        }
    }

}
