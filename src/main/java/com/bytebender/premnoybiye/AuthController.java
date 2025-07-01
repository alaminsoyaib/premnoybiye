package com.bytebender.premnoybiye;

import java.io.IOException;
import com.bytebender.premnoybiye.DBConnection.userInfo;
import com.bytebender.premnoybiye.DBConnection.FirebaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class AuthController {
    static userInfo CurrentUser;
    static String CurrentUserEmail;
    static String CurrentUserPassword;
    private FirebaseConnection firebaseConnection = new FirebaseConnection();

    @FXML
    private TextField emailTextField, nameTextField, passwordTextField;

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

        userInfo loggedInUser = firebaseConnection.loginUser(email, password);
        if (loggedInUser != null) {
            CurrentUser = loggedInUser;
            CurrentUserEmail = email;
            CurrentUserPassword = password;
            App.setRoot("sidebar");
        } else {
            System.err.println("Login failed. Please check your credentials and try again.");
        }
    }

    @FXML
    private void switchToStepper() throws IOException {
        String name = nameTextField.getText();
        String email = emailTextField.getText();
        String password = passwordTextField.getText();

        String userId = firebaseConnection.registerUser(name, email, password);
        if (userId != null) {
            CurrentUser = new userInfo(name, email, "", "", "", "", "", "", "", "", "", "", "", "", userId);
            CurrentUserEmail = email;
            CurrentUserPassword = password;
            App.setRoot("stepper");
        } else {
            System.err.println("Registration failed. Please try again.");
        }
    }
}
